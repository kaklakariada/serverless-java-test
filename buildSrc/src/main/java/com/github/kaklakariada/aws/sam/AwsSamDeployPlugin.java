package com.github.kaklakariada.aws.sam;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ExtensionAware;
import org.slf4j.Logger;

import com.github.kaklakariada.aws.sam.config.SamConfig;
import com.github.kaklakariada.aws.sam.config.Stage;

public class AwsSamDeployPlugin implements Plugin<Project> {
	private static final Logger LOG = Logging.getLogger(AwsSamDeployPlugin.class);

	@Override
	public void apply(Project project) {
		LOG.info("Initialize AwsSam plugin");

		final NamedDomainObjectContainer<Stage> stages = project.container(Stage.class);

		final SamConfig samConfig = project.getExtensions().create("sam", SamConfig.class, stages);
		((ExtensionAware) samConfig).getExtensions().add("stages", stages);

	}
}
