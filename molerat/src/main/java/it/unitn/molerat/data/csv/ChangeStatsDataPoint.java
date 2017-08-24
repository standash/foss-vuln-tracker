package it.unitn.molerat.data.csv;

/**
 * Created by standash on 06/12/2016.
 */
public class ChangeStatsDataPoint extends AbstractDataPoint {
	public String PROJECT;
	public String CURRENT_COMMIT;
	public String ADDED_LINES;
	public String DELETED_LINES;
	public String TOUCHED_METHODS;
	public String TOUCHED_FILES;

	public ChangeStatsDataPoint(String[] entries) throws Exception {
		super(entries);
	}

	public ChangeStatsDataPoint(String str) throws Exception {
		super(str);
	}

	public ChangeStatsDataPoint() {

	}
}
