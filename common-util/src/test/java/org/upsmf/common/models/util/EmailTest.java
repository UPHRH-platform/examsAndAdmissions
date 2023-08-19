/** */
package org.upsmf.common.models.util;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;
import org.upsmf.common.models.util.mail.GMailAuthenticator;
import org.upsmf.common.models.util.mail.SendMail;

import javax.mail.PasswordAuthentication;

/** @author Manzarul */
public class EmailTest {

  private static GMailAuthenticator authenticator = null;

  @BeforeClass
  public static void setUp() {
    authenticator = new GMailAuthenticator("test123", "test");
    // clear Mock JavaMail box
    Mailbox.clearAll();
  }

  @Test
  public void createGmailAuthInstance() {
    GMailAuthenticator authenticator = new GMailAuthenticator("test123", "test");
    Assert.assertNotEquals(null, authenticator);
  }

  @Test
  public void passwordAuthTest() {
    PasswordAuthentication authentication = authenticator.getPasswordAuthentication();
    Assert.assertEquals("test", authentication.getPassword());
  }


  @Test
  public void initialiseFromPropertyTest() {
    SendMail.initialiseFromProperty();
    Assert.assertTrue(true);
  }

  @AfterClass
  public static void tearDown() {
    authenticator = null;
    Mailbox.clearAll();
  }
}
