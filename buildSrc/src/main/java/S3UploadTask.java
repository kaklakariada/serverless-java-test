
import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

public class S3UploadTask extends DefaultTask {

	@Input
	public String bucket;

	@Input
	public String region;

	@InputFile
	public File file;

	private final long timestamp = System.currentTimeMillis();

	@TaskAction
	public void uploadFileToS3() {
		final AmazonS3 s3Client = new AmazonS3Client().withRegion(Regions.fromName(region));
		upload(s3Client, getS3Key());
	}

	public String getS3Url() {
		return "s3://" + bucket + "/" + getS3Key();
	}

	private String getS3Key() {
		final String version = getProject().getVersion().toString();
		return getProject().getName() + "/" + version + "/" + timestamp + "-" + file.getName();
	}

	private void upload(final AmazonS3 s3Client, final String key) {
		if (!s3Client.doesObjectExist(bucket, key)) {
			transferFileToS3(s3Client, key);
		}
	}

	private void transferFileToS3(final AmazonS3 s3Client, final String key) {
		getProject().getLogger().info("Uploading file {} as {} to bucket {}", file, key, bucket);
		final TransferManager transferManager = new TransferManager(s3Client);
		final Upload upload = transferManager.upload(bucket, key, file);
		try {
			upload.waitForCompletion();
			getProject().getLogger().info("Upload complete");
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AssertionError("Upload interrupted", e);
		}
	}
}
