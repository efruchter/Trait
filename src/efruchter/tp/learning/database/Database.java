package efruchter.tp.learning.database;

import java.util.HashMap;


public interface Database {

    void init();

    boolean storeVector(final SessionInfo sessionInfo);

    /**
     * Struct for storing the necessary info.
     * @author toriscope
     *
     */
    public static class SessionInfo extends HashMap<String, String> {

    	public static String SEPERATOR = "THAWKISBEST";
    	
    	public SessionInfo() {
    		
    	}
    	
    	public SessionInfo(String data) {
    		fromDataString(data);
    	}
    	
		public String toDataString() {
			StringBuffer s = new StringBuffer();
			for (java.util.Map.Entry<String, String> entry : this.entrySet()) {
				s.append(SEPERATOR).append(entry.getKey()).append(SEPERATOR).append(entry.getValue());
			}
			return s.toString().replaceFirst(SEPERATOR, "");
		}
		
		public void fromDataString(String data) {
			clear();
			String[] d = data.split(SEPERATOR);
			for (int i = 0; i < d.length; i+=2) {
				put(d[i], d[i+1]);
			}
		}
    }
}
