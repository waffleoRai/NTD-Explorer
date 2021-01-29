package waffleoRai_NTDExCore.filetypes.fileactions;

import java.awt.Frame;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import waffleoRai_Files.Converter;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;

public abstract class FA_ExportFile implements FileAction{

private static final String DEFO_ENG_STRING = "Export File";
	
	private String str;
	
	public FA_ExportFile(){
		str = DEFO_ENG_STRING;
		onConstruct();
	}

	protected void onConstruct(){}
	protected abstract Converter getConverter(FileNode node);
	
	public void doAction(FileNode node, NTDProject project, Frame gui_parent) {
		
		//Nab converter
		Converter c = getConverter(node);
		if(c == null){
			JOptionPane.showMessageDialog(gui_parent, 
					"Error: Export failed - no target format specified for this type!", 
					"No Conversion Target", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//Or maybe just call the extract method from elsewhere?
		JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(NTDProgramFiles.INIKEY_LAST_EXPORTED));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		int select = fc.showSaveDialog(gui_parent);
		
		if(select != JFileChooser.APPROVE_OPTION) return;
		String dir = fc.getSelectedFile().getAbsolutePath();
		
		//Get output path
		String targetpath = dir + File.separator + c.changeExtension(node.getFileName());
		
		//Spawn task and dialog
		NTDProgramFiles.setIniValue(NTDProgramFiles.INIKEY_LAST_EXPORTED, targetpath);
		IndefProgressDialog dialog = new IndefProgressDialog(gui_parent, "File Export");
		dialog.setPrimaryString("Converting Data");
		dialog.setSecondaryString("Exporting to " + targetpath);
		
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{

			protected Void doInBackground() throws Exception 
			{
				try
				{
					c.writeAsTargetFormat(node, targetpath);
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
	
	
}
