package waffleoRai_NTDExCore.importer;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import waffleoRai_NTDExCore.Console;
import waffleoRai_NTDExCore.GameRegion;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;

public class PSXParserOption implements ParserOption{
	
	public static final String DEFO_ENG_STR = "Sony PlayStation 1 Raw Disc Track (.iso)";
	public static final String DEFO_ENG_FFSTR = "Sony PlayStation 1 Raw Disc Track";
	
	private String optionString;
	private String fileFilterString;
	
	public PSXParserOption(){
		optionString = DEFO_ENG_STR;
		fileFilterString = DEFO_ENG_FFSTR;
	}

	public void setOptionString(String op) {
		optionString = op;
	}

	public void setFileFilterString(String s){
		fileFilterString = s;
	}

	@Override
	public NTDProject generateProject(String path, GameRegion reg, Frame dialog_parent) throws IOException {
		// TODO Auto-generated method stub
		//Spawn dialog...
		IndefProgressDialog dialog = new IndefProgressDialog(dialog_parent, "Importing PS1 Image...");
				
		dialog.setPrimaryString("Reading");
		dialog.setSecondaryString("Parsing PSX track data from " + path);
				
		NTDProject proj = null;
		
		return null;
	}

	public Console getConsole() {return Console.PS1;}

	public List<FileFilter> getExtFilters(){
		List<FileFilter> list = new LinkedList<FileFilter>();
		list.add(new FileFilter(){

			@Override
			public boolean accept(File f) {
				if(f.isDirectory()) return true;
				String path = f.getAbsolutePath();
				return path.endsWith(".iso") || path.endsWith(".ISO");
			}

			@Override
			public String getDescription() {
				return fileFilterString + " (.iso)";
			}
			
		});
		return list;
	}
	
	public String toString(){return optionString;}

}
