package it.unitn.molerat.evidence;

import java.util.Map;
import java.util.TreeMap;

public class Changes {

    private final String path;
    private final String leftRev;
    private final String rightRev;
    private final Map<Integer, String> deletions;
    private final Map<Integer, String> additions;
	private String renamedTo = "";


	public Changes(String path, String leftRev, String rightRev) {
		this.path = path;
		this.leftRev = leftRev;
		this.rightRev = rightRev;
		this.deletions = new TreeMap<>();
		this.additions = new TreeMap<>();
	}

	public Changes(String path, String renamedTo, String leftRev, String rightRev) {
		this(path, leftRev, rightRev);
		this.renamedTo = renamedTo;
	}

	public Changes(Changes changes, String leftRev, String rightRev) {
        this.path = changes.path;
        this.leftRev = leftRev;
        this.rightRev = rightRev;
        this.deletions = changes.deletions;
        this.additions = changes.additions;
    }

    public String getRenamedTo() {
		return renamedTo;
	}

	public boolean wasRenamed() {
		return !renamedTo.equals("");
	}

    public String getPath() {
        return this.path;
    }

    public String getLeftRevision() {
        return this.leftRev;
    }

    public String getRightRevision() {
        return this.rightRev;
    }

    public void putDeletedLine(int lineNumber, String line) {
        this.deletions.put(lineNumber, line);
    }

    public void putAddedLine(int lineNumber, String line) {
        this.additions.put(lineNumber, line);
    }

    public Map<Integer, String> getDeletions() {
        return this.deletions;
    }

    public Map<Integer, String> getAdditions() {
        return this.additions;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CHANGES:\n");
        for (Map.Entry<Integer, String> entry : this.deletions.entrySet()) {
            builder.append("-, " + this.formatAsCSV(entry) + "\n");
        }
        for (Map.Entry<Integer, String> entry : this.additions.entrySet()) {
            builder.append("+, " + this.formatAsCSV(entry) + "\n");
        }
       return builder.toString();
    }

    private String formatAsCSV(Map.Entry<Integer, String> entry) {
        return  this.leftRev + ", " +
                this.path + ", " +
                entry.getKey() + ", " +
                entry.getValue();
    }

}
