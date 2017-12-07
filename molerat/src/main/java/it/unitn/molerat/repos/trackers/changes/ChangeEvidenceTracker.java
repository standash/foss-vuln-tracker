package it.unitn.molerat.repos.trackers.changes;

import it.unitn.molerat.evidence.ChangeEvidence;
import it.unitn.molerat.repos.trackers.AbstractEvidenceTracker;
import it.unitn.molerat.repos.utils.CommitMetrics;
import it.unitn.molerat.repos.utils.SignatureExtractor;
import it.unitn.molerat.repos.wrappers.RepoWrapper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ChangeEvidenceTracker extends AbstractEvidenceTracker {

    protected final Map<String, Set<ChangeEvidence>> evidences = new TreeMap<>();
    protected Map<String, Set<String>> fixConstructs;

    protected ChangeEvidenceTracker(RepoWrapper wrapper, String fixedRev) throws Exception {
        super(wrapper, fixedRev);
    }

    public ChangeEvidenceTracker(RepoWrapper repoWrapper, String fixedCommit, Set<String> commits) throws Exception {
        this(repoWrapper, fixedCommit);
        this.commits = commits;
        this.fixConstructs = extractSignatures(this.fixedRevision);
    }

    protected Map<String, Set<String>> extractSignatures(String commit) throws Exception {
        Map<String, Set<String>> result = new TreeMap<>();
        Map<String, String> files = CommitMetrics.getFileContentsPerRevision(commit, this.repoWrapper);
        for (Map.Entry<String, String> file : files.entrySet()) {
            SignatureExtractor se = new SignatureExtractor(file.getValue());
            Set<String> signatures = new HashSet<>();
            for (String signature : se.getSignaturesWithLines().keySet()) {
                signatures.add(signature);
            }
            result.put(file.getKey(), signatures);
        }
        return result;
    }


    @Override
    public void trackEvidence() throws Exception {
        for (String commit : this.commits) {
            Map<String, Set<String>> currentSignatures = extractSignatures(commit);
            for (Map.Entry<String, Set<String>> currentSignature : currentSignatures.entrySet()) {
                Set<String> temp = this.fixConstructs.get(currentSignature.getKey());
                if (temp == null) {
                    for (String sign : currentSignature.getValue()) {
                        ChangeEvidence evidence = new ChangeEvidence(
                                currentSignature.getKey(),
                                commit,
                                sign,
                                true
                        );
                        if (evidence.isPublicMethodOrConstructor()) {
                            addEvidence(evidence);
                        }
                    }
                }
                else {
                    for (String sign : currentSignature.getValue()) {
                        boolean removed = true;
                        for (String fixSign : temp) {
                            if (sign.equals(fixSign)) {
                                removed = false;
                                break;
                            }
                        }
                        ChangeEvidence evidence = new ChangeEvidence(
                                currentSignature.getKey(),
                                commit,
                                sign,
                                removed
                        );
                        if (evidence.isPublicMethodOrConstructor()) {
                            addEvidence(evidence);
                        }
                    }
                }

            }
        }
    }

    protected void addEvidence(ChangeEvidence evidence) {
        if (this.evidences.containsKey(evidence.getCommit())) {
            this.evidences.get(evidence.getCommit()).add(evidence);
        }
        else {
            Set<ChangeEvidence> set = new HashSet<>();
            set.add(evidence);
            this.evidences.put(evidence.getCommit(), set);
        }
    }

    public Set<ChangeEvidence> getEvidence(String commit) {
        Set<ChangeEvidence> evd = this.evidences.get(commit);
        return (evd != null) ? evd : new HashSet<>();
    }

    public Map<String, Set<ChangeEvidence>> getEvidences() {
        return this.evidences;
    }
}
