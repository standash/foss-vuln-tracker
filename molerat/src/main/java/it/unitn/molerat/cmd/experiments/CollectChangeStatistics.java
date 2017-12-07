package it.unitn.molerat.cmd.experiments;

import it.unitn.molerat.data.csv.ChangeStatsDataPoint;
import it.unitn.molerat.data.csv.InputDataPoint;
import it.unitn.molerat.repos.trackers.vuln.FixStatisticsVulnerabilityEvidenceTracker;
import it.unitn.molerat.repos.trackers.vuln.VulnerabilityEvidenceTrackerFactory;
import it.unitn.molerat.repos.wrappers.GitRepoWrapper;
import it.unitn.molerat.repos.wrappers.RepoWrapper;

import java.util.Iterator;
import java.util.Set;

public class CollectChangeStatistics extends AbstractExperiment {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("USAGE: [input.csv] [output.csv]");
            return;
        }
        String inputCsvPath = args[0];
        String outputCsvPath = args[1];

        try {
            saveOutput(outputCsvPath, new ChangeStatsDataPoint().getHeader());
            Set<InputDataPoint> inputs = readInputDataPoints(inputCsvPath);
            for (InputDataPoint ip : inputs) {
                System.out.format("Processing %s project\n", ip.PROJECTID);
                long timeNow = System.currentTimeMillis();

                RepoWrapper repoWrapper = new GitRepoWrapper(ip.REPO_ROOT);
                Set<String> commits = repoWrapper.getAllRepositoryTransactions();
                Iterator<String> it = commits.iterator();
                while (it.hasNext()) {
                    String commit = it.next();
                    if (!it.hasNext()) {
                        break;
                    }
                    collect(outputCsvPath,
                            ip.PROJECTID,
                            ip.REPO_TYPE,
                            ip.REPO_ROOT,
                            commit);
                }

                long timeAfter = System.currentTimeMillis();
                System.out.println("TIME (ms): " + (timeAfter - timeNow));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private static void collect(String outputCsvPath, String project, String repo_type, String repo_root, String rev) {
        try {
            FixStatisticsVulnerabilityEvidenceTracker tracker = (FixStatisticsVulnerabilityEvidenceTracker)
                    VulnerabilityEvidenceTrackerFactory.getTracker(
                            repo_root,
                            rev,
                            repo_type,
                            "fixstatisticsvulnerabilityevidencetracker"
                    );
            tracker.trackEvidence();

            // if no Java files were modified, we don't care
            if (tracker.getNumberOfTouchedFiles() == 0) {
                return;
            }
            // else -> save the output
            String[] output = new String[] {
                    project,
                    rev,
                    String.valueOf(tracker.getNumberOfAddedLines()),
                    String.valueOf(tracker.getNumberOfDeletedLines()),
                    String.valueOf(tracker.getNumberOfTouchedMethods()),
                    String.valueOf(tracker.getNumberOfTouchedFiles())
            };
            ChangeStatsDataPoint odp = new ChangeStatsDataPoint(output);
            saveOutput(outputCsvPath, odp.toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

