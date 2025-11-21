package com.example.handler.api.domain;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.model.Domain;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateDomainTest {

    @Mock
    private DynamoDbTable<Domain> mockDomainTable;
    
    private ObjectMapper objectMapper = new ObjectMapper();
    private CreateDomain handler;

    @BeforeEach
    void setUp() {
        handler = new CreateDomain(mockDomainTable, objectMapper);
    }

    @Test
    void testSuccessfulCreation() throws Exception {
        String requestBody = "{\"name\": \"Test Domain\", \"description\": \"A new test domain\", \"domainExecutionRole\": \"arn:aws:iam::123456789012:role/DataZoneExecutionRole\"}";
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withBody(requestBody)
                .withResource("/domains")
                .withHttpMethod("POST");

        APIGatewayProxyResponseEvent response = handler.handle(request);
        Domain createdDomain = objectMapper.readValue(response.getBody(), Domain.class);

        assertEquals(201, response.getStatusCode());
        assertNotNull(createdDomain.getIdentifier());
        assertEquals("arn:aws:iam::123456789012:role/DataZoneExecutionRole", createdDomain.getDomainExecutionRole());
        verify(mockDomainTable, times(1)).putItem(any(Domain.class));
    }

    @Test
    void testMissingNameReturns400() {
        String requestBody = "{\"description\": \"Missing name\", \"domainExecutionRole\": \"arn:aws:iam::123456789012:role/DataZoneExecutionRole\"}";
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent().withBody(requestBody);

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Missing domain name"));
        verify(mockDomainTable, never()).putItem(any());
    }

    @Test
    void testMissingExecutionRoleReturns400() {
        String requestBody = "{\"name\": \"Test Domain\", \"description\": \"Missing role\"}";
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent().withBody(requestBody);

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Missing domainExecutionRole"));
        verify(mockDomainTable, never()).putItem(any());
    }

    @Test
    void testInvalidExecutionRoleFormatReturns400() {
        String requestBody = "{\"name\": \"Test Domain\", \"domainExecutionRole\": \"invalid-role-arn\"}";
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent().withBody(requestBody);

        APIGatewayProxyResponseEvent response = handler.handle(request);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid domainExecutionRole format"));
        verify(mockDomainTable, never()).putItem(any());
    }
}
