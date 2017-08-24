package it.unitn.molerat.data.csv;

import java.lang.reflect.Field;

public abstract class AbstractDataPoint {

	public AbstractDataPoint(String[] entries) throws Exception {
		Field[] fields = this.getClass().getFields();
		if (entries.length != fields.length) {
			throw new Exception("Bad entries for initializing a molerat.data point!");
		}
		int i = 0;
		for (Field field : fields) {
			if (!field.getName().equals("this$0")) {
				try {
					field.set(this, entries[i++]);
				} catch (IllegalArgumentException e) {
					System.out.println("ERROR: " + e.getMessage());
				} catch (IllegalAccessException e) {
					System.out.println("ERROR: " + e.getMessage());
				}
			}
		}
	}
	
	public AbstractDataPoint(String str) throws Exception {
		this(str.replace(" ", "").split(","));
	}
	
	public AbstractDataPoint() {

	}
	
	@Override
	public final String toString() {
		StringBuilder builder = new StringBuilder();
		Field[] fields = this.getClass().getFields();
		for (Field field : fields) {
			if (!field.getName().equals("this$0")) {
				try {
					builder.append(field.get(this) + ",");
				} catch (IllegalArgumentException e) {
					System.out.println("ERROR: " + e.getMessage());
				} catch (IllegalAccessException e) {
					System.out.println("ERROR: " + e.getMessage());
				}
			}
		}
		builder.deleteCharAt(builder.length()-1);
		return builder.toString();
	}
	
	public final String getHeader() {
		StringBuilder builder = new StringBuilder();
		Field[] fields = this.getClass().getFields();
		for (Field field : fields) {
			if (!field.getName().equals("this$0")) {
				try {
					builder.append(field.getName() + ",");
				} catch (IllegalArgumentException e) {
					System.out.println("ERROR: " + e.getMessage());
				}
			}
		}
		builder.deleteCharAt(builder.length()-1);
		return builder.toString();
	}
}