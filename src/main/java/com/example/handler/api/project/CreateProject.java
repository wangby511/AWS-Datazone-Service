package com.example.handler.api.project;

import com.example.handler.api.RouteHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.model.Project;
import com.example.utils.IdUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import java.util.Map;

public class CreateProject implements RouteHandler {
    private final DynamoDbTable<Project> table;
    private final ObjectMapper objectMapper;

    public CreateProject(DynamoDbTable<Project> table, ObjectMapper objectMapper) {
        this.table = table;
        this.objectMapper = objectMapper;
    }

    static class CreateProjectRequest {
        public String name;
        public String description; // Optional parameter
    }

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent request) {
        try {
            Map<String, String> pathParams = request.getPathParameters();
            // Path parameter name {domainIdentifier}
            String domainIdentifier = (pathParams != null) ? pathParams.get("domainIdentifier") : null;
            if (domainIdentifier == null) return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Missing domainIdentifier.");

            CreateProjectRequest reqBody = objectMapper.readValue(request.getBody(), CreateProjectRequest.class);
            
            if (reqBody.name == null || reqBody.name.isEmpty()) return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Missing project name.");

            // 随机生成 Project ID
            String projectId = IdUtils.generateProjectId();

            Project project = new Project();
            project.setId(projectId);
            project.setDomainIdentifier(domainIdentifier);
            project.setName(reqBody.name);
            project.setDescription(reqBody.description); // Set description
            
            table.putItem(project);
            return new APIGatewayProxyResponseEvent().withStatusCode(201).withBody(objectMapper.writeValueAsString(project));
        } catch (Exception e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Error creating project: " + e.getMessage());
        }
    }
}
