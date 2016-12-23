package com.github.kaklakariada.aws.sam.config;

import org.gradle.api.NamedDomainObjectContainer;

public class SamConfig {
	private final NamedDomainObjectContainer<Stage> stages;
	public String currentStage;

	public SamConfig(NamedDomainObjectContainer<Stage> stages) {
		this.stages = stages;
	}

	public Stage getStage() {
		return stages.getByName(currentStage);
	}
}
