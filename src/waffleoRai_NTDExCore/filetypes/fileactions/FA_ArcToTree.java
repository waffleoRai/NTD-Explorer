package waffleoRai_NTDExCore.filetypes.fileactions;

import java.awt.Frame;
import java.io.IOException;

import javax.swing.JOptionPane;

import waffleoRai_Containers.nintendo.NARC;
import waffleoRai_Containers.nintendo.sar.DSSoundArchive;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.NTDTools;
import waffleoRai_Utils.DirectoryNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileNode;

/*
 * File Action for reading the file as an archive (of various types
 * specified by subclasses here) and joining it to the master ROM tree
 * (converting the file node representing the arc to a dir node)
 */

public class FA_ArcToTree {
	
	/* ------ NARC ------*/
	
	private static final String DEFO_ENG_STRING = "Extract archive to ROM tree";
	
	public static class FAMergeTree_Narc implements FileAction
	{
		private String str;
		
		public FAMergeTree_Narc()
		{
			str = DEFO_ENG_STRING;
		}

		@Override
		public void doAction(FileNode node, NTDProject project, Frame gui_parent) 
		{
			//Load file
			//System.err.println("FAMergeTree_Narc.doAction || Called!");
			try
			{
				FileBuffer buffer = node.loadDecompressedData();
				NARC arc = NARC.readNARC(buffer, 0);
				//System.err.println("FAMergeTree_Narc.doAction || File loaded!");
				
				DirectoryNode root = arc.getArchiveTree();
				NTDTools.notateTree(root, node);
				NTDTools.doTypeScan(root, null);
				
				root.setFileName(node.getFileName());
				DirectoryNode arc_parent = node.getParent();
				//System.err.println("arc_parent -- " + arc_parent.getFullPath());
				if(!arc_parent.removeChild(node)) System.err.println("Remove failed!");
				
				root.setParent(arc_parent);
				
				//gui_parent.repaint();
			}
			catch(IOException e)
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(gui_parent, "ERROR: File could not be read!", "I/O Error", JOptionPane.ERROR_MESSAGE);
			} 
			catch (UnsupportedFileTypeException e) 
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(gui_parent, "ERROR: File could not be read as NARC!\n"
						+ "Parser Message: " + e.getErrorMessage(), "Parser Error", JOptionPane.ERROR_MESSAGE);
			}
			
		}

		@Override
		public void setString(String s) {
			str = s;
		}
		
		public String toString()
		{
			return str;
		}
		
	}

	private static FAMergeTree_Narc static_narc;
	
	public static FileAction getAction_NARC()
	{
		if(static_narc != null) return static_narc;
		static_narc = new FAMergeTree_Narc();
		return static_narc;
	}
	
	/* ------ SDAT ------*/
	
	public static class FAMergeTree_SDAT implements FileAction
	{
		private String str;
		
		public FAMergeTree_SDAT()
		{
			str = DEFO_ENG_STRING;
		}

		@Override
		public void doAction(FileNode node, NTDProject project, Frame gui_parent) 
		{
			//Load file
			//System.err.println("FAMergeTree_Narc.doAction || Called!");
			try
			{
				FileBuffer buffer = node.loadDecompressedData();
				//NARC arc = NARC.readNARC(buffer, 0);
				DSSoundArchive arc = DSSoundArchive.readSDAT(buffer);
				//System.err.println("FAMergeTree_Narc.doAction || File loaded!");
				
				DirectoryNode root = arc.getArchiveView();
				NTDTools.notateTree(root, node);
				NTDTools.doTypeScan(root, null);
				
				root.setFileName(node.getFileName());
				DirectoryNode arc_parent = node.getParent();
				//System.err.println("arc_parent -- " + arc_parent.getFullPath());
				if(!arc_parent.removeChild(node)) System.err.println("Remove failed!");
				
				root.setParent(arc_parent);
				
				//gui_parent.repaint();
			}
			catch(IOException e)
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(gui_parent, "ERROR: File could not be read!", "I/O Error", JOptionPane.ERROR_MESSAGE);
			} 
			
		}

		@Override
		public void setString(String s) {
			str = s;
		}
		
		public String toString()
		{
			return str;
		}
		
	}

	private static FAMergeTree_SDAT static_sdat;
	
	public static FileAction getAction_SDAT()
	{
		if(static_sdat != null) return static_sdat;
		static_sdat = new FAMergeTree_SDAT();
		return static_sdat;
	}
	
}
