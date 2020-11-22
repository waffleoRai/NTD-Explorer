package waffleoRai_NTDExCore.filetypes;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import waffleoRai_Files.Converter;
import waffleoRai_Files.FileDefinitions;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExGUI.ImageMasterPanel;

public class TM_EmptyFile extends TypeManager{
	
	public FileTypeNode detectFileType(FileNode node) {
		if(node == null) return null;
		if(node.getLength() > 0) return null;
		return new FileTypeDefNode(FileDefinitions.getEmptyFileDef());
	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {
		return new ImageMasterPanel.EmptyPreviewPanel();
	}

	@Override
	public List<FileAction> getFileActions() {
		List<FileAction> alist = new ArrayList<FileAction>(1);
		//alist.add(FA_ExtractFile.getAction());
		return alist;
	}
	
	public Converter getStandardConverter(){
		return null;
	}
	
	public boolean isOfType(FileNode node){
		if(node == null) return true;
		return (node.getLength() <= 0);
	}

}
