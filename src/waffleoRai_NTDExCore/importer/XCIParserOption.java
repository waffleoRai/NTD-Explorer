package waffleoRai_NTDExCore.importer;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import waffleoRai_NTDExCore.Console;
import waffleoRai_NTDExCore.GameRegion;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.consoleproj.NXProject;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;

public class XCIParserOption implements ParserOption{

	public static final String DEFO_ENG_STR = "Nintendo Switch Cartridge Image (.xci)";
	public static final String DEFO_ENG_FFSTR = "Nintendo Switch Cartridge Image";
	
	private String optionString;
	private String fileFilterString;
	
	public XCIParserOption(){
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
		IndefProgressDialog dialog = new IndefProgressDialog(dialog_spawn_parent, "Importing NX Cart Image...");
		
		dialog.setPrimaryString("Reading");
		dialog.setSecondaryString("Initializing " + path);
		
		//First, ask user for 5 alphanumeric character gamecode
		//(eg. BOtW is "AAAAA")
		String code5 = null;
		/*HACGamecodeDialog gcdia = new HACGamecodeDialog(dialog_spawn_parent);
		gcdia.setVisible(true);
		switch(gcdia.getSelection()){
		case HACGamecodeDialog.SELECTION_CANCEL: return null;
		case HACGamecodeDialog.SELECTION_OKAY:
			code5 = gcdia.getCode(); break;
		case HACGamecodeDialog.SELECTION_UNKNOWN:
			break; //Leave null for NXProject importer to generate
		}*/
		
		NTDProject proj = null;
		
		String finalcode = code5;
		SwingWorker<NTDProject, NTDProject> task = new SwingWorker<NTDProject, NTDProject>()
		{

			protected NTDProject doInBackground() throws Exception 
			{
				NTDProject p = null;
				try
				{
					//Load project
					dialog.setPrimaryString("Importing");
					dialog.setSecondaryString("Parsing file tree from \"" + path + "\"");
					p = NXProject.createFromXCI(path, finalcode, reg);
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

	public Console getConsole() {return Console.SWITCH;}

	public String toString(){return optionString;}
	
	public List<FileFilter> getExtFilters()
	{
		List<FileFilter> list = new LinkedList<FileFilter>();
		list.add(new FileFilter(){

			public boolean accept(File f) {
				if(f.isDirectory()) return true;
				String path = f.getAbsolutePath().toLowerCase();
				return path.endsWith(".xci");
			}

			public String getDescription() {
				return fileFilterString + " (.xci)";
			}
			
		});
		return list;
	}
	
	
}
