package com.example.handler.api.project;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.model.Project;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetProjectTest {

    @Mock
    private DynamoDbTable<Project> mockProjectTable;
    
    private ObjectMapper objectMapper = new ObjectMapper();
    private GetProject handler;
    
    private static final String VALID_DOMAIN_ID = "dzd-123456789012345678901234567890123456";
    private static final String VALID_PROJECT_ID = "abcdefghijklmnopqrstuvwxyz1234567890"; // 36 chars

    @BeforeEach
    void setUp() {
        handler = new GetProject(mockProjectTable, objectMapper);
    }

    @Test
    void testSuccessfulRetrieval() throws Exception {
        Project mockProject = new Project();
        mockProject.setId(VALID_PROJECT_ID);
        mockProject.setDomainIdentifier(VALID_DOMAIN_ID);
        mockProject.setName("My Test Project");

        when(mockProjectTable.getItem(any(Key.class))).thenReturn(mockProject);

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("domainIdentifier", VALID_DOMAIN_ID, "identifier", VALID_PROJECT_ID))
                .withResource("/domains/{domainIdentifier}/projects/{identifier}")
                .withHttpMethod("GET");

        APIGatewayProxyResponseEvent response = handler.handle(request);
        Project retrievedProject = objectMapper.readValue(response.getBody(), Project.class);

        assertEquals(200, response.getStatusCode());
        assertEquals(VALID_PROJECT_ID, retrievedProject.getId());
        assertEquals(VALID_DOMAIN_ID, retrievedProject.getDomainIdentifier());
    }

    @Test
    void testMissingIdentifierReturns400() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("domainIdentifier", VALID_DOMAIN_ID)); // Missing 'identifier'

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Missing ID parameters"));
        verify(mockProjectTable, never()).getItem(any(Key.class));
    }

    @Test
    void testInvalidIdentifierFormatReturns400() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("domainIdentifier", VALID_DOMAIN_ID, "identifier", "bad-id"));

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid project identifier format"));
        verify(mockProjectTable, never()).getItem(any(Key.class));
    }

    @Test
    void testProjectNotFoundReturns404() {
        when(mockProjectTable.getItem(any(Key.class))).thenReturn(null);

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("domainIdentifier", VALID_DOMAIN_ID, "identifier", VALID_PROJECT_ID));

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(404, response.getStatusCode());
        assertEquals("Project not found.", response.getBody());
    }

    @Test
    void testDomainMismatchReturns404() {
        Project mockProject = new Project();
        mockProject.setId(VALID_PROJECT_ID);
        mockProject.setDomainIdentifier("OTHER_DOMAIN"); // Mismatch

        when(mockProjectTable.getItem(any(Key.class))).thenReturn(mockProject);

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("domainIdentifier", VALID_DOMAIN_ID, "identifier", VALID_PROJECT_ID));

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("Project not found in this domain"));
    }
}
