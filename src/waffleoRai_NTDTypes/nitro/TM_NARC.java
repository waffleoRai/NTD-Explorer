package waffleoRai_NTDTypes.nitro;

import java.awt.Component;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import waffleoRai_Containers.nintendo.NARC;
import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDTools;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ArcToTree;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_DumpArc;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ViewHex;
import waffleoRai_NTDExGUI.TreePanel;
import waffleoRai_Utils.DirectoryNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileNode;

public class TM_NARC extends TypeManager{
	
	public FileTypeNode detectFileType(FileNode node) {
		return NitroFiles.detectNitroFile(node, NARC.MAGIC, NARC.getTypeDef(), false);
	}

	@Override
	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) 
	{
		//Read in
		try 
		{
			FileBuffer buffer = node.loadDecompressedData();
			//System.err.println("Buffer size: 0x" + Long.toHexString(buffer.getFileSize()));
			
			//Parse
			NARC arc = NARC.readNARC(buffer, 0);
			DirectoryNode root = arc.getArchiveTree();
			NTDTools.doTypeScan(root, null);
			
			//Return panel
			//return new ArchivePreviewPanel(arc.getArchiveTree());
			return new TreePanel(root);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, "Failed to load preview!\n"
					+ "File marked as NARC type, but could not be read as such.", 
					"Preview Load Failed", JOptionPane.ERROR_MESSAGE);
		} 
		catch (UnsupportedFileTypeException e) 
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, "Failed to load preview!\n"
					+ "File marked as NARC type, but could not be read as such.\n"
					+ "Parser Message: " + e.getErrorMessage(), 
					"Preview Load Failed", JOptionPane.ERROR_MESSAGE);
		}
		
		return null;
	}

	@Override
	public List<FileAction> getFileActions() 
	{
		/*
		 * Merge to tree
		 * Dump to disk (needs option dialog)
		 * View hex (needs option dialog if compressed to choose compressed or decomp)
		 */
		
		List<FileAction> list = new LinkedList<FileAction>();
		
		list.add(FA_ArcToTree.getAction_NARC());
		list.add(FA_DumpArc.getNARCAction());
		list.add(FA_ViewHex.getAction());
		
		return list;
	}

	public Converter getStandardConverter()
	{
		return NARC.getConverter();
	}
	
	public boolean isOfType(FileNode node)
	{
		//Checks for NARC magic
		return(detectFileType(node) != null);
	}
	
}
