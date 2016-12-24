package com.github.kaklakariada.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.github.kaklakariada.aws.lambda.exception.LambdaException;
import com.github.kaklakariada.aws.lambda.request.ApiGatewayRequest;

public class ErrorResponseBody {

	public ErrorResponseBody(int statusCode, String errorMessage, String awsRequestId, String logGroupName,
			String logStreamName) {
		this.statusCode = statusCode;
		this.errorMessage = errorMessage;
		this.awsRequestId = awsRequestId;
		this.logGroupName = logGroupName;
		this.logStreamName = logStreamName;
	}

	public static ErrorResponseBody create(LambdaException e, ApiGatewayRequest request, Context context) {
		return new ErrorResponseBody(e.getErrorCode(), e.getErrorMessage(), context.getAwsRequestId(),
				context.getLogGroupName(), context.getLogStreamName());
	}

	private final int statusCode;
	private final String errorMessage;
	private final String awsRequestId;
	private final String logGroupName;
	private final String logStreamName;

	public String getErrorMessage() {
		return errorMessage;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getAwsRequestId() {
		return awsRequestId;
	}

	public String getLogGroupName() {
		return logGroupName;
	}

	public String getLogStreamName() {
		return logStreamName;
	}
}
