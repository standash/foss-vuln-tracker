package it.unitn.molerat.evidence;

public class GenericEvidence {

    protected final String commit;
    protected final String container;
    protected String path;

    protected GenericEvidence(String file, String commit, String container) {
        this.path = file;
        this.commit = commit;
        this.container = container;
    }

    public String getContainer() {
        return this.container;
    }

    public String getCommit() {
        return this.commit;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
		this.path = path;
	}
}
