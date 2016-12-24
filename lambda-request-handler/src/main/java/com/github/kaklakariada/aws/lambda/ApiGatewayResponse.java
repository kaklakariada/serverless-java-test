package com.github.kaklakariada.aws.lambda;

import java.util.Collections;
import java.util.Map;

import com.github.kaklakariada.aws.lambda.exception.LambdaException;

public class ApiGatewayResponse {
	private final int statusCode;
	private final Map<String, String> headers;
	private final String body;

	public ApiGatewayResponse(int statusCode, Map<String, String> headers, String body) {
		this.statusCode = statusCode;
		this.headers = headers;
		this.body = body;
	}

	public ApiGatewayResponse(String body) {
		this(200, Collections.emptyMap(), body);
	}

	public ApiGatewayResponse(LambdaException e) {
		this(e.getErrorCode(), Collections.emptyMap(), e.getErrorMessage());
	}

	public int getStatusCode() {
		return statusCode;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public String getBody() {
		return body.toString();
	}
}
