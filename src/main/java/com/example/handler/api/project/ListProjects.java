package com.example.handler.api.project;

import com.example.handler.api.RouteHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.model.Project;
import com.example.model.PaginatedResult;
import com.example.constant.Constants;
import com.example.utils.PaginationUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ListProjects implements RouteHandler {
    private final DynamoDbTable<Project> table;
    private final ObjectMapper objectMapper;

    public ListProjects(DynamoDbTable<Project> table, ObjectMapper objectMapper) {
        this.table = table;
        this.objectMapper = objectMapper;
    }

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent request) {
        try {
            Map<String, String> pathParams = request.getPathParameters();
            Map<String, String> queryParams = request.getQueryStringParameters();
            
            // Path parameter name {domainIdentifier}
            String domainIdentifier = (pathParams != null) ? pathParams.get("domainIdentifier") : null;
            String nextToken = (queryParams != null) ? queryParams.get("next_token") : null;
            int maxNumber = (queryParams != null && queryParams.containsKey("max_number")) 
                            ? Integer.parseInt(queryParams.get("max_number")) : 10;

            if (domainIdentifier == null) return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Missing domainIdentifier.");

            DynamoDbIndex<Project> index = table.index(Constants.GSI_BY_DOMAIN);
            
            QueryEnhancedRequest.Builder queryBuilder = QueryEnhancedRequest.builder()
                    .queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(domainIdentifier)))
                    .limit(maxNumber);

            if (nextToken != null) {
                queryBuilder.exclusiveStartKey(PaginationUtils.decodeToken(nextToken));
            }

            Iterator<Page<Project>> pageIterator = index.query(queryBuilder.build()).iterator();
            List<Project> items = Collections.emptyList();
            String newNextToken = null;

            if (pageIterator.hasNext()) {
                Page<Project> page = pageIterator.next();
                items = page.items();
                Map<String, AttributeValue> lastKey = page.lastEvaluatedKey();
                newNextToken = PaginationUtils.encodeToken(lastKey);
            }

            PaginatedResult<Project> result = new PaginatedResult<>(items, newNextToken);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(objectMapper.writeValueAsString(result));

        } catch (Exception e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Error listing projects: " + e.getMessage());
        }
    }
}
