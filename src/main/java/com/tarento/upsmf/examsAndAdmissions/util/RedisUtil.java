package com.tarento.upsmf.examsAndAdmissions.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tarento.upsmf.examsAndAdmissions.exception.InvalidRequestException;
import com.tarento.upsmf.examsAndAdmissions.exceptions.UserInfoFetchException;
import com.tarento.upsmf.examsAndAdmissions.model.User;
import org.apache.kafka.common.requests.DeleteAclsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.kafka.common.requests.DeleteAclsResponse.log;

@Component
public class RedisUtil {

    public static final String ROLES_CAMELCASE = "Role";
    public static final String ROLES_LOWERCASE = "role";
    @Resource(name="redisTemplate")
    private HashOperations<String, String, User> hashOperations;

    @Value("${api.user.details}")
    private String userInfoUrl;

    @Value("${user.redis.hash.key}")
    private String userRedisHashKey;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Method to get user info
     * @param id
     * @return
     */
    public User getUserById(String id) {
        try {
            validateId(id);

            // Check in Redis
            if (Boolean.TRUE.equals(hashOperations.getOperations().hasKey(id))) {
                return hashOperations.get(userRedisHashKey, id);
            }

            // Fetch user information from the external service
            ResponseEntity<String> response = fetchUserInfoFromExternalService(id);
            return handleUserInfoResponse(response, id);

        } catch (Exception e) {
            // Log the exception and rethrow a custom exception
            log.error("Error in getting user info for ID: {}", id, e);
            throw new UserInfoFetchException("Error in getting user info.", e);
        }
    }

    private void validateId(String id) {
        if (id == null || id.isBlank()) {
            throw new InvalidRequestException("Invalid user ID");
        }
    }

    private ResponseEntity<String> fetchUserInfoFromExternalService(String id) {
        ObjectNode request = createUserInfoRequest(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return restTemplate.exchange(userInfoUrl, HttpMethod.POST, new HttpEntity<>(request, headers), String.class);
    }

    private ObjectNode createUserInfoRequest(String id) {
        ObjectNode root = mapper.createObjectNode();
        root.put("userName", id);

        ObjectNode request = mapper.createObjectNode();
        request.put("request", root);

        return request;
    }

    private User handleUserInfoResponse(ResponseEntity<String> response, String id) {
        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                JsonNode responseBody = mapper.readTree(response.getBody());
                User user = mapper.treeToValue(responseBody, User.class);

                if (user != null) {
                    hashOperations.put(userRedisHashKey, user.getId(), user);
                    return user;
                }
            } catch (IOException e) {
                log.error("Error parsing user info response for ID: {}", id, e);
            }
        }

        throw new UserInfoFetchException("Error in getting user info. Status: " + response.getStatusCode());
    }


    /**
     * Method to roles from user
     * @param id
     * @return
     */
    public List<String> getRolesByUserId(String id) {
        List<String> roles = new ArrayList<>();
        User user = getUserById(id);
        if(user != null) {
            if(user.getAttributes() != null && !user.getAttributes().isEmpty()) {
                List<String> rolesCamelCase = user.getAttributes().get(ROLES_CAMELCASE);
                List<String> rolesLowerCase = user.getAttributes().get(ROLES_LOWERCASE);
                if(rolesLowerCase != null && !rolesLowerCase.isEmpty()) {
                    roles.addAll(rolesLowerCase);
                }
                if(rolesCamelCase != null && !rolesCamelCase.isEmpty()) {
                    roles.addAll(rolesCamelCase);
                }
            }
        }
        return roles;
    }
}
