package it.unitn.molerat.data.csv;

public class PerfOutputDataPoint extends AbstractDataPoint {

    public String CVEID;
    public String TRACKING_METHOD;
    public String TIME_MS;

    public PerfOutputDataPoint(String[] entries) throws Exception  {
        super(entries);
    }

    public PerfOutputDataPoint(String str) throws Exception {
        super(str);
    }

    public PerfOutputDataPoint() {

	}

}
