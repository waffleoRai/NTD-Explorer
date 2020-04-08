package waffleoRai_NTDExCore;

import java.awt.Frame;

import waffleoRai_Utils.FileNode;

public interface FileAction {

	public void doAction(FileNode node, NTDProject project, Frame gui_parent);
	public void setString(String s);
	
}
