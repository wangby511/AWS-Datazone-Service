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
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteDomainTest {

    @Mock
    private DynamoDbTable<Domain> mockTable;
    @Mock
    private ObjectMapper mockObjectMapper;
    @Mock
    private APIGatewayProxyRequestEvent mockRequest;
    
    private DeleteDomain deleteDomainHandler;
    private static final String TEST_DOMAIN_ID = "dzd-_testdomainid";

    @BeforeEach
    void setUp() {
        deleteDomainHandler = new DeleteDomain(mockTable, mockObjectMapper);
    }

    @Test
    void handle_Success_ShouldReturn204() {
        Domain deletedDomain = new Domain();
        deletedDomain.setIdentifier(TEST_DOMAIN_ID);

        when(mockRequest.getPathParameters()).thenReturn(Map.of("domainId", TEST_DOMAIN_ID));
        when(mockTable.deleteItem(any(DeleteItemEnhancedRequest.class))).thenReturn(deletedDomain);

        APIGatewayProxyResponseEvent response = deleteDomainHandler.handle(mockRequest);

        assertEquals(204, response.getStatusCode());
        assertEquals("", response.getBody());
        verify(mockTable).deleteItem(argThat(req -> req.key().partitionKeyValue().s().equals(TEST_DOMAIN_ID)));
    }

    @Test
    void handle_DomainNotFound_ShouldReturn404() {
        when(mockRequest.getPathParameters()).thenReturn(Map.of("domainId", TEST_DOMAIN_ID));
        when(mockTable.deleteItem(any(DeleteItemEnhancedRequest.class))).thenReturn(null);

        APIGatewayProxyResponseEvent response = deleteDomainHandler.handle(mockRequest);

        assertEquals(404, response.getStatusCode());
        assertEquals("Domain not found or already deleted.", response.getBody());
    }
}
