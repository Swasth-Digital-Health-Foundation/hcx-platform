package org.swasth.hcx.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.swasth.common.dto.RegistryResponse;
import org.swasth.common.service.RegistryService;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Service
public class AsyncService extends UserService {

    @Async("taskExecutor")
    public CompletableFuture<Map<String,Object>> performAsyncTask(Map<String, Object> user, Map<String, Object> requestBody, HttpHeaders headers, String action) throws Exception {
        Map<String, Object> userRequest = constructRequestBody(requestBody, user);
        CompletableFuture<Map<String,Object>> future = new CompletableFuture<>();
        try {
            Map<String,Object> registryResponse;
            if (StringUtils.equals(action, Constants.PARTICIPANT_USER_ADD)) {
                registryResponse = addUser(userRequest, headers);
            } else {
                registryResponse = removeUser(userRequest, headers);
            }
            future.complete(registryResponse);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }
}
