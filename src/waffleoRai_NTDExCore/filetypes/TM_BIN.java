package waffleoRai_NTDExCore.filetypes;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDExGUI.panels.preview.HexPreviewPanel;
import waffleoRai_Utils.FileNode;

public class TM_BIN extends TypeManager{

	public FileTypeNode detectFileType(FileNode node) {
		return null;
	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {
		HexPreviewPanel pnl = new HexPreviewPanel();
		try{pnl.load(node, 0, false);}
		catch(IOException e){
			System.err.println("Node points to: " + node.getSourcePath() + " 0x" + 
						Long.toHexString(node.getOffset()));
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, 
					"I/O Error: Node data could not be loaded!", 
					"I/O Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return pnl;
	}

	@Override
	public List<FileAction> getFileActions() {
		List<FileAction> alist = new ArrayList<FileAction>(1);
		alist.add(FA_ExtractFile.getAction());
		return alist;
	}
	
	public Converter getStandardConverter()
	{
		return null;
	}
	
	public boolean isOfType(FileNode node)
	{
		return true;
	}

}
