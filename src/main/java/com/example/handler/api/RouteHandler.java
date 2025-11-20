package com.example.handler.api;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public interface RouteHandler {
    APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent request);
}
