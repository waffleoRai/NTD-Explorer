package waffleoRai_NTDTypes.textfiles;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import waffleoRai_Files.Converter;
import waffleoRai_Files.FileBufferInputStream;
import waffleoRai_Files.FileClass;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDTypeLoader;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ViewHex;
import waffleoRai_NTDExGUI.panels.preview.BasicTablePanel;
import waffleoRai_Utils.FileBuffer;

public class TSVManager extends TypeManager{
	
	//Definition
	public static final int DEF_ID = 0x09737654;
	public static final String DEFO_ENG_NAME = "Tab Separated Values Table";
	
	public static final int CHAR_LOAD_LIMIT = 0x100000;
	
	private static TSVFileDefinition stat_def;
	
	public static class TSVFileDefinition implements FileTypeDefinition{

		private String str = DEFO_ENG_NAME;
		
		public Collection<String> getExtensions() {
			List<String> list = new ArrayList<String>(1);
			list.add("tsv");
			return list;
		}

		public String getDescription() {return str;}
		public FileClass getFileClass() {return FileClass.DAT_TABLE;}
		public int getTypeID() {return DEF_ID;}
		public void setDescriptionString(String s) {str = s;}
		public String getDefaultExtension() {return "tsv";}
		public String toString(){return FileTypeDefinition.stringMe(this);}
		
	}

	public static TSVFileDefinition getDefinition(){
		if(stat_def == null) stat_def = new TSVFileDefinition();
		return stat_def;
	}
	
	//Loader
	public static class TSVFileDefLoader implements NTDTypeLoader{

		public TypeManager getTypeManager() {return new TSVManager();}
		public FileTypeDefinition getDefinition() {return TSVManager.getDefinition();}
			
	}
	
	//Manager
	
	public FileTypeNode detectFileType(FileNode node) {
		//No fancy detection right now. Just check extension...
		if(node.getFileName().endsWith(".tsv")) return new FileTypeDefNode(getDefinition());
		return null;
	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {
		
		//Do with table.
		BasicTablePanel pnl = new BasicTablePanel();
		try {
			FileBuffer dat = node.loadDecompressedData();
			
			//Init
			int cols = 0;
			int ccount = 0;
			boolean cmax = false; //If file is longer than can preview
			List<String[]> rows = new LinkedList<String[]>();
			
			//Feed to reader
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileBufferInputStream(dat), "UTF8"));
			String line = null;
			while((line = br.readLine()) != null && !cmax){
				if(line.isEmpty()){
					rows.add(new String[]{" "});
					if(++ccount >= CHAR_LOAD_LIMIT) cmax = true;
					if(cols < 1) cols = 1;
					continue;
				}
				
				String[] fields = line.split("\t");
				//Add and count columns and characters...
				if(fields.length > cols) cols = fields.length;
				rows.add(fields);
				
				for(String s : fields) ccount += s.length();
				if(ccount >= CHAR_LOAD_LIMIT) cmax = true;
			}
			br.close();
			dat.dispose();
			
			//Copy to table
			String[] colnames = BasicTablePanel.genDefaultColNames(cols);
			int rcount = rows.size();
			if(cmax) rcount++;
			
			String[][] tdat = new String[rcount][ccount];
			int r = 0;
			for(String[] row : rows){
				int l = 0;
				while(l < row.length){
					tdat[r][l] = row[l];
					l++;
				}
				while(l < ccount){
					tdat[r][l] = " ";
					l++;
				}
			}
			//Put a "continued" row on the bottom if capped...
			if(cmax){
				for(int l = 0; l < ccount; l++) tdat[rcount-1][l] = "... ";
			}
			
			pnl.setTable(colnames, tdat);
			
		} 
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return pnl;
	}

	public List<FileAction> getFileActions() {
		//Extract, View Hex
		List<FileAction> list = new ArrayList<FileAction>(2);
		list.add(FA_ExtractFile.getAction());
		list.add(FA_ViewHex.getAction());
		return list;
	}

	public Converter getStandardConverter() {return null;}
	public boolean isOfType(FileNode node) {return (detectFileType(node) != null);}
	

}
