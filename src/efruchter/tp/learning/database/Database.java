package efruchter.tp.learning.database;

import efruchter.tp.learning.GeneVector;

public interface Database {
	
	void init();
	
	boolean storeVector(final SessionInfo userInfo, final GeneVector vector);
	
	public static class SessionInfo {
		public final String username, score, date;
		
		public SessionInfo(final String username, final String score, final String date) {
			this.username = username;
			this.score = score;
			this.date = date;
		}
		
	}
}
