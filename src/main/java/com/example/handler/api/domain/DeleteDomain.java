package com.example.handler.api.domain;

import com.example.handler.api.RouteHandler;
import com.example.model.Domain;
import com.example.constant.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import java.util.Map;

public class DeleteDomain implements RouteHandler {
    
    private final DynamoDbTable<Domain> domainTable; 
    private final ObjectMapper mapper;

    public DeleteDomain(DynamoDbTable<Domain> domainTable, ObjectMapper mapper) { 
        this.domainTable = domainTable; 
        this.mapper = mapper; 
    }

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent request) {
        try {
            Map<String, String> pathParams = request.getPathParameters();
            String identifier = (pathParams != null) ? pathParams.get("identifier") : null;
            
            if (identifier == null) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("Missing identifier.");
            }
            if (!identifier.matches(Constants.DOMAIN_ID_RULE_REFERENCE)) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("Invalid identifier format.");
            }
            
            domainTable.deleteItem(Key.builder().partitionValue(identifier).build());
            
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(204)
                    .withBody("");
        } catch (Exception e) { 
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("Error deleting domain: " + e.getMessage()); 
        }
    }
}
