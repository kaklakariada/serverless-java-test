package com.github.kaklakariada.aws.lambda;

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
	private final Class<O> responseType;

	protected LambdaRequestHandler(Class<I> requestType, Class<O> responseType) {
		this.requestType = requestType;
		this.responseType = responseType;
		this.objectMapper = new ObjectMapper();
	}

	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		final ApiGatewayRequest request = parseRequest(readStream(input), ApiGatewayRequest.class);
		final I body = parseBody(request.getBody());
		final ApiGatewayResponse response = handleRequestInternally(request, body, context);
		sendResponse(output, response);
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

	private ApiGatewayResponse handleRequestInternally(ApiGatewayRequest request, final I body, Context context) {
		try {
			final O result = handleRequest(request, body, context);
			return convertRespone(result);
		} catch (final LambdaException e) {
			LOG.error("Error processing request: " + e.getMessage());
			return new ApiGatewayResponse(e);
		} catch (final Exception e) {
			LOG.error("Error processing request: " + e.getMessage(), e);
			return new ApiGatewayResponse(new InternalServerErrorException("Error", e));
		}
	}

	private ApiGatewayResponse convertRespone(O result) {
		return new ApiGatewayResponse(serializeResult(result));
	}

	private String serializeResult(O result) {
		try {
			return objectMapper.writeValueAsString(result);
		} catch (final Exception e) {
			LOG.error("Error serializing response: " + e.getMessage(), e);
			throw new InternalServerErrorException("Error serializing response", e);
		}
	}

	public abstract O handleRequest(ApiGatewayRequest request, final I body, Context context);
}
