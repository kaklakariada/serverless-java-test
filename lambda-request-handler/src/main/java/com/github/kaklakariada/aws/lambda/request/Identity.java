package com.github.kaklakariada.aws.lambda.request;

public class Identity {
	private String cognitoIdentityPoolId;
	private String accountId;
	private String cognitoIdentityId;
	private String caller;
	private String apiKey;
	private String sourceIp;
	private String cognitoAuthenticationType;
	private String cognitoAuthenticationProvider;
	private String userArn;
	private String userAgent;
	private String user;
	private String accessKey;

	public String getCognitoIdentityPoolId() {
		return cognitoIdentityPoolId;
	}

	public String getAccountId() {
		return accountId;
	}

	public String getCognitoIdentityId() {
		return cognitoIdentityId;
	}

	public String getCaller() {
		return caller;
	}

	public String getApiKey() {
		return apiKey;
	}

	public String getSourceIp() {
		return sourceIp;
	}

	public String getCognitoAuthenticationType() {
		return cognitoAuthenticationType;
	}

	public String getCognitoAuthenticationProvider() {
		return cognitoAuthenticationProvider;
	}

	public String getUserArn() {
		return userArn;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public String getUser() {
		return user;
	}

	public String getAccessKey() {
		return accessKey;
	}

	@Override
	public String toString() {
		return "Identity [cognitoIdentityPoolId=" + cognitoIdentityPoolId + ", accountId=" + accountId
				+ ", cognitoIdentityId=" + cognitoIdentityId + ", caller=" + caller + ", apiKey=" + apiKey
				+ ", sourceIp=" + sourceIp + ", cognitoAuthenticationType=" + cognitoAuthenticationType
				+ ", cognitoAuthenticationProvider=" + cognitoAuthenticationProvider + ", userArn=" + userArn
				+ ", userAgent=" + userAgent + ", user=" + user + "]";
	}

}
