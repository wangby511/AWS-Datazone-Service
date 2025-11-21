package com.example.handler.api.domain;

import com.example.handler.api.RouteHandler;
import com.example.model.Domain;
import com.example.dto.CreateDomainRequest;
import com.example.constant.Constants;
import com.example.utils.IdUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

public class CreateDomain implements RouteHandler {
    
    private final DynamoDbTable<Domain> domainTable; 
    private final ObjectMapper mapper;

    public CreateDomain(DynamoDbTable<Domain> domainTable, ObjectMapper mapper) { 
        this.domainTable = domainTable; 
        this.mapper = mapper; 
    }

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent request) {
        try {
            CreateDomainRequest reqBody = mapper.readValue(request.getBody(), CreateDomainRequest.class);
            
            if (reqBody.getName() == null || reqBody.getName().isEmpty()) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("Missing domain name.");
            }
            if (reqBody.getDomainExecutionRole() == null || reqBody.getDomainExecutionRole().isEmpty()) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("Missing domainExecutionRole.");
            }
            if (!reqBody.getDomainExecutionRole().matches(Constants.IAM_ROLE_ARN_REGEX)) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("Invalid domainExecutionRole format.");
            }

            Domain domain = Domain.builder()
                .identifier(IdUtils.generateDomainId())
                .name(reqBody.getName())
                .description(reqBody.getDescription())
                .domainExecutionRole(reqBody.getDomainExecutionRole())
                .build();
            
            domainTable.putItem(domain);
            
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(201)
                    .withBody(mapper.writeValueAsString(domain));
        } catch (Exception e) { 
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("Error creating domain: " + e.getMessage()); 
        }
    }
}
