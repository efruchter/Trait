package efruchter.tp.learning.database;

import efruchter.tp.learning.SessionInfo;


public interface Database {

    void init();

    boolean storeVector(final SessionInfo sessionInfo);
}
