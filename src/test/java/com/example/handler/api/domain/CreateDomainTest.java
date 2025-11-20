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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateDomainTest {

    @Mock
    private DynamoDbTable<Domain> mockTable;
    @Mock
    private ObjectMapper mockObjectMapper;
    @Mock
    private APIGatewayProxyRequestEvent mockRequest;
    
    private CreateDomain createDomainHandler;

    private static final String VALID_ARN = "arn:aws:iam::123456789012:role/DatazoneExecutionRole";
    private static final String DUMMY_BODY = "{\"name\":\"TestDomain\",\"domainExecutionRole\":\"" + VALID_ARN + "\",\"description\":\"A test description\"}";

    @BeforeEach
    void setUp() {
        createDomainHandler = new CreateDomain(mockTable, mockObjectMapper);
    }

    @Test
    void handle_Success_ShouldReturn201() throws Exception {
        Domain inputDomain = new Domain();
        inputDomain.setName("TestDomain");
        inputDomain.setDomainExecutionRole(VALID_ARN);
        inputDomain.setDescription("A test description");
        
        when(mockRequest.getBody()).thenReturn(DUMMY_BODY);
        when(mockObjectMapper.readValue(eq(DUMMY_BODY), eq(Domain.class))).thenReturn(inputDomain);

        ArgumentCaptor<Domain> domainCaptor = ArgumentCaptor.forClass(Domain.class);
        doNothing().when(mockTable).putItem(domainCaptor.capture());
        
        when(mockObjectMapper.writeValueAsString(any(Domain.class))).thenReturn("{}");

        APIGatewayProxyResponseEvent response = createDomainHandler.handle(mockRequest);

        assertEquals(201, response.getStatusCode());
        verify(mockTable, times(1)).putItem(any(Domain.class));
        
        Domain capturedDomain = domainCaptor.getValue();
        assertEquals("TestDomain", capturedDomain.getName());
        // Domain ID is 40 chars: "dzd" (3) + separator (1) + payload (36)
        assertEquals(40, capturedDomain.getIdentifier().length()); 
    }

    @Test
    void handle_MissingName_ShouldReturn400() throws Exception {
        Domain inputDomain = new Domain();
        inputDomain.setDomainExecutionRole(VALID_ARN);
        when(mockRequest.getBody()).thenReturn("{}");
        when(mockObjectMapper.readValue(any(String.class), eq(Domain.class))).thenReturn(inputDomain);

        APIGatewayProxyResponseEvent response = createDomainHandler.handle(mockRequest);

        assertEquals(400, response.getStatusCode());
        assertEquals("Missing domain name.", response.getBody());
        verify(mockTable, never()).putItem(any());
    }
    
    @Test
    void handle_InvalidArn_ShouldReturn400() throws Exception {
        Domain inputDomain = new Domain();
        inputDomain.setName("TestDomain");
        inputDomain.setDomainExecutionRole("invalid-arn"); 
        when(mockRequest.getBody()).thenReturn("{}");
        when(mockObjectMapper.readValue(any(String.class), eq(Domain.class))).thenReturn(inputDomain);

        APIGatewayProxyResponseEvent response = createDomainHandler.handle(mockRequest);

        assertEquals(400, response.getStatusCode());
        assertEquals("Invalid domainExecutionRole format.", response.getBody());
        verify(mockTable, never()).putItem(any());
    }
}
