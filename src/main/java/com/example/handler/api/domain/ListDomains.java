package com.example.handler.api.domain;

import com.example.handler.api.RouteHandler;
import com.example.utils.PaginationUtils;
import com.example.model.Domain;
import com.example.model.PaginatedResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

public class ListDomains implements RouteHandler {
    
    private final DynamoDbTable<Domain> domainTable; 
    private final ObjectMapper mapper;

    public ListDomains(DynamoDbTable<Domain> domainTable, ObjectMapper mapper) { 
        this.domainTable = domainTable; 
        this.mapper = mapper; 
    }

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent request) {
        try {
            Map<String, String> queryParams = request.getQueryStringParameters();
            String nextToken = (queryParams != null) ? queryParams.get("nextToken") : null;
            int maxNumber = 10;
            if (queryParams != null && queryParams.containsKey("maxResults")) {
                try { 
                    maxNumber = Integer.parseInt(queryParams.get("maxResults")); 
                } catch (NumberFormatException ignored) {}
            }

            ScanEnhancedRequest.Builder scanBuilder = ScanEnhancedRequest.builder().limit(maxNumber);
            
            if (nextToken != null) {
                Map<String, AttributeValue> key = PaginationUtils.decodeToken(nextToken);
                if (key != null && key.containsKey("TOKEN_VALUE")) {
                    scanBuilder.exclusiveStartKey(Map.of("identifier", key.get("TOKEN_VALUE")));
                }
            }

            var pageIterator = domainTable.scan(scanBuilder.build()).iterator();
            List<Domain> items = java.util.Collections.emptyList();
            String newNextToken = null;

            if (pageIterator.hasNext()) {
                var page = pageIterator.next();
                items = page.items();
                newNextToken = PaginationUtils.encodeToken(page.lastEvaluatedKey());
            }

            PaginatedResult<Domain> result = new PaginatedResult<>(items, newNextToken);
            
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(mapper.writeValueAsString(result));
        } catch (Exception e) { 
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("Error listing domains: " + e.getMessage()); 
        }
    }
}
