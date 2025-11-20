package com.example.handler.api.domain;

import com.example.handler.api.RouteHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.model.Domain;
import com.example.model.PaginatedResult;
import com.example.utils.PaginationUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ListDomains implements RouteHandler {
    private final DynamoDbTable<Domain> table;
    private final ObjectMapper objectMapper;

    public ListDomains(DynamoDbTable<Domain> table, ObjectMapper objectMapper) {
        this.table = table;
        this.objectMapper = objectMapper;
    }

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent request) {
        try {
            Map<String, String> queryParams = request.getQueryStringParameters();
            
            String nextToken = (queryParams != null) ? queryParams.get("next_token") : null;
            int maxNumber = (queryParams != null && queryParams.containsKey("max_number")) 
                            ? Integer.parseInt(queryParams.get("max_number")) : 10;

            ScanEnhancedRequest.Builder scanBuilder = ScanEnhancedRequest.builder()
                    .limit(maxNumber);

            if (nextToken != null) {
                scanBuilder.exclusiveStartKey(PaginationUtils.decodeToken(nextToken));
            }

            Iterator<Page<Domain>> pageIterator = table.scan(scanBuilder.build()).iterator();
            
            List<Domain> items = Collections.emptyList();
            String newNextToken = null;

            if (pageIterator.hasNext()) {
                Page<Domain> page = pageIterator.next();
                items = page.items();
                
                Map<String, AttributeValue> lastKey = page.lastEvaluatedKey();
                newNextToken = PaginationUtils.encodeToken(lastKey);
            }

            PaginatedResult<Domain> result = new PaginatedResult<>(items, newNextToken);
            
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(objectMapper.writeValueAsString(result));

        } catch (Exception e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Error listing domains: " + e.getMessage());
        }
    }
}
