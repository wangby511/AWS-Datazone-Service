package com.example.handler.api.project;

import com.example.handler.api.RouteHandler;
import com.example.model.Project;
import com.example.dto.CreateProjectRequest;
import com.example.utils.IdUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import java.util.Map;

public class CreateProject implements RouteHandler {
    
    private final DynamoDbTable<Project> projectTable; 
    private final ObjectMapper mapper;

    public CreateProject(DynamoDbTable<Project> projectTable, ObjectMapper mapper) { 
        this.projectTable = projectTable; 
        this.mapper = mapper; 
    }

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent request) {
        try {
            Map<String, String> pathParams = request.getPathParameters();
            String domainIdentifier = (pathParams != null) ? pathParams.get("domainIdentifier") : null;
            if (domainIdentifier == null) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("Missing domainIdentifier.");
            }
            
            CreateProjectRequest reqBody = mapper.readValue(request.getBody(), CreateProjectRequest.class);
            if (reqBody.getName() == null || reqBody.getName().isEmpty()) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("Missing project name.");
            }

            Project project = Project.builder()
                .id(IdUtils.generateProjectId())
                .domainIdentifier(domainIdentifier)
                .name(reqBody.getName())
                .description(reqBody.getDescription())
                .build();
            
            projectTable.putItem(project);
            
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(201)
                    .withBody(mapper.writeValueAsString(project));
        } catch (Exception e) { 
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("Error creating project: " + e.getMessage()); 
        }
    }
}
