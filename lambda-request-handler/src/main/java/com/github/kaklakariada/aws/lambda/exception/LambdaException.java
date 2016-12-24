package com.github.kaklakariada.aws.lambda.exception;

public class LambdaException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final int errorCode;
	private final String errorMessage;

	protected LambdaException(String message, Throwable cause, int errorCode, String errorMessage) {
		super(message, cause);
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public int getErrorCode() {
		return errorCode;
	}
}
