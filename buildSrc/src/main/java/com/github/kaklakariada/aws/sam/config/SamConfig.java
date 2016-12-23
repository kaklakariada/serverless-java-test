package com.github.kaklakariada.aws.sam.config;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

import com.amazonaws.regions.Regions;

import groovy.lang.Closure;

public class SamConfig {
	private final NamedDomainObjectContainer<Stage> stages;
	private final long deploymentTimestamp;
	private final Project projct;

	public String currentStage;
	public ApiConfig api;

	public String defaultRegion;
	public String defaultDeploymentBucket;

	public SamConfig(Project projct, NamedDomainObjectContainer<Stage> stages) {
		this.projct = projct;
		this.stages = stages;
		this.deploymentTimestamp = System.currentTimeMillis();
	}

	public void api(Closure<?> config) {
		api = (ApiConfig) projct.configure(new ApiConfig(), config);
	}

	public Project getProjct() {
		return projct;
	}

	private Stage getStage() {
		return stages.getByName(currentStage);
	}

	public Regions getRegion() {
		final String stageRegion = getStage().region;
		return Regions.fromName(stageRegion != null ? stageRegion : defaultRegion);
	}

	public String getDeploymentBucket() {
		final String stageBucket = getStage().deploymentBucket;
		return stageBucket != null ? stageBucket : defaultDeploymentBucket;
	}

	public long getDeploymentTimestamp() {
		return deploymentTimestamp;
	}

	@Override
	public String toString() {
		return "SamConfig [stages=" + stages + ", currentStage=" + currentStage + ", api=" + api + "]";
	}
}
