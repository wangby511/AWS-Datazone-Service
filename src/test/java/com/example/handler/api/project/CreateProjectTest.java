package com.example.handler.api.project;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.handler.api.project.CreateProject.CreateProjectRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProjectTest {

    @Mock
    private DynamoDbTable<Project> mockTable;
    @Mock
    private ObjectMapper mockObjectMapper;
    @Mock
    private APIGatewayProxyRequestEvent mockRequest;
    
    private CreateProject createProjectHandler;

    private static final String TEST_DOMAIN_ID = "dzd-_domain123";
    private static final String DUMMY_BODY = "{\"name\":\"NewProject\",\"description\":\"A shiny new project\"}";

    @BeforeEach
    void setUp() {
        createProjectHandler = new CreateProject(mockTable, mockObjectMapper);
    }

    @Test
    void handle_Success_ShouldReturn201() throws Exception {
        CreateProjectRequest reqBody = new CreateProjectRequest();
        reqBody.name = "NewProject";
        reqBody.description = "A shiny new project";
        
        when(mockRequest.getPathParameters()).thenReturn(Map.of("domainIdentifier", TEST_DOMAIN_ID));
        when(mockRequest.getBody()).thenReturn(DUMMY_BODY);
        when(mockObjectMapper.readValue(eq(DUMMY_BODY), eq(CreateProjectRequest.class))).thenReturn(reqBody);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        doNothing().when(mockTable).putItem(projectCaptor.capture());
        
        when(mockObjectMapper.writeValueAsString(any(Project.class))).thenReturn("{}"); 

        APIGatewayProxyResponseEvent response = createProjectHandler.handle(mockRequest);

        assertEquals(201, response.getStatusCode());
        
        Project capturedProject = projectCaptor.getValue();
        assertEquals("NewProject", capturedProject.getName());
        assertEquals("A shiny new project", capturedProject.getDescription());
        assertEquals(TEST_DOMAIN_ID, capturedProject.getDomainIdentifier());
        assertEquals(36, capturedProject.getId().length()); 
    }

    @Test
    void handle_MissingDomainIdentifier_ShouldReturn400() {
        when(mockRequest.getPathParameters()).thenReturn(Map.of());

        APIGatewayProxyResponseEvent response = createProjectHandler.handle(mockRequest);

        assertEquals(400, response.getStatusCode());
        assertEquals("Missing domainIdentifier.", response.getBody());
        verify(mockTable, never()).putItem(any());
    }
}
