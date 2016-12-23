package com.github.kaklakariada.aws.sam.config;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

import groovy.lang.Closure;

public class SamConfig {
	private final NamedDomainObjectContainer<Stage> stages;
	private final Project projct;
	public String currentStage;
	public ApiConfig api;

	public SamConfig(Project projct, NamedDomainObjectContainer<Stage> stages) {
		this.projct = projct;
		this.stages = stages;
	}

	public void api(Closure<?> config) {
		api = (ApiConfig) projct.configure(new ApiConfig(), config);
	}

	public Stage getStage() {
		return stages.getByName(currentStage);
	}

	@Override
	public String toString() {
		return "SamConfig [stages=" + stages + ", currentStage=" + currentStage + ", api=" + api + "]";
	}
}
