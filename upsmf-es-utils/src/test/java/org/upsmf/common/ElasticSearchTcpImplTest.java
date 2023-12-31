package org.upsmf.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequestBuilder;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkProcessor.Listener;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.FutureUtils;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.upsmf.common.exception.ProjectCommonException;
import org.upsmf.common.factory.EsClientFactory;
import org.upsmf.common.inf.ElasticSearchService;
import org.upsmf.common.models.response.Response;
import org.upsmf.common.models.util.HttpUtil;
import org.upsmf.common.models.util.JsonKey;
import org.upsmf.common.responsecode.ResponseCode;
import org.upsmf.dto.SearchDTO;
import org.upsmf.helper.ConnectionManager;
import scala.concurrent.Future;

/**
 * Test class for Elastic search TCP client Impl
 *
 * @author github.com/iostream04
 */
@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*", "javax.security.*","jdk.internal.reflect.*"})
@PrepareForTest({
  ConnectionManager.class,
  TransportClient.class,
  AcknowledgedResponse.class,
  GetRequestBuilder.class,
  HttpUtil.class,
  BulkProcessor.class,
  FutureUtils.class,
  SearchHit.class,
  SearchHits.class,
  Aggregations.class
})
@SuppressStaticInitializationFor({"org.sunbird.common.ConnectionManager"})
public class ElasticSearchTcpImplTest {
  private static Map<String, Object> chemistryMap = null;
  private static Map<String, Object> physicsMap = null;
  private static TransportClient client = null;
  private static final String INDEX_NAME = "sbtestindex";
  private static final String STARTS_WITH = "startsWith";
  private static final String ENDS_WITH = "endsWith";
  private ElasticSearchService esService = EsClientFactory.getInstance(JsonKey.TCP);

  @BeforeClass
  public static void initClass() throws Exception {
    chemistryMap = initializeChemistryCourse(5);
    physicsMap = intitializePhysicsCourse(60);
  }

  @Before
  public void initBeforeTest() {
    mockBaseRules();
    mockRulesForGet();
    mockRulesForInsert();
    mockRulesForUpdate();
    mockRulesForDelete();

    mockRulesForIndexes();
    mockRulesHttpRequest();
    mockRulesBulkInsert();
  }

  @Test
  public void testCreateDataSuccess() {
    mockRulesForInsert();

    esService.save(INDEX_NAME, (String) chemistryMap.get("courseId"), chemistryMap);
    assertNotNull(chemistryMap.get("courseId"));

    esService.save(INDEX_NAME, (String) physicsMap.get("courseId"), physicsMap);
    assertNotNull(physicsMap.get("courseId"));
  }

  @Test
  public void testGetByIdentifierSuccess() {
    Future<Map<String, Object>> responseMapF =
        esService.getDataByIdentifier(INDEX_NAME, (String) chemistryMap.get("courseId"));

    Map<String, Object> responseMap =
        (Map<String, Object>) ElasticSearchHelper.getResponseFromFuture(responseMapF);

    assertEquals(responseMap.get("courseId"), chemistryMap.get("courseId"));
  }

  @Test
  public void testUpdateDataSuccess() {
    Map<String, Object> innermap = new HashMap<>();
    innermap.put("courseName", "Updated course name");
    innermap.put("organisationId", "updatedOrgId");

    GetRequestBuilder grb = mock(GetRequestBuilder.class);
    GetResponse getResponse = mock(GetResponse.class);
    when(client.prepareGet(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.eq((String) chemistryMap.get("courseId"))))
        .thenReturn(grb);
    when(grb.get()).thenReturn(getResponse);
    when(getResponse.getSource()).thenReturn(innermap);

    Future<Boolean> responseF =
        esService.update(INDEX_NAME, (String) chemistryMap.get("courseId"), innermap);
    boolean response = (boolean) ElasticSearchHelper.getResponseFromFuture(responseF);
    assertTrue(response);
  }

