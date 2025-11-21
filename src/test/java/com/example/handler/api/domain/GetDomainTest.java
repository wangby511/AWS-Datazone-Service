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

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetDomainTest {

    @Mock
    private DynamoDbTable<Domain> mockTable;
    private ObjectMapper objectMapper = new ObjectMapper();
    private GetDomain handler;
    
    private static final String VALID_DOMAIN_ID = "dzd-123456789012345678901234567890123456";

    @BeforeEach
    void setUp() {
        handler = new GetDomain(mockTable, objectMapper);
    }

    @Test
    void testSuccessfulRetrieval() throws Exception {
        Domain domain = new Domain();
        domain.setIdentifier(VALID_DOMAIN_ID);
        domain.setName("Test Domain");

        when(mockTable.getItem(any(Key.class))).thenReturn(domain);

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("identifier", VALID_DOMAIN_ID));

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains(VALID_DOMAIN_ID));
    }

    @Test
    void testMissingIdentifierReturns400() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withPathParameters(Collections.emptyMap());

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(400, response.getStatusCode());
        assertEquals("Missing identifier.", response.getBody());
    }

    @Test
    void testInvalidIdentifierFormatReturns400() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("identifier", "invalid-format-id"));

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(400, response.getStatusCode());
        assertEquals("Invalid identifier format.", response.getBody());
    }

    @Test
    void testDomainNotFoundReturns404() {
        when(mockTable.getItem(any(Key.class))).thenReturn(null);

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("identifier", VALID_DOMAIN_ID));

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(404, response.getStatusCode());
        assertEquals("Domain not found.", response.getBody());
    }
}
