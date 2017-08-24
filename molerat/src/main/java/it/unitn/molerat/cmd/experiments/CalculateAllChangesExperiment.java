package it.unitn.molerat.cmd.experiments;

import it.unitn.molerat.data.csv.InputDataPoint;
import it.unitn.molerat.data.csv.OutputDataPoint;
import it.unitn.molerat.data.csv.PerfOutputDataPoint;
import it.unitn.molerat.repos.utils.CommitMetrics;
import it.unitn.molerat.repos.wrappers.GitRepoWrapper;
import it.unitn.molerat.repos.wrappers.RepoWrapper;

import java.util.Set;

public class CalculateAllChangesExperiment extends AbstractExperiment {
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
				long timeNow = System.currentTimeMillis();

				RepoWrapper wrapper = new GitRepoWrapper(idp.REPO_ROOT);
				Set<String> processedCommits = wrapper.getRevisionNumbers(idp.FIX_REV);
				int timestamp = 0;
				for (String commit : processedCommits) {

					int publicAPICount = CommitMetrics.getNumberOfPublicMethodsPerRevision(commit, wrapper);
					int rem = CommitMetrics.getGlobalPublicMethodsRemoved(commit, idp.FIX_REV, wrapper);
					double currentUntouched = (((double) publicAPICount - (double) rem) / (double) publicAPICount) * 100.0;
					currentUntouched = Math.round(currentUntouched * 100.0) / 100.0;

					String[] output = new String[]{
							idp.CVEID,
							idp.TRACKER_TYPE,
							"empty",
							commit,
							String.valueOf(timestamp),
							"empty",
							String.valueOf(publicAPICount),
							String.valueOf(rem),
							String.valueOf(currentUntouched)
					};
					OutputDataPoint odp = new OutputDataPoint(output);
					saveOutput(outputCsvPath, odp.toString());
					timestamp--;
				}
				long timeAfter = System.currentTimeMillis();
				System.out.println("TIME (ms): " + (timeAfter - timeNow));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
