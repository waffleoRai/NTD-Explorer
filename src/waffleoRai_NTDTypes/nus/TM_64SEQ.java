package waffleoRai_NTDTypes.nus;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDTypeLoader;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExportFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDExGUI.panels.preview.HexPreviewPanel;
import waffleoRai_SeqSound.n64al.NUSALSeq;

public class TM_64SEQ extends TypeManager{
	
	//Loader
	
	public static class NUSSeqLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_64SEQ();}
		public FileTypeDefinition getDefinition() {return NUSALSeq.getDefinition();}
	}
	
	//Manager
	
	public FileTypeNode detectFileType(FileNode node) {return null;}

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

	public List<FileAction> getFileActions() {
		List<FileAction> alist = new ArrayList<FileAction>(2);
		alist.add(FA_ExtractFile.getAction());
		alist.add(new FA_ExportFile(){
			
			protected void onConstruct(){super.setString("Export to MIDI");}

			protected Converter getConverter(FileNode node) {
				return getStandardConverter();
			}
			});
		return alist;
	}

	public Converter getStandardConverter() {return NUSALSeq.getMIDIConverter();}
	public boolean isOfType(FileNode node) {return false;}

}
