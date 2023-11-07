package com.tarento.upsmf.examsAndAdmissions.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tarento.upsmf.examsAndAdmissions.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
        if(id == null || id.isBlank()) {
            throw new RuntimeException("Invalid Request");
        }
        // check in redis
        boolean keyExists = Boolean.TRUE.equals(hashOperations.getOperations().hasKey(id));
        if(keyExists) {
            return hashOperations.get(userRedisHashKey, id);
        }
        ObjectNode request = mapper.createObjectNode();
        ObjectNode root = mapper.createObjectNode();
        root.put("userName", id);
        request.put("request", root);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            ResponseEntity<String> response = restTemplate.exchange(
                    userInfoUrl, HttpMethod.POST,
                    new HttpEntity<>(request, headers), String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseBody = mapper.readTree(response.getBody());
                User user = mapper.treeToValue(responseBody, User.class);
                if(user != null) {
                    hashOperations.put(userRedisHashKey, user.getId(), user);
                    return user;
                }
            }
            throw new RuntimeException("Error in getting user info.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error in getting user info.");
        }
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