  @Test
  @Ignore
  public void testComplexSearchSuccess() throws Exception {
    SearchDTO searchDTO = new SearchDTO();

    List<String> fields = new ArrayList<String>();
    fields.add("courseId");
    fields.add("courseType");
    fields.add("createdOn");
    fields.add("description");

    Map<String, Object> sortMap = new HashMap<>();
    sortMap.put("courseType", "ASC");
    searchDTO.setSortBy(sortMap);

    List<String> excludedFields = new ArrayList<String>();
    excludedFields.add("createdOn");
    searchDTO.setExcludedFields(excludedFields);

    searchDTO.setLimit(20);
    searchDTO.setOffset(0);

    Map<String, Object> additionalPro = new HashMap<String, Object>();
    searchDTO.addAdditionalProperty("test", additionalPro);

    List<String> existsList = new ArrayList<String>();
    existsList.add("pkgVersion");
    existsList.add("size");

    Map<String, Object> additionalProperties = new HashMap<String, Object>();
    additionalProperties.put(JsonKey.EXISTS, existsList);

    List<String> description = new ArrayList<String>();
    description.add("This is for chemistry");
    description.add("Hindi Jii");

    List<Integer> sizes = new ArrayList<Integer>();
    sizes.add(10);
    sizes.add(20);

    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("description", description);
    filterMap.put("size", sizes);
    additionalProperties.put(JsonKey.FILTERS, filterMap);

    Map<String, Object> rangeMap = new HashMap<String, Object>();
    rangeMap.put(">", 0);
    filterMap.put("pkgVersion", rangeMap);

    Map<String, Object> lexicalMap = new HashMap<>();
    lexicalMap.put(STARTS_WITH, "type");
    filterMap.put("courseType", lexicalMap);
    Map<String, Object> lexicalMap1 = new HashMap<>();
    lexicalMap1.put(ENDS_WITH, "sunbird");
    filterMap.put("courseAddedByName", lexicalMap1);
    filterMap.put("orgName", "Name of the organisation");

    searchDTO.setAdditionalProperties(additionalProperties);
    searchDTO.setFields(fields);
    searchDTO.setQuery("organisation");

    List<String> mode = Arrays.asList("soft");
    searchDTO.setMode(mode);
    Map<String, Integer> constraintMap = new HashMap<String, Integer>();
    constraintMap.put("grades", 10);
    constraintMap.put("pkgVersion", 5);
    searchDTO.setSoftConstraints(constraintMap);
    searchDTO.setQuery("organisation Name published");
    mockRulesForSearch(3);
    Future<Map<String, Object>> map = esService.search(searchDTO, INDEX_NAME);
    Map<String, Object> response =
        (Map<String, Object>) ElasticSearchHelper.getResponseFromFuture(map);

    assertEquals(2, response.size());
  }

  @Test
  @Ignore
  public void testComplexSearchSuccessWithRangeGreaterThan() {
    SearchDTO searchDTO = new SearchDTO();
    Map<String, Object> additionalProperties = new HashMap<String, Object>();
    List<Integer> sizes = new ArrayList<Integer>();
    sizes.add(10);
    sizes.add(20);
    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("size", sizes);
    Map<String, String> innerMap = new HashMap<>();
    innerMap.put("createdOn", "2017-11-06");
    filterMap.put(">=", innerMap);
    additionalProperties.put(JsonKey.FILTERS, filterMap);
    Map<String, Object> rangeMap = new HashMap<String, Object>();
    rangeMap.put(">", 0);
    filterMap.put("pkgVersion", rangeMap);
    Map<String, Object> lexicalMap = new HashMap<>();
    lexicalMap.put(STARTS_WITH, "type");
    filterMap.put("courseType", lexicalMap);
    Map<String, Object> lexicalMap1 = new HashMap<>();
    lexicalMap1.put(ENDS_WITH, "sunbird");
    filterMap.put("courseAddedByName", lexicalMap1);
    filterMap.put("orgName", "Name of the organisation");

    searchDTO.setAdditionalProperties(additionalProperties);
    searchDTO.setQuery("organisation");
    mockRulesForSearch(3);

    Future<Map<String, Object>> map = esService.search(searchDTO, INDEX_NAME);
    Map<String, Object> response =
        (Map<String, Object>) ElasticSearchHelper.getResponseFromFuture(map);
    assertEquals(2, response.size());
  }

