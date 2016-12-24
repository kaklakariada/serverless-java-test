package com.github.kaklakariada.aws.lambda.request;

public class RequestContext {
	private String accountId;
	private String resourceId;
	private String stage;
	private String requestId;
	private Identity identity;
	private String resourcePath;
	private String httpMethod;
	private String apiId;

	public String getAccountId() {
		return accountId;
	}

	public String getResourceId() {
		return resourceId;
	}

	public String getStage() {
		return stage;
	}

	public String getRequestId() {
		return requestId;
	}

	public Identity getIdentity() {
		return identity;
	}

	public String getResourcePath() {
		return resourcePath;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public String getApiId() {
		return apiId;
	}

	@Override
	public String toString() {
		return "RequestContext [accountId=" + accountId + ", resourceId=" + resourceId + ", stage=" + stage
				+ ", requestId=" + requestId + ", identity=" + identity + ", resourcePath=" + resourcePath
				+ ", httpMethod=" + httpMethod + ", apiId=" + apiId + "]";
	}
}
