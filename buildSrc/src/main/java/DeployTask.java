import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.ChangeSetType;
import com.amazonaws.services.cloudformation.model.CreateChangeSetRequest;
import com.amazonaws.services.cloudformation.model.CreateChangeSetResult;
import com.amazonaws.services.cloudformation.model.ExecuteChangeSetRequest;
import com.amazonaws.services.cloudformation.model.ExecuteChangeSetResult;

public class DeployTask extends DefaultTask {

	@Input
	public String region;

	@Input
	public String s3Url;

	@InputFile
	public File template;

	@TaskAction
	public void uploadFileToS3() throws IOException, InterruptedException {
		final AmazonCloudFormation cf = new AmazonCloudFormationClient().withRegion(Regions.fromName(region));
		String templateBody = loadTemplate();
		templateBody = replaceVariables(templateBody);

		// final CreateStackRequest request = new CreateStackRequest() //
		// .withStackName(getProject().getName()) //
		// .withCapabilities(Capability.CAPABILITY_IAM) //
		// .withTemplateBody(templateBody);
		//
		// cf.createStack(request);

		final String stackName = getProject().getName();
		final String changeSetName = stackName + "-" + System.currentTimeMillis();
		final ProgressListener progressListener = new ProgressListener() {

			@Override
			public void progressChanged(ProgressEvent progressEvent) {
				getLogger().info("Update progress: " + progressEvent);
			}

		};
		final CreateChangeSetRequest changeSetRequest = new CreateChangeSetRequest() //
				.withCapabilities(Capability.CAPABILITY_IAM) //
				.withStackName(stackName) //
				.withChangeSetName(changeSetName) //
				.withChangeSetType(ChangeSetType.UPDATE) //
				.withTemplateBody(templateBody).withGeneralProgressListener(progressListener);
		final CreateChangeSetResult result = cf.createChangeSet(changeSetRequest);

		getLogger().info("Change set created: " + result);

		Thread.sleep(5000);

		final ExecuteChangeSetResult result2 = cf.executeChangeSet(new ExecuteChangeSetRequest() //
				.withChangeSetName(changeSetName) //
				.withStackName(stackName) //
				.withGeneralProgressListener(progressListener));

		getLogger().info("Change set executed: " + result2);

	}

	private String replaceVariables(String templateBody) {
		return templateBody.replace("${codeUri}", s3Url);
	}

	private String loadTemplate() throws IOException {
		return new String(Files.readAllBytes(template.toPath()));
	}
}