  @Test
  @Ignore
  public void testComplexSearchSuccessWithRangeLessThan() {
    SearchDTO searchDTO = new SearchDTO();
    Map<String, Object> additionalProperties = new HashMap<String, Object>();
    List<Integer> sizes = new ArrayList<Integer>();
    sizes.add(10);
    sizes.add(20);
    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("size", sizes);
    Map<String, String> innerMap = new HashMap<>();
    innerMap.put("createdOn", "2017-11-06");
    filterMap.put("<=", innerMap);
    additionalProperties.put(JsonKey.FILTERS, filterMap);
    Map<String, Object> rangeMap = new HashMap<String, Object>();
    rangeMap.put(">", 0);
    filterMap.put("pkgVersion", rangeMap);
    Map<String, Object> lexicalMap = new HashMap<>();
    lexicalMap.put(STARTS_WITH, "type");
    filterMap.put("courseType", lexicalMap);
    Map<String, Object> lexicalMap1 = new HashMap<>();
    lexicalMap1.put(ENDS_WITH, "sunbird");
    filterMap.put("courseAddedByName", lexicalMap1);
    filterMap.put("orgName", "Name of the organisation");

    searchDTO.setAdditionalProperties(additionalProperties);
    searchDTO.setQuery("organisation");
    mockRulesForSearch(3);
    Future<Map<String, Object>> map = esService.search(searchDTO, INDEX_NAME);
    Map<String, Object> response =
        (Map<String, Object>) ElasticSearchHelper.getResponseFromFuture(map);
    assertEquals(2, response.size());
  }

  @Test
  public void testGetByIdentifierFailureWithoutIndex() {
    try {
      esService.getDataByIdentifier(null, (String) chemistryMap.get("courseId"));
    } catch (ProjectCommonException ex) {
      assertEquals(ResponseCode.SERVER_ERROR.getResponseCode(), ex.getResponseCode());
    }
  }

  @Test
  public void testGetByIdentifierFailureWithoutTypeAndIndexIdentifier() {
    try {
      esService.getDataByIdentifier(null, "");
    } catch (ProjectCommonException ex) {
      assertEquals(ResponseCode.SERVER_ERROR.getResponseCode(), ex.getResponseCode());
    }
  }

  @Test
  public void testGetDataByIdentifierFailureWithoutIdentifier() {
    Future<Map<String, Object>> responseMap = esService.getDataByIdentifier(INDEX_NAME, "");
    Map<String, Object> map =
        (Map<String, Object>) ElasticSearchHelper.getResponseFromFuture(responseMap);
    assertEquals(0, map.size());
  }

  @Test
  public void testUpdateDataFailureWithoutIdentifier() {
    Map<String, Object> innermap = new HashMap<>();
    innermap.put("courseName", "Updated Course Name");
    innermap.put("organisationId", "updatedOrgId");
    Future<Boolean> response = esService.update(INDEX_NAME, null, innermap);
    boolean result = (boolean) ElasticSearchHelper.getResponseFromFuture(response);
    assertFalse(result);
  }

  @Test
  public void testUpdateDataFailureWithEmptyMap() {
    Map<String, Object> innermap = new HashMap<>();
    Future<Boolean> response =
        esService.update(INDEX_NAME, (String) chemistryMap.get("courseId"), innermap);
    boolean result = (boolean) ElasticSearchHelper.getResponseFromFuture(response);
    assertFalse(result);
  }

  @Test
  public void testUpdateDataFailureWithNullMap() {
    Future<Boolean> response =
        esService.update(INDEX_NAME, (String) chemistryMap.get("courseId"), null);
    boolean result = (boolean) ElasticSearchHelper.getResponseFromFuture(response);
    assertFalse(result);
  }

  @Test
  public void testUpsertDataFailureWithoutIdentifier() {
    Map<String, Object> innermap = new HashMap<>();
    innermap.put("courseName", "Updated Course Name");
    innermap.put("organisationId", "updatedOrgId");
    Future<Boolean> response = esService.upsert(INDEX_NAME, null, innermap);
    boolean result = (boolean) ElasticSearchHelper.getResponseFromFuture(response);
    assertFalse(result);
  }

  @Test
  public void testUpsertDataFailureWithoutIndex() {
    Map<String, Object> innermap = new HashMap<>();
    innermap.put("courseName", "Updated Course Name");
    innermap.put("organisationId", "updatedOrgId");
    Future<Boolean> response =
        esService.upsert(null, (String) chemistryMap.get("courseId"), innermap);
    boolean result = (boolean) ElasticSearchHelper.getResponseFromFuture(response);
    assertFalse(result);
  }

