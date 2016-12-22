import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;

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

		final Logger logger = getLogger();
		// final CreateStackRequest request = new CreateStackRequest() //
		// .withStackName(getProject().getName()) //
		// .withCapabilities(Capability.CAPABILITY_IAM) //
		// .withTemplateBody(templateBody);
		//
		// cf.createStack(request);

		final String stackName = getProject().getName();

		final DeployService deployService = new DeployService(Regions.fromName(region));
		deployService.deploy(stackName, templateBody);

		// final String changeSetName = stackName + "-" +
		// System.currentTimeMillis();
		// final CreateChangeSetRequest changeSetRequest = new
		// CreateChangeSetRequest() //
		// .withCapabilities(Capability.CAPABILITY_IAM) //
		// .withStackName(stackName) //
		// .withChangeSetName(changeSetName) //
		// .withChangeSetType(ChangeSetType.UPDATE) //
		// .withTemplateBody(templateBody);
		// final CreateChangeSetResult result =
		// cf.createChangeSet(changeSetRequest);
		//
		// getLogger().info("Change set created: " + result);

		//
		// final ExecuteChangeSetResult result2 = cf.executeChangeSet(new
		// ExecuteChangeSetRequest() //
		// .withChangeSetName(changeSetName) //
		// .withStackName(stackName));

		// getLogger().info("Change set executed: " + result2);

	}

	private String replaceVariables(String templateBody) {
		return templateBody.replace("${codeUri}", s3Url);
	}

	private String loadTemplate() throws IOException {
		return new String(Files.readAllBytes(template.toPath()));
	}
}
