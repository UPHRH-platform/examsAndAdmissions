package org.upsmf.common.factory;

import org.upsmf.common.ElasticSearchRestHighImpl;
import org.upsmf.common.ElasticSearchTcpImpl;
import org.upsmf.common.inf.ElasticSearchService;
import org.upsmf.common.models.util.JsonKey;
import org.upsmf.common.models.util.LoggerEnum;
import org.upsmf.common.models.util.ProjectLogger;

public class EsClientFactory {

  private static ElasticSearchService tcpClient = null;
  private static ElasticSearchService restClient = null;

  /**
   * This method return REST/TCP client for elastic search
   *
   * @param type can be "tcp" or "rest"
   * @return ElasticSearchService with the respected type impl
   */
  public static ElasticSearchService getInstance(String type) {
    if (JsonKey.TCP.equals(type)) {
      return getTcpClient();
    } else if (JsonKey.REST.equals(type)) {
      return getRestClient();
    } else {
      ProjectLogger.log(
          "EsClientFactory:getInstance: value for client type provided null ", LoggerEnum.ERROR);
    }
    return null;
  }

  private static ElasticSearchService getTcpClient() {
    if (tcpClient == null) {
      tcpClient = new ElasticSearchTcpImpl();
    }
    return tcpClient;
  }

  private static ElasticSearchService getRestClient() {
    if (restClient == null) {
      restClient = new ElasticSearchRestHighImpl();
    }
    return restClient;
  }
}