  @Test
  public void testUpsertDataFailureWithEmptyMap() {
    Map<String, Object> innermap = new HashMap<>();
    Future<Boolean> response =
        esService.upsert(INDEX_NAME, (String) chemistryMap.get("courseId"), innermap);
    boolean result = (boolean) ElasticSearchHelper.getResponseFromFuture(response);
    assertFalse(result);
  }

  @Test
  public void testSaveDataFailureWithoutIndexName() {
    Future<String> response =
        esService.save("", (String) chemistryMap.get("courseId"), chemistryMap);
    String result = (String) ElasticSearchHelper.getResponseFromFuture(response);
    assertEquals("ERROR", result);
  }

  @Test
  public void testGetDataByIdentifierFailureByEmptyIdentifier() {
    Future<Map<String, Object>> responseMap = esService.getDataByIdentifier(INDEX_NAME, "");
    Map<String, Object> response =
        (Map<String, Object>) ElasticSearchHelper.getResponseFromFuture(responseMap);
    assertEquals(0, response.size());
  }

  @Test
  public void testRemoveDataSuccessByIdentifier() {
    Future<Boolean> response = esService.delete(INDEX_NAME, (String) chemistryMap.get("courseId"));
    boolean result = (boolean) ElasticSearchHelper.getResponseFromFuture(response);
    assertEquals(true, result);
  }

  @Test
  public void testRemoveDataFailureByIdentifierEmpty() {
    Future<Boolean> response = esService.delete(INDEX_NAME, "");
    boolean result = (boolean) ElasticSearchHelper.getResponseFromFuture(response);
    assertEquals(false, result);
  }

  @Test
  public void testInitialiseConnectionFailureFromProperties() {
    boolean response =
        ConnectionManager.initialiseConnectionFromPropertiesFile(
            "Test", "localhost1,128.0.0.1", "9200,9300");
    assertFalse(response);
  }

  @Test
  public void testHealthCheckSuccess() {
    Future<Boolean> response = esService.healthCheck();
    boolean result = (boolean) ElasticSearchHelper.getResponseFromFuture(response);
    assertEquals(true, result);
  }

  @Test
  public void testUpsertDataSuccess() {
    Map<String, Object> data = new HashMap<String, Object>();
    data.put("test", "test");
    Future<Boolean> response = esService.upsert(INDEX_NAME, "test-12349", data);
    boolean result = (boolean) ElasticSearchHelper.getResponseFromFuture(response);
    assertEquals(true, result);
  }

  @Test
  public void testBulkInsertDataSuccess() {
    Map<String, Object> data = new HashMap<String, Object>();
    data.put("test1", "test");
    data.put("test2", "manzarul");
    List<Map<String, Object>> listOfMap = new ArrayList<Map<String, Object>>();
    listOfMap.add(data);
    Future<Boolean> response = esService.bulkInsert(INDEX_NAME, listOfMap);
    boolean result = (boolean) ElasticSearchHelper.getResponseFromFuture(response);
    assertEquals(true, result);
  }

  @Test
  public void testSearchMetricsDataSuccess() {
    String index = "searchindex";
    String rawQuery = "{\"query\":{\"match_none\":{}}}";
    Response response = esService.searchMetricsData(index, rawQuery);
    assertEquals(ResponseCode.OK, response.getResponseCode());
  }

  @Test
  public void testSearchMetricsDataFailure() {
    String index = "searchtest";
    String rawQuery = "{\"query\":{\"match_none\":{}}}";
    try {
      esService.searchMetricsData(index, rawQuery);
    } catch (ProjectCommonException e) {
      assertEquals(ResponseCode.unableToConnectToES.getErrorCode(), e.getCode());
    }
  }

