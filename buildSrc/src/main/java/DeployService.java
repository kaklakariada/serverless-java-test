import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.model.ChangeSetType;

public class DeployService {
	private final CloudformationService service;

	public DeployService(CloudformationService service) {
		this.service = service;
	}

	public DeployService(Regions region) {
		this(new CloudformationService(region));
	}

	public void deploy(String stackName, String templateBody) {
		final String changeSetName = stackName + "-" + System.currentTimeMillis();
		final ChangeSetType changeSetType = service.stackExists(stackName) ? ChangeSetType.UPDATE
				: ChangeSetType.CREATE;
		final String changeSetArn = service.createChangeSet(stackName, changeSetName, changeSetType, templateBody);
		service.waitForChangeSetReady(changeSetArn);
		service.executeChangeSet(changeSetArn);
		service.waitForStackReady(stackName);
	}
}
