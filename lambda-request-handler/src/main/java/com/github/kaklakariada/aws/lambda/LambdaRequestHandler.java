package com.github.kaklakariada.aws.lambda;

import static java.util.Collections.emptyMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kaklakariada.aws.lambda.exception.BadRequestException;
import com.github.kaklakariada.aws.lambda.exception.InternalServerErrorException;
import com.github.kaklakariada.aws.lambda.exception.LambdaException;
import com.github.kaklakariada.aws.lambda.request.ApiGatewayRequest;

public abstract class LambdaRequestHandler<I, O> implements RequestStreamHandler {

	private static final Logger LOG = LoggerFactory.getLogger(LambdaRequestHandler.class);

	private final ObjectMapper objectMapper;
	private final Class<I> requestType;

	protected LambdaRequestHandler(Class<I> requestType) {
		this.requestType = requestType;
		this.objectMapper = new ObjectMapper();
	}

	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {

		final ApiGatewayResponse response = handleRequest(input, context);
		sendResponse(output, response);
	}

	private ApiGatewayResponse handleRequest(InputStream input, Context context) {
		try {
			final ApiGatewayRequest request = parseRequest(readStream(input), ApiGatewayRequest.class);
			final I body = parseBody(request.getBody());
			final O result = handleRequest(request, body, context);
			return new ApiGatewayResponse(serializeResult(result));
		} catch (final LambdaException e) {
			LOG.error("Error processing request: " + e.getMessage());
			return buildErrorResponse(e, context);
		} catch (final Exception e) {
			LOG.error("Error processing request: " + e.getMessage(), e);
			return buildErrorResponse(new InternalServerErrorException("Error", e), context);
		}
	}

	private String readStream(InputStream input) {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
			return buffer.lines().collect(Collectors.joining("\n"));
		} catch (final IOException e) {
			throw new InternalServerErrorException("Error reading input stream", e);
		}
	}

	private I parseBody(String body) {
		if (body == null || body.isEmpty()) {
			return null;
		}
		return parseRequest(body, requestType);
	}

	private void sendResponse(OutputStream output, ApiGatewayResponse response) {
		try {
			objectMapper.writeValue(output, response);
		} catch (final Exception e) {
			LOG.error("Error serializing response: " + e.getMessage(), e);
			throw new InternalServerErrorException("Error serializing response", e);
		}
	}

	private <T> T parseRequest(String input, Class<T> type) {
		try {
			return objectMapper.readValue(input, type);
		} catch (final Exception e) {
			LOG.error("Error parsing input '" + input + "': " + e.getMessage(), e);
			throw new BadRequestException("Error parsing request", e);
		}
	}

	private ApiGatewayResponse buildErrorResponse(LambdaException e, Context context) {
		final ErrorResponseBody errorResult = ErrorResponseBody.create(e, context);
		return new ApiGatewayResponse(e.getErrorCode(), emptyMap(), serializeResult(errorResult));
	}

	private String serializeResult(Object result) {
		try {
			return objectMapper.writeValueAsString(result);
		} catch (final Exception e) {
			LOG.error("Error serializing response: " + e.getMessage(), e);
			throw new InternalServerErrorException("Error serializing response", e);
		}
	}

	public abstract O handleRequest(ApiGatewayRequest request, final I body, Context context);
}
