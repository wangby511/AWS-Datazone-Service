package com.example.handler.api.domain;

import com.example.handler.api.RouteHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.model.Domain;
import com.example.constant.Constants;
import com.example.utils.IdUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

public class CreateDomain implements RouteHandler {
    private final DynamoDbTable<Domain> table;
    private final ObjectMapper objectMapper;

    public CreateDomain(DynamoDbTable<Domain> table, ObjectMapper objectMapper) {
        this.table = table;
        this.objectMapper = objectMapper;
    }

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent request) {
        try {
            // Domain.java 包含可选的 description
            Domain newDomain = objectMapper.readValue(request.getBody(), Domain.class);
            
            // 校验: name 和 domainExecutionRole 不能为空
            if (newDomain.getName() == null || newDomain.getName().isEmpty()) {
                return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Missing domain name.");
            }
            if (newDomain.getDomainExecutionRole() == null || newDomain.getDomainExecutionRole().isEmpty()) {
                return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Missing domainExecutionRole.");
            }

            // 校验: domainExecutionRole 必须符合 IAM ARN 格式
            if (!newDomain.getDomainExecutionRole().matches(Constants.IAM_ROLE_ARN_REGEX)) {
                return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Invalid domainExecutionRole format.");
            }

            // 生成并设置 identifier
            newDomain.setIdentifier(IdUtils.generateDomainId());

            table.putItem(newDomain);
            
            return new APIGatewayProxyResponseEvent().withStatusCode(201).withBody(objectMapper.writeValueAsString(newDomain));
        } catch (Exception e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Error creating domain: " + e.getMessage());
        }
    }
}
