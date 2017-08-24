package it.unitn.molerat.repos.wrappers;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unitn.molerat.evidence.Changes;

public abstract class RepoWrapper {

	protected String diffFilePrefix;

    protected final Pattern hunkRegexPattern = Pattern.compile("@@[ ]*\\-([0-9]+)[\\,0-9 ]+\\+([0-9]+)[\\,0-9 ]+@@");

	protected final Pattern commentsPattern = Pattern.compile("(\\*|\\/\\*|\\*\\/|\\/\\/)(.*)");

	public abstract String doDiff(String leftRev, String rightRev) throws Exception;

	public abstract String doDiff(String path, String leftRev, String rightRev) throws Exception;

	public abstract void annotate(String path, String rev, Object callback) throws Exception;

	public abstract String doCat(String path, String rev) throws Exception;
	
	public abstract String getBasePath();
	
	public abstract Set<String> getRevisionFiles(String rev, String filter) throws Exception;

	public abstract Set<String> getRevisionNumbers(String topRev) throws Exception;
	
	public abstract Map<Integer, String> determineOriginatingRevision(final String filePath, final String revision, final Map<Integer, String> lines) throws Exception;
	
	public Set<Changes> inferChangesFromDiff(String diffText, String leftRev, String rightRev) throws IOException {
		Set<Changes> relevantChanges = new LinkedHashSet<>();

		Changes newRelevantChanges = null;
		int deletionRange = -1;
		int additionRange = -1;
		boolean additionRangeNotSet = true;
		boolean deletionRangeNotSet = true;
				
		String[] diffLines = diffText.split(System.getProperty("line.separator"));
		//for (String line : diffLines) {
		for (int i=0; i<diffLines.length; i++) {
			String line = diffLines[i];
			if (line.startsWith(this.diffFilePrefix)) {
				line = line.replaceAll(this.diffFilePrefix, "");
				// do some additional filename filtering ----------
				StringBuilder filename = new StringBuilder(); 
				for (int j=0; j<line.length(); j++) {
					char c = line.charAt(j);
					if (String.valueOf(c).equals(" ")) {
						break;
					}
					filename.append(c);
				}
				//--------------------------------------------------
				// Here we check if a file was simply renamed...
				//--------------------------------------------------
				String renameTo = "";
				if (diffLines[i+1].startsWith("similarity index 100%")) {
					renameTo = diffLines[i+3].replace("rename to ", "");
				}
				//--------------------------------------------------
				if (!renameTo.equals("")) {
					newRelevantChanges = new Changes(filename.toString(), "/"+renameTo, leftRev, rightRev);
				}
				else {
					newRelevantChanges = new Changes(filename.toString(), leftRev, rightRev);
				}
				relevantChanges.add(newRelevantChanges);
			}
			else if (newRelevantChanges != null && line.startsWith("@@") && line.endsWith("@@")) {
				Matcher hunkMatcher = this.hunkRegexPattern.matcher(line);
				hunkMatcher.matches();
				deletionRange = Integer.parseInt(hunkMatcher.group(1));
				additionRange = Integer.parseInt(hunkMatcher.group(2));
				hunkMatcher.reset();
			}
			else if (newRelevantChanges != null) {
				if (line.startsWith("-") && !line.startsWith("---")) {
                    line = line.substring(1).trim().replace("\t","");
					newRelevantChanges.putDeletedLine(deletionRange, line);
					if (deletionRangeNotSet) {
						deletionRangeNotSet = false;
					}
					deletionRange++;
					continue;
				}
				else if (line.startsWith("+") && !line.startsWith("+++")) {
                    line = line.substring(1).trim().replace("\t","");
                    newRelevantChanges.putAddedLine(additionRange, line);
					if (additionRangeNotSet) {
						additionRangeNotSet = false;
					}
					additionRange++;
					continue;
				}
				else {
					deletionRange++;
					additionRange++;
				}
			}
		}

		return relevantChanges;
	}
	
	public int countNumberOfHunksInDiff(String diffText) {
		int numberOfHunks = 0;
		String[] diffLines = diffText.split(System.getProperty("line.separator"));
		for (String line : diffLines) {
			if (line.startsWith("@@") && line.endsWith("@@")) {
				numberOfHunks++;
			}
		}
		return numberOfHunks;
	}
	
	
	public void filterCommentsAndBlanks(Map<Integer, String> lines) {
		Iterator<Map.Entry<Integer,String>> it = lines.entrySet().iterator();
		while (it.hasNext()) {
			String line = it.next().getValue();
			Matcher commentMatcher = this.commentsPattern.matcher(line.trim());
			if (line.trim().isEmpty() || commentMatcher.matches()) {
				it.remove();
			}
		}
	}
	
	public Map<Integer, String> getLineMappings(final String fileContents) {
		Map<Integer, String> lineMappings = new TreeMap<Integer, String>();
		String[] lines = fileContents.split(System.getProperty("line.separator"));
		int lineNumber = 1;
		for (String line : lines) {
            line = line.trim().replace("\t","");
			lineMappings.put(lineNumber++, line);
		}
		return lineMappings;
	}
	
	public int getNumberOfLoc(final String fileContents) {
		return this.getLineMappings(fileContents).size();
	}
	
	public int getNumberOfLocFiltered(final String fileContents) {
		Map<Integer, String> locs = this.getLineMappings(fileContents);
		this.filterCommentsAndBlanks(locs);
		return locs.size();
	}

    protected abstract String getReleaseTag(String release) throws Exception;
    public abstract String getReleaseCommit(String release) throws Exception;
    public abstract Set<String> getAllRepositoryTransactions() throws Exception;
}
