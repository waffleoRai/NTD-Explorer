package waffleoRai_NTDExCore.consoleproj;

import waffleoRai_Containers.nintendo.wiidisc.WiiCryptListener;
import waffleoRai_NTDExGUI.dialogs.progress.ProgressListeningDialog;

public class WiiDecObserver implements WiiCryptListener{

	private ProgressListeningDialog dialog;
	
	private int current_part;
	private int pcount;
	
	private int current_sec;
	private int sec_count;
	
	public WiiDecObserver(ProgressListeningDialog prog_dialog){
		dialog = prog_dialog;
		
		dialog.setPrimaryString("Reading Image");
		dialog.setSecondaryString("Determining disc partition structure");
	}
	
	public void setPartitionCount(int count){
		pcount = count; current_part = 0;
		dialog.setPrimaryString("Partition 1/" + pcount);
	}
	
	public void onPartitionStart(){
		//System.err.println("um hey?");
		dialog.setPrimaryString("Partition " + (current_part+1)+ "/" + pcount);
	}
	
	public void setSectorCount(int count) {
		sec_count = count;
		current_sec = 0;
		
		dialog.setSecondaryString("Working on data sector 1/" + sec_count);
	}

	public void onSectorDecrypted(int sectorIndex) {
		current_sec=sectorIndex;
		dialog.setSecondaryString("Working on data sector " + (current_sec+1) + "/" + sec_count);
	}

	public void onSectorWrittenToBuffer(int sectorIndex) {
		dialog.setSecondaryString("Buffering data sector " + (sectorIndex+1) + "/" + sec_count);
	}

	public int getUpdateFrequencyMillis() {return 500;}

	public void onPartitionDecryptionComplete(boolean isgood){
		current_part++;
		if(isgood){
			dialog.setPrimaryString("Partition Read");
			dialog.setSecondaryString("Partition " + current_part + " successfully decrypted.");
		}
		else{
			dialog.setPrimaryString("Partition Failed");
			dialog.setSecondaryString("Partition " + current_part + " could not be read.");
		}
	}
	
	public void onDecryptionComplete(boolean isgood) {
		if(isgood){
			dialog.setPrimaryString("Success");
			dialog.setSecondaryString("Disc read completed successfully.");
		}
		else{
			dialog.setPrimaryString("Error");
			dialog.setSecondaryString("There was an error in the disc read.");
		}
	}

}
