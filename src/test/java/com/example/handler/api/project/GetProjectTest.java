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
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetProjectTest {

    @Mock
    private DynamoDbTable<Project> mockTable;
    @Mock
    private ObjectMapper mockObjectMapper;
    @Mock
    private APIGatewayProxyRequestEvent mockRequest;
    
    private GetProject getProjectHandler;
    private static final String TEST_DOMAIN_ID = "dzd-_domain123";
    private static final String TEST_PROJECT_ID = "proj-abc-123";

    @BeforeEach
    void setUp() {
        getProjectHandler = new GetProject(mockTable, mockObjectMapper);
    }

    @Test
    void handle_Success_ShouldReturn200() throws Exception {
        Project project = new Project();
        project.setId(TEST_PROJECT_ID);
        project.setDomainIdentifier(TEST_DOMAIN_ID);

        when(mockRequest.getPathParameters()).thenReturn(Map.of(
            "domainIdentifier", TEST_DOMAIN_ID, 
            "projectId", TEST_PROJECT_ID
        ));
        when(mockTable.getItem(any(GetItemEnhancedRequest.class))).thenReturn(project);
        when(mockObjectMapper.writeValueAsString(project)).thenReturn("{\"id\":\"" + TEST_PROJECT_ID + "\"}");

        APIGatewayProxyResponseEvent response = getProjectHandler.handle(mockRequest);

        assertEquals(200, response.getStatusCode());
        verify(mockTable).getItem(argThat(req -> req.key().partitionKeyValue().s().equals(TEST_PROJECT_ID))); 
    }

    @Test
    void handle_ProjectNotFound_ShouldReturn404() {
        when(mockRequest.getPathParameters()).thenReturn(Map.of(
            "domainIdentifier", TEST_DOMAIN_ID, 
            "projectId", TEST_PROJECT_ID
        ));
        when(mockTable.getItem(any(GetItemEnhancedRequest.class))).thenReturn(null);

        APIGatewayProxyResponseEvent response = getProjectHandler.handle(mockRequest);

        assertEquals(404, response.getStatusCode());
        assertEquals("Project not found.", response.getBody());
    }
}
