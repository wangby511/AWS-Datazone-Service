package com.example.handler.api.project;

import com.example.handler.api.RouteHandler;
import com.example.utils.PaginationUtils;
import com.example.model.Project;
import com.example.model.PaginatedResult;
import com.example.constant.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.List;

public class ListProjects implements RouteHandler {
    
    private final DynamoDbTable<Project> projectTable; 
    private final ObjectMapper mapper;

    public ListProjects(DynamoDbTable<Project> projectTable, ObjectMapper mapper) { 
        this.projectTable = projectTable; 
        this.mapper = mapper; 
    }

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent request) {
        try {
            Map<String, String> queryParams = request.getQueryStringParameters();
            Map<String, String> pathParams = request.getPathParameters();
            
            String domainIdentifier = (pathParams != null) ? pathParams.get("domainIdentifier") : null;
            if (domainIdentifier == null) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("Missing domainIdentifier.");
            }

            String nextToken = (queryParams != null) ? queryParams.get("nextToken") : null;
            int maxNumber = 10;
            if (queryParams != null && queryParams.containsKey("maxResults")) {
                try { 
                    maxNumber = Integer.parseInt(queryParams.get("maxResults")); 
                } catch (NumberFormatException ignored) {}
            }

            QueryEnhancedRequest.Builder queryBuilder = QueryEnhancedRequest.builder()
                .queryConditional(software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional.keyEqualTo(
                    Key.builder().partitionValue(domainIdentifier).build()
                ))
                .limit(maxNumber);

            if (nextToken != null) {
                Map<String, AttributeValue> key = PaginationUtils.decodeToken(nextToken);
                if (key != null && key.containsKey("TOKEN_VALUE")) {
                    queryBuilder.exclusiveStartKey(Map.of(
                        "domainIdentifier", AttributeValue.builder().s(domainIdentifier).build(),
                        "id", key.get("TOKEN_VALUE")
                    ));
                }
            }

            var pageIterator = projectTable.index(Constants.GSI_BY_DOMAIN).query(queryBuilder.build()).iterator();
            List<Project> items = java.util.Collections.emptyList();
            String newNextToken = null;

            if (pageIterator.hasNext()) {
                var page = pageIterator.next();
                items = page.items();
                newNextToken = PaginationUtils.encodeToken(page.lastEvaluatedKey());
            }

            PaginatedResult<Project> result = new PaginatedResult<>(items, newNextToken);
            
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(mapper.writeValueAsString(result));
        } catch (Exception e) { 
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("Error listing projects: " + e.getMessage()); 
        }
    }
}
