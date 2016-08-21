package hello;

import static java.util.stream.Collectors.toList;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class Response {

	private final String message;
	private final List<Invocation> invocations;

	public Response(String message, List<Invocation> invocations) {
		this.message = message;
		this.invocations = invocations;
	}

	public String getMessage() {
		return this.message;
	}

	public List<String> getInvocations() {
		return invocations.stream().map(Invocation::toString).collect(toList());
	}

	public static class Invocation {
		private final int id;
		private final Instant timestamp;
		private final Invocation previousInvocation;

		public Invocation(int id, Instant timestamp, Invocation previousInvocation) {
			this.id = id;
			this.timestamp = timestamp;
			this.previousInvocation = previousInvocation;
		}

		private Duration getDuration() {
			return previousInvocation != null ? Duration.between(previousInvocation.timestamp, timestamp) : null;
		}

		private boolean isShortDuration() {
			final Duration duration = getDuration();
			return duration != null ? duration.compareTo(Duration.ofMillis(5)) < 0 : false;
		}

		private int getCombinedCount() {
			if (previousInvocation == null) {
				return 0;
			}
			if (this.isShortDuration()) {
				return 1 + previousInvocation.getCombinedCount();
			}
			return 0;
		}

		@Override
		public String toString() {
			final Duration duration = getDuration();
			final String durationText;
			if (duration == null) {
				durationText = "initial invocation";
			} else if (isShortDuration()) {
				durationText = "~ 0 (" + duration + ")";
			} else {
				durationText = "" + duration;
			}
			return "#" + id + ": " + timestamp + ", combined count: " + getCombinedCount() + ", duration: "
					+ durationText;
		}
	}
}
