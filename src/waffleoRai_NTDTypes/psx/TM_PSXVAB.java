package waffleoRai_NTDTypes.psx;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import waffleoRai_NTDExGUI.panels.preview.SimpleSoundbankTreePanel;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_soundbank.SoundbankNode;
import waffleoRai_soundbank.vab.PSXVAB;

public class TM_PSXVAB extends TypeManager{
	
	//Loaders
	
	public static class PSXVABDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_PSXVAB();}
		public FileTypeDefinition getDefinition() {return PSXVAB.getDefinition();}
	}
	
	public static class PSXVABHeadDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_PSXVAB();}
		public FileTypeDefinition getDefinition() {return PSXVAB.getHeadDefinition();}	
	}
	
	public static class PSXVABBodyDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_PSXVAB();}
		public FileTypeDefinition getDefinition() {return PSXVAB.getBodyDefinition();}	
	}
	
	//Manager
	
	public FileTypeNode detectFileType(FileNode node) {
		//Look for "pBAV"
		//Cannot autodetect VAB body
		
		try{
			FileBuffer dat = null;
			long ed = 0x10;
			if(node.getLength() < ed) ed = node.getLength();
			if(node.hasCompression()) dat = node.loadDecompressedData();
			else dat = node.loadData(0, ed);
			
			long moff = dat.findString(0, ed, "pBAV");
			if(moff != 0) return null;
			
			if(node.getFileName().endsWith(".vh")) return new FileTypeDefNode(PSXVAB.getHeadDefinition());
			return new FileTypeDefNode(PSXVAB.getDefinition());
		}
		catch(Exception x){
			x.printStackTrace();
			return null;
		}

	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {

		if(node == null) return null;
		
		PSXVAB bank = null;
		try {
			FileTypeNode tail = node.getTypeChainTail();
			FileTypeDefinition tdef = null;
			if(tail != null) tdef = tail.getTypeDefinition();
			boolean headonly = (tdef != null && tdef.getTypeID() == PSXVAB.DEF_ID_HEAD);
			if(headonly){
				//Look for VAB body.
				FileNode vb = PSXVAB.findVABBody(node);
				FileBuffer vhdat = node.loadDecompressedData();
				FileBuffer vbdat = null;
				if(vb != null) vbdat = node.loadDecompressedData();
				bank = new PSXVAB(vhdat, vbdat);
			}
			else{
				FileBuffer dat = node.loadDecompressedData();
				bank = new PSXVAB(dat);
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, 
					"I/O Error: VAB could not be opened! See stderr for details.", 
					"VAB Preview Error", JOptionPane.ERROR_MESSAGE);
			return null;
		} 
		catch (UnsupportedFileTypeException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, 
					"Parser Error: VAB could not be read! See stderr for details.", 
					"VAB Preview Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		//Get name
		String name = node.getMetadataValue("TITLE");
		if(name == null) name = node.getFileName();
		
		//Get data
		SoundbankNode root = bank.getBankTree(name);
		
		//Generate playable bank

		//Determine which banks/programs are valid
		Collection<Integer> bnos = bank.getUsableBanks();
		Collection<Integer> pnos = bank.getUsablePrograms();
		
		//Spawn panel
		SimpleSoundbankTreePanel pnl = new SimpleSoundbankTreePanel(root);
		pnl.loadPlayer(bank, bnos, pnos);
		pnl.startPlayer();
		
		return pnl;
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

	public Converter getStandardConverter() {return PSXVAB.getConverter();}
	public boolean isOfType(FileNode node) {return (detectFileType(node) != null);}


}
