import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.Collection;
import java.util.Objects;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.model.ChangeSetType;
import com.amazonaws.services.cloudformation.model.Parameter;

public class DeployService {
	private final CloudformationService service;

	public DeployService(CloudformationService service) {
		this.service = service;
	}

	public DeployService(Regions region) {
		this(new CloudformationService(region));
	}

	public void deploy(String stackName, String templateBody, String codeUri, String swaggerDefinitionUri,
			String stage) {
		final String changeSetName = stackName + "-" + System.currentTimeMillis();
		final ChangeSetType changeSetType = service.stackExists(stackName) ? ChangeSetType.UPDATE
				: ChangeSetType.CREATE;
		String newTemplateBody = updateTemplateBody(templateBody, codeUri, swaggerDefinitionUri, stage);
		final String changeSetArn = service.createChangeSet(stackName, changeSetName, changeSetType,
				newTemplateBody, emptyList());
		service.waitForChangeSetReady(changeSetArn);
		service.executeChangeSet(changeSetArn);
		service.waitForStackReady(stackName);
	}

	private String updateTemplateBody(String templateBody, String codeUri, String swaggerDefinitionUri, String stage) {
		final Collection<Parameter> parameters = asList(
				new Parameter().withParameterKey("CodeUri").withParameterValue(Objects.requireNonNull(codeUri)),
				new Parameter().withParameterKey("DefinitionUri")
						.withParameterValue(Objects.requireNonNull(swaggerDefinitionUri)),
				new Parameter().withParameterKey("stage").withParameterValue(Objects.requireNonNull(stage)));

		return replaceParameters(templateBody, parameters);
	}

	private String replaceParameters(String original, Collection<Parameter> parameters) {
		String result = original;
		for (final Parameter param : parameters) {
			System.out.println("replace " + param);
			result = result.replace("${" + param.getParameterKey() + "}", param.getParameterValue());
		}
		return result;
	}
}
