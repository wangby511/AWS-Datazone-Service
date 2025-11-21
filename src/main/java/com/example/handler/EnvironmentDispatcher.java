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

    private final Map<String, Map<String, RouteHandler>> handlers = new HashMap<>();

    public EnvironmentDispatcher() {
        DynamoDbClient ddb = DynamoDbClient.builder().build();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(ddb).build();
        ObjectMapper objectMapper = new ObjectMapper();
        
        String domainTableName = System.getenv("DOMAIN_TABLE_NAME");
        DynamoDbTable<Domain> domainTable = enhancedClient.table(domainTableName, TableSchema.fromBean(Domain.class));
        String projTableName = System.getenv("PROJ_TABLE_NAME");
        DynamoDbTable<Project> projTable = enhancedClient.table(projTableName, TableSchema.fromBean(Project.class));
        
        registerHandlers(domainTable, projTable, objectMapper);
    }
    
    public EnvironmentDispatcher(DynamoDbTable<Domain> domainTable, DynamoDbTable<Project> projTable, ObjectMapper objectMapper) {
        registerHandlers(domainTable, projTable, objectMapper);
    }
    
    private void registerHandlers(DynamoDbTable<Domain> domainTable, DynamoDbTable<Project> projTable, ObjectMapper objectMapper) {
        var register = (String uri, String method, RouteHandler handler) -> 
            handlers.computeIfAbsent(uri, k -> new HashMap<>()).put(method, handler);

        register.accept("/domains", "POST", new CreateDomain(domainTable, objectMapper));
        register.accept("/domains", "GET", new ListDomains(domainTable, objectMapper));
        register.accept("/domains/{identifier}", "GET", new GetDomain(domainTable, objectMapper));
        register.accept("/domains/{identifier}", "DELETE", new DeleteDomain(domainTable, objectMapper));
        
        register.accept("/domains/{domainIdentifier}/projects", "POST", new CreateProject(projTable, objectMapper));
        register.accept("/domains/{domainIdentifier}/projects", "GET", new ListProjects(projTable, objectMapper));
        register.accept("/domains/{domainIdentifier}/projects/{identifier}", "GET", new GetProject(projTable, objectMapper));
        register.accept("/domains/{domainIdentifier}/projects/{identifier}", "DELETE", new DeleteProject(projTable, objectMapper));
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String resourcePath = request.getResource();
        String httpMethod = request.getHttpMethod();
        Map<String, RouteHandler> methodMap = handlers.get(resourcePath);
        
        if (methodMap != null) {
            RouteHandler handler = methodMap.get(httpMethod);
            if (handler != null) return handler.handle(request);
            return createError(405, "Method Not Allowed: " + httpMethod);
        }
        return createError(404, "Resource Not Found: " + resourcePath);
    }
    
    private APIGatewayProxyResponseEvent createError(int statusCode, String message) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody(String.format("{\"message\": \"%s\"}", message));
    }
}
