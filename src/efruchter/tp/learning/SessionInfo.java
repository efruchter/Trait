package efruchter.tp.learning;

import java.util.HashMap;

/**
 * Struct for storing the necessary info to shuttle to the database.
 * 
 * @author toriscope
 * 
 */
public class SessionInfo extends HashMap<String, String> {

	private static final long serialVersionUID = 1L;
	public static String SEPERATOR = "THAWKISBEST";

	public SessionInfo() {

	}

	public SessionInfo(String data) {
		fromDataString(data);
	}

	public String toDataString() {
		StringBuffer s = new StringBuffer();
		for (java.util.Map.Entry<String, String> entry : this.entrySet()) {
			s.append(SEPERATOR).append(entry.getKey()).append(SEPERATOR)
					.append(entry.getValue());
		}
		return s.toString().replaceFirst(SEPERATOR, "");
	}

	public void fromDataString(String data) {
		clear();
		String[] d = data.split(SEPERATOR);
		for (int i = 0; i < d.length; i += 2) {
			put(d[i], d[i + 1]);
		}
	}
}
