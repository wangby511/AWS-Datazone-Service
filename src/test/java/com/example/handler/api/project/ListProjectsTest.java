package com.example.handler.api.project;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.model.Project;
import com.example.model.PaginatedResult;
import com.example.constant.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;
import java.util.Map;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListProjectsTest {

    @Mock
    private DynamoDbTable<Project> mockTable;
    @Mock
    private DynamoDbIndex<Project> mockIndex;
    @Mock
    private ObjectMapper mockObjectMapper;
    @Mock
    private APIGatewayProxyRequestEvent mockRequest;
    
    private ListProjects listProjectsHandler;
    private static final String TEST_DOMAIN_ID = "dzd-_domain123";

    @BeforeEach
    void setUp() {
        listProjectsHandler = new ListProjects(mockTable, mockObjectMapper);
        when(mockTable.index(eq(Constants.GSI_BY_DOMAIN))).thenReturn(mockIndex);
    }

    @Test
    void handle_Success_ShouldQueryGSIAndReturn200() throws Exception {
        Project project = new Project();
        List<Project> items = List.of(project);
        
        Page<Project> mockPage = Page.builder(Project.class)
                .items(items)
                .lastEvaluatedKey(null)
                .build();
        Iterator<Page<Project>> mockIterator = List.of(mockPage).iterator();

        when(mockRequest.getPathParameters()).thenReturn(Map.of("domainIdentifier", TEST_DOMAIN_ID));
        when(mockRequest.getQueryStringParameters()).thenReturn(null);
        when(mockIndex.query(any(QueryEnhancedRequest.class))).thenReturn(() -> mockIterator);
        when(mockObjectMapper.writeValueAsString(any(PaginatedResult.class))).thenReturn("{}");

        APIGatewayProxyResponseEvent response = listProjectsHandler.handle(mockRequest);

        assertEquals(200, response.getStatusCode());
        
        verify(mockIndex).query(argThat(req -> 
            ((QueryConditional)req.queryConditional()).key().partitionValue().s().equals(TEST_DOMAIN_ID)
        ));
    }

    @Test
    void handle_MissingDomainIdentifier_ShouldReturn400() {
        when(mockRequest.getPathParameters()).thenReturn(Map.of());

        APIGatewayProxyResponseEvent response = listProjectsHandler.handle(mockRequest);

        assertEquals(400, response.getStatusCode());
        assertEquals("Missing domainIdentifier.", response.getBody());
        verify(mockTable, never()).index(any());
    }
}
