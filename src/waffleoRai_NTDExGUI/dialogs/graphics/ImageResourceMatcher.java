package waffleoRai_NTDExGUI.dialogs.graphics;

import java.util.List;

import waffleoRai_GUITools.CheckeredImagePane;
import waffleoRai_Utils.FileNode;

public interface ImageResourceMatcher {
	
	public List<FileNode> getResourceList();
	public void drawSelected(FileNode selected, CheckeredImagePane pane);
	public void applySelected(FileNode selected);
	public String getDialogTitle();

}
