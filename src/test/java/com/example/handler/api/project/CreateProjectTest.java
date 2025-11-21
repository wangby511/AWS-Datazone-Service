package com.example.handler.api.project;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.model.Project;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProjectTest {

    @Mock
    private DynamoDbTable<Project> mockTable;
    private ObjectMapper objectMapper = new ObjectMapper();
    private CreateProject handler;
    
    private static final String VALID_DOMAIN_ID = "dzd-123456789012345678901234567890123456";

    @BeforeEach
    void setUp() {
        handler = new CreateProject(mockTable, objectMapper);
    }

    @Test
    void testSuccessfulCreation() {
        String requestBody = "{\"name\": \"Test Project\", \"description\": \"Project Desc\"}";
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("domainIdentifier", VALID_DOMAIN_ID))
                .withBody(requestBody);

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(201, response.getStatusCode());
        
        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(mockTable).putItem(captor.capture());
        
        Project savedProject = captor.getValue();
        assertEquals(VALID_DOMAIN_ID, savedProject.getDomainIdentifier());
        assertEquals("Test Project", savedProject.getName());
        assertNotNull(savedProject.getId());
    }

    @Test
    void testMissingDomainIdReturns400() {
        String requestBody = "{\"name\": \"Test Project\"}";
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withBody(requestBody); // No path params

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(400, response.getStatusCode());
        assertEquals("Missing domainIdentifier.", response.getBody());
    }

    @Test
    void testMissingNameReturns400() {
        String requestBody = "{\"description\": \"Only description\"}";
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("domainIdentifier", VALID_DOMAIN_ID))
                .withBody(requestBody);

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(400, response.getStatusCode());
        assertEquals("Missing project name.", response.getBody());
    }
}
