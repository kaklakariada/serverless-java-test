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
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.ExecuteChangeSetRequest;
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
			String templateBody) {
		final CreateChangeSetRequest changeSetRequest = new CreateChangeSetRequest() //
				.withCapabilities(Capability.CAPABILITY_IAM) //
				.withStackName(stackName) //
				.withChangeSetName(changeSetName) //
				.withChangeSetType(changeSetType) //
				.withTemplateBody(templateBody);
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
		final StatusPoller statusPoller = new StatusPoller(() -> cloudFormation
				.describeChangeSet(new DescribeChangeSetRequest().withChangeSetName(changeSetArn)).getStatus());
		statusPoller.waitUntilFinished();
	}

	public void waitForStackReady(String stackName) {
		LOG.info("Waiting for stack {}", stackName);
		final StatusPoller statusPoller = new StatusPoller(() -> {
			final DescribeStacksResult result = cloudFormation
					.describeStacks(new DescribeStacksRequest().withStackName(stackName));
			return result.getStacks().stream() //
					.map(Stack::getStackStatus) //
					.findFirst() //
					.orElseThrow(() -> new DeploymentException("Stack '" + stackName + "' not found"));
		});
		statusPoller.waitUntilFinished();
	}

	public void executeChangeSet(String changeSetArn) {
		cloudFormation.executeChangeSet(new ExecuteChangeSetRequest().withChangeSetName(changeSetArn));
		LOG.info("Executing change set {}", changeSetArn);
	}

	private static class StatusPoller {
		private final Supplier<String> statusSupplier;

		public StatusPoller(Supplier<String> statusSupplier) {
			this.statusSupplier = statusSupplier;
		}

		public void waitUntilFinished() {
			while (true) {
				final String status = statusSupplier.get();
				LOG.info("Got status {}", status);
				if (isFailed(status)) {
					throw new DeploymentException("Got failure status " + status);
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
			return status.toUpperCase().contains("COMPLETE");
		}

		private boolean isFailed(String status) {
			return status.toUpperCase().contains("FAILED");
		}
	}
}
