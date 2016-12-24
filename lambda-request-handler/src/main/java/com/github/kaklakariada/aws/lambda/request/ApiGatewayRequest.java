package com.github.kaklakariada.aws.lambda.request;

import java.util.Map;

public class ApiGatewayRequest {

	private String resource;
	private String path;
	private HttpMethod httpMethod;
	private Map<String, String> headers;
	private Map<String, String> queryStringParameters;
	private Map<String, String> pathParameters;
	private Map<String, String> stageVariables;
	private RequestContext requestContext;
	private String body;
	private boolean isBase64Encoded;

	public String getResource() {
		return resource;
	}

	public String getPath() {
		return path;
	}

	public HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public Map<String, String> getQueryStringParameters() {
		return queryStringParameters;
	}

	public Map<String, String> getPathParameters() {
		return pathParameters;
	}

	public Map<String, String> getStageVariables() {
		return stageVariables;
	}

	public RequestContext getRequestContext() {
		return requestContext;
	}

	public String getBody() {
		return body;
	}

	public boolean getIsBase64Encoded() {
		return isBase64Encoded;
	}

	@Override
	public String toString() {
		return "ApiGatewayRequest [resource=" + resource + ", path=" + path + ", httpMethod=" + httpMethod
				+ ", headers=" + headers + ", queryStringParameters=" + queryStringParameters + ", stageVariables="
				+ stageVariables + ", requestContext=" + requestContext + ", body=" + body + ", isBase64Encoded="
				+ isBase64Encoded + "]";
	}
}
