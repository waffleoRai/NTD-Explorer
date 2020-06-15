package waffleoRai_NTDTypes.procyon;

import java.awt.Component;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import waffleoRai_NTDExGUI.dialogs.NodeSelectDialog;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_NTDExGUI.panels.preview.HexPreviewPanel;
import waffleoRai_NTDExGUI.panels.preview.SimpleSoundbankTreePanel;
import waffleoRai_NTDExGUI.panels.preview.WaveArchiveViewPanel;
import waffleoRai_Sound.Sound;
import waffleoRai_Utils.DirectoryNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileNode;
import waffleoRai_soundbank.SoundbankNode;
import waffleoRai_soundbank.procyon.SWD;

public class TM_ProcyonSWD extends TypeManager{

	public FileTypeNode detectFileType(FileNode node) {
		String path = node.getSourcePath();
		long offset = node.getOffset();
		
		try{
			FileBuffer head = new FileBuffer(path, offset, offset+0x10, false);
			long mpos = head.findString(0, 0x10, SWD.MAGIC);
			if(mpos != 0) return null;
			
			return new FileTypeDefNode(SWD.getDefinition());
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {

		//Need to check if it's an wave archive type or bank type.
		try {
			SWD swd = SWD.readSWD(node.loadDecompressedData(), 0);
			
			//If it's a bank type, see if it has sound partner.
			if(swd.hasArticulationData()) {
				if(!swd.hasSoundData()){
					if(!swd.loadPartnerSWD(node)){
						JOptionPane.showMessageDialog(gui_parent, 
								"Sound Data Error: SWD has no sound data or partner SWD with sound data!", 
								"SWD Preview Error", JOptionPane.WARNING_MESSAGE);
						HexPreviewPanel pnl = new HexPreviewPanel();
						pnl.load(node, 0, false);
						return pnl;
					}	
				}
				
				//Bank panel
				SoundbankNode root = swd.getSoundbankTree();
				
				Collection<Integer> bnos = new ArrayList<Integer>(1); bnos.add(0);
				Collection<Integer> pnos = swd.getUsablePrograms();
				
				SimpleSoundbankTreePanel pnl = new SimpleSoundbankTreePanel(root);
				pnl.loadPlayer(swd, bnos, pnos);
				pnl.startPlayer();
				
				return pnl;
			}
			else{
				//Warc panel
				
				Map<String, Sound> smap = new HashMap<String, Sound>();
				int wcount = swd.countWaveSlots();
				for(int i = 0; i < wcount; i++){
					String wname = "swd_wavi_" + String.format("%04d", i);
					Sound snd = swd.getWave(i);
					if(snd != null) smap.put(wname, snd);
				}
				
				WaveArchiveViewPanel pnl = new WaveArchiveViewPanel(smap);
				
				return pnl;
			}
			
		} 
		catch (UnsupportedFileTypeException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, 
					"Parser Error: SWD could not be read! See stderr for details.", 
					"SWD Preview Error", JOptionPane.ERROR_MESSAGE);
			return new JPanel();
		} 
		catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, 
					"I/O Error: SWD could not be opened! See stderr for details.", 
					"SWD Preview Error", JOptionPane.ERROR_MESSAGE);
			return new JPanel();
		}
	}

	public List<FileAction> getFileActions() {
		//Extract, Convert to SF2, View Hex, Link PCMD, Set as dir sound source
		List<FileAction> falist = new ArrayList<FileAction>(4);
		falist.add(new FA_ExportSF2());
		falist.add(FA_ExtractFile.getAction());
		falist.add(FA_ViewHex.getAction());
		falist.add(new FA_LinkBank());
		falist.add(new FA_SetAsPCMD());
				
		return falist;
	}

	public Converter getStandardConverter() {
		return SWD.getDefaultConverter();
	}

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
			
			Converter conv = SWD.getDefaultConverter();
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
								"SWD Conversion Error", JOptionPane.ERROR_MESSAGE);
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
	
	public static class FA_LinkBank implements FileAction{

		private static String DEFO_ENG = "Link Wave Bank";
		
		private String str;
		
		public FA_LinkBank(){str = DEFO_ENG;}
		
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
					SWD.partnerSWD(node, partner.getFullPath());
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

	public static class FA_SetAsPCMD implements FileAction{

		private static String DEFO_ENG = "Set As Directory Wave Source";
		
		private String str;
		
		public FA_SetAsPCMD(){str = DEFO_ENG;}
		
		public void doAction(FileNode node, NTDProject project, Frame gui_parent) {
			
			//Load this SWD to make sure that it actually has wave data
			
			try {
				SWD swd = SWD.readSWD(node.loadDecompressedData(), 0);
				if(!swd.hasSoundData()){
					JOptionPane.showMessageDialog(gui_parent, 
							"This SWD does not have wave data!", 
							"SWD Link Error", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				//Link for every known SWD in the directory.
				DirectoryNode dir = node.getParent();
				List<FileNode> sibs = dir.getChildren();
				for(FileNode sib : sibs){
					if(!(sib instanceof DirectoryNode)){
						//Check type.
						FileTypeNode sibtype = sib.getTypeChainTail();
						if(sibtype.getTypeID() == SWD.TYPE_ID){
							//Link
							SWD.partnerSWD(sib, node.getFileName());
						}
					}
				}
			} 
			catch (UnsupportedFileTypeException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(gui_parent, 
						"Parser Error: File could not be read as SWD!", 
						"SWD Link Error", JOptionPane.ERROR_MESSAGE);
				return;
			} 
			catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(gui_parent, 
						"I/O Error: Error loading file!", 
						"SWD Link Error", JOptionPane.ERROR_MESSAGE);
			}
			
			
		}

		public void setString(String s) {str = s;}
		public String toString(){return str;}
		
	}

	
}
