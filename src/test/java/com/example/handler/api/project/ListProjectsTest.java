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
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListProjectsTest {

    @Mock
    private DynamoDbTable<Project> mockTable;
    @Mock
    private DynamoDbIndex<Project> mockIndex;
    private ObjectMapper objectMapper = new ObjectMapper();
    private ListProjects handler;
    
    private static final String VALID_DOMAIN_ID = "dzd-123";

    @BeforeEach
    void setUp() {
        handler = new ListProjects(mockTable, objectMapper);
        when(mockTable.index(any(String.class))).thenReturn(mockIndex);
    }

    @Test
    void testSuccessfulList() {
        PageIterable<Project> mockPageIterable = mock(PageIterable.class);
        Iterator<Page<Project>> mockIterator = mock(Iterator.class);
        Page<Project> mockPage = Page.create(Collections.emptyList());

        when(mockIndex.query(any(QueryEnhancedRequest.class))).thenReturn(mockPageIterable);
        when(mockPageIterable.iterator()).thenReturn(mockIterator);
        when(mockIterator.next()).thenReturn(mockPage);

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("domainIdentifier", VALID_DOMAIN_ID));

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testMissingDomainIdReturns400() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(400, response.getStatusCode());
        assertEquals("Missing domainIdentifier.", response.getBody());
    }
}
