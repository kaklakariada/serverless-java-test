package com.github.kaklakariada.aws.sam.config;

public class Stage {
	private final String name;
	String region;
	String deploymentBucket;

	public Stage(String name) {
		this.name = name;
	}
}
