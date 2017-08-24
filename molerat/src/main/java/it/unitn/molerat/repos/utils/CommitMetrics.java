package it.unitn.molerat.repos.utils;

import it.unitn.molerat.evidence.Changes;
import it.unitn.molerat.repos.wrappers.RepoWrapper;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommitMetrics {

    private static final Pattern deletedPublicMethodPattern = Pattern.compile("public .*\\([^\\)]*\\)[^;]*$");

    public static int getGlobalPublicMethodsRemoved(String leftRev, String rightRev, RepoWrapper wrapper) throws Exception {
        int removed = 0;
        String diff = wrapper.doDiff(leftRev, rightRev);
        Set<Changes> changes = wrapper.inferChangesFromDiff(diff, leftRev, rightRev);
        for (Changes change : changes) {
            if (!change.getPath().endsWith(".java")) {
                continue;
            }
            Map<Integer, String> deletions = change.getDeletions();
            wrapper.filterCommentsAndBlanks(deletions);
            for (String del : deletions.values()) {
                del = StringUtils.trim(del);
                Matcher matcher = deletedPublicMethodPattern.matcher(del);
                if (matcher.matches()) {
                    removed++;
                }
                matcher.reset();
            }
        }
        return removed;
    }

    public static int getNumberOfPublicMethodsPerRevision(String rev, RepoWrapper wrapper) throws Exception {
        int pubAPICount = 0;
        Set<String>	files = wrapper.getRevisionFiles(rev, ".java");
        for (String f : files) {
            String fc = wrapper.doCat(f, rev);
            pubAPICount += countPublicMethodDeclarationsInFile(fc);
        }
        return pubAPICount;
    }

    public static Map<String, String> getFileContentsPerRevision(String rev, RepoWrapper wrapper) throws Exception {
       Map<String,String> result = new TreeMap<>();
       Set<String> files = wrapper.getRevisionFiles(rev, ".java");
        for (String file : files) {
            String contents = wrapper.doCat(file, rev);
            result.put(file, contents);
        }
        return result;
    }

    @Deprecated
    public static int countPublicMethodDeclarationsInFile(String fileContents) {
        int pubAPICount = 0;
        String[] lines = StringUtils.split(fileContents, System.getProperty("line.separator"));
        for (int i=0; i<lines.length; i++) {
            String line = StringUtils.replace(lines[i], "\t", " ");
            line = StringUtils.trim(line);
            if (StringUtils.startsWith(line, "public ")) {
                int indexOfPublic = StringUtils.indexOf(line, "public");
                int firstOpenBracket = StringUtils.indexOf(line, '(');
                int lastOpenBracket = StringUtils.indexOf(line, '(');
                int firstCloseBracket = StringUtils.indexOf(line, ')');
                int lastCloseBracket = StringUtils.indexOf(line, ')');

                if ((indexOfPublic < firstOpenBracket) && (firstOpenBracket == lastOpenBracket) &&
                        (firstCloseBracket == lastCloseBracket) && (firstOpenBracket < firstCloseBracket)) {
                    int semicolonIndex = StringUtils.indexOf(line, ';');
                    if (semicolonIndex == -1) {
                        StringBuilder chunk = new StringBuilder();
                        String nextLine = "";
                        for (int j=i; j<lines.length; j++) {
                            nextLine = StringUtils.replace(lines[j], "\t", " ");
                            nextLine = StringUtils.trim(nextLine);
                            if (StringUtils.indexOf(nextLine, ';') != -1) {
                                break;
                            }
                            else {
                                chunk.append(nextLine);
                            }
                        }
                        if (StringUtils.contains(chunk.toString(), '{')) {
                            pubAPICount++;
                        }
                    }
                }
            }
        }
        return pubAPICount;
    }
}
