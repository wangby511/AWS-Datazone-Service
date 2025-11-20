package com.example.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import com.example.constant.Constants;
import lombok.Data;

@Data
@DynamoDbBean
public class Project {
    private String id; // Project ID (Primary Key)
    private String domainIdentifier; // Foreign Key to Domain (GSI partition key)
    private String name;
    private String description; // Optional field
    // userRoles field removed

    @DynamoDbPartitionKey
    public String getId() { return id; }

    @DynamoDbSecondaryPartitionKey(indexNames = Constants.GSI_BY_DOMAIN)
    public String getDomainIdentifier() { return domainIdentifier; }
}
