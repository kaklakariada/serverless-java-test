package hello;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiGatewayJsonResponse<T> {

	private final int statusCode;
	private final Map<String, String> headers;
	private final T body;
	private ObjectMapper objectMapper;

	public ApiGatewayJsonResponse(int statusCode, Map<String, String> headers, T body) {
		this.statusCode = statusCode;
		this.headers = headers;
		this.body = body;
		objectMapper = new ObjectMapper();
	}

	public ApiGatewayJsonResponse(T body) {
		this(200, Collections.emptyMap(), body);
	}

	public int getStatusCode() {
		return statusCode;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public String getBody() {
		try {
			return objectMapper.writeValueAsString(body);
		} catch (final JsonProcessingException e) {
			throw new RuntimeException("Error serializing body to json", e);
		}
	}
}
