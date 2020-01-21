package waffleoRai_NTDExCore;

import java.awt.Component;

import waffleoRai_Utils.FileNode;

public interface FileAction {

	public void doAction(FileNode node, NTDProject project, Component gui_parent);
	public void setString(String s);
	
}
