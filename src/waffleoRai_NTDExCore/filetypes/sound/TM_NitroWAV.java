package waffleoRai_NTDExCore.filetypes.sound;

import java.awt.Component;
import java.awt.Frame;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.filetypes.NitroFiles;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ViewHex;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_NTDExGUI.panels.preview.SoundPreviewPanel;
import waffleoRai_Sound.Sound;
import waffleoRai_Sound.nintendo.DSWave;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileNode;

public class TM_NitroWAV extends TypeManager{

	public FileTypeNode detectFileType(FileNode node) {
		/*This scans for the SWAV magic to check for compression.
		 * As a result, it cannot sense SWAVs that lack the magic number directly
		 * In which case, internal SWAVs should be assigned their 
		 * type node when extracted from their SWAR.
		 * Luckily it looks like DS is the only one that leaves its waves unmarked
		 * 
		 * Right now, I'm having it generate a node based on extension if it
		 * fails to find the magic number which is always a flaky approach.
		 * Reason is in case user wipes type marks after extracting from an SDAT/SWAR
		 * and does a fresh detection scan.
		 * 
		 * Be careful using this for auto detection.
		 */
		
		return NitroFiles.detectNitroFile(node, DSWave.MAGIC, DSWave.getDefinition(), false);
	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {
		
		//Try to load sound
		Sound snd = null;
		try {
			FileBuffer dat = node.loadDecompressedData();
			long mpos = dat.findString(0, 0x10, DSWave.MAGIC);
			if(mpos == 0) snd = DSWave.readSWAV(dat, 0);
			else snd = DSWave.readInternalSWAV(dat, 0);
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
					"Parser Error: File could not be read as SWAV! See stderr for details.", 
					"SWAV Exception", JOptionPane.ERROR_MESSAGE);
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
		
		return pnl;
	}

	public List<FileAction> getFileActions() {
		//Extract, Extract + Add Header, Export WAV, View Hex
		List<FileAction> falist = new ArrayList<FileAction>(4);
		falist.add(new FA_ExportToWave());
		falist.add(FA_ExtractFile.getAction());
		falist.add(new FA_ExtractWithHeader());
		falist.add(FA_ViewHex.getAction());
		
		return falist;
	}

	public Converter getStandardConverter() {return DSWave.getDefaultConverter();}

	public boolean isOfType(FileNode node) {

		/*
		 *If it doesn't find the SWAV magic, it'll take
		 *your word for it and return true if the extension is
		 *.swav, .nwav, or .bnwav.
		 *
		 * Be careful using this for autodetect
		 */
		
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
			
			Converter conv = DSWave.getDefaultConverter();
			String targetpath = conv.changeExtension(dir + File.separator + node.getFileName());
			NTDProgramFiles.setIniValue(NTDProgramFiles.INIKEY_LAST_EXPORTED, targetpath);
			IndefProgressDialog dialog = new IndefProgressDialog(gui_parent, "SWAV Export");
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
								"SWAV to WAV Conversion Error", JOptionPane.ERROR_MESSAGE);
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
	
	public static class FA_ExtractWithHeader implements FileAction{

		private static String DEFO_ENG = "Extract w/ SWAV Header";
		
		private String str;
		
		public FA_ExtractWithHeader(){str = DEFO_ENG;}
		
		public void doAction(FileNode node, NTDProject project, Frame gui_parent) {

			JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(NTDProgramFiles.INIKEY_LAST_EXTRACTED));
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			int select = fc.showSaveDialog(gui_parent);
			
			if(select != JFileChooser.APPROVE_OPTION) return;
			String dir = fc.getSelectedFile().getAbsolutePath();
			
			String targetpath = dir + File.separator + node.getFileName();
			NTDProgramFiles.setIniValue(NTDProgramFiles.INIKEY_LAST_EXTRACTED, targetpath);
			IndefProgressDialog dialog = new IndefProgressDialog(gui_parent, "SWAV Extract");
			dialog.setPrimaryString("Extracting SWAV");
			dialog.setSecondaryString("Writing to " + targetpath);
			
			SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
			{

				protected Void doInBackground() throws Exception 
				{
					try
					{
						FileBuffer dat = node.loadDecompressedData();
						//See if already headered...
						long mpos = dat.findString(0, 0x10, DSWave.MAGIC);
						if(mpos == 0){
							//Write as is
							dat.writeFile(targetpath);
						}
						else{
							//Add header
							int datsize = (int)dat.getFileSize();
							FileBuffer header = DSWave.generateSWAVHeader(datsize);
							FileBuffer dathead = new FileBuffer(8, false);
							dathead.printASCIIToFile("DATA");
							dathead.addToFile(datsize);
							
							//Write
							BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetpath));
							header.writeToStream(bos);
							dathead.writeToStream(bos);
							dat.writeToStream(bos);
							bos.close();
						}
					}
					catch(Exception x)
					{
						x.printStackTrace();
						JOptionPane.showMessageDialog(gui_parent, 
								"Unknown Error: Extract Failed! See stderr for details.", 
								"SWAV Headered Extraction Error", JOptionPane.ERROR_MESSAGE);
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
