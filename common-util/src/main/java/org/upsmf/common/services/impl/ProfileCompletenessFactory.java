/** */
package org.upsmf.common.services.impl;

import org.upsmf.common.services.ProfileCompletenessService;

/** @author Manzarul */
public class ProfileCompletenessFactory {

  /** @return */
  public static ProfileCompletenessService getInstance() {
    return new ProfileCompletenessServiceImpl();
  }
}
