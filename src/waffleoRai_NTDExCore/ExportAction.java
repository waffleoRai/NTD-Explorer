package waffleoRai_NTDExCore;

import java.io.IOException;

import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;

public interface ExportAction {
	
	public void doExport(String dirpath, IndefProgressDialog dialog) throws IOException;

}
