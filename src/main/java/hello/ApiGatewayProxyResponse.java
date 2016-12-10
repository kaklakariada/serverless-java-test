package hello;

import java.util.Collections;
import java.util.Map;

public class ApiGatewayProxyResponse<T> {

	private final int statusCode;
	private final Map<String, String> headers;

	private final T body;

	public ApiGatewayProxyResponse(int statusCode, Map<String, String> headers, T body) {
		this.statusCode = statusCode;
		this.headers = headers;
		this.body = body;
	}

	public ApiGatewayProxyResponse(T body) {
		this(200, Collections.emptyMap(), body);
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
