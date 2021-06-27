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
import waffleoRai_NTDExCore.consoleproj.NUSProject;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;

public class NUSParserOption implements ParserOption{
	
	public static final String DEFO_ENG_STR = "Nintendo 64 GamePak ROM Image (.n64, .z64, .v64, .p64)";
	public static final String DEFO_ENG_FFSTR = "Nintendo 64 GamePak ROM Image";
	
	private String optionString = DEFO_ENG_STR;
	private String fileFilterString = DEFO_ENG_FFSTR;

	@Override
	public void setOptionString(String op) {
		optionString = op;
	}

	@Override
	public NTDProject generateProject(String path, GameRegion reg, Frame dialog_parent) throws IOException {
		IndefProgressDialog dialog = new IndefProgressDialog(dialog_parent, "Importing N64 Image...");
		
		dialog.setPrimaryString("Reading");
		dialog.setSecondaryString("Parsing N64 ROM data from " + path);
		
		NTDProject proj = null;
		
		SwingWorker<NTDProject, NTDProject> task = new SwingWorker<NTDProject, NTDProject>(){
			protected NTDProject doInBackground() throws Exception {
				NTDProject p = null;
				try{
					//Load project
					dialog.setPrimaryString("Importing");
					dialog.setSecondaryString("Loading data from \"" + path + "\"");
					p = NUSProject.createFromROMImage(path, reg);
					
					if(NTDProgramFiles.gameHasBeenLoaded(Console.N64, p.getGameCode4(), reg)){
						//TODO Update this eventually to allow for multiple versions
						dialog.showWarningMessage("An image of this game has apparently been imported previously!\n"
								+ "Import will be cancelled.");
						return null;
					}	
				}
				catch (Exception e){
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

	@Override
	public Console getConsole() {
		return Console.N64;
	}

	public String toString(){return optionString;}
	
	@Override
	public List<FileFilter> getExtFilters() {
		List<FileFilter> list = new LinkedList<FileFilter>();
		list.add(new FileFilter(){

			public boolean accept(File f) {
				if(f.isDirectory()) return true;
				String path = f.getAbsolutePath().toLowerCase();
				return path.endsWith(".z64") || path.endsWith(".n64") || path.endsWith(".v64") || path.endsWith(".p64");
			}

			public String getDescription() {
				return fileFilterString + " (.z64, .n64, .v64, .p64)";
			}
			
		});
		return list;
	}

}