  private static Map<String, Object> initializeChemistryCourse(int appendVal) {
    Map<String, Object> chemistryMap = new HashMap<>();
    chemistryMap.put("courseType", "type of the course. all , private");
    chemistryMap.put("description", "This is for chemistry");
    chemistryMap.put("size", 10);
    chemistryMap.put("objectType", "course");
    chemistryMap.put("courseId", "course id_" + appendVal);
    chemistryMap.put("courseName", "NTP course_" + appendVal);
    chemistryMap.put("courseDuration", appendVal);
    chemistryMap.put("noOfLecture", 30 + appendVal);
    chemistryMap.put("organisationId", "org id");
    chemistryMap.put("orgName", "Name of the organisation");
    chemistryMap.put("courseAddedById", "who added the course in NTP");
    chemistryMap.put("courseAddedByName", "Name of the person who added the course under sunbird");
    chemistryMap.put("coursePublishedById", "who published the course");
    chemistryMap.put("coursePublishedByName", "who published the course");
    chemistryMap.put("enrollementStartDate", new Date());
    chemistryMap.put("publishedDate", new Date());
    chemistryMap.put("updatedDate", new Date());
    chemistryMap.put("updatedById", "last updated by id");
    chemistryMap.put("updatedByName", "last updated person name");

    chemistryMap.put("facultyId", "faculty for this course");
    chemistryMap.put("facultyName", "name of the faculty");
    chemistryMap.put(
        "CoursecontentType",
        "list of course content type as comma separated , pdf, video, wordDoc");
    chemistryMap.put("availableFor", "[\"C.B.S.C\",\"I.C.S.C\",\"all\"]");
    chemistryMap.put("tutor", "[{\"id\":\"name\"},{\"id\":\"name\"}]");
    chemistryMap.put("operationType", "add/updated/delete");
    chemistryMap.put("owner", "EkStep");

    chemistryMap.put("visibility", "Default");
    chemistryMap.put(
        "downloadUrl",
        "https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_112228048362078208130/test-content-1_1493905653021_do_112228048362078208130_5.0.ecar");

    chemistryMap.put("language", "[\"Hindi\"]");
    chemistryMap.put("mediaType", "content");
    chemistryMap.put(
        "variants",
        "{\"spine\": {\"ecarUrl\": \"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_112228048362078208130/test-content-1_1493905655272_do_112228048362078208130_5.0_spine.ecar\",\"size\": 863}}");
    chemistryMap.put("mimeType", "application/vnd.ekstep.html-archive");
    chemistryMap.put("osId", "org.ekstep.quiz.app");
    chemistryMap.put("languageCode", "hi");
    chemistryMap.put("createdOn", "2017-05-04T13:47:32.676+0000");
    chemistryMap.put("pkgVersion", appendVal);
    chemistryMap.put("versionKey", "1495646809112");

    chemistryMap.put("lastPublishedOn", "2017-05-04T13:47:33.000+0000");
    chemistryMap.put(
        "collections",
        "[{\"identifier\": \"do_1121912573615472641169\",\"name\": \"A\",\"objectType\": \"Content\",\"relation\": \"hasSequenceMember\",\"description\": \"A.\",\"index\": null}]");
    chemistryMap.put("name", "Test Content 1");
    chemistryMap.put(
        "artifactUrl",
        "https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/do_112228048362078208130/artifact/advancedenglishassessment1_1533_1489654074_1489653812104_1492681721669.zip");
    chemistryMap.put("lastUpdatedOn", "2017-05-24T17:26:49.112+0000");
    chemistryMap.put("contentType", "Story");
    chemistryMap.put("status", "Live");
    chemistryMap.put("channel", "NTP");
    return chemistryMap;
  }

