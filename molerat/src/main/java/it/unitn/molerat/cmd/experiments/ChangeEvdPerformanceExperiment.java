package it.unitn.molerat.cmd.experiments;

import it.unitn.molerat.data.csv.InputDataPoint;
import it.unitn.molerat.data.csv.PerfOutputDataPoint;
import it.unitn.molerat.repos.trackers.vuln.VulnerabilityEvidenceTracker;
import it.unitn.molerat.repos.trackers.vuln.VulnerabilityEvidenceTrackerFactory;
import it.unitn.molerat.repos.utils.CommitMetrics;
import java.util.Set;

public class ChangeEvdPerformanceExperiment extends AbstractExperiment {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("USAGE: [input.csv] [output.csv]");
            return;
        }
        String inputCsvPath = args[0];
        String outputCsvPath = args[1];

        try {
            Set<InputDataPoint> inputs = readInputDataPoints(inputCsvPath);
            saveOutput(outputCsvPath, new PerfOutputDataPoint().getHeader());
            for (InputDataPoint idp : inputs) {

                System.out.println("Processing the " + idp.CVEID + "...");

                VulnerabilityEvidenceTracker vulnTracker = VulnerabilityEvidenceTrackerFactory.getTracker(idp.REPO_ROOT, idp.FIX_REV, idp.REPO_TYPE, idp.TRACKER_TYPE);
                vulnTracker.trackEvidence();

                Set<String> processedCommits = vulnTracker.getProcessedCommits();
                for (String commit : processedCommits) {
                    long timeNow = System.currentTimeMillis();

                    int publicAPICount = CommitMetrics.getNumberOfPublicMethodsPerRevision(commit, vulnTracker.getRepoWrapper());
                    int rem = CommitMetrics.getGlobalPublicMethodsRemoved(commit, vulnTracker.getFixedRevision(), vulnTracker.getRepoWrapper());
                    double currentUntouched = (((double) publicAPICount - (double) rem) / (double) publicAPICount) * 100.0;
                    currentUntouched = Math.round(currentUntouched * 100.0) / 100.0;

                    long timeAfter = System.currentTimeMillis();
                    String[] output = new String[]{
                            idp.CVEID,
                            idp.TRACKER_TYPE,
                            String.valueOf(timeAfter - timeNow)
                    };
                    PerfOutputDataPoint odp = new PerfOutputDataPoint(output);
                    saveOutput(outputCsvPath, odp.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
