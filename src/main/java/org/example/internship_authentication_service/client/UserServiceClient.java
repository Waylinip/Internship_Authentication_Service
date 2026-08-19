package org.example.internship_authentication_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.internship_authentication_service.dto.UserProfileRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestClient userServiceRestClient;

    public void createProfile(UserProfileRequest request) {
        userServiceRestClient.post()
                .uri("/api/internal/users")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
    public void rollbackProfile(Long authUserId) {
        userServiceRestClient.delete()
                .uri("/api/internal/users/{authUserId}", authUserId)
                .retrieve()
                .toBodilessEntity();
    }
}
