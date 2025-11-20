package com.example.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import com.example.handler.api.RouteHandler;
import com.example.handler.api.domain.*;
import com.example.handler.api.project.*;

import com.example.model.Domain;
import com.example.model.Project;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentDispatcher implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Map<String, RouteHandler> handlers = new HashMap<>();

    public EnvironmentDispatcher() {
        DynamoDbClient ddb = DynamoDbClient.builder().build();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(ddb).build();
        ObjectMapper objectMapper = new ObjectMapper();

        String domainTableName = System.getenv("DOMAIN_TABLE_NAME");
        DynamoDbTable<Domain> domainTable = enhancedClient.table(domainTableName, TableSchema.fromBean(Domain.class));

        String projTableName = System.getenv("PROJ_TABLE_NAME");
        DynamoDbTable<Project> projTable = enhancedClient.table(projTableName, TableSchema.fromBean(Project.class));

        // Domain APIs (Use {domainId} path name)
        handlers.put("/domains/createDomain", new CreateDomain(domainTable, objectMapper));
        handlers.put("/domains/listDomains", new ListDomains(domainTable, objectMapper));
        handlers.put("/domains/getDomain/{domainId}", new GetDomain(domainTable, objectMapper));
        handlers.put("/domains/deleteDomain/{domainId}", new DeleteDomain(domainTable, objectMapper));
        
        // Project APIs (Nested under {domainIdentifier} and use {projectId})
        handlers.put("/domains/{domainIdentifier}/projects/createProject", new CreateProject(projTable, objectMapper));
        handlers.put("/domains/{domainIdentifier}/projects/listProjects", new ListProjects(projTable, objectMapper));
        handlers.put("/domains/{domainIdentifier}/projects/getProject/{projectId}", new GetProject(projTable, objectMapper)); 
        handlers.put("/domains/{domainIdentifier}/projects/deleteProject/{projectId}", new DeleteProject(projTable, objectMapper));
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String resourcePath = request.getResource();
        RouteHandler handler = handlers.get(resourcePath);
        if (handler != null) {
            return handler.handle(request);
        } else {
            // 设置默认的 CORS 响应头，防止 API Gateway 404/403 错误
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Access-Control-Allow-Origin", "*");
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(404)
                    .withHeaders(headers)
                    .withBody("{\"message\": \"Handler not found: " + resourcePath + "\"}");
        }
    }
}
