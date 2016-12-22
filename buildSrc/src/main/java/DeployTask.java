import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.regions.Regions;

public class DeployTask extends DefaultTask {

	@Input
	public String region;
	@Input
	public String codeUri;
	@Input
	public String swaggerUri;
	@Input
	public String stage;

	@InputFile
	public File template;

	@TaskAction
	public void uploadFileToS3() throws IOException, InterruptedException {

		final String templateBody = loadTemplate();

		final String stackName = getProject().getName();

		final DeployService deployService = new DeployService(Regions.fromName(region));
		deployService.deploy(stackName, templateBody, codeUri, swaggerUri, stage);
	}

	private String loadTemplate() throws IOException {
		return new String(Files.readAllBytes(template.toPath()));
	}
}
