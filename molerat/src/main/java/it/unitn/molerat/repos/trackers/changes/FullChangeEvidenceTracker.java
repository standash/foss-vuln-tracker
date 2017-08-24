package it.unitn.molerat.repos.trackers.changes;


import it.unitn.molerat.repos.wrappers.RepoWrapper;

public class FullChangeEvidenceTracker extends ChangeEvidenceTracker {

    public FullChangeEvidenceTracker(RepoWrapper wrapper, String fixedRev) throws Exception {
        super(wrapper, fixedRev);
        this.commits = wrapper.getRevisionNumbers(fixedRev);
        this.fixConstructs = extractSignatures(this.fixedRevision);
    }
}
