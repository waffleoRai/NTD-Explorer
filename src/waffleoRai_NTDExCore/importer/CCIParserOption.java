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
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.consoleproj.CitrusProject;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;

public class CCIParserOption implements ParserOption{
	
	public static final String DEFO_ENG_STR = "Nintendo 3DS CCI(Cart) Image (.3ds, .cci)";
	public static final String DEFO_ENG_FFSTR = "Nintendo 3DS CCI(Cart) Image";
	
	private String optionString;
	private String fileFilterString;
	
	public CCIParserOption(){
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
		IndefProgressDialog dialog = new IndefProgressDialog(dialog_spawn_parent, "Importing 3DS Cartridge Image");
		
		dialog.setPrimaryString("Reading");
		dialog.setSecondaryString("Parsing CCI data from " + path);
		
		NTDProject proj = null;
		
		SwingWorker<NTDProject, NTDProject> task = new SwingWorker<NTDProject, NTDProject>()
		{

			protected NTDProject doInBackground() throws Exception 
			{
				NTDProject p = null;
				try
				{
					
					//Load project
					dialog.setPrimaryString("Importing");
					dialog.setSecondaryString("Loading data from \"" + path + "\"");
					p = CitrusProject.create3DSProject(path, reg, dialog);
					
					if(NTDProgramFiles.gameHasBeenLoaded(Console._3DS, p.getGameCode4(), reg))
					{
						dialog.showWarningMessage("An image of this game has apparently been imported previously!\n"
								+ "Import will be cancelled.");
						return null;
					}	
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

	public Console getConsole() {return Console._3DS;}

	public String toString(){return optionString;}
	
	public List<FileFilter> getExtFilters()
	{
		List<FileFilter> list = new LinkedList<FileFilter>();
		list.add(new FileFilter(){

			public boolean accept(File f) {
				if(f.isDirectory()) return true;
				String path = f.getAbsolutePath().toLowerCase();
				return path.endsWith(".3ds") || path.endsWith(".cci");
			}

			public String getDescription() {
				return fileFilterString + " (.3ds, .cci)";
			}
			
		});
		return list;
	}

}
