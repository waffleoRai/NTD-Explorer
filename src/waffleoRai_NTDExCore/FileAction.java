package waffleoRai_NTDExCore;

import java.awt.Frame;

import waffleoRai_Files.tree.FileNode;

public interface FileAction {

	public void doAction(FileNode node, NTDProject project, Frame gui_parent);
	public void setString(String s);
	
}
