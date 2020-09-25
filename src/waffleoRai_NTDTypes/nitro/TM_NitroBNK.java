package waffleoRai_NTDTypes.nitro;

import java.awt.Component;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import waffleoRai_Containers.nintendo.sar.DSSoundArchive;
import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ViewHex;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_NTDExGUI.panels.preview.SimpleSoundbankTreePanel;
import waffleoRai_Sound.nintendo.DSWarc;
import waffleoRai_SoundSynth.SynthBank;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_soundbank.SoundbankNode;
import waffleoRai_soundbank.nintendo.DSBank;

public class TM_NitroBNK extends TypeManager{

	public FileTypeNode detectFileType(FileNode node) {
		return NitroFiles.detectNitroFile(node, DSBank.MAGIC, DSBank.getDefinition(), false);
	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {

		//Load bank
		DSBank bank = null;
		try {
			FileBuffer dat = node.loadDecompressedData();
			bank = DSBank.readSBNK(dat, 0);
		} 
		catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, 
					"I/O Error: SBNK could not be opened! See stderr for details.", 
					"SBNK Preview Error", JOptionPane.ERROR_MESSAGE);
			return null;
		} 
		catch (UnsupportedFileTypeException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, 
					"Parser Error: SBNK could not be read! See stderr for details.", 
					"SBNK Preview Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		//Get name
		String name = node.getMetadataValue("TITLE");
		if(name == null) name = node.getFileName();
		
		//Get data
		SoundbankNode root = bank.getBankTree(name);
		DSWarc[] warcs = null;
		try {warcs = DSSoundArchive.loadLinkedWavearcs(node);} 
		catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, 
					"I/O Error: Linked wave archives could not be found or opened! See stderr for details.", 
					"SBNK Preview Error", JOptionPane.ERROR_MESSAGE);
			return null;
		} 
		catch (UnsupportedFileTypeException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, 
					"Parser Error: Linked wave archives could not be read! See stderr for details.", 
					"SBNK Preview Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		if(warcs == null){
			JOptionPane.showMessageDialog(gui_parent, 
					"Unknown Error: Linked wave archives could not be found! See stderr for details.", 
					"SBNK Preview Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		//Generate playable bank
		//System.err.println("Generating playable bank...");
		SynthBank pbnk = bank.generatePlayableBank(warcs, 0);
		
		//Determine which banks/programs are valid
		//System.err.println("Getting playable node numbers...");
		Collection<Integer> bnos = bank.getUsableBanks();
		Collection<Integer> pnos = bank.getUsablePrograms();
		
		//Spawn panel
		//System.err.println("Spawning panel...");
		//root.printMeToStderr(0);
		SimpleSoundbankTreePanel pnl = new SimpleSoundbankTreePanel(root);
		pnl.loadPlayer(pbnk, bnos, pnos);
		pnl.startPlayer();
		
		return pnl;
	}

	public List<FileAction> getFileActions() {
		//Extract, Convert to SF2, View Hex
		List<FileAction> falist = new ArrayList<FileAction>(4);
		falist.add(new FA_ExportSF2());
		falist.add(FA_ExtractFile.getAction());
		falist.add(FA_ViewHex.getAction());
		
		return falist;
	}

	public Converter getStandardConverter() {return DSBank.getDefaultConverter();}

	public boolean isOfType(FileNode node) {
		return detectFileType(node) != null;
	}

	//----------------------------
	
	public static class FA_ExportSF2 implements FileAction{

		private static String DEFO_ENG = "Export to SF2";
		
		private String str;
		
		public FA_ExportSF2(){str = DEFO_ENG;}
		
		public void doAction(FileNode node, NTDProject project, Frame gui_parent) {
			
			JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(NTDProgramFiles.INIKEY_LAST_EXPORTED));
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			int select = fc.showSaveDialog(gui_parent);
			
			if(select != JFileChooser.APPROVE_OPTION) return;
			String dir = fc.getSelectedFile().getAbsolutePath();
			
			Converter conv = DSBank.getDefaultConverter();
			String targetpath = conv.changeExtension(dir + File.separator + node.getFileName());
			NTDProgramFiles.setIniValue(NTDProgramFiles.INIKEY_LAST_EXPORTED, targetpath);
			IndefProgressDialog dialog = new IndefProgressDialog(gui_parent, "SF2 Conversion");
			dialog.setPrimaryString("Exporting bank to SF2");
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
								"Unknown Error: Conversion Failed! See stderr for details.", 
								"SBNK Conversion Error", JOptionPane.ERROR_MESSAGE);
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
