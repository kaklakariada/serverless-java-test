package com.github.kaklakariada.aws.lambda;

import static java.util.Collections.emptyMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Base64;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kaklakariada.aws.lambda.exception.BadRequestException;
import com.github.kaklakariada.aws.lambda.exception.InternalServerErrorException;
import com.github.kaklakariada.aws.lambda.exception.LambdaException;
import com.github.kaklakariada.aws.lambda.request.ApiGatewayRequest;

public class RequestHandlingService<I, O> {
	private static final Logger LOG = LoggerFactory.getLogger(RequestHandlingService.class);

	private final ObjectMapper objectMapper;
	private final Class<I> requestType;
	private final LambdaRequestHandler<I, O> handler;

	public RequestHandlingService(LambdaRequestHandler<I, O> handler, Class<I> requestType) {
		this.objectMapper = new ObjectMapper();
		this.requestType = requestType;
		this.handler = handler;
	}

	public void handleRequest(InputStream input, OutputStream output, Context context) {
		final ApiGatewayResponse response = handleRequest(input, context);
		sendResponse(output, response);
	}

	private ApiGatewayResponse handleRequest(InputStream input, Context context) {
		try {
			final ApiGatewayRequest request = parseRequest(readStream(input), ApiGatewayRequest.class);
			final I body = parseBody(request);
			final O result = handler.handleRequestInternal(request, body, context);
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

	private I parseBody(ApiGatewayRequest request) {
		final String body = request.getBody();
		if (body == null || body.isEmpty()) {
			return null;
		}
		if (!request.getIsBase64Encoded()) {
			return parseRequest(body, requestType);
		}
		if (!requestType.equals(byte[].class)) {
			throw new IllegalStateException(
					"Got base64 encoded body but expected request type is " + requestType.getName());
		}
		return base64Decode(body);
	}

	private I base64Decode(String body) {
		@SuppressWarnings("unchecked")
		final I decodedBody = (I) Base64.getDecoder().decode(body);
		return decodedBody;
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
}
