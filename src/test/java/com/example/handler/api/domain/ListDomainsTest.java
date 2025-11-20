package com.example.handler.api.domain;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.model.Domain;
import com.example.model.PaginatedResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListDomainsTest {

    @Mock
    private DynamoDbTable<Domain> mockTable;
    @Mock
    private ObjectMapper mockObjectMapper;
    @Mock
    private APIGatewayProxyRequestEvent mockRequest;
    
    private ListDomains listDomainsHandler;

    @BeforeEach
    void setUp() {
        listDomainsHandler = new ListDomains(mockTable, mockObjectMapper);
    }

    @Test
    void handle_SuccessWithoutPagination_ShouldReturn200() throws Exception {
        Domain domain = new Domain();
        List<Domain> items = List.of(domain);
        Page<Domain> mockPage = Page.builder(Domain.class)
                .items(items)
                .lastEvaluatedKey(null)
                .build();
        Iterator<Page<Domain>> mockIterator = List.of(mockPage).iterator();

        when(mockRequest.getQueryStringParameters()).thenReturn(null);
        when(mockTable.scan(any(ScanEnhancedRequest.class))).thenReturn(() -> mockIterator);
        when(mockObjectMapper.writeValueAsString(any(PaginatedResult.class))).thenReturn("{\"items\":[...],\"nextToken\":null}");

        APIGatewayProxyResponseEvent response = listDomainsHandler.handle(mockRequest);

        assertEquals(200, response.getStatusCode());
        verify(mockTable).scan(argThat(req -> req.limit().equals(10)));
    }
    
    @Test
    void handle_SuccessWithPaginationToken_ShouldReturn200() throws Exception {
        Domain domain = new Domain();
        List<Domain> items = List.of(domain);
        
        // Note: Real token decoding is complex, here we verify the logic flow.
        String mockToken = "ZGlkOnRlc3QxMjM="; 
        
        Page<Domain> mockPage = Page.builder(Domain.class)
                .items(items)
                .lastEvaluatedKey(Map.of("identifier", AttributeValue.builder().s("lastId").build()))
                .build();
        Iterator<Page<Domain>> mockIterator = List.of(mockPage).iterator();

        when(mockRequest.getQueryStringParameters()).thenReturn(Map.of("next_token", mockToken, "max_number", "5"));
        when(mockTable.scan(any(ScanEnhancedRequest.class))).thenReturn(() -> mockIterator);
        when(mockObjectMapper.writeValueAsString(any(PaginatedResult.class))).thenReturn("{\"items\":[...],\"nextToken\":\"...\"}");

        APIGatewayProxyResponseEvent response = listDomainsHandler.handle(mockRequest);

        assertEquals(200, response.getStatusCode());
        verify(mockTable).scan(argThat(req -> req.limit().equals(5)));
    }
}
