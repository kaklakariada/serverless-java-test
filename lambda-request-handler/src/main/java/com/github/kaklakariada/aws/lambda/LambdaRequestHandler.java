package com.github.kaklakariada.aws.lambda;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kaklakariada.aws.lambda.exception.BadRequestException;
import com.github.kaklakariada.aws.lambda.exception.InternalServerErrorException;
import com.github.kaklakariada.aws.lambda.exception.LambdaException;
import com.github.kaklakariada.aws.lambda.request.ApiGatewayRequest;

public abstract class LambdaRequestHandler<I, O> implements RequestStreamHandler {

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
		final ApiGatewayRequest request = parseRequest(new InputStreamReader(input, StandardCharsets.UTF_8),
				ApiGatewayRequest.class);
		final I body = parseBody(request.getBody());
		final ApiGatewayResponse response = handleRequestInternally(request, body, context);
		sendResponse(output, response);
	}

	private I parseBody(String body) {
		if (body == null || body.isEmpty()) {
			return null;
		}
		return parseRequest(new StringReader(body), requestType);
	}

	private void sendResponse(OutputStream output, ApiGatewayResponse response) {
		try {
			objectMapper.writeValue(output, response);
		} catch (final Exception e) {
			throw new InternalServerErrorException("Error serializing response", e);
		}
	}

	private <T> T parseRequest(Reader input, Class<T> type) {
		try {
			return objectMapper.readValue(input, type);
		} catch (final Exception e) {
			throw new BadRequestException("Error parsing request", e);
		}
	}

	private ApiGatewayResponse handleRequestInternally(ApiGatewayRequest request, final I body, Context context) {
		try {
			final O result = handleRequest(request, body, context);
			return convertRespone(result);
		} catch (final LambdaException e) {
			return new ApiGatewayResponse(e);
		}
	}

	private ApiGatewayResponse convertRespone(O result) {
		return new ApiGatewayResponse(serializeResult(result));
	}

	private String serializeResult(O result) {
		try {
			return objectMapper.writeValueAsString(result);
		} catch (final Exception e) {
			throw new InternalServerErrorException("Error serializing response", e);
		}
	}

	public abstract O handleRequest(ApiGatewayRequest request, final I body, Context context);
}
