package waffleoRai_NTDScriptAPI;

import javax.swing.text.StyledDocument;

import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;

public interface NTDScript {

	public String getDisplayName();
	public StyledDocument getUsageInfo();
	public int run(String[] args, IndefProgressDialog observer);
	
}
