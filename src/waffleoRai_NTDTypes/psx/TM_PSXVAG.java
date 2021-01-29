package waffleoRai_NTDTypes.psx;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import waffleoRai_NTDExGUI.panels.preview.SoundPreviewPanel;
import waffleoRai_Sound.psx.PSXVAG;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class TM_PSXVAG extends TypeManager{
	
	//Loader
	
	public static class PSXVAGDefLoader implements NTDTypeLoader{

		public TypeManager getTypeManager() {return new TM_PSXVAG();}
		public FileTypeDefinition getDefinition() {return PSXVAG.getDefinition();}
		
	}
	
	//Manager
	
	public FileTypeNode detectFileType(FileNode node) {
		//Look for "pGAV"
		
		try{
			FileBuffer dat = null;
			long ed = 0x10;
			if(node.getLength() < ed) ed = node.getLength();
			if(node.hasCompression()) dat = node.loadDecompressedData();
			else dat = node.loadData(0, ed);
			
			long moff = dat.findString(0, ed, "pGAV");
			if(moff != 0) return null;
			
			return new FileTypeDefNode(PSXVAG.getDefinition());
		}
		catch(Exception x){
			x.printStackTrace();
			return null;
		}

	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {

		try{
			FileBuffer wdat = node.loadDecompressedData();
			PSXVAG wave = new PSXVAG(wdat);
			
			SoundPreviewPanel pnl = new SoundPreviewPanel();
			
			//Info
			Map<String, String> imap = new HashMap<String, String>();
			imap.put("Sample Rate", "44100 hz");
			imap.put("Bit Depth", "4 bit");
			imap.put("Channels", Integer.toString(wave.totalChannels()));
			pnl.setSoundInfo(imap);
			
			//Load sound
			pnl.setSound(wave);
			
			return pnl;
		}
		catch(IOException x){
			x.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, "Error: File could not be loaded!", 
					"I/O Error", JOptionPane.ERROR_MESSAGE);
		}
		catch(UnsupportedFileTypeException x){
			x.printStackTrace();
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

	public Converter getStandardConverter() {return PSXVAG.getConverter();}
	public boolean isOfType(FileNode node) {return (detectFileType(node) != null);}


}
