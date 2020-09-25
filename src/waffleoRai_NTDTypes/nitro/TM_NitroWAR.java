package waffleoRai_NTDTypes.nitro;

import java.awt.Component;
import java.awt.Frame;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import waffleoRai_NTDExGUI.panels.preview.WaveArchiveViewPanel;
import waffleoRai_Sound.Sound;
import waffleoRai_Sound.nintendo.DSWarc;
import waffleoRai_Sound.nintendo.DSWave;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class TM_NitroWAR extends TypeManager{
	
	public FileTypeNode detectFileType(FileNode node) {
		return NitroFiles.detectNitroFile(node, DSWarc.MAGIC, DSWarc.getDefinition(), false);
	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {
	
		//Load data
		DSWarc warc = null;
		try{
			FileBuffer dat = node.loadDecompressedData();
			warc = DSWarc.readSWAR(dat, 0);
		}
		catch(IOException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, 
					"I/O Error: File count not be opened! See stderr for details.", 
					"SWAR Preview Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		catch(UnsupportedFileTypeException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, 
					"Parser Error: File count not be read! See stderr for details.", 
					"SWAR Preview Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		//Build name -> SWAV map
		Map<String, Sound> smap = new HashMap<String, Sound>();
		int wcount = warc.countSounds();
		for(int i = 0; i < wcount; i++){
			String name = "SWAV" + String.format("%04d", i);
			smap.put(name, warc.getWave(i));
		}
		
		WaveArchiveViewPanel pnl = new WaveArchiveViewPanel(smap);
		
		return pnl;
	}

	public List<FileAction> getFileActions() {
		//Extract, Dump, Dump & Convert, View Hex
		List<FileAction> falist = new ArrayList<FileAction>(4);
		falist.add(new FA_ExportSWAR());
		falist.add(new FA_DumpWarc());
		falist.add(FA_ExtractFile.getAction());
		falist.add(FA_ViewHex.getAction());
		
		return falist;
	}

	public Converter getStandardConverter() {return DSWarc.getDefaultConverter();}

	public boolean isOfType(FileNode node) {
		return detectFileType(node) != null;
	}

	//----------------------------
	
	public static class FA_DumpWarc implements FileAction{

		private static String DEFO_ENG = "Dump Archive to Disk";
		
		private String str;
		
		public FA_DumpWarc(){str = DEFO_ENG;}
		
		public void doAction(FileNode node, NTDProject project, Frame gui_parent) {

			JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(NTDProgramFiles.INIKEY_LAST_ARCDUMP));
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			int select = fc.showSaveDialog(gui_parent);
			
			if(select != JFileChooser.APPROVE_OPTION) return;
			String dir = fc.getSelectedFile().getAbsolutePath();
			
			String targetpath = dir + File.separator + node.getFileName();
			NTDProgramFiles.setIniValue(NTDProgramFiles.INIKEY_LAST_ARCDUMP, targetpath);
			
			IndefProgressDialog dialog = new IndefProgressDialog(gui_parent, "SWAR Dump");
			dialog.setPrimaryString("Dumping SWAR archive");
			dialog.setSecondaryString("Writing to " + targetpath);
			
			SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
			{

				protected Void doInBackground() throws Exception 
				{
					try
					{
						FileBuffer dat = node.loadDecompressedData();
						DSWarc warc = DSWarc.readSWAR(dat, 0);
						
						if(!FileBuffer.directoryExists(targetpath)){
							Files.createDirectories(Paths.get(targetpath));
						}
						
						int wavcount = warc.countSounds();
						for(int i = 0; i < wavcount; i++){
							String fname = "NitroSWAR_SWAV" + String.format("%03d", i) + ".swav";
							dialog.setSecondaryString("Writing " + fname);
							
							long stpos = warc.getStartOffsetOfSWAV(i);
							long edpos = warc.getEndOffsetOfSWAV(i);
							
							FileBuffer swavdat = dat.createReadOnlyCopy(stpos, edpos);
							int sz = (int)swavdat.getFileSize();
							FileBuffer header = DSWave.generateSWAVHeader(sz);
							FileBuffer dathead = new FileBuffer(8, false);
							dathead.printASCIIToFile("DATA");
							dathead.addToFile(sz);
							
							BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fname));
							header.writeToStream(bos);
							dathead.writeToStream(bos);
							swavdat.writeToStream(bos);
							bos.close();
						}
					}
					catch(Exception x)
					{
						x.printStackTrace();
						JOptionPane.showMessageDialog(gui_parent, 
								"Unknown Error: SWAV Extract Failed! See stderr for details.", 
								"SWAR Dump Error", JOptionPane.ERROR_MESSAGE);
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
	
	public static class FA_ExportSWAR implements FileAction{

		private static String DEFO_ENG = "Export Wave Archive";
		
		private String str;
		
		public FA_ExportSWAR(){str = DEFO_ENG;}
		
		public void doAction(FileNode node, NTDProject project, Frame gui_parent) {

			JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(NTDProgramFiles.INIKEY_LAST_EXPORTED));
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			int select = fc.showSaveDialog(gui_parent);
			
			if(select != JFileChooser.APPROVE_OPTION) return;
			String dir = fc.getSelectedFile().getAbsolutePath();
			
			Converter conv = DSWarc.getDefaultConverter();
			String targetpath = conv.changeExtension(dir + File.separator + node.getFileName());
			NTDProgramFiles.setIniValue(NTDProgramFiles.INIKEY_LAST_EXPORTED, targetpath);
			IndefProgressDialog dialog = new IndefProgressDialog(gui_parent, "SWAR Export");
			dialog.setPrimaryString("Exporting archive");
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
								"SWAR Dump Error", JOptionPane.ERROR_MESSAGE);
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
