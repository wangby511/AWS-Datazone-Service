package com.example.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import lombok.Data;

@Data
@DynamoDbBean
public class Domain {
    private String identifier; // Primary Key
    private String name;
    private String description; // Optional parameter
    private String domainExecutionRole; // IAM Role ARN

    @DynamoDbPartitionKey
    public String getIdentifier() { return identifier; }
}
