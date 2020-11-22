package waffleoRai_NTDTypes.audio;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import waffleoRai_Files.Converter;
import waffleoRai_Files.FileClass;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDTypeLoader;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ViewHex;
import waffleoRai_NTDExGUI.panels.preview.SoundPreviewPanel;
import waffleoRai_Sound.Sound;
import waffleoRai_Sound.SoundFileDefinition;
import waffleoRai_Sound.WAV;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class WAVManager extends TypeManager{

	//Definition
	public static final int DEF_ID = 0x57415645;
	public static final String DEFO_ENG_NAME = "RIFF Waveform Audio File (WAV)";
	
	private static WAVDefinition stat_def;
	
	public static class WAVDefinition extends SoundFileDefinition{

		private String desc = DEFO_ENG_NAME;
		
		public Collection<String> getExtensions() {
			List<String> list = new ArrayList<String>(2);
			list.add("wav");
			list.add("wave"); //Rare, but I'm pretty sure I've seen it
			return list;
		}

		public String getDescription() {return desc;}

		public FileClass getFileClass() {
			//STM would be accurate too, but since
			// wavs are usually uncompressed, they are
			//	usually used for short sound effects and whatnot
			return FileClass.SOUND_WAVE;
		}

		public int getTypeID() {return DEF_ID;}
		public void setDescriptionString(String s) {desc = s;}
		public String getDefaultExtension() {return "wav";}

		public Sound readSound(FileNode file) {
			try{
				FileBuffer dat = file.loadDecompressedData();
				return new WAV(dat);
			}
			catch(Exception x){
				x.printStackTrace();
				return null;
			}
		}
		
	}
	
	public static WAVDefinition getDefinition(){
		if(stat_def == null) stat_def = new WAVDefinition();
		return stat_def;
	}
	
	//Loader
	public static class WAVDefLoader implements NTDTypeLoader{

		public TypeManager getTypeManager() {return new WAVManager();}
		public FileTypeDefinition getDefinition() {return WAVManager.getDefinition();}
		
	}
	
	//Manager

	public FileTypeNode detectFileType(FileNode node) {
		//Look for RIFF()WAVE
		
		try{
			FileBuffer dat = null;
			long ed = 0x10;
			if(node.getLength() < ed) ed = node.getLength();
			if(node.hasCompression()) dat = node.loadDecompressedData();
			else dat = node.loadData(0, ed);
			
			long riff = dat.findString(0, ed, "RIFF");
			if(riff != 0) return null;
			
			long wave = dat.findString(0, ed, "WAVE");
			if(wave != 8) return null;
			
			return new FileTypeDefNode(WAVManager.getDefinition());
		}
		catch(Exception x){
			x.printStackTrace();
			return null;
		}

	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {

		try{
			FileBuffer wdat = node.loadDecompressedData();
			WAV wave = new WAV(wdat);
			
			SoundPreviewPanel pnl = new SoundPreviewPanel();
			
			//Info
			Map<String, String> imap = new HashMap<String, String>();
			imap.put("Sample Rate", wave.getSampleRate() + " hz");
			imap.put("Bit Depth", wave.getRawBitDepth() + " bit");
			imap.put("Channels", Integer.toString(wave.numberChannels()));
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

		return list;
	}

	public Converter getStandardConverter() {return null;}
	public boolean isOfType(FileNode node) {return (detectFileType(node) != null);}

}
