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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteProjectTest {

    @Mock
    private DynamoDbTable<Project> mockTable;
    @Mock
    private ObjectMapper mockObjectMapper;
    @Mock
    private APIGatewayProxyRequestEvent mockRequest;
    
    private DeleteProject deleteProjectHandler;
    private static final String TEST_DOMAIN_ID = "dzd-_domain123";
    private static final String TEST_PROJECT_ID = "proj-abc-123";

    @BeforeEach
    void setUp() {
        deleteProjectHandler = new DeleteProject(mockTable, mockObjectMapper);
    }

    @Test
    void handle_Success_ShouldReturn204() {
        Project projectToDelete = new Project();
        projectToDelete.setId(TEST_PROJECT_ID);
        projectToDelete.setDomainIdentifier(TEST_DOMAIN_ID);

        when(mockRequest.getPathParameters()).thenReturn(Map.of(
            "domainIdentifier", TEST_DOMAIN_ID, 
            "projectId", TEST_PROJECT_ID
        ));
        
        when(mockTable.getItem(any(GetItemEnhancedRequest.class))).thenReturn(projectToDelete); 
        when(mockTable.deleteItem(any(Project.class))).thenReturn(projectToDelete); 

        APIGatewayProxyResponseEvent response = deleteProjectHandler.handle(mockRequest);

        assertEquals(204, response.getStatusCode());
        assertEquals("", response.getBody());
        
        verify(mockTable).getItem(argThat(req -> req.key().partitionKeyValue().s().equals(TEST_PROJECT_ID))); 
        verify(mockTable).deleteItem(eq(projectToDelete));
    }

    @Test
    void handle_ProjectInWrongDomain_ShouldReturn404() {
        Project projectInWrongDomain = new Project();
        projectInWrongDomain.setDomainIdentifier("WRONG-DOMAIN");
        
        when(mockRequest.getPathParameters()).thenReturn(Map.of(
            "domainIdentifier", TEST_DOMAIN_ID, 
            "projectId", TEST_PROJECT_ID
        ));
        when(mockTable.getItem(any(GetItemEnhancedRequest.class))).thenReturn(projectInWrongDomain);

        APIGatewayProxyResponseEvent response = deleteProjectHandler.handle(mockRequest);

        assertEquals(404, response.getStatusCode());
        assertEquals("Project not found in this domain.", response.getBody());
        verify(mockTable, never()).deleteItem(any(Project.class));
    }
}
