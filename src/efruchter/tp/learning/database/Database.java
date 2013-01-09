package efruchter.tp.learning.database;


public interface Database {

    void init();

    boolean storeVector(final SessionInfo sessionInfo);

    public static class SessionInfo {
        public final String username, score, date, vector;

        public SessionInfo(final String username, final String score, final String date, final String vector) {
            this.username = username;
            this.score = score;
            this.date = date;
            this.vector = vector;
        }

    }
}
