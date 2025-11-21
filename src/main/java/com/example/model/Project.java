package com.example.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import com.example.constant.Constants;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@DynamoDbBean
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @DynamoDbPartitionKey
    private String id;

    @DynamoDbSecondaryPartitionKey(indexNames = Constants.GSI_BY_DOMAIN)
    private String domainIdentifier;

    private String name;

    private String description;
}
