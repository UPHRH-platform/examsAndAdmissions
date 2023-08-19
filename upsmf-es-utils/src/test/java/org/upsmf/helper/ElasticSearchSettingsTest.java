package org.upsmf.helper;

import org.junit.Assert;
import org.junit.Test;

public class ElasticSearchSettingsTest {

	@Test
	public void testcreateSettingsForIndex() {

		String settings = ElasticSearchSettings.createSettingsForIndex();
		Assert.assertNotNull(settings);
	}

}
