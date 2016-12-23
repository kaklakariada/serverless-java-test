package com.github.kaklakariada.aws.sam;

import static java.util.Collections.singletonMap;

import java.util.function.Consumer;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.CopySpec;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.bundling.Zip;
import org.slf4j.Logger;

import com.github.kaklakariada.aws.sam.config.SamConfig;
import com.github.kaklakariada.aws.sam.config.Stage;
import com.github.kaklakariada.aws.sam.tasks.S3UploadTask;

import groovy.lang.Closure;

public class AwsSamDeployPlugin implements Plugin<Project> {
	private static final Logger LOG = Logging.getLogger(AwsSamDeployPlugin.class);
	private static final String TASK_GROUP = "deploy";

	@Override
	public void apply(Project project) {
		LOG.info("Initialize AwsSam plugin...");

		final SamConfig config = createConfigDsl(project);

		final Zip zipTask = createBuildZipTask(project);

		createUploadZipTask(config, zipTask);
	}

	private S3UploadTask createUploadZipTask(final SamConfig config, final Zip zipTask) {
		final S3UploadTask task = (S3UploadTask) config.getProjct().task(singletonMap("type", S3UploadTask.class),
				"uploadZip");
		task.setDescription("Upload lambda zip to s3");
		task.setGroup(TASK_GROUP);
		task.dependsOn(zipTask);
		task.config = config;
		task.file = zipTask.getOutputs().getFiles().getSingleFile();
		return task;
	}

	private Zip createBuildZipTask(Project project) {
		final Zip task = (Zip) project.task(singletonMap("type", Zip.class), "buildZip");
		task.setDescription("Build lambda zip");
		task.setGroup(TASK_GROUP);
		task.setBaseName(project.getName());
		task.setVersion("");
		task.into("lib", closure(task, CopySpec.class, (delegate) -> {
			delegate.from(project.getConfigurations().getByName("runtime"));
		}));
		task.into("", closure(task, CopySpec.class, (delegate) -> {
			delegate.from(project.getTasks().getByPath(":compileJava"),
					project.getTasks().getByPath(":processResources"));
		}));
		return task;

	}

	private SamConfig createConfigDsl(Project project) {
		final NamedDomainObjectContainer<Stage> stages = project.container(Stage.class);
		final SamConfig samConfig = project.getExtensions().create("sam", SamConfig.class, project, stages);
		((ExtensionAware) samConfig).getExtensions().add("stages", stages);
		return samConfig;
	}

	private <T> Closure<Void> closure(Object thisObject, Class<T> delegateType, Consumer<T> code) {
		return new Closure<Void>(this, thisObject) {
			private static final long serialVersionUID = 1L;

			@Override
			public Void call() {
				final T delegate = delegateType.cast(getDelegate());
				code.accept(delegate);
				return null;
			}
		};
	}
}
