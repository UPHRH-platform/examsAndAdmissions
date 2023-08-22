/** */
package com.tarento.upsmf.examsAndAdmissions.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class will used to log the project message in any level.
 *
 * @author Manzarul
 */
public class ProjectLogger {

  private static String eVersion = "1.0";
  private static String pVersion = "1.0";
  private static String dataId = "Sunbird";
  private static ObjectMapper mapper = new ObjectMapper();
  private static Logger rootLogger = (Logger) LogManager.getLogger("defaultLogger");
  //	private static TelemetryLmaxWriter lmaxWriter = TelemetryLmaxWriter.getInstance();

  /** To log only message. */
  public static void log(String message) {
    log(message, null, LoggerEnum.DEBUG.name());
  }

  private static void log(String message, Object o, String name) {
  }
}
