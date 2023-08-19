package org.upsmf.common.factory;

import org.junit.Assert;
import org.junit.Test;
import org.upsmf.common.ElasticSearchRestHighImpl;
import org.upsmf.common.ElasticSearchTcpImpl;
import org.upsmf.common.inf.ElasticSearchService;

public class EsClientFactoryTest {

	@Test
	public void testGetTcpClient() {
		ElasticSearchService service = EsClientFactory.getInstance("tcp");
		Assert.assertTrue(service instanceof ElasticSearchTcpImpl);
	}
	@Test
	public void testGetRestClient() {
		ElasticSearchService service = EsClientFactory.getInstance("rest");
		Assert.assertTrue(service instanceof ElasticSearchRestHighImpl);
	}
	@Test
	public void testInstanceNull() {
		ElasticSearchService service = EsClientFactory.getInstance("test");
		Assert.assertNull(service);
	}
}
