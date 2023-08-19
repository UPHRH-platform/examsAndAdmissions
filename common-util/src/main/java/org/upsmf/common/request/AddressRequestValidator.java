package org.upsmf.common.request;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.upsmf.common.exception.ProjectCommonException;
import org.upsmf.common.models.util.JsonKey;
import org.upsmf.common.models.util.ProjectUtil;
import org.upsmf.common.models.util.ProjectUtil.AddressType;
import org.upsmf.common.responsecode.ResponseCode;

public class AddressRequestValidator extends BaseRequestValidator {

  private static final int ERROR_CODE = ResponseCode.CLIENT_ERROR.getResponseCode();

  public void validateAddress(Map<String, Object> address, String type) {
    if (StringUtils.isBlank((String) address.get(JsonKey.ADDRESS_LINE1))) {
      throw new ProjectCommonException(
          ResponseCode.addressError.getErrorCode(),
          ProjectUtil.formatMessage(
              ResponseCode.addressError.getErrorMessage(), type, JsonKey.ADDRESS_LINE1),
          ERROR_CODE);
    }
    if (StringUtils.isBlank((String) address.get(JsonKey.CITY))) {
      throw new ProjectCommonException(
          ResponseCode.addressError.getErrorCode(),
          ProjectUtil.formatMessage(
              ResponseCode.addressError.getErrorMessage(), type, JsonKey.CITY),
          ERROR_CODE);
    }
    if (address.containsKey(JsonKey.ADD_TYPE)) {

      if (StringUtils.isBlank((String) address.get(JsonKey.ADD_TYPE))) {
        throw new ProjectCommonException(
            ResponseCode.addressError.getErrorCode(),
            ProjectUtil.formatMessage(
                ResponseCode.addressError.getErrorMessage(), JsonKey.ADDRESS, JsonKey.TYPE),
            ERROR_CODE);
      }

      if (!StringUtils.isBlank((String) address.get(JsonKey.ADD_TYPE))
          && !checkAddressType((String) address.get(JsonKey.ADD_TYPE))) {
        throw new ProjectCommonException(
            ResponseCode.addressTypeError.getErrorCode(),
            ResponseCode.addressTypeError.getErrorMessage(),
            ERROR_CODE);
      }
    }
  }

  private static boolean checkAddressType(String addrType) {
    for (AddressType type : AddressType.values()) {
      if (type.getTypeName().equals(addrType)) {
        return true;
      }
    }
    return false;
  }
}
