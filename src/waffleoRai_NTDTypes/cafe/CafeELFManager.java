package waffleoRai_NTDTypes.cafe;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_fdefs.nintendo.PowerGCSysFileDefs;

public class CafeELFManager extends TypeManager{

	public FileTypeNode detectFileType(FileNode node) {
		if(node.getLength() == 0) return null;
		String nname = node.getFileName();
		if(!(nname.endsWith(".rpx") || nname.endsWith(".rpl"))) return null;
		
		FileBuffer dat = null;
		long ed = 0x10;
		if(node.getLength() < ed) ed = node.getLength();
		try {
			if(node.hasCompression()) dat = node.loadDecompressedData();
			else dat = node.loadData(0, ed);
			
			//Check for magic #
			int magic = 0x7f454c46;
			dat.setEndian(true);
			int first = dat.intFromFile(0);
			
			if(first == magic){
				if(nname.endsWith(".rpx")) return new FileTypeDefNode(PowerGCSysFileDefs.getWiiUExeDef());
				if(nname.endsWith(".rpl")) return new FileTypeDefNode(PowerGCSysFileDefs.getWiiURPLDef());
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {
		return TypeManager.getDefaultManager().generatePreviewPanel(node, gui_parent);
	}

	public List<FileAction> getFileActions() {
		List<FileAction> list = new ArrayList<FileAction>(2);
		list.add(FA_ExtractFile.getAction());
		//list.add(FA_ViewHex.getAction());
		return list;
	}

	public Converter getStandardConverter() {return null;}
	public boolean isOfType(FileNode node) {return (detectFileType(node) != null);}

}
