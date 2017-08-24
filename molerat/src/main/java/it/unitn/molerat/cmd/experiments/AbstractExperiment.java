package it.unitn.molerat.cmd.experiments;

import it.unitn.molerat.data.csv.InputDataPoint;
import it.unitn.molerat.repos.utils.IORoutines;
import java.util.Set;

public class AbstractExperiment {

    protected static Set<InputDataPoint> readInputDataPoints(String path) throws Exception {
        return IORoutines.readInputDataPoints(path);
    }

    protected static void saveOutput(String path, String output) throws Exception {
        IORoutines.writeFile(path, output + "\n");
    }
}
