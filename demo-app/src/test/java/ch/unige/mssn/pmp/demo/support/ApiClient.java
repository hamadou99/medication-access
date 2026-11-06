package ch.unige.mssn.pmp.demo.support;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Appels HTTP réels : jeton, filtre de sécurité, module et base sont tous traversés. */
public final class ApiClient {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final HttpClient http = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    private final String base;

    public ApiClient(int port) {
        this.base = "http://localhost:" + port;
    }

    public record Answer(int status, String body) {
        public JsonNode json() {
            return JSON.readTree(body);
        }

        public boolean accepted() {
            return status >= 200 && status < 300;
        }
    }

    public Answer get(String token, String path) {
        return send(token, path, "GET", null);
    }

    public Answer post(String token, String path, String json) {
        return send(token, path, "POST", json);
    }

    public Answer patch(String token, String path, String json) {
        return send(token, path, "PATCH", json);
    }

    public Answer send(String token, String path, String method, String json) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base + path))
                .method(method, json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json));
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        if (json != null) {
            request.header("Content-Type", "application/json");
        }
        try {
            HttpResponse<String> response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
            return new Answer(response.statusCode(), response.body());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    public static String json(Object value) {
        return JSON.writeValueAsString(value);
    }
}
