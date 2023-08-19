package org.upsmf.common.request.orgvalidator;

import org.upsmf.common.models.util.JsonKey;
import org.upsmf.common.request.Request;
import org.upsmf.common.responsecode.ResponseCode;

public class OrgTypeRequestValidator extends BaseOrgRequestValidator {

  public void validateUpdateOrgTypeRequest(Request request) {
    validateCreateOrgTypeRequest(request);
    validateParam(
        (String) request.getRequest().get(JsonKey.ID),
        ResponseCode.mandatoryParamsMissing,
        JsonKey.ID);
  }

  public void validateCreateOrgTypeRequest(Request request) {
    validateParam(
        (String) request.getRequest().get(JsonKey.NAME),
        ResponseCode.mandatoryParamsMissing,
        JsonKey.NAME);
  }
}
