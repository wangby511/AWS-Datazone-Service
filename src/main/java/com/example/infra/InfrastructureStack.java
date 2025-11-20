package com.example.infra;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.CorsOptions;
import software.amazon.awscdk.services.dynamodb.*;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;
import com.example.constant.Constants;

import java.util.HashMap;
import java.util.Map;

public class InfrastructureStack extends Stack {
    public InfrastructureStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // Domain Table
        Table domainTable = Table.Builder.create(this, "DomainTable")
                .partitionKey(Attribute.builder().name("identifier").type(AttributeType.STRING).build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        // Project Table
        Table projTable = Table.Builder.create(this, "ProjectTable")
                .partitionKey(Attribute.builder().name("id").type(AttributeType.STRING).build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        // Project GSI (by domainIdentifier)
        projTable.addGlobalSecondaryIndex(GlobalSecondaryIndexProps.builder()
                .indexName(Constants.GSI_BY_DOMAIN)
                .partitionKey(Attribute.builder().name("domainIdentifier").type(AttributeType.STRING).build())
                .build());

        // Lambda Handler
        Map<String, String> envVars = new HashMap<>();
        envVars.put("DOMAIN_TABLE_NAME", domainTable.getTableName());
        envVars.put("PROJ_TABLE_NAME", projTable.getTableName());

        Function handler = Function.Builder.create(this, "AppHandler")
                .runtime(Runtime.JAVA_17)
                .code(Code.fromAsset("target/aws-datazone-api-1.0-SNAPSHOT.jar"))
                .handler("com.example.handler.EnvironmentDispatcher::handleRequest")
                .timeout(Duration.seconds(30))
                .memorySize(1024)
                .environment(envVars)
                .build();

        domainTable.grantReadWriteData(handler);
        projTable.grantReadWriteData(handler);

        // API Gateway
        RestApi api = RestApi.Builder.create(this, "DatazoneServiceApi")
                .restApiName("AWS Datazone Service")
                .defaultCorsPreflightOptions(CorsOptions.builder()
                        .allowOrigins(CorsOptions.ALL_ORIGINS)
                        .allowMethods(CorsOptions.ALL_METHODS)
                        .build())
                .build();
        
        LambdaIntegration integration = new LambdaIntegration(handler);

        // --- Domain API Endpoints ---
        Resource domainsResource = api.getRoot().addResource("domains");
        
        domainsResource.addResource("createDomain").addMethod("POST", integration);
        domainsResource.addResource("listDomains").addMethod("GET", integration);
        domainsResource.addResource("getDomain").addResource("{domainId}").addMethod("GET", integration);
        domainsResource.addResource("deleteDomain").addResource("{domainId}").addMethod("DELETE", integration);


        // --- Project API Endpoints (Nested) ---
        Resource domainIdResource = domainsResource.addResource("{domainIdentifier}");
        Resource projectsResource = domainIdResource.addResource("projects");

        projectsResource.addResource("createProject").addMethod("POST", integration);
        projectsResource.addResource("listProjects").addMethod("GET", integration);
        projectsResource.addResource("getProject").addResource("{projectId}").addMethod("GET", integration);
        projectsResource.addResource("deleteProject").addResource("{projectId}").addMethod("DELETE", integration);


        software.amazon.awscdk.CfnOutput.Builder.create(this, "ApiUrl").value(api.getUrl()).build();
    }
}
