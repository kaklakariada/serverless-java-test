package com.github.kaklakariada.aws.sam.task;

import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.github.kaklakariada.aws.sam.config.SamConfig;

public class S3UploadTask extends DefaultTask {

	@InputFile
	public File file;

	@Input
	public SamConfig config;

	@TaskAction
	public void uploadFileToS3() {
		final AmazonS3 s3Client = new AmazonS3Client().withRegion(config.getRegion());
		upload(s3Client, getS3Key());
	}

	public String getS3Url() {
		return "s3://" + config.getDeploymentBucket() + "/" + getS3Key();
	}

	private String getS3Key() {
		final String version = getProject().getVersion().toString();
		return getProject().getName() + "/" + version + "/" + config.getDeploymentTimestamp() + "/" + file.getName();
	}

	private void upload(final AmazonS3 s3Client, final String key) {
		if (!s3Client.doesObjectExist(config.getDeploymentBucket(), key)) {
			transferFileToS3(s3Client, key);
		}
	}

	private void transferFileToS3(final AmazonS3 s3Client, final String key) {
		getProject().getLogger().info("Uploading file {} as {} to bucket {}", file, key, config.getDeploymentBucket());
		final TransferManager transferManager = new TransferManager(s3Client);
		final Upload upload = transferManager.upload(config.getDeploymentBucket(), key, file);
		try {
			upload.waitForCompletion();
			getProject().getLogger().info("Uploaded {} to {}", file, getS3Url());
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AssertionError("Upload interrupted", e);
		}
	}
}
