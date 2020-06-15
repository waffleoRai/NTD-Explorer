package waffleoRai_NTDTypes.nitro;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import waffleoRai_Containers.nintendo.sar.DSSoundArchive;
import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDTools;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ArcToTree;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_DumpArc;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ViewHex;
import waffleoRai_NTDExGUI.TreePanel;
import waffleoRai_Utils.DirectoryNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileNode;

public class TM_SDAT extends TypeManager{
	
	public FileTypeNode detectFileType(FileNode node) {
		return NitroFiles.detectNitroFile(node, DSSoundArchive.MAGIC, DSSoundArchive.getTypeDef(), false);
	}

	@Override
	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {

		try 
		{
			FileBuffer buffer = node.loadDecompressedData();
			//System.err.println("Buffer size: 0x" + Long.toHexString(buffer.getFileSize()));
			
			//Parse
			DSSoundArchive arc = DSSoundArchive.readSDAT(buffer);
			DirectoryNode root = arc.getArchiveView();
			
			//Scan for known types (icons r sxy)
			NTDTools.notateTree(root, node);
			NTDTools.doTypeScan(root, null);
			
			//Return panel
			return new TreePanel(root);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, "Failed to load preview!\n"
					+ "File marked as SDAT type, but could not be read as such.", 
					"Preview Load Failed", JOptionPane.ERROR_MESSAGE);
		} 
		
		
		return null;
	}

	public List<FileAction> getFileActions() {
		//View hex, mount tree, export, extract
		List<FileAction> falist = new ArrayList<FileAction>(6);
		falist.add(FA_ArcToTree.getAction_SDAT());
		falist.add(FA_DumpArc.getSDATAction());
		falist.add(FA_ExtractFile.getAction());
		falist.add(FA_ViewHex.getAction());
		
		return falist;
	}

	@Override
	public Converter getStandardConverter() {
		return DSSoundArchive.getConverter();
	}

	public boolean isOfType(FileNode node) {
		return detectFileType(node) != null;
	}

}
