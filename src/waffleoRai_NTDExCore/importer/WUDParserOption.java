package waffleoRai_NTDExCore.importer;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JFileChooser;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import waffleoRai_NTDExCore.Console;
import waffleoRai_NTDExCore.GameRegion;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.consoleproj.WiiUProject;
import waffleoRai_NTDExGUI.dialogs.ImportDialog;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_Utils.FileBuffer;

public class WUDParserOption implements ParserOption{

	public static final String DEFO_ENG_STR = "Nintendo WiiU Raw Disc Image (.wud)";
	public static final String DEFO_ENG_FFSTR = "Nintendo WiiU Disc Image";
	
	private String optionString;
	private String fileFilterString;
	
	public WUDParserOption(){
		optionString = DEFO_ENG_STR;
		fileFilterString = DEFO_ENG_FFSTR;
	}
	
	public void setOptionString(String op) {
		optionString = op;
	}

	public void setFileFilterString(String s){
		fileFilterString = s;
	}
	
	public NTDProject generateProject(String path, GameRegion reg, Frame dialog_spawn_parent) throws IOException {
		//Spawn dialog...
		IndefProgressDialog dialog = new IndefProgressDialog(dialog_spawn_parent, "Importing Wii U Image...");
		
		dialog.setPrimaryString("Reading");
		dialog.setSecondaryString("Parsing Wii U disc data from " + path);
		
		//Will need key!!!
		String lastpath = NTDProgramFiles.getIniValue(ImportDialog.LAST_BROWSE_PATH_KEY);
		JFileChooser fc = new JFileChooser(lastpath);
		fc.setDialogTitle("Select Game Key");
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.addChoosableFileFilter(new FileFilter(){

			public boolean accept(File f) {
				if(f == null) return false;
				if(f.isDirectory()) return true;
				String path = f.getAbsolutePath();
				if(path == null) return false;
				return path.endsWith(File.separator + "game.key");
			}

			public String getDescription() {
				return "Wii U Disc Unique Key (game.key)";
			}});
		
		int retval = fc.showOpenDialog(dialog_spawn_parent);
		if(retval != JFileChooser.APPROVE_OPTION){
			return null;
		}
		String gkpath = fc.getSelectedFile().getAbsolutePath();
			
		NTDProject proj = null;
		
		SwingWorker<NTDProject, NTDProject> task = new SwingWorker<NTDProject, NTDProject>()
		{

			protected NTDProject doInBackground() throws Exception 
			{
				NTDProject p = null;
				try
				{
					//Read key
					dialog.setPrimaryString("Importing");
					dialog.setSecondaryString("Loading game key");
					byte[] gkey = FileBuffer.createBuffer(gkpath).getBytes();
					
					
					//Load project
					dialog.setPrimaryString("Importing");
					dialog.setSecondaryString("Loading data from \"" + path + "\"");
					p = WiiUProject.createFromWUD(path, gkey, reg, dialog);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					dialog.showWarningMessage("ERROR: Import failed! See stderr for details.");
				}
				return p;
			}
			
			public void done(){
				dialog.closeMe();
			}
			
		};
		task.execute();
		dialog.render();
		
		try {proj = task.get();} 
		catch (InterruptedException e) {e.printStackTrace();} 
		catch (ExecutionException e) {e.printStackTrace();}
		
		return proj;
	}

	public Console getConsole() {return Console.WIIU;}

	public String toString(){return optionString;}
	
	public List<FileFilter> getExtFilters()
	{
		List<FileFilter> list = new LinkedList<FileFilter>();
		list.add(new FileFilter(){

			public boolean accept(File f) {
				if(f.isDirectory()) return true;
				String path = f.getAbsolutePath().toLowerCase();
				return path.endsWith(".wud");
			}

			public String getDescription() {
				return fileFilterString + " (.wud)";
			}
			
		});
		return list;
	}
	
}
