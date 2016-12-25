package com.github.kaklakariada.aws.lambda;

import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.github.kaklakariada.aws.lambda.request.ApiGatewayRequest;

public abstract class LambdaRequestHandler<I, O> implements RequestStreamHandler {

	private static final Logger LOG = LoggerFactory.getLogger(LambdaRequestHandler.class);
	private final RequestHandlingService<I, O> requestHandlingService;

	protected LambdaRequestHandler(Class<I> requestType) {
		requestHandlingService = new RequestHandlingService<>(this, requestType);
	}

	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) {
		requestHandlingService.handleRequest(input, output, context);
	}

	final O handleRequestInternal(ApiGatewayRequest request, final I body, Context context) {
		return handleRequest(request, body, context);
	}

	public abstract O handleRequest(ApiGatewayRequest request, final I body, Context context);
}
