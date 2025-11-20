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
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetDomainTest {

    @Mock
    private DynamoDbTable<Domain> mockTable;
    @Mock
    private ObjectMapper mockObjectMapper;
    @Mock
    private APIGatewayProxyRequestEvent mockRequest;
    
    private GetDomain getDomainHandler;
    private static final String TEST_DOMAIN_ID = "dzd-_testdomainid";

    @BeforeEach
    void setUp() {
        getDomainHandler = new GetDomain(mockTable, mockObjectMapper);
    }

    @Test
    void handle_Success_ShouldReturn200() throws Exception {
        Domain domain = new Domain();
        domain.setIdentifier(TEST_DOMAIN_ID);
        domain.setName("TestDomain");

        when(mockRequest.getPathParameters()).thenReturn(Map.of("domainId", TEST_DOMAIN_ID));
        when(mockTable.getItem(any(GetItemEnhancedRequest.class))).thenReturn(domain);
        when(mockObjectMapper.writeValueAsString(domain)).thenReturn("{\"identifier\":\"" + TEST_DOMAIN_ID + "\"}");

        APIGatewayProxyResponseEvent response = getDomainHandler.handle(mockRequest);

        assertEquals(200, response.getStatusCode());
        verify(mockTable).getItem(argThat(req -> req.key().partitionKeyValue().s().equals(TEST_DOMAIN_ID)));
    }

    @Test
    void handle_DomainNotFound_ShouldReturn404() {
        when(mockRequest.getPathParameters()).thenReturn(Map.of("domainId", TEST_DOMAIN_ID));
        when(mockTable.getItem(any(GetItemEnhancedRequest.class))).thenReturn(null);

        APIGatewayProxyResponseEvent response = getDomainHandler.handle(mockRequest);

        assertEquals(404, response.getStatusCode());
        assertEquals("Domain not found.", response.getBody());
    }

    @Test
    void handle_MissingPathParameter_ShouldReturn400() {
        when(mockRequest.getPathParameters()).thenReturn(Collections.emptyMap());

        APIGatewayProxyResponseEvent response = getDomainHandler.handle(mockRequest);

        assertEquals(400, response.getStatusCode());
        assertEquals("Missing domainId.", response.getBody());
        verify(mockTable, never()).getItem(any(Key.class));
    }
}
