package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.github.kaklakariada.aws.lambda.LambdaRequestHandler;
import com.github.kaklakariada.aws.lambda.exception.BadRequestException;
import com.github.kaklakariada.aws.lambda.request.ApiGatewayRequest;

public class Handler extends LambdaRequestHandler<Request, Response> {

	private static final Logger LOG = LoggerFactory.getLogger(Handler.class);

	public Handler() {
		super(Request.class);
	}

	@Override
	public Response handleRequest(ApiGatewayRequest request, Request body, Context context) {
		LOG.debug("Got request {}", request);
		LOG.debug("Got body {}", body);
		if (request.getQueryStringParameters().containsKey("illegal")) {
			throw new IllegalArgumentException("blubb");
		}
		if (request.getQueryStringParameters().containsKey("bad")) {
			throw new BadRequestException("bad request exception requested", null);
		}
		return new Response("Success: " + body);
	}
}