  private static Map<String, Object> intitializePhysicsCourse(int appendVal) {
    Map<String, Object> physicsCourseMap = new HashMap<>();
    physicsCourseMap.put("courseType", "type of the course. all , private");
    physicsCourseMap.put("description", "This is for physics");
    physicsCourseMap.put("size", 20);
    physicsCourseMap.put("objectType", "course");
    physicsCourseMap.put("courseId", "course id_" + appendVal);
    physicsCourseMap.put("courseName", "NTP course_" + appendVal);
    physicsCourseMap.put("courseDuration", appendVal);
    physicsCourseMap.put("noOfLecture", 30 + appendVal);
    physicsCourseMap.put("organisationId", "org id");
    physicsCourseMap.put("orgName", "Name of the organisation");
    physicsCourseMap.put("courseAddedById", "who added the course in NTP");
    physicsCourseMap.put(
        "courseAddedByName", "Name of the person who added the course under sunbird");
    physicsCourseMap.put("coursePublishedById", "who published the course");
    physicsCourseMap.put("coursePublishedByName", "who published the course");
    physicsCourseMap.put("enrollementStartDate", new Date());
    physicsCourseMap.put("publishedDate", new Date());
    physicsCourseMap.put("updatedDate", new Date());
    physicsCourseMap.put("updatedById", "last updated by id");
    physicsCourseMap.put("updatedByName", "last updated person name");

    physicsCourseMap.put("facultyId", "faculty for this course");
    physicsCourseMap.put("facultyName", "name of the faculty");
    physicsCourseMap.put(
        "CoursecontentType",
        "list of course content type as comma separated , pdf, video, wordDoc");
    physicsCourseMap.put("availableFor", "[\"C.B.S.C\",\"I.C.S.C\",\"all\"]");
    physicsCourseMap.put("tutor", "[{\"id\":\"name\"},{\"id\":\"name\"}]");
    physicsCourseMap.put("operationType", "add/updated/delete");
    physicsCourseMap.put("owner", "EkStep");

    physicsCourseMap.put("visibility", "Default");
    physicsCourseMap.put(
        "downloadUrl",
        "https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_112228048362078208130/test-content-1_1493905653021_do_112228048362078208130_5.0.ecar");

    physicsCourseMap.put("language", "[\"Hindi\"]");
    physicsCourseMap.put("mediaType", "content");
    physicsCourseMap.put(
        "variants",
        "{\"spine\": {\"ecarUrl\": \"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_112228048362078208130/test-content-1_1493905655272_do_112228048362078208130_5.0_spine.ecar\",\"size\": 863}}");
    physicsCourseMap.put("mimeType", "application/vnd.ekstep.html-archive");
    physicsCourseMap.put("osId", "org.ekstep.quiz.app");
    physicsCourseMap.put("languageCode", "hi");
    physicsCourseMap.put("createdOn", "2017-06-04T13:47:32.676+0000");
    physicsCourseMap.put("pkgVersion", appendVal);
    physicsCourseMap.put("versionKey", "1495646809112");

    physicsCourseMap.put("lastPublishedOn", "2017-05-04T13:47:33.000+0000");
    physicsCourseMap.put(
        "collections",
        "[{\"identifier\": \"do_1121912573615472641169\",\"name\": \"A\",\"objectType\": \"Content\",\"relation\": \"hasSequenceMember\",\"description\": \"A.\",\"index\": null}]");
    physicsCourseMap.put("name", "Test Content 1");
    physicsCourseMap.put(
        "artifactUrl",
        "https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/do_112228048362078208130/artifact/advancedenglishassessment1_1533_1489654074_1489653812104_1492681721669.zip");
    physicsCourseMap.put("lastUpdatedOn", "2017-05-24T17:26:49.112+0000");
    physicsCourseMap.put("contentType", "Story");
    physicsCourseMap.put("status", "Live");
    physicsCourseMap.put("channel", "NTP");
    return physicsCourseMap;
  }

  private void mockBaseRules() {
    client = mock(TransportClient.class);
    PowerMockito.mockStatic(ConnectionManager.class);
    try {
      doNothing().when(ConnectionManager.class, "registerShutDownHook");
    } catch (Exception e) {
      Assert.fail("Initialization of test case failed due to " + e.getLocalizedMessage());
    }
    when(ConnectionManager.getClient()).thenReturn(client);
  }

  private static void mockRulesForGet() {
    mockRulesForGet(false);
  }

