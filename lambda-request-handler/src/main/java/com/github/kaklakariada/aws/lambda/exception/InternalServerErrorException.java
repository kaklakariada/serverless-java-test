package com.github.kaklakariada.aws.lambda.exception;

public class InternalServerErrorException extends LambdaException {

	private static final long serialVersionUID = 1L;

	public InternalServerErrorException(String errorMessage, Throwable cause) {
		super("Internal Server Error", cause, 500, errorMessage);
	}
}
