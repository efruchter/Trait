package efruchter.tp.learning.database;

import efruchter.tp.learning.GeneVector;

public interface Database {
	
	void init();
	
	boolean storeVector(SessionInfo userInfo, GeneVector vector);
	
	public static class SessionInfo {
		public final String username, score, date;
		
		public SessionInfo(String username, String score, String date) {
			this.username = username;
			this.score = score;
			this.date = date;
		}
		
	}
}
