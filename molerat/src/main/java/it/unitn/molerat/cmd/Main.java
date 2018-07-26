package it.unitn.molerat.cmd;

import it.unitn.molerat.data.csv.InputDataPoint;
import it.unitn.molerat.data.csv.VulnEvidenceDataPoint;
import it.unitn.molerat.data.db.MongoWrapper;
import it.unitn.molerat.data.memory.AnalysisEntry;
import it.unitn.molerat.evidence.VulnerabilityEvidence;
import it.unitn.molerat.repos.trackers.vuln.VulnerabilityEvidenceTracker;
import it.unitn.molerat.repos.trackers.vuln.VulnerabilityEvidenceTrackerFactory;
import it.unitn.molerat.repos.utils.IORoutines;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.cli.*;
import org.bson.types.ObjectId;

public class Main {

    private static MongoWrapper db = new MongoWrapper("molerat");

    public static void main(String[] args) {
        Options opts = new Options();

        Option listTrackersOpt = Option.builder()
                .longOpt("list-trackers")
                .desc("List the available trackers for vulnerability vulnerability evidence")
                .build();
        opts.addOption(listTrackersOpt);

        Option projectNameOpt = Option.builder()
                .longOpt("project-name")
                .desc("The name of the project")
                .hasArg()
                .build();
        opts.addOption(projectNameOpt);

        Option repoTypeOpt = Option.builder()
                .longOpt("repo-type")
                .desc("The type of the source code repository ('git' or 'svn')")
                .hasArg()
                .build();
        opts.addOption(repoTypeOpt);

        Option repoPathOpt = Option.builder()
                .longOpt("repo-path")
                .desc("The path of the working copy of the source code repository")
                .hasArg()
                .build();
        opts.addOption(repoPathOpt);

        Option cveIdOpt = Option.builder()
                .longOpt("cve-id")
                .desc("The CVE identifier of a vulnerability")
                .hasArg()
                .build();
        opts.addOption(cveIdOpt);

        Option fixCommitOpt = Option.builder()
                .longOpt("fix-commit")
                .desc("The commit that fixed a vulnerability")
                .hasArg()
                .build();
        opts.addOption(fixCommitOpt);

        Option trackerTypeOpt = Option.builder()
                .longOpt("tracker-type")
                .desc("The type of the vulnerability evidence tracker")
                .hasArg()
                .build();
        opts.addOption(trackerTypeOpt);

        Option inputFileOpt = Option.builder()
                .longOpt("input-file")
                .desc("Path to the input .csv file")
                .hasArg()
                .argName("input-file-path")
                .build();
        opts.addOption(inputFileOpt);

        Option outputFileOpt = Option.builder()
                .longOpt("output-file")
                .desc("Path to the output .csv file")
                .hasArg()
                .argName("output-file-path")
                .build();
        opts.addOption(outputFileOpt);

        CommandLineParser cmdParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        CommandLine cmd;
        try {
            cmd = cmdParser.parse(opts, args);
            if (cmd.getOptions().length == 0) {
                throw new ParseException("Arguments are not specified");
            }

            if (cmd.hasOption("list-trackers")) {
                System.out.println(VulnerabilityEvidenceTrackerFactory.getTrackersList());
            }
            else if (cmd.hasOption("input-file")) {
                String i = cmd.getOptionValue("input-file");
                collectVulnEvidence(i);
            }
            else {
                String projectName = null;
                String repoType = null;
                String repoPath = null;
                String cveId = null;
                String fixCommit = null;
                String trackerType = null;

                if (cmd.hasOption("project-name")) {
                    projectName = cmd.getOptionValue("project-name");
                }
                if (cmd.hasOption("repo-type")) {
                    repoType = cmd.getOptionValue("repo-type");
                }
                if(cmd.hasOption("repo-path")) {
                    repoPath = cmd.getOptionValue("repo-path");
                }
                if(cmd.hasOption("cve-id")) {
                    cveId = cmd.getOptionValue("cve-id");
                }
                if(cmd.hasOption("fix-commit")) {
                    fixCommit = cmd.getOptionValue("fix-commit");
                }
                if(cmd.hasOption("tracker-type")) {
                    trackerType = cmd.getOptionValue("tracker-type");
                }
                if (projectName == null) {
                    throw new ParseException("The 'project-name' parameter is not specified");
                }
                if (repoType == null) {
                    throw new ParseException("The 'repo-type' parameter is not specified");
                }
                if (repoPath == null) {
                    throw new ParseException("The 'repo-path' parameter is not specified");
                }
                if (cveId == null) {
                    throw new ParseException("The 'cve-id' parameter is not specified");
                }
                if (fixCommit == null) {
                    throw new ParseException("The 'fix-commit' parameter is not specified");
                }
                if (trackerType == null) {
                    throw new ParseException("The 'tracker-type' parameter is not specified");
                }
                collect(projectName, repoType, repoPath, cveId, fixCommit, trackerType);

                if (cmd.hasOption("output-file")) {
                    String o = cmd.getOptionValue("output-file");
                    generateCsv(o);
                }
            }
        }
        catch (ParseException e) {
            System.out.println("ERROR: " + e.getMessage());
            helpFormatter.printHelp("java -jar molerat.jar", opts);
        }
    }

