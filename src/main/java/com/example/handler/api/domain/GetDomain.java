package com.example.handler.api.domain;

import com.example.handler.api.RouteHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.model.Domain;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import java.util.Map;

public class GetDomain implements RouteHandler {
    private final DynamoDbTable<Domain> table;
    private final ObjectMapper objectMapper;

    public GetDomain(DynamoDbTable<Domain> table, ObjectMapper objectMapper) {
        this.table = table;
        this.objectMapper = objectMapper;
    }

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent request) {
        try {
            Map<String, String> pathParams = request.getPathParameters();
            // Domain APIs path uses {domainId}
            String domainId = (pathParams != null) ? pathParams.get("domainId") : null; 
            
            if (domainId == null) return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Missing domainId.");

            Domain domain = table.getItem(r -> r.key(Key.builder().partitionValue(domainId).build()));

            if (domain == null) {
                return new APIGatewayProxyResponseEvent().withStatusCode(404).withBody("Domain not found.");
            }

            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(objectMapper.writeValueAsString(domain));
        } catch (Exception e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Error retrieving domain: " + e.getMessage());
        }
    }
}
