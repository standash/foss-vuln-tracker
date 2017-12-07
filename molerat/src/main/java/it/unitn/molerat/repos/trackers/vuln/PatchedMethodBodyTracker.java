package it.unitn.molerat.repos.trackers.vuln;

import it.unitn.molerat.evidence.Changes;
import it.unitn.molerat.evidence.VulnerabilityEvidence;
import it.unitn.molerat.repos.utils.SignatureExtractor;
import it.unitn.molerat.repos.wrappers.RepoWrapper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PatchedMethodBodyTracker extends VulnerabilityEvidenceTracker {

	public PatchedMethodBodyTracker(RepoWrapper wrapper, String fixedRev) throws Exception {
		super(wrapper, fixedRev, true);
	}

	public PatchedMethodBodyTracker(RepoWrapper wrapper, String fixedRev, boolean ignoreChangesToTests) throws Exception {
		super(wrapper, fixedRev, ignoreChangesToTests);
	}

	@Override
	protected Set<VulnerabilityEvidence> getInitialVulnerabilityEvidence(Changes changes) throws Exception {
		SignatureExtractor se = new SignatureExtractor(repoWrapper.doCat(changes.getPath(), changes.getRightRevision()));
		Map<String, Set<Integer>> rightSignatures = se.getSignaturesWithLines();

		Set<String> relevantSignatures = new HashSet<>();

		for (int line : changes.getAdditions().keySet()) {
			for (Map.Entry<String, Set<Integer>> entry : rightSignatures.entrySet()) {
				if (entry.getValue().contains(line)) {
					relevantSignatures.add(entry.getKey());
				}
			}
		}

		String leftFile = repoWrapper.doCat(changes.getPath(), changes.getLeftRevision());
		Map<Integer, String> lineMappings = repoWrapper.getLineMappings(leftFile);
		se = new SignatureExtractor(leftFile);
		Map<String, Set<Integer>> leftSignatures = se.getSignaturesWithLines();

		for (int line : changes.getDeletions().keySet()) {
			for (Map.Entry<String, Set<Integer>> entry : leftSignatures.entrySet()) {
				if (entry.getValue().contains(line)) {
					relevantSignatures.add(entry.getKey());
				}
			}
		}

		Set<VulnerabilityEvidence> initialEvidence = new HashSet<>();
		for (String signature : relevantSignatures) {
			for (Map.Entry<String, Set<Integer>> entry : leftSignatures.entrySet()) {
				if (signature.equals(entry.getKey())) {
					for (int line : entry.getValue()) {
						VulnerabilityEvidence evd = new VulnerabilityEvidence(
								changes.getPath(),
								changes.getLeftRevision(),
								signature,
								line,
								lineMappings.get(line)
						);
						initialEvidence.add(evd);
					}
				}
			}
		}

		return initialEvidence;
	}


	@Override
	protected Set<VulnerabilityEvidence> getVulnerabilityEvidence(String currentEvidenceCommit, String previousEvidenceCommit, Set<Changes> changes) throws Exception {
		Set<VulnerabilityEvidence> newEvidences = new HashSet<>();
		Set<VulnerabilityEvidence> previousEvidences = getEvidences(previousEvidenceCommit);

		Set<VulnerabilityEvidence> changedEvidence = new HashSet<>();
		Set<Changes> changesToProcess = new HashSet<>();
		Set<VulnerabilityEvidence> stillEvidence = new HashSet<>();

		// filter non-Java files
		changes = filterNonJavaChanges(changes);

		if (previousEvidences == null) {
			return newEvidences;
		}

		for (VulnerabilityEvidence previousEvidence : previousEvidences) {
			for (Changes change : changes )  {
				// The file has been just renamed
				if (change.wasRenamed()) {
					if (change.getRenamedTo().equals(previousEvidence.getPath())) {
						previousEvidence.setPath(change.getPath());
					}
				}
				// The file has been changed
				else if (previousEvidence.getPath().equals(change.getPath())) {
					changedEvidence.add(previousEvidence);
					changesToProcess.add(change);
				}
			}
		}

		stillEvidence.addAll(previousEvidences);
		stillEvidence.removeAll(changedEvidence);

		// "Refresh" the molerat.evidence when a file was not changed
		for (VulnerabilityEvidence e : stillEvidence) {
			VulnerabilityEvidence newEvidence = new VulnerabilityEvidence(
					e.getPath(),
					currentEvidenceCommit,
					e.getContainer(),
					e.getLineNumber(),
					e.getLineContents()
			);
			newEvidences.add(newEvidence);
		}

		// "Triage" the molerat.evidence when a file was changed
		for (Changes change : changesToProcess) {
			Set<String> relevantSignatures = new HashSet<>();
			for (VulnerabilityEvidence e : changedEvidence) {
				relevantSignatures.add(e.getContainer());
			}

			String leftFile = repoWrapper.doCat(change.getPath(), change.getLeftRevision());
			Map<Integer, String> lineMappings = repoWrapper.getLineMappings(leftFile);
			SignatureExtractor se = new SignatureExtractor(leftFile);
			Map<String, Set<Integer>> leftSignatures = se.getSignaturesWithLines();

			for (String signature : relevantSignatures) {
				for (Map.Entry<String, Set<Integer>> entry : leftSignatures.entrySet()) {
					if (signature.equals(entry.getKey())) {
						for (int line : entry.getValue()) {
							VulnerabilityEvidence evd = new VulnerabilityEvidence(
									change.getPath(),
									change.getLeftRevision(),
									signature,
									line,
									lineMappings.get(line)
							);
							newEvidences.add(evd);
						}
					}
				}
			}
		}
		return newEvidences;
	}


}