    private static void collectVulnEvidence(String inputCsvPath) {
        try {
            Set<InputDataPoint> inputs = IORoutines.readInputDataPoints(inputCsvPath);
            for (InputDataPoint ip : inputs) {
                collect(ip.PROJECTID,
                        ip.REPO_TYPE,
                        ip.REPO_ROOT,
                        ip.CVEID,
                        ip.FIX_REV,
                        ip.TRACKER_TYPE);
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private static void collect(String projectName, String repositoryType,
                                String repositoryPath, String cveName, String fixCommit, String trackerType)  {
        try {
            boolean entryExists = db.analysisEntryExists(projectName, cveName, repositoryType, repositoryPath);
            if (entryExists) {
                System.out.format("WARNING: The analysis entry for '%s' from '%s' already exists, skipping\n", cveName, projectName);
                return;
            }

            System.out.format("INFO: Collecting the vulnerability evidence for '%s' from '%s'\n", cveName, projectName);
            VulnerabilityEvidenceTracker vulnTracker = VulnerabilityEvidenceTrackerFactory.getTracker(
                    repositoryPath,
                    fixCommit,
                    repositoryType,
                    trackerType
            );
            vulnTracker.trackEvidence();

            AnalysisEntry entry = new AnalysisEntry(
                    cveName,
                    projectName,
                    repositoryType,
                    repositoryPath,
                    fixCommit,
                    vulnTracker.getProcessedCommits(),
                    vulnTracker.getEvidences(),
                    null // we're not tracking any changes so far
            );
            boolean isSuccessful = db.insertAnalysisEntry(entry);
            if (!isSuccessful) {
                System.out.format("WARNING: Could not get any vulnerability evidence for '%s' in '%s'\n", cveName, projectName);
            }
            System.out.println("INFO: Done!");
        }
        catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private static void generateCsv(String outFilePath) {
        System.out.format("INFO: generating the .csv file '%s'\n", outFilePath);
        String csvHeader = new VulnEvidenceDataPoint().getHeader() + "\n";
        try {
            IORoutines.writeFile(outFilePath, csvHeader);
            Set<ObjectId> cveIds = db.getAllCveIds();
            for (ObjectId cveId : cveIds) {
                AnalysisEntry analysis = db.getAnalysisEntry(cveId);
                if (analysis == null) {
                    continue;
                }
                String projectName = analysis.getProjectName();
                String cveName = analysis.getCveName();

                System.out.format("INFO: writing the entries for '%s'\n", cveName);
                Set<VulnerabilityEvidence> evidences = analysis.getVulnEvidencesSet();
                if (evidences.size() == 0) {
                    throw new Exception(String.format("There is no vulnerability evidence for '%s' in the database", cveName));
                }
                Map<String, Integer> locsCount = new LinkedHashMap<>();
                for (VulnerabilityEvidence evd : evidences) {
                    if (locsCount.containsKey(evd.getCommit())) {
                        int count = locsCount.get(evd.getCommit());
                        locsCount.remove(evd.getCommit());
                        locsCount.put(evd.getCommit(), ++count);
                    } else {
                        locsCount.put(evd.getCommit(), 1);
                    }
                }
                int timestamp = 0;
                for (Map.Entry<String, Integer> entry : locsCount.entrySet()) {
                    VulnEvidenceDataPoint dp = new VulnEvidenceDataPoint(new String[]{
                            projectName,
                            cveName,
                            entry.getKey(),
                            String.valueOf(timestamp--),
                            String.valueOf(entry.getValue())
                    });
                    IORoutines.writeFile(outFilePath, dp.toString() + "\n");
                }

            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}

