package waffleoRai_NTDExCore.consoleproj;

import waffleoRai_Containers.nintendo.cafe.CafeCryptListener;
import waffleoRai_NTDExGUI.dialogs.progress.ProgressListeningDialog;
import waffleoRai_Utils.Arunnable;

public class WudDecObserver implements CafeCryptListener{
	
	public static final int UPDATE_MILLIS = 1000;
	
	private ProgressListeningDialog dialog;
	
	private int part;
	private int pcount;
	
	private int clust;
	private int ccount;
	
	private long cpos;
	private long csize;
	
	private Arunnable tUpdater;
	
	private class Runner extends Arunnable{

		public Runner(){
			super.sleeps = true;
			super.sleeptime = UPDATE_MILLIS;
			super.delay = UPDATE_MILLIS;
		}
		
		public void doSomething() {
			if(csize <= 0L) return;
			double cperc = ((double)cpos/(double)csize) * 100.0;
			dialog.setSecondaryString("Processing cluster " + (clust+1) + 
					"/" + ccount + " (" + String.format("%.2f", cperc) + "%)");
		}
		
	}
	
	public WudDecObserver(ProgressListeningDialog d){
		dialog = d;
		
		tUpdater = new Runner();
		Thread t = new Thread(tUpdater);
		t.setName("WUDDecryptionObserver");
		t.setDaemon(true);
		t.start();
	}

	public void setPartitionCount(int count) {pcount = count;}

	public void onPartitionComplete(int idx) {
		part = idx+1;
		dialog.setPrimaryString("Partition " + (part+1) + "/" + pcount);
	}

	public void setClusterSize(long size) {
		csize = size;
		cpos = 0L;
	}

	public void setClusterPosition(long cpos) {
		this.cpos = cpos;
	}

	public void setClusterCount(int count) {
		this.ccount = count;
	}

	public void onClusterStart(int idx) {
		clust = idx;
		cpos = 0;
		dialog.setSecondaryString("Processing cluster " + (clust+1) + "/" + ccount + " (0.00%)");
	}

	public int getUpdateInterval() {return UPDATE_MILLIS;}
	
	public void dispose(){
		tUpdater.requestTermination();
	}

}
