package waffleoRai_NTDTypes.textfiles;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JPanel;

import waffleoRai_Files.Converter;
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
import waffleoRai_NTDExGUI.panels.preview.text.EncodedTextPanel;

public class TXTManager extends TypeManager{

	//Definition
	public static final int DEF_ID = 0x74657874;
	public static final String DEFO_ENG_NAME = "Text File";
	
	public static final String METAKEY_ENCODING = "STRENCODING"; //Defaults to UTF-8
	
	private static TextFileDefinition stat_def;
	
	public static class TextFileDefinition implements FileTypeDefinition{

		private String str = DEFO_ENG_NAME;
		
		public Collection<String> getExtensions() {
			List<String> list = new ArrayList<String>(1);
			list.add("txt");
			return list;
		}

		public String getDescription() {return str;}
		public FileClass getFileClass() {return FileClass.TEXT_FILE;}
		public int getTypeID() {return DEF_ID;}
		public void setDescriptionString(String s) {str = s;}
		public String getDefaultExtension() {return "txt";}
		
	}

	public static TextFileDefinition getDefinition(){
		if(stat_def == null) stat_def = new TextFileDefinition();
		return stat_def;
	}
	
	//Loader
	public static class TXTFileDefLoader implements NTDTypeLoader{

		public TypeManager getTypeManager() {return new TXTManager();}
		public FileTypeDefinition getDefinition() {return TXTManager.getDefinition();}
			
	}
	
	//Manager
	
	public FileTypeNode detectFileType(FileNode node) {
		//No fancy detection right now. Just check extension...
		if(node.getFileName().endsWith(".txt")) return new FileTypeDefNode(getDefinition());
		return null;
	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {
		
		EncodedTextPanel pnl = new EncodedTextPanel();
		pnl.setNode(node);
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
