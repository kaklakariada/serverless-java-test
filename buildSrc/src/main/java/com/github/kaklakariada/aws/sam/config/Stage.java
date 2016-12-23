package com.github.kaklakariada.aws.sam.config;

public class Stage {
	private final String name;

	public Stage(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Stage [name=" + name + "]";
	}

}
