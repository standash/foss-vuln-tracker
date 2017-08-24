package it.unitn.molerat.data.memory;

import it.unitn.molerat.evidence.ChangeEvidence;
import it.unitn.molerat.evidence.VulnerabilityEvidence;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class AnalysisEntry {
    private final String cveId;
    private final String projectId;
    private final String fixCommit;
    private final String repositoryType;
    private final String repositoryPath;
    private final Set<String> commits;
    private final Map<String, Set<VulnerabilityEvidence>> vulnEvidences;
    private final Map<String, Set<ChangeEvidence>> changEvidences;

    public AnalysisEntry(String cveId, String projectId, String repositoryType, String repositoryPath,
                         String fixCommit, Set<String> commits,
                         Map<String, Set<VulnerabilityEvidence>> vulnEvidences,
                         Map<String, Set<ChangeEvidence>> changEvidences) {
        this.cveId = cveId;
        this.projectId = projectId;
        this.repositoryType = repositoryType;
        this.repositoryPath = repositoryPath;
        this.commits = commits;
        this.fixCommit = fixCommit;
        this.vulnEvidences = vulnEvidences;
        this.changEvidences = changEvidences;
    }

    public String getCveName() {
        return cveId;
    }

    public Set<String> getCommits() {
        return commits;
    }

    public String getFixCommit() {
        return fixCommit;
    }

    public String getRepositoryType() {
        return repositoryType;
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public Map<String, Set<VulnerabilityEvidence>> getVulnEvidences() {
        return vulnEvidences;
    }

    public Set<VulnerabilityEvidence> getVulnEvidencesSet() {
        Set<VulnerabilityEvidence> evidences = new LinkedHashSet<>();
        for (Set<VulnerabilityEvidence> eset : vulnEvidences.values()) {
            evidences.addAll(eset);
        }
        return evidences;
    }

    public Map<String, Set<ChangeEvidence>> getChangEvidences() {
        return changEvidences;
    }

    public String getProjectName() {
        return projectId;
    }

}
