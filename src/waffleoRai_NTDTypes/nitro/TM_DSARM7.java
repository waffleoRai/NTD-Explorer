package waffleoRai_NTDTypes.nitro;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import waffleoRai_Executable.nintendo.DSExeDefs;
import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDExGUI.panels.preview.HexPreviewPanel;
import waffleoRai_Files.tree.FileNode;

public class TM_DSARM7 extends TypeManager{

	public FileTypeNode detectFileType(FileNode node) {
		if(!isOfType(node)) return null;
		return new FileTypeDefNode(DSExeDefs.getDefARM7());
	}
	
	public boolean isOfType(FileNode node) {
		//No internal test for now
		//To avoid auto-detection, just use extension
		return node.getFileName().endsWith(".arm7");
	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {
		HexPreviewPanel pnl = new HexPreviewPanel();
		try{pnl.load(node, 0, false);}
		catch(IOException e){
			JOptionPane.showMessageDialog(gui_parent, 
					"I/O Error: Node data could not be loaded!", 
					"I/O Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return pnl;
	}

	public List<FileAction> getFileActions() {
		List<FileAction> alist = new ArrayList<FileAction>(1);
		alist.add(FA_ExtractFile.getAction());
		return alist;
	}

	public Converter getStandardConverter() {return null;}

}
