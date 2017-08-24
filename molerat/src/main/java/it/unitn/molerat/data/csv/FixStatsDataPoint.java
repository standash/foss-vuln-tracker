package it.unitn.molerat.data.csv;

public class FixStatsDataPoint extends AbstractDataPoint {
	public String CVEID;
	public String ADDED_LINES;
	public String DELETED_LINES;
	public String TOUCHED_METHODS;
	public String TOUCHED_FILES;

	public FixStatsDataPoint(String[] entries) throws Exception {
		super(entries);
	}

	public FixStatsDataPoint(String str) throws Exception {
		super(str);
	}

	public FixStatsDataPoint() {

	}
}
