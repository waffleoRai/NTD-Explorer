package waffleoRai_NTDTypes.procyon;

import java.awt.Component;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import waffleoRai_Files.Converter;
import waffleoRai_Files.FileClass;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.NTDTools;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ViewHex;
import waffleoRai_NTDExCore.seq.SMDLoader;
import waffleoRai_NTDExGUI.dialogs.NodeSelectDialog;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_NTDExGUI.panels.preview.seq.GeneralSeqPreviewPanel;
import waffleoRai_SeqSound.misc.SMD;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_soundbank.procyon.SWD;

public class TM_ProcyonSMD extends TypeManager{

	public FileTypeNode detectFileType(FileNode node) {
		
		String path = node.getSourcePath();
		long offset = node.getOffset();
		
		try{
			FileBuffer head = new FileBuffer(path, offset, offset+0x10, false);
			long mpos = head.findString(0, 0x10, SMD.MAGIC);
			if(mpos != 0) return null;
			
			return new FileTypeDefNode(SMD.getDefinition());
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}

	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {
		 
		try{
			SMDLoader loader = new SMDLoader(node);
		
			Frame f = null;
			if(gui_parent instanceof Frame) f = (Frame)gui_parent;
			GeneralSeqPreviewPanel pnl = new GeneralSeqPreviewPanel(f, loader);
			return pnl;
		}
		catch(IOException x){
			x.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, 
					"I/O Error: File read error! See stderr for details.", 
					"SMD/SWD Load Error", JOptionPane.ERROR_MESSAGE);
		}
		catch(UnsupportedFileTypeException x){
			x.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, 
					"Parser Error: SMD or bank couldn't be read! See stderr for details.", 
					"SMD/SWD Load Error", JOptionPane.ERROR_MESSAGE);
		}
		

		return null;
	}

	public List<FileAction> getFileActions() {
		//Export to MIDI, Extract, View Hex, Set Bank
		List<FileAction> falist = new ArrayList<FileAction>(4);
		falist.add(new FA_ExportMIDI());
		falist.add(FA_ExtractFile.getAction());
		falist.add(FA_ViewHex.getAction());
		falist.add(new FA_SetBank());
		return falist;
	}

	public Converter getStandardConverter() {
		return SMD.getDefaultConverter();
	}

	public boolean isOfType(FileNode node) {
		return detectFileType(node) != null;
	}

	//----------------------------
	
	public static class FA_ExportMIDI implements FileAction{

		private static String DEFO_ENG = "Export to MIDI";
			
		private String str;
			
		public FA_ExportMIDI(){str = DEFO_ENG;}
			
		public void doAction(FileNode node, NTDProject project, Frame gui_parent) {
				
			JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(NTDProgramFiles.INIKEY_LAST_EXPORTED));
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				
			int select = fc.showSaveDialog(gui_parent);
				
			if(select != JFileChooser.APPROVE_OPTION) return;
			String dir = fc.getSelectedFile().getAbsolutePath();
				
			Converter conv = SMD.getDefaultConverter();
			String targetpath = conv.changeExtension(dir + File.separator + node.getFileName());
			NTDProgramFiles.setIniValue(NTDProgramFiles.INIKEY_LAST_EXPORTED, targetpath);
			IndefProgressDialog dialog = new IndefProgressDialog(gui_parent, "MIDI Conversion");
			dialog.setPrimaryString("Exporting SMD to MIDI");
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
									"SMD Conversion Error", JOptionPane.ERROR_MESSAGE);
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
			
	public static class FA_SetBank implements FileAction{

		private static String DEFO_ENG = "Link Default Bank";
		
		private String str;
		
		public FA_SetBank(){str = DEFO_ENG;}
		
		public void doAction(FileNode node, NTDProject project, Frame gui_parent) {
			
			//Detect all existing SWDs, then load into list dialog
			List<FileNode> bnklist = NTDTools.scanForType(node, FileClass.SOUNDBANK);
			
			NodeSelectDialog dialog = new NodeSelectDialog(gui_parent, bnklist);
			dialog.setLocationRelativeTo(gui_parent);
			dialog.setVisible(true);
			
			if(dialog.getConfirmed()){
				FileNode partner = dialog.getSelection();
				dialog.dispose();
				if(partner != null){
					SWD.partnerSMD(node, partner.getFullPath());
				}
			}
			else{
				dialog.dispose();
				return;
			}
			
		}

		public void setString(String s) {str = s;}
		public String toString(){return str;}
		
	}

}
