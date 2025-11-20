package com.example.handler.api.project;

import com.example.handler.api.RouteHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.model.Project;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import java.util.Map;

public class DeleteProject implements RouteHandler {
    private final DynamoDbTable<Project> table;
    private final ObjectMapper objectMapper;

    public DeleteProject(DynamoDbTable<Project> table, ObjectMapper objectMapper) {
        this.table = table;
        this.objectMapper = objectMapper;
    }

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent request) {
        try {
            Map<String, String> pathParams = request.getPathParameters();
            String domainIdentifier = (pathParams != null) ? pathParams.get("domainIdentifier") : null;
            // Path parameter name {projectId}
            String projectId = (pathParams != null) ? pathParams.get("projectId") : null;
            
            if (domainIdentifier == null || projectId == null) {
                return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Missing ID parameters.");
            }
            
            // 1. Get to verify Domain association before delete
            Project project = table.getItem(r -> r.key(Key.builder().partitionValue(projectId).build()));

            if (project == null || !domainIdentifier.equals(project.getDomainIdentifier())) {
                 return new APIGatewayProxyResponseEvent().withStatusCode(404).withBody("Project not found in this domain.");
            }

            // 2. Perform Delete
            table.deleteItem(project);

            return new APIGatewayProxyResponseEvent().withStatusCode(204).withBody("");
        } catch (Exception e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Error deleting project: " + e.getMessage());
        }
    }
}
