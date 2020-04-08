package waffleoRai_NTDExGUI.panels.preview.soundbank;

public interface KeyboardListener {
	
	public void onNotePressed(int note, int vel);
	public void onNoteReleased(int note);
	public void allNotesOff();

}
