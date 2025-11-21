package com.example.handler.api.domain;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.model.Domain;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.Collections;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListDomainsTest {

    @Mock
    private DynamoDbTable<Domain> mockTable;
    private ObjectMapper objectMapper = new ObjectMapper();
    private ListDomains handler;

    @BeforeEach
    void setUp() {
        handler = new ListDomains(mockTable, objectMapper);
    }

    @Test
    void testSuccessfulList() {
        PageIterable<Domain> mockPageIterable = mock(PageIterable.class);
        Iterator<Page<Domain>> mockIterator = mock(Iterator.class);
        Page<Domain> mockPage = Page.create(Collections.singletonList(new Domain()));

        when(mockTable.scan(any(ScanEnhancedRequest.class))).thenReturn(mockPageIterable);
        when(mockPageIterable.iterator()).thenReturn(mockIterator);
        when(mockIterator.next()).thenReturn(mockPage);

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        
        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testListWithParams() {
        PageIterable<Domain> mockPageIterable = mock(PageIterable.class);
        Iterator<Page<Domain>> mockIterator = mock(Iterator.class);
        Page<Domain> mockPage = Page.create(Collections.emptyList());

        when(mockTable.scan(any(ScanEnhancedRequest.class))).thenReturn(mockPageIterable);
        when(mockPageIterable.iterator()).thenReturn(mockIterator);
        when(mockIterator.next()).thenReturn(mockPage);

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withQueryStringParameters(java.util.Map.of("maxResults", "5"));

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(200, response.getStatusCode());
    }
}
