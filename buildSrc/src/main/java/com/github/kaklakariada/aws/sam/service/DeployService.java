package com.github.kaklakariada.aws.sam.service;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.Collection;
import java.util.Objects;

import com.amazonaws.services.cloudformation.model.ChangeSetType;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.github.kaklakariada.aws.sam.config.SamConfig;

public class DeployService {
	private final CloudformationService cloudFormationService;
	private final TemplateService templateService;
	private final SamConfig config;

	public DeployService(SamConfig config, CloudformationService cloudFormationService,
			TemplateService templateService) {
		this.config = config;
		this.cloudFormationService = cloudFormationService;
		this.templateService = templateService;
	}

	public DeployService(SamConfig config) {
		this(config, new CloudformationService(config), new TemplateService());
	}

	public void deploy(String stackName, String templateBody, String codeUri, String swaggerDefinitionUri) {
		final String changeSetName = stackName + "-" + System.currentTimeMillis();
		final ChangeSetType changeSetType = cloudFormationService.stackExists() ? ChangeSetType.UPDATE
				: ChangeSetType.CREATE;
		final String newTemplateBody = updateTemplateBody(templateBody, codeUri, swaggerDefinitionUri);
		final String changeSetArn = cloudFormationService.createChangeSet(changeSetName, changeSetType, newTemplateBody,
				emptyList());
		cloudFormationService.waitForChangeSetReady(changeSetArn);
		cloudFormationService.executeChangeSet(changeSetArn);
		cloudFormationService.waitForStackReady();
	}

	private String updateTemplateBody(String templateBody, String codeUri, String swaggerDefinitionUri) {
		final Collection<Parameter> parameters = asList(
				new Parameter().withParameterKey("CodeUri").withParameterValue(Objects.requireNonNull(codeUri)),
				new Parameter().withParameterKey("stage").withParameterValue(config.currentStage));
		if (swaggerDefinitionUri != null) {
			parameters.add(new Parameter().withParameterKey("DefinitionUri").withParameterValue(swaggerDefinitionUri));
		}
		return templateService.replaceParameters(templateBody, parameters);
	}
}
