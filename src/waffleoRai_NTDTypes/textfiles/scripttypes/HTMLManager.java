package waffleoRai_NTDTypes.textfiles.scripttypes;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;
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
import waffleoRai_NTDExGUI.panels.preview.WriterPanel;
import waffleoRai_Utils.FileBuffer;

public class HTMLManager extends TypeManager{
	
	public static final int DEF_ID = 0x48544d4c;
	public static final String DEFO_ENG_NAME = "Hypertext Markup Language Document";
	
	private static HTMLFileDefinition stat_def;
	
	public static class HTMLFileDefinition implements FileTypeDefinition{

		private String str = DEFO_ENG_NAME;
		
		public Collection<String> getExtensions() {
			List<String> list = new ArrayList<String>(1);
			list.add("html");
			return list;
		}

		public String getDescription() {return str;}
		public FileClass getFileClass() {return FileClass.MARKUP_SCRIPT;}
		public int getTypeID() {return DEF_ID;}
		public void setDescriptionString(String s) {str = s;}
		public String getDefaultExtension() {return "html";}
		
	}
	
	public static HTMLFileDefinition getDefinition(){
		if(stat_def == null) stat_def = new HTMLFileDefinition();
		return stat_def;
	}
	
	//Loader
	public static class HTMLFileDefLoader implements NTDTypeLoader{

		public TypeManager getTypeManager() {return new HTMLManager();}
		public FileTypeDefinition getDefinition() {return HTMLManager.getDefinition();}
			
	}
	
	//Manager

	public FileTypeNode detectFileType(FileNode node) {
		if(node == null) return null;
		try{
			FileBuffer dat = null;
			if(node.hasCompression()) dat = node.loadDecompressedData();
			else{
				long end = node.getLength();
				if(end > 0x40) end = 0x40;
				dat = node.loadData(0, end);
			}
			
			long cend = 0x40;
			if(dat.getFileSize() < cend) cend = dat.getFileSize();
			long spos = dat.findString(0, cend, "<!DOCTYPE html>");
			if(spos >= 0 && spos < 0x40) return new FileTypeDefNode(getDefinition());
			spos = dat.findString(0, cend, "<html");
			if(spos >= 0 && spos < 0x40) return new FileTypeDefNode(getDefinition());
		}
		catch(Exception x){
			x.printStackTrace();
			return null;
		}
		
		return null;
	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {

		try{
			WriterPanel pnl = new WriterPanel();
			FileBufferInputStream is = new FileBufferInputStream(node.loadDecompressedData());
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"));
			Writer w = pnl.getWriter();
			String line = null;
			while((line = reader.readLine()) != null){
				w.write(line + "\n");
			}
			reader.close();
			w.close();
			
			return pnl;
		}
		catch(IOException x){
			x.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, "I/O Error: File could not be loaded for preview!", 
					"Preview Failed", JOptionPane.ERROR_MESSAGE);
		}
		
		return null;
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
