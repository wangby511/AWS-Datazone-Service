package com.example.handler.api.domain;

import com.example.handler.api.RouteHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.model.Domain;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import java.util.Map;

public class DeleteDomain implements RouteHandler {
    private final DynamoDbTable<Domain> table;
    private final ObjectMapper objectMapper;

    public DeleteDomain(DynamoDbTable<Domain> table, ObjectMapper objectMapper) {
        this.table = table;
        this.objectMapper = objectMapper;
    }

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent request) {
        try {
            Map<String, String> pathParams = request.getPathParameters();
            String domainId = (pathParams != null) ? pathParams.get("domainId") : null;
            
            if (domainId == null) return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Missing domainId.");

            // WARNING: A real application would first check for associated projects and delete them (cascading delete).
            Domain deletedDomain = table.deleteItem(r -> r.key(Key.builder().partitionValue(domainId).build()));

            if (deletedDomain == null) {
                return new APIGatewayProxyResponseEvent().withStatusCode(404).withBody("Domain not found or already deleted.");
            }

            return new APIGatewayProxyResponseEvent().withStatusCode(204).withBody("");
        } catch (Exception e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Error deleting domain: " + e.getMessage());
        }
    }
}
