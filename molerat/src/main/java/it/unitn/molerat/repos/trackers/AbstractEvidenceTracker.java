package it.unitn.molerat.repos.trackers;

import it.unitn.molerat.evidence.Changes;
import it.unitn.molerat.repos.wrappers.RepoWrapper;
import java.util.*;

public abstract class AbstractEvidenceTracker {
    protected final String fixedRevision;
    protected final String vulnRevision;
    protected final String repoPath;
    protected final RepoWrapper repoWrapper;
    protected Set<Changes> changes;
    protected Set<String> commits = new LinkedHashSet<>();
    protected Set<String> restOfCommits = new LinkedHashSet<>();
    protected boolean ignoreChangesToTests;

    public AbstractEvidenceTracker(RepoWrapper wrapper, String fixedRev, boolean ignoreChangesToTests) throws Exception {
        this.ignoreChangesToTests = ignoreChangesToTests;
        this.fixedRevision = fixedRev;
        this.repoPath = wrapper.getBasePath();
        this.repoWrapper = wrapper;
        Iterator<String> commitsIter = repoWrapper.getRevisionNumbers(fixedRev).iterator();
        this.vulnRevision = commitsIter.next();
        while (commitsIter.hasNext()) {
            restOfCommits.add(commitsIter.next());
        }
        String initDiff = repoWrapper.doDiff(vulnRevision, fixedRevision);
        this.changes = repoWrapper.inferChangesFromDiff(initDiff, vulnRevision, fixedRevision);
    }

    public AbstractEvidenceTracker(RepoWrapper wrapper, String fixedRev) throws Exception {
        this(wrapper, fixedRev, true);
    }

    public RepoWrapper getRepoWrapper() {
        return this.repoWrapper;
    }

    public String getFixedRevision() {
        return this.fixedRevision;
    }

    public Set<String> getProcessedCommits() {
        return this.commits;
    }

    // apart from non-java files, filter the files that contain "test" in ther name as well
    protected Set<Changes> filterNonJavaChanges(Set<Changes> changes) {
        Set<Changes> filteredChanges = new HashSet<>();
        for (Changes change : changes) {
            // ignore non-Java files
            if (!change.getPath().endsWith(".java")) {
                filteredChanges.add(change);
            }
            else {
                // ignore tests if the corresponding setting is set
                if (ignoreChangesToTests && change.getPath().toLowerCase().contains("test")) {
                    filteredChanges.add(change);
                }
            }
            this.repoWrapper.filterCommentsAndBlanks(change.getDeletions());
            this.repoWrapper.filterCommentsAndBlanks(change.getAdditions());
        }
        changes.removeAll(filteredChanges);
        return changes;
    }

    public abstract void trackEvidence() throws Exception;
}
