package hello;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import hello.Response.Invocation;

public class Handler implements RequestHandler<Request, Response> {

	private static final Logger LOG = LoggerFactory.getLogger(Handler.class);
	private final ScheduledExecutorService executor;
	private final Instant startup;

	private Invocation previousInvocation;

	private final Clock clock;
	private int handlerCount = 0;
	private final ConcurrentLinkedQueue<Invocation> invocations = new ConcurrentLinkedQueue<>();

	public Handler() {
		LOG.info("Initializing handler");
		clock = Clock.systemUTC();
		startup = clock.instant();
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(() -> {
			final Instant now = clock.instant();
			previousInvocation = new Invocation(invocations.size(), now, previousInvocation);
			LOG.debug("Thread {}, alive for {}, invocations: {}, current: {}", Thread.currentThread().getName(),
					getTimeSinceStartup(), invocations.size(), previousInvocation);
			invocations.add(previousInvocation);
		}, 0, 1, TimeUnit.SECONDS);
	}

	@Override
	public Response handleRequest(Request input, Context context) {
		handlerCount++;
		final String message = "Request #" + handlerCount + ", sime since startup: " + getTimeSinceStartup()
				+ ", scheduled invocations: " + invocations.size();
		LOG.debug(message);
		return new Response(message, new ArrayList<>(invocations));
	}

	private Duration getTimeSinceStartup() {
		return Duration.between(startup, clock.instant());
	}
}