  private static void mockRulesForGet(boolean expectedEmptyMap) {
    GetRequestBuilder grb = mock(GetRequestBuilder.class);
    GetResponse gResp = mock(GetResponse.class);
    when(client.prepareGet(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
        .thenReturn(grb);

    when(client.prepareGet()).thenReturn(grb);
    when(grb.setIndex(Mockito.anyString())).thenReturn(grb);
    when(grb.setId(Mockito.anyString())).thenReturn(grb);
    when(grb.get()).thenReturn(gResp);
    Map expMap = expectedEmptyMap ? Collections.emptyMap() : chemistryMap;
    when(gResp.getSource()).thenReturn(expMap);
  }

  private void mockRulesForSearch(long expectedValue) {
    SearchRequestBuilder srb = mock(SearchRequestBuilder.class);
    ListenableActionFuture<SearchResponse> lstActFtr = mock(ListenableActionFuture.class);
    List<SearchHit> lst = new ArrayList<>();
    SearchHit hit1 = mock(SearchHit.class);
    lst.add(hit1);

    SearchResponse searchResponse = mock(SearchResponse.class);
    Aggregations aggregations = mock(Aggregations.class);
    Terms terms = mock(Terms.class);
    Histogram histogram = mock(Histogram.class);
    SearchHits searchHits = mock(SearchHits.class);

    when(client.prepareSearch(Mockito.anyVararg())).thenReturn(srb);
    when(srb.setIndices(Mockito.anyVararg())).thenReturn(srb);
    when(srb.setTypes(Mockito.anyVararg())).thenReturn(srb);
    when(srb.addSort(Mockito.anyString(), Mockito.any(SortOrder.class))).thenReturn(srb);
    when(srb.execute()).thenReturn(lstActFtr);
    when(lstActFtr.actionGet()).thenReturn(searchResponse);
    when(searchResponse.getHits()).thenReturn(searchHits);
    when(searchResponse.getAggregations()).thenReturn(aggregations);
    when(aggregations.get(Mockito.eq("description"))).thenReturn(terms);
    // when(aggregations.get(Mockito.eq("createdOn"))).thenReturn(histogram);
    when(terms.getBuckets()).thenReturn(new ArrayList<>());
    when(histogram.getBuckets()).thenReturn(new ArrayList<>());

    when(searchHits.getTotalHits()).thenReturn(expectedValue);

    when(searchHits.iterator()).thenReturn(lst.iterator());
    when(hit1.getSourceAsMap()).thenReturn(new HashMap());
  }

  private static void mockRulesForInsert() {
    IndexRequestBuilder irb = mock(IndexRequestBuilder.class);
    IndexResponse ir = mock(IndexResponse.class);

    when(client.prepareIndex(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
        .thenReturn(irb);
    when(irb.setSource(Mockito.anyMap())).thenReturn(irb);
    when(irb.get()).thenReturn(ir);
    when(ir.getId()).thenReturn((String) chemistryMap.get("courseId"));
  }

  private static void mockRulesForUpdate() {
    UpdateRequestBuilder urbForUpdate = mock(UpdateRequestBuilder.class);
    UpdateRequestBuilder urbForEmptyUpdate = mock(UpdateRequestBuilder.class);
    ActionFuture<UpdateResponse> actFtr = mock(ActionFuture.class);
    UpdateResponse updateResponse = mock(UpdateResponse.class);
    UpdateResponse updateForEmptyRespose = mock(UpdateResponse.class);

    when(client.prepareUpdate(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
        .thenReturn(urbForUpdate);
    when(urbForUpdate.setDoc(Mockito.anyMap())).thenReturn(urbForUpdate);
    when(urbForUpdate.get()).thenReturn(updateResponse);
    when(updateResponse.getResult()).thenReturn(Result.UPDATED);

    // Making sure update returns empty for empty response.
    when(urbForUpdate.setDoc(Mockito.eq(new HashMap<String, Object>())))
        .thenReturn(urbForEmptyUpdate);
    when(urbForEmptyUpdate.get()).thenReturn(updateForEmptyRespose);
    when(updateForEmptyRespose.getResult()).thenReturn(Result.NOOP);

    when(client.update(Mockito.any(UpdateRequest.class))).thenReturn(actFtr);
    try {
      when(actFtr.get()).thenReturn(updateResponse);
    } catch (InterruptedException | ExecutionException e) {
      Assert.fail("Initialization of test case failed due to " + e.getLocalizedMessage());
    }
  }

  private static void mockRulesForDelete() {
    DeleteRequestBuilder drb = mock(DeleteRequestBuilder.class);
    DeleteResponse delResponse = mock(DeleteResponse.class);
    when(client.prepareDelete(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
        .thenReturn(drb);
    when(drb.get()).thenReturn(delResponse);
    when(delResponse.getResult()).thenReturn(Result.DELETED);
  }

  public static void mockRulesForIndexes() {
    mockRulesForIndexes(true);
  }

  public static void mockRulesForIndexes(boolean mappingsDone) {

    IndicesAdminClient indicesAdminMock = mock(IndicesAdminClient.class);
    AdminClient adminMock = mock(AdminClient.class);
    RefreshRequestBuilder refReqBldr = mock(RefreshRequestBuilder.class);
    RefreshResponse refResponse = mock(RefreshResponse.class);
    ActionFuture actFtrType = mock(ActionFuture.class);
    ActionFuture actFtrIndex = mock(ActionFuture.class);
    IndicesExistsResponse indExistResponse = mock(IndicesExistsResponse.class);
    TypesExistsResponse typeExistsResponse = mock(TypesExistsResponse.class);

    CreateIndexRequestBuilder mockCreateIndexReqBldr = mock(CreateIndexRequestBuilder.class);
    PutMappingRequestBuilder mockPutMappingReqBldr = mock(PutMappingRequestBuilder.class);
    PutMappingResponse mockPutMappingResponse = mock(PutMappingResponse.class);
    CreateIndexResponse mockCreateIndResp = mock(CreateIndexResponse.class);

    doReturn(adminMock).when(client).admin();
    doReturn(indicesAdminMock).when(adminMock).indices();
    doReturn(actFtrType).when(indicesAdminMock).typesExists(Mockito.any(TypesExistsRequest.class));
    doReturn(actFtrIndex).when(indicesAdminMock).exists(Mockito.any(IndicesExistsRequest.class));
    doReturn(refReqBldr).when(indicesAdminMock).prepareRefresh(Mockito.anyVararg());

    doReturn(refResponse).when(refReqBldr).get();
    try {
      doReturn(indExistResponse).when(actFtrIndex).get();
      doReturn(true).when(indExistResponse).isExists();
    } catch (InterruptedException | ExecutionException e) {
      Assert.fail("Exception occurred " + e.getLocalizedMessage());
    }

    try {
      doReturn(typeExistsResponse).when(actFtrType).get();
      doReturn(true).when(typeExistsResponse).isExists();
    } catch (InterruptedException | ExecutionException e) {
      Assert.fail("Exception occurred " + e.getLocalizedMessage());
    }
    doReturn(mockCreateIndexReqBldr).when(indicesAdminMock).prepareCreate(Mockito.anyString());

    doReturn(mockCreateIndexReqBldr)
        .when(mockCreateIndexReqBldr)
        .setSettings(Mockito.any(Settings.class));

    doReturn(mockCreateIndResp).when(mockCreateIndexReqBldr).get();
    doReturn(true).when(mockCreateIndResp).isAcknowledged();
    doReturn(mockPutMappingReqBldr).when(indicesAdminMock).preparePutMapping(Mockito.anyString());
    doReturn(mockPutMappingReqBldr).when(indicesAdminMock).preparePutMapping(Mockito.anyString());
    doReturn(mockPutMappingReqBldr).when(mockPutMappingReqBldr).setSource(Mockito.anyString());
    doReturn(mockPutMappingReqBldr).when(mockPutMappingReqBldr).setType(Mockito.anyString());
    doReturn(mockPutMappingResponse).when(mockPutMappingReqBldr).get();
    doReturn(mappingsDone).when(mockPutMappingResponse).isAcknowledged();
  }

  private static void mockRulesHttpRequest() {
    PowerMockito.mockStatic(HttpUtil.class);
    try {
      when(HttpUtil.sendPostRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap()))
          .thenReturn("{}");
    } catch (Exception e) {
      Assert.fail("Exception occurred " + e.getLocalizedMessage());
    }
  }

  private static void mockRulesBulkInsert() {
    PowerMockito.mockStatic(BulkProcessor.class);
    BulkProcessor.Builder bldr = mock(BulkProcessor.Builder.class);
    BulkProcessor bProcessor = mock(BulkProcessor.class);
    when(BulkProcessor.builder(Mockito.any(Client.class), Mockito.any(Listener.class)))
        .thenReturn(bldr);
    when(bldr.setBulkActions(Mockito.anyInt())).thenReturn(bldr);
    when(bldr.setConcurrentRequests(Mockito.anyInt())).thenReturn(bldr);
    when(bldr.build()).thenReturn(bProcessor);
    when(bProcessor.add(Mockito.any(IndexRequest.class))).thenReturn(bProcessor);
  }
}
