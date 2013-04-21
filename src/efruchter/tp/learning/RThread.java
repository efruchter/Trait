package efruchter.tp.learning;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import javax.swing.SwingWorker;

import efruchter.tp.state.ClientStateManager;

public class RThread extends SwingWorker<Integer, Integer> {
	private long waveCount;
	private long playerID;
	private String learnMode;
	
	
	public RThread(long playerID, long waveCount, String learnMode) {
		System.out.println("RThread input playerID = " + playerID);
		this.playerID = playerID;
		this.learnMode = learnMode;
		this.waveCount = waveCount;
		
		System.out.println("new RThread(pID = " + playerID + ", wave = " + waveCount + ", learnMode = " + learnMode + ")");
	}

	@Override
	public Integer doInBackground() {
		return runR();
	}
	
	private Integer runR() {
		int exitVal = -2;
		System.out.println("running SwingWorker R");
//    	ClientStateManager.togglePauseState();
    	try {
    		Runtime rt = Runtime.getRuntime();
    		Process pr;
    		if (System.getProperty("os.name").startsWith("Windows")) {
    			pr = rt.exec("cmd /C \"Rscript r_script.R " + playerID + " " + learnMode + " " + waveCount +  "\""); // change directory, then call the r script
    		} else {
    			pr = rt.exec("Rscript r_script.R " + playerID + " " + learnMode + " " + waveCount); //call the r script
    		}
    		BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
    		
    		String line = null;
    		while((line = input.readLine()) != null) {
    			System.out.println(line);
    		}
    		exitVal = pr.waitFor();
    		System.out.println("exited w/error code: " + exitVal);
    		return exitVal;
    		
    	} catch (Exception e) {
    		System.out.println(e.toString());
    		e.printStackTrace();
    		exitVal = -1;
    	} finally {
//    		ClientStateManager.togglePauseState();
    	}
		return exitVal;
    }
	
	public long getWave() {
		return waveCount;
	}
}
