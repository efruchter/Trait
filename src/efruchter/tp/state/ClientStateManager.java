package efruchter.tp.state;

public class ClientStateManager {

    private static boolean isPaused = false;
    private static FlowState flowState = FlowState.INIT;

    public static enum PauseState {
        PAUSED, UNPAUSED
    }

    public static enum FlowState {
        INIT("Initializing"), EDITING("Editing Vector"), LOADING_VECT("Loading Vector"), STORING_VECT("Storing Vector"), PLAYING("Playing"), FREE("Ready"), FETCHING_ID("Fetching unique ID");
        
        private final String name;
        
        private FlowState(final String s) {
            name = s;
        }
        
        public String toString() {
            return name;
        }
    }
    
    public static void togglePauseState() {
        isPaused = !isPaused;
    }

    public static boolean isPaused() {
        return isPaused;
    }

    public static void setPaused(final boolean pauseState) {
        isPaused = pauseState;
    }

    public static FlowState getFlowState() {
        return flowState;
    }

    public static void setFlowState(final FlowState flowState) {
        ClientStateManager.flowState = flowState;
    }
}
