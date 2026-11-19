package org.medlearn;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;

import java.util.*;

@Service
public class KeycloakAdmin {
    private final RestClient http = RestClient.create();
    @Value("${KEYCLOAK_INTERNAL:http://keycloak:8080}")
    String base;
    @Value("${KEYCLOAK_CLIENT_SECRET}")
    String secret;

    String token() {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", "medlearn-backend");
        form.add("client_secret", secret);
        @SuppressWarnings("unchecked") Map<String, Object> r = http.post().uri(base + "/realms/medlearn/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form)
                .retrieve().body(Map.class);
        return (String) Objects.requireNonNull(r).get("access_token");
    }

    UUID create(String email, String password, String first, String last, boolean enabled, String role) {
        Map<String, Object> body = Map.of("username", email, "email", email, "firstName", first, "lastName", last, "enabled", enabled,
                "emailVerified", false, "credentials", List.of(Map.of("type", "password", "value", password, "temporary", false)));
        ResponseEntity<Void> res = http.post().uri(base + "/admin/realms/medlearn/users").header("Authorization", "Bearer " + token()).body(body).retrieve().toBodilessEntity();
        String loc = Objects.requireNonNull(res.getHeaders().getLocation()).toString();
        UUID id = UUID.fromString(loc.substring(loc.lastIndexOf('/') + 1));
        try {
            assignRole(id, role);
            return id;
        } catch (Exception e) {
            delete(id);
            throw e;
        }
    }


    void assignRole(UUID id, String role) {
        String auth = "Bearer " + token();

        String roleUrl = base + "/admin/realms/medlearn/roles/" + role;

        Map<String, Object> found;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = http.get()
                    .uri(roleUrl)
                    .header(HttpHeaders.AUTHORIZATION, auth)
                    .retrieve()
                    .body(Map.class);

            found = Objects.requireNonNull(
                    result,
                    "Keycloak returned an empty role response"
            );
        } catch (org.springframework.web.client.RestClientResponseException e) {
            throw new IllegalStateException(
                    "Keycloak failed to retrieve realm role '" + role +
                            "' (HTTP " + e.getStatusCode().value() + ")",
                    e
            );
        }

        String mappingUrl = base +
                "/admin/realms/medlearn/users/" + id +
                "/role-mappings/realm";

        try {
            http.post()
                    .uri(mappingUrl)
                    .header(HttpHeaders.AUTHORIZATION, auth)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(List.of(found))
                    .retrieve()
                    .toBodilessEntity();
        } catch (org.springframework.web.client.RestClientResponseException e) {
            throw new IllegalStateException(
                    "Keycloak failed to assign realm role '" + role +
                            "' (HTTP " + e.getStatusCode().value() + ")",
                    e
            );
        }
    }


    void enable(UUID id) {
        http.put().uri(base + "/admin/realms/medlearn/users/" + id).header("Authorization", "Bearer " + token()).body(Map.of("enabled", true)).retrieve().toBodilessEntity();
    }

    void delete(UUID id) {
        http.delete().uri(base + "/admin/realms/medlearn/users/" + id).header("Authorization", "Bearer " + token()).retrieve().toBodilessEntity();
    }
}
