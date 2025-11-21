package com.example.handler.api.project;

import com.example.handler.api.RouteHandler;
import com.example.model.Project;
import com.example.constant.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import java.util.Map;

public class DeleteProject implements RouteHandler {
    
    private final DynamoDbTable<Project> projectTable; 
    private final ObjectMapper mapper;

    public DeleteProject(DynamoDbTable<Project> projectTable, ObjectMapper mapper) { 
        this.projectTable = projectTable; 
        this.mapper = mapper; 
    }

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent request) {
        try {
            Map<String, String> pathParams = request.getPathParameters();
            String domainIdentifier = (pathParams != null) ? pathParams.get("domainIdentifier") : null;
            String identifier = (pathParams != null) ? pathParams.get("identifier") : null;
            
            if (domainIdentifier == null || identifier == null) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("Missing ID parameters.");
            }
            if (!identifier.matches(Constants.PROJECT_ID_REGEX)) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("Invalid project identifier format.");
            }

            Project project = projectTable.getItem(Key.builder().partitionValue(identifier).build());
            
            if (project == null || !project.getDomainIdentifier().equals(domainIdentifier)) {
                 return new APIGatewayProxyResponseEvent()
                        .withStatusCode(404)
                        .withBody("Project not found in this domain.");
            }
            
            projectTable.deleteItem(Key.builder().partitionValue(identifier).build());
            
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(204)
                    .withBody("");
        } catch (Exception e) { 
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("Error deleting project: " + e.getMessage()); 
        }
    }
}
