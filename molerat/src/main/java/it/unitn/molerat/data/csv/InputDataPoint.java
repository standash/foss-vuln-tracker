package it.unitn.molerat.data.csv;

public final class InputDataPoint extends AbstractDataPoint {

    public String PROJECTID;
	public String CVEID;
	public String REPO_TYPE;
	public String REPO_ROOT;
	public String FIX_REV; 
	public String TRACKER_TYPE;
	
	public InputDataPoint(String[] entries) throws Exception  {
		super(entries);
	}

	public InputDataPoint(String str) throws Exception {
		super(str);
	}

	public InputDataPoint() {

	}
}