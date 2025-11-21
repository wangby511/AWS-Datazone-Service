package com.example.utils;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;

public class PaginationUtils {
    public static String encodeToken(Map<String, AttributeValue> lastEvaluatedKey) {
        if (lastEvaluatedKey == null || lastEvaluatedKey.isEmpty()) return null;
        try {
            String mapString = lastEvaluatedKey.entrySet().stream()
                .map(e -> e.getKey() + ":" + (e.getValue().s() != null ? e.getValue().s() : ""))
                .collect(Collectors.joining("|"));
            return Base64.getUrlEncoder().encodeToString(mapString.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) { return null; }
    }

    public static Map<String, AttributeValue> decodeToken(String token) {
        if (token == null || token.isEmpty()) return null;
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token));
            Map<String, AttributeValue> map = new HashMap<>();
            for (String part : decoded.split("\\|")) {
                String[] kv = part.split(":", 2);
                if (kv.length == 2) map.put(kv[0], AttributeValue.builder().s(kv[1]).build());
            }
            return map;
        } catch (Exception e) { return null; }
    }
}
