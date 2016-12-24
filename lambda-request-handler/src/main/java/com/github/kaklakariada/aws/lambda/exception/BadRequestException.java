package com.github.kaklakariada.aws.lambda.exception;

public class BadRequestException extends LambdaException {

	private static final long serialVersionUID = 1L;

	public BadRequestException(String errorMessage, Throwable cause) {
		super("Bad request", cause, 400, errorMessage);
	}
}
