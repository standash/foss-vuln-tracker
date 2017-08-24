package it.unitn.molerat.data.csv;

public class VulnEvidenceDataPoint extends AbstractDataPoint  {

	public String PROJECT_NAME;
	public String CVEID;
	public String CURRENT_REV;
	public String TIMESTAMP;
	public String EVIDENCE_LOC;

	public VulnEvidenceDataPoint(String[] entries) throws Exception  {
		super(entries);
	}

	public VulnEvidenceDataPoint(String str) throws Exception {
		super(str);
	}

	public VulnEvidenceDataPoint() {

	}
}
