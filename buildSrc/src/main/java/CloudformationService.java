import java.util.Collection;
import java.util.function.Supplier;

import org.gradle.api.logging.Logging;
import org.slf4j.Logger;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.ChangeSetType;
import com.amazonaws.services.cloudformation.model.CreateChangeSetRequest;
import com.amazonaws.services.cloudformation.model.CreateChangeSetResult;
import com.amazonaws.services.cloudformation.model.DescribeChangeSetRequest;
import com.amazonaws.services.cloudformation.model.DescribeChangeSetResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.ExecuteChangeSetRequest;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;

public class CloudformationService {

	private static final Logger LOG = Logging.getLogger(CloudformationService.class);
	private final AmazonCloudFormation cloudFormation;

	public CloudformationService(AmazonCloudFormation cloudFormation) {
		this.cloudFormation = cloudFormation;
	}

	public CloudformationService(Regions region) {
		this(new AmazonCloudFormationClient().withRegion(region));
	}

	public String createChangeSet(String stackName, String changeSetName, ChangeSetType changeSetType,
			String templateBody, Collection<Parameter> parameters) {
		LOG.info("Creating change set for stack {} with parameters {}", stackName, parameters);
		final CreateChangeSetRequest changeSetRequest = new CreateChangeSetRequest() //
				.withCapabilities(Capability.CAPABILITY_IAM) //
				.withStackName(stackName) //
				.withChangeSetName(changeSetName) //
				.withChangeSetType(changeSetType) //
				.withParameters(parameters).withTemplateBody(templateBody);
		final CreateChangeSetResult result = cloudFormation.createChangeSet(changeSetRequest);
		LOG.info("Change set created: {}", result);
		return result.getId();
	}

	public boolean stackExists(String stackName) {
		final DescribeStacksResult stacks = cloudFormation
				.describeStacks(new DescribeStacksRequest().withStackName(stackName));
		return !stacks.getStacks().isEmpty();
	}

	public void waitForChangeSetReady(String changeSetArn) {
		LOG.info("Waiting for change set {}", changeSetArn);
		final StatusPoller statusPoller = new StatusPoller(() -> getChangeSetStatus(changeSetArn).getStatus(),
				() -> getChangeSetStatus(changeSetArn).getStatusReason());
		statusPoller.waitUntilFinished();
	}

	private DescribeChangeSetResult getChangeSetStatus(String changeSetArn) {
		return cloudFormation.describeChangeSet(new DescribeChangeSetRequest().withChangeSetName(changeSetArn));
	}

	public void waitForStackReady(String stackName) {
		LOG.info("Waiting for stack {}", stackName);
		final StatusPoller statusPoller = new StatusPoller(() -> {
			return getStackStatus(stackName).getStackStatus();
		}, () -> getStackStatus(stackName).toString());
		statusPoller.waitUntilFinished();
	}

	private Stack getStackStatus(String stackName) {
		final DescribeStacksResult result = cloudFormation
				.describeStacks(new DescribeStacksRequest().withStackName(stackName));
		return result.getStacks().stream() //
				.findFirst() //
				.orElseThrow(() -> new DeploymentException("Stack '" + stackName + "' not found"));
	}

	public void executeChangeSet(String changeSetArn) {
		cloudFormation.executeChangeSet(new ExecuteChangeSetRequest().withChangeSetName(changeSetArn));
		LOG.info("Executing change set {}", changeSetArn);
	}

	private static class StatusPoller {
		private final Supplier<String> statusSupplier;
		private final Supplier<String> failureMessageSupplier;

		public StatusPoller(Supplier<String> statusSupplier, Supplier<String> failureMessageSupplier) {
			this.statusSupplier = statusSupplier;
			this.failureMessageSupplier = failureMessageSupplier;
		}

		public void waitUntilFinished() {
			while (true) {
				final String status = statusSupplier.get();
				LOG.info("Got status {}", status);
				if (isFailed(status)) {
					throw new DeploymentException("Got failure status " + status + ": " + failureMessageSupplier.get());
				}
				if (isSuccess(status)) {
					return;
				}
				sleep();
			}
		}

		private void sleep() {
			try {
				Thread.sleep(2000);
			} catch (final InterruptedException e) {
				throw new DeploymentException("Exception while sleeping", e);
			}
		}

		private boolean isSuccess(String status) {
			return status.toUpperCase().endsWith("COMPLETE");
		}

		private boolean isFailed(String status) {
			return status.toUpperCase().contains("FAILED");
		}
	}
}