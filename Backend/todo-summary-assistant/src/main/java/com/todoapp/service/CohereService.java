package com.todoapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class CohereService {

    @Value("${cohere.api.key}")
    private String cohereApiKey;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CohereService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        this.objectMapper = new ObjectMapper();
    }

    public String summarizeText(String text) throws IOException {
        String url = "https://api.cohere.ai/v2/chat";

        ObjectNode requestBodyJson = objectMapper.createObjectNode();

        requestBodyJson.put("model", "command-a-03-2025");

        ArrayNode messages = requestBodyJson.putArray("messages");

        ObjectNode message = messages.addObject();
        message.put("role", "user");
        message.put(
                "content",
                "Summarize the following pending todo items concisely. " +
                "Return only the summary in a clear paragraph.\n\n" +
                text
        );

        RequestBody body = RequestBody.create(
                requestBodyJson.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + cohereApiKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null
                        ? response.body().string()
                        : "";

                throw new IOException(
                        "Unexpected code " + response + " Body: " + errorBody
                );
            }

            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            JsonNode content = jsonNode
                    .path("message")
                    .path("content");

            if (content.isArray() && content.size() > 0) {
                JsonNode textNode = content.get(0).path("text");

                if (!textNode.isMissingNode()) {
                    return textNode.asText();
                }
            }

            return "No summary found.";
        }
    }
}