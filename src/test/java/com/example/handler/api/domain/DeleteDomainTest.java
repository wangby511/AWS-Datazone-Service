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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteDomainTest {

    @Mock
    private DynamoDbTable<Domain> mockTable;
    private ObjectMapper objectMapper = new ObjectMapper();
    private DeleteDomain handler;
    
    private static final String VALID_DOMAIN_ID = "dzd-123456789012345678901234567890123456";

    @BeforeEach
    void setUp() {
        handler = new DeleteDomain(mockTable, objectMapper);
    }

    @Test
    void testSuccessfulDeletion() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("identifier", VALID_DOMAIN_ID));

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(204, response.getStatusCode());
        verify(mockTable, times(1)).deleteItem(any(Key.class));
    }

    @Test
    void testInvalidIdentifierReturns400() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("identifier", "bad-id"));

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(400, response.getStatusCode());
        assertEquals("Invalid identifier format.", response.getBody());
        verify(mockTable, never()).deleteItem(any(Key.class));
    }

    @Test
    void testMissingIdentifierReturns400() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withPathParameters(null);

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(400, response.getStatusCode());
        assertEquals("Missing identifier.", response.getBody());
    }
}
