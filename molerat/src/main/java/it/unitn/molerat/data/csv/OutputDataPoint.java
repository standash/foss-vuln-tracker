package it.unitn.molerat.data.csv;

public class OutputDataPoint extends AbstractDataPoint {
	
	public String CVEID;
	public String TRACKING_METHOD;
	public String CURRENT_FILE;
	public String CURRENT_REV;
	public String TIMESTAMP;
	public String EVIDENCE_LOC;
	//public String GLOBAL_PUBLIC_API_COUNT;
	//public String GLOBAL_PUBLIC_API_REMOVED;
	//public String GLOBAL_PUBLIC_API_UNTOUCHED;

	public OutputDataPoint(String[] entries) throws Exception  {
		super(entries);
	}

	public OutputDataPoint(String str) throws Exception {
		super(str);
	}

	public OutputDataPoint() {

	}
}
