package waffleoRai_NTDExCore.filetypes.fileactions;

import java.awt.Frame;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_Utils.FileNode;

public class FA_ExtractFile implements FileAction{

	private static final String DEFO_ENG_STRING = "Extract file";
	
	private static FA_ExtractFile static_me;
	
	private String str;
	
	public FA_ExtractFile(){
		str = DEFO_ENG_STRING;
	}

	public void doAction(FileNode node, NTDProject project, Frame gui_parent) {
		
		//Or maybe just call the extract method from elsewhere?
		JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(NTDProgramFiles.INIKEY_LAST_EXTRACTED));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		int select = fc.showSaveDialog(gui_parent);
		
		if(select != JFileChooser.APPROVE_OPTION) return;
		String dir = fc.getSelectedFile().getAbsolutePath();
		
		//Option dialog asking if should decompress?
		int decomp_choice = JOptionPane.showConfirmDialog(gui_parent, 
				"Would you like to decompress extracted file(s)?", 
				"Decompress & Extract", JOptionPane.YES_NO_CANCEL_OPTION, 
				JOptionPane.QUESTION_MESSAGE);
		if(decomp_choice == JOptionPane.CANCEL_OPTION) return;
		
		//Spawn task and dialog
		String targetpath = dir + File.separator + node.getFileName();
		IndefProgressDialog dialog = new IndefProgressDialog(gui_parent, "File Extraction");
		dialog.setPrimaryString("Extracting Data");
		dialog.setSecondaryString("Extracting to " + targetpath);
		
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{

			protected Void doInBackground() throws Exception 
			{
				try
				{
					boolean decomp = (decomp_choice == JOptionPane.YES_OPTION);
					node.copyDataTo(targetpath, decomp);
				}
				catch(Exception x)
				{
					x.printStackTrace();
					JOptionPane.showMessageDialog(gui_parent, 
							"Unknown Error: Extraction Failed! See stderr for details.", 
							"File Extraction Error", JOptionPane.ERROR_MESSAGE);
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

	public void setString(String s) {
		str = s;
	}
	
	public String toString(){
		return str;
	}
	
	public static FileAction getAction()
	{
		if(static_me == null) static_me = new FA_ExtractFile();
		return static_me;
	}
	
}
