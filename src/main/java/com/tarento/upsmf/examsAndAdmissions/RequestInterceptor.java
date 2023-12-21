package com.tarento.upsmf.examsAndAdmissions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.tarento.upsmf.examsAndAdmissions.controller.BaseController;
import com.tarento.upsmf.examsAndAdmissions.util.AccessTokenValidator;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import com.tarento.upsmf.examsAndAdmissions.util.ResponseCode;

@Component
@Slf4j
public class RequestInterceptor extends BaseController implements HandlerInterceptor {

	@Autowired
	private AccessTokenValidator accessTokenValidator;

	@Value("${user.management.exam.fee.auth.token}")
	private String userManagementAuthToken;

	@Value("${request.interceptor.enabled}")
	private String isInterceptorEnabled;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if("true".equalsIgnoreCase(isInterceptorEnabled)) {
			return Boolean.TRUE;
		}
		if(request.getRequestURI().endsWith("login")) {
			return Boolean.TRUE;
		}
		log.info("Request type - {}", request.getMethod());
		if(request.getMethod().equalsIgnoreCase("options")){
			return Boolean.TRUE;
		}
		// read auth token from header
		if(request.getHeader(Constants.Parameters.X_USER_TOKEN) == null
				|| request.getHeader(Constants.Parameters.X_USER_TOKEN).isBlank()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(handleResponse(false, ResponseCode.TOKEN_MISSING));
			response.setContentType(MediaType.APPLICATION_JSON);
			return Boolean.FALSE;
		}
		String authToken = request.getHeader(Constants.Parameters.X_USER_TOKEN);
		if (StringUtils.isBlank(authToken)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(handleResponse(false, ResponseCode.TOKEN_MISSING));
			response.setContentType(MediaType.APPLICATION_JSON);
			return Boolean.FALSE;
		}
		if(request.getRequestURI().endsWith("/status/update")) {
			if(authToken.equals(userManagementAuthToken)) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}
		// authentication
		log.debug("request_token :"+ authToken);
		String userId = verifyRequestData(authToken, request.getRequestURI());
		//String userId = "userId";

		log.debug("userId :"+ userId);
		if (userId.equalsIgnoreCase(Constants.Parameters.UNAUTHORIZED)) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write(handleResponse(false, ResponseCode.UNAUTHORIZED));
			response.setContentType(MediaType.APPLICATION_JSON);
			return Boolean.FALSE;
		}
		request.setAttribute(Constants.Parameters.USER_ID, userId);
		return Boolean.TRUE;
	}

	private String verifyRequestData(String accessToken, String uri) {
		log.debug("verifyRequestData () "+accessToken);
		String clientAccessTokenId = accessTokenValidator.verifyUserToken(accessToken, true, uri);
		log.debug("verifyRequestData clientAccessTokenId (): "+clientAccessTokenId);
		return StringUtils.isBlank(clientAccessTokenId) ? Constants.Parameters.UNAUTHORIZED : clientAccessTokenId;
	}

}
