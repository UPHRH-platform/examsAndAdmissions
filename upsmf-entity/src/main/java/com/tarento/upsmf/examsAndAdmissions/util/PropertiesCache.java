package com.tarento.upsmf.examsAndAdmissions.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tarento.upsmf.examsAndAdmissions.model.LoggerEnum;

public class PropertiesCache {

	public static final Logger logger = LoggerFactory.getLogger(PropertiesCache.class);

	private final String[] fileName = { "elasticsearch.config.properties", "cassandra.config.properties",
			"dbconfig.properties", "externalresource.properties", "sso.properties", "userencryption.properties",
			"profilecompleteness.properties", "mailTemplates.properties" };
	private final Properties configProp = new Properties();
	public final Map<String, Float> attributePercentageMap = new ConcurrentHashMap<>();
	private static PropertiesCache propertiesCache = null;

	/** private default constructor */
	private PropertiesCache() {
		for (String file : fileName) {
			InputStream in = this.getClass().getClassLoader().getResourceAsStream(file);
			try {
				configProp.load(in);
			} catch (IOException e) {
				logger.error("Error in properties cache", e);
			}
		}
		loadWeighted();
	}

	public static PropertiesCache getInstance() {

		// change the lazy holder implementation to simple singleton implementation ...
		if (null == propertiesCache) {
			synchronized (PropertiesCache.class) {
				if (null == propertiesCache) {
					propertiesCache = new PropertiesCache();
				}
			}
		}

		return propertiesCache;
	}

	public void saveConfigProperty(String key, String value) {
		configProp.setProperty(key, value);
	}

	public String getProperty(String key) {
		String value = System.getenv(key);
		if (StringUtils.isNotBlank(value))
			return value;
		return configProp.getProperty(key) != null ? configProp.getProperty(key) : key;
	}

	private void loadWeighted() {
		String key = configProp.getProperty("user.profile.attribute");
		String value = configProp.getProperty("user.profile.weighted");
		if (StringUtils.isBlank(key)) {
			logger.error("Profile completeness value is not set==", LoggerEnum.INFO.name());
		} else {
			String keys[] = key.split(",");
			String values[] = value.split(",");
			if (keys.length == value.length()) {
				// then take the value from user
				logger.error("weighted value is provided by user.");
				for (int i = 0; i < keys.length; i++)
					attributePercentageMap.put(keys[i], new Float(values[i]));
			} else {
				// equally divide all the provided field.
				logger.error("weighted value is not provided  by user.");
				float perc = (float) 100.0 / keys.length;
				for (int i = 0; i < keys.length; i++)
					attributePercentageMap.put(keys[i], perc);
			}
		}
	}

	/**
	 * Method to read value from resource file .
	 *
	 * @param key
	 * @return
	 */
	public String readProperty(String key) {
		String value = System.getenv(key);
		if (StringUtils.isNotBlank(value))
			return value;
		return configProp.getProperty(key);
	}
}
