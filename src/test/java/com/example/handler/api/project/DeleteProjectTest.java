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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteProjectTest {

    @Mock
    private DynamoDbTable<Project> mockTable;
    private ObjectMapper objectMapper = new ObjectMapper();
    private DeleteProject handler;
    
    private static final String VALID_DOMAIN_ID = "dzd-123456789012345678901234567890123456";
    private static final String VALID_PROJECT_ID = "proj-123456789012345678901234567890123456";

    @BeforeEach
    void setUp() {
        handler = new DeleteProject(mockTable, objectMapper);
    }

    @Test
    void testSuccessfulDeletion() {
        Project mockProject = new Project();
        mockProject.setId(VALID_PROJECT_ID);
        mockProject.setDomainIdentifier(VALID_DOMAIN_ID);

        // First get to verify ownership
        when(mockTable.getItem(any(Key.class))).thenReturn(mockProject);

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("domainIdentifier", VALID_DOMAIN_ID, "identifier", VALID_PROJECT_ID));

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(204, response.getStatusCode());
        verify(mockTable, times(1)).deleteItem(any(Key.class));
    }

    @Test
    void testProjectNotFoundReturns404() {
        // Get returns null
        when(mockTable.getItem(any(Key.class))).thenReturn(null);

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("domainIdentifier", VALID_DOMAIN_ID, "identifier", VALID_PROJECT_ID));

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(404, response.getStatusCode());
        assertEquals("Project not found in this domain.", response.getBody());
        verify(mockTable, never()).deleteItem(any(Key.class));
    }

    @Test
    void testDomainMismatchReturns404() {
        Project mockProject = new Project();
        mockProject.setId(VALID_PROJECT_ID);
        mockProject.setDomainIdentifier("OTHER_DOMAIN");

        when(mockTable.getItem(any(Key.class))).thenReturn(mockProject);

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("domainIdentifier", VALID_DOMAIN_ID, "identifier", VALID_PROJECT_ID));

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(404, response.getStatusCode());
        assertEquals("Project not found in this domain.", response.getBody());
        verify(mockTable, never()).deleteItem(any(Key.class));
    }
    
    @Test
    void testInvalidIdFormatReturns400() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("domainIdentifier", VALID_DOMAIN_ID, "identifier", "bad-id"));

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(400, response.getStatusCode());
        assertEquals("Invalid project identifier format.", response.getBody());
    }
}
