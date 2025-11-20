package com.example.utils;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PaginationUtils {

    /**
     * Encodes the DynamoDB ExclusiveStartKey map into a Base64 URL-safe token.
     */
    public static String encodeToken(Map<String, AttributeValue> lastEvaluatedKey) {
        if (lastEvaluatedKey == null || lastEvaluatedKey.isEmpty()) {
            return null;
        }
        
        try {
            // Convert Map<String, AttributeValue> to a simple string representation (Simplified version for example)
            String mapString = lastEvaluatedKey.entrySet().stream()
                .map(entry -> entry.getKey() + ":" + getAttributeValueString(entry.getValue()))
                .collect(Collectors.joining("|"));
            
            return Base64.getUrlEncoder().encodeToString(mapString.getBytes());
        } catch (Exception e) {
            System.err.println("Error encoding token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Decodes a Base64 URL-safe token back into a DynamoDB ExclusiveStartKey map.
     */
    public static Map<String, AttributeValue> decodeToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(token);
            String mapString = new String(decodedBytes);
            
            Map<String, AttributeValue> lastKey = new HashMap<>();
            
            for (String part : mapString.split("\|")) {
                String[] keyValue = part.split(":", 2);
                if (keyValue.length == 2) {
                    // Assuming all keys are String (S) for simplicity
                    lastKey.put(keyValue[0], AttributeValue.builder().s(keyValue[1]).build());
                }
            }
            return lastKey;

        } catch (IllegalArgumentException e) {
            System.err.println("Error decoding token: Invalid token format.");
            return null;
        } catch (Exception e) {
            System.err.println("Error decoding token: " + e.getMessage());
            return null;
        }
    }

    private static String getAttributeValueString(AttributeValue value) {
        if (value.s() != null) return value.s();
        return "";
    }
}
