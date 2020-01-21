package waffleoRai_NTDExCore.filetypes;

import java.awt.Component;
import java.util.List;

import javax.swing.JPanel;

import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_Utils.FileNode;

public class TM_BIN extends TypeManager{

	@Override
	public FileTypeNode detectFileType(FileNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<FileAction> getFileActions() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Converter getStandardConverter()
	{
		return null;
	}

}
