package waffleoRai_NTDTypes.nitro;

import java.awt.Component;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ViewHex;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_NTDExGUI.panels.preview.SoundPreviewPanel;
import waffleoRai_Sound.nintendo.DSStream;
import waffleoRai_Sound.nintendo.NinSound;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class TM_NitroSTM extends TypeManager{
	
	public FileTypeNode detectFileType(FileNode node) {
		return NitroFiles.detectNitroFile(node, DSStream.MAGIC, DSStream.getDefinition(), false);
	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {
		
		//Try to load sound
		DSStream snd = null;
		try {
			FileBuffer dat = node.loadDecompressedData();
			snd = DSStream.readSTRM(dat, 0);
		} 
		catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, 
					"I/O Error: File could not be loaded! See stderr for details.", 
					"I/O Exception", JOptionPane.ERROR_MESSAGE);
			return null;
		} 
		catch (UnsupportedFileTypeException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, 
					"Parser Error: File could not be read as DS STRM! See stderr for details.", 
					"STRM Exception", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		if(snd == null){
			JOptionPane.showMessageDialog(gui_parent, 
					"Unknown Error: Sound data could not be loaded!", 
					"File Load Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		//Generate panel
		SoundPreviewPanel pnl = new SoundPreviewPanel();
		pnl.setSound(snd);
		
		//Add metadata
		Map<String, String> meta = new HashMap<String, String>();
		meta.put("Sample Rate", snd.getSampleRate() + " hz");
		meta.put("Bit Depth", snd.getBitDepth().getBitCount() + " bits");
		if(snd.totalChannels() == 2) meta.put("Channels", "Stereo");
		else meta.put("Channels", "Mono");
		
		int encoding = snd.getEncodingType();
		switch(encoding){
		case NinSound.ENCODING_TYPE_PCM8: 
			meta.put("Encoding", "8-bit PCM"); break;
		case NinSound.ENCODING_TYPE_PCM16:
			meta.put("Encoding", "16-bit PCM"); break;
		case NinSound.ENCODING_TYPE_IMA_ADPCM:
			meta.put("Encoding", "IMA ADPCM"); break;
		}
		
		pnl.setSoundInfo(meta);
		
		return pnl;
	}

	public List<FileAction> getFileActions() {
		//Extract, Export WAV, View Hex
		List<FileAction> falist = new ArrayList<FileAction>(4);
		falist.add(new FA_ExportToWave());
		falist.add(FA_ExtractFile.getAction());
		falist.add(FA_ViewHex.getAction());
		
		return falist;
	}

	public Converter getStandardConverter() {return DSStream.getDefaultConverter();}

	public boolean isOfType(FileNode node) {
		return detectFileType(node) != null;
	}

	//----------------------------
	
	public static class FA_ExportToWave implements FileAction{

		private static String DEFO_ENG = "Export to WAV";
		
		private String str;
		
		public FA_ExportToWave(){str = DEFO_ENG;}
		
		public void doAction(FileNode node, NTDProject project, Frame gui_parent) {

			JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(NTDProgramFiles.INIKEY_LAST_EXPORTED));
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			int select = fc.showSaveDialog(gui_parent);
			
			if(select != JFileChooser.APPROVE_OPTION) return;
			String dir = fc.getSelectedFile().getAbsolutePath();
			
			Converter conv = DSStream.getDefaultConverter();
			String targetpath = conv.changeExtension(dir + File.separator + node.getFileName());
			NTDProgramFiles.setIniValue(NTDProgramFiles.INIKEY_LAST_EXPORTED, targetpath);
			IndefProgressDialog dialog = new IndefProgressDialog(gui_parent, "STRM Export");
			dialog.setPrimaryString("Exporting to WAV");
			dialog.setSecondaryString("Writing to " + targetpath);
			
			SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
			{

				protected Void doInBackground() throws Exception 
				{
					try
					{
						FileBuffer dat = node.loadDecompressedData();
						conv.writeAsTargetFormat(dat, targetpath);
					}
					catch(Exception x)
					{
						x.printStackTrace();
						JOptionPane.showMessageDialog(gui_parent, 
								"Unknown Error: Export Failed! See stderr for details.", 
								"STRM to WAV Conversion Error", JOptionPane.ERROR_MESSAGE);
					}
					
					return null;
				}
				
				public void done()
				{
					dialog.closeMe();
				}
			};
			
			task.execute();
			dialog.render();
			
		}

		public void setString(String s) {str = s;}
		public String toString(){return str;}
		
	}
	
}
