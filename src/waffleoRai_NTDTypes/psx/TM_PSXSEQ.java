package waffleoRai_NTDTypes.psx;

import java.awt.Component;
import java.awt.Frame;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDTypeLoader;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExportFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ViewHex;
import waffleoRai_NTDExCore.seq.SEQPLoader;
import waffleoRai_NTDExGUI.panels.preview.seq.GeneralSeqPreviewPanel;
import waffleoRai_SeqSound.psx.SEQP;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class TM_PSXSEQ extends TypeManager{
	
	//Loader
	public static class PSXSEQDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_PSXSEQ();}
		public FileTypeDefinition getDefinition() {return SEQP.getDefinition();}
	}
	
	//Manager
	
	public FileTypeNode detectFileType(FileNode node) {
		if(node.getLength() <= 0) return null;
		
		try{
			FileBuffer dat = null;
			long ed = 0x10;
			if(node.getLength() < ed) ed = node.getLength();
			if(node.hasCompression()) dat = node.loadDecompressedData();
			else dat = node.loadData(0, ed);
			
			long moff = dat.findString(0, ed, SEQP.MAGIC);
			if(moff != 0) return null;
			
			return new FileTypeDefNode(SEQP.getDefinition());
		}
		catch(Exception x){
			x.printStackTrace();
			return null;
		}

	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {
		try{
			SEQPLoader loader = new SEQPLoader(node);
		
			Frame f = null;
			if(gui_parent instanceof Frame) f = (Frame)gui_parent;
			GeneralSeqPreviewPanel pnl = new GeneralSeqPreviewPanel(f, loader);
			return pnl;
		}
		catch(IOException x){
			x.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, 
					"I/O Error: File read error! See stderr for details.", 
					"SEQP Load Error", JOptionPane.ERROR_MESSAGE);
		}
		catch(UnsupportedFileTypeException x){
			x.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, 
					"Parser Error: Sequence or bank couldn't be read! See stderr for details.", 
					"SEQP Load Error", JOptionPane.ERROR_MESSAGE);
		}
		

		return null;
	}

	public List<FileAction> getFileActions() {
		List<FileAction> list = new ArrayList<FileAction>(3);
		list.add(FA_ExtractFile.getAction());
		list.add(FA_ViewHex.getAction());
		list.add(new FA_ExportFile(){

			protected Converter getConverter(FileNode node) {
				return getStandardConverter();
			}
			
		});
		return list;
	}

	public Converter getStandardConverter() {return SEQP.getConverter();}
	public boolean isOfType(FileNode node) {return (detectFileType(node) != null);}

}
