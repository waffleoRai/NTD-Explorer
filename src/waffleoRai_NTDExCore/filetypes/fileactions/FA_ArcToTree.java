package waffleoRai_NTDExCore.filetypes.fileactions;

import java.awt.Component;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import waffleoRai_Compression.definitions.*;
import waffleoRai_Containers.nintendo.NARC;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
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
	
	private static final String DEFO_ENG_STRING = "Extract archive to ROM tree";
	
	private static List<CompressionInfoNode> getCompressionChain(FileNode archive)
	{
		List<CompressionInfoNode> list = new LinkedList<CompressionInfoNode>();
		FileTypeNode t = archive.getTypeChainHead();
		
		long off = archive.getOffset();
		long len = archive.getLength();
		while(t != null)
		{
			if(t.isCompression())
			{
				AbstractCompDef def = ((CompDefNode)t).getDefinition();
				list.add(new CompressionInfoNode(def, off, len));
				t = t.getChild();
				off = 0;
				len = -1;
			}
		}
		
		return list;
	}
	
	private static void notateDir(DirectoryNode dir, List<CompressionInfoNode> chain, String srcpath)
	{
		List<FileNode> children = dir.getChildren();
		for(FileNode child : children)
		{
			if(child instanceof DirectoryNode)
			{
				notateDir(((DirectoryNode)child), chain, srcpath);
			}
			else
			{
				for(CompressionInfoNode c : chain)
				{
					child.addCompressionChainNode(c.getDefinition(), c.getStartOffset(), c.getLength());
				}
				child.setSourcePath(srcpath);
			}
		}
		
	}
	
	private static void notateTree(DirectoryNode root, FileNode archive)
	{
		/*Basically, this function notes the source archive file's
		 * compression routines in the nodes of its contents.
		 * 
		 * That way, when the project tree is saved with these noted, these
		 * internal files can be loaded without later without having to re-parse
		 * the source archive. (Though it does have to be decompressed).
		 */
		
		notateDir(root, getCompressionChain(archive), archive.getSourcePath());
		
	}
	
	public static class FAMergeTree_Narc implements FileAction
	{
		private String str;
		
		public FAMergeTree_Narc()
		{
			str = DEFO_ENG_STRING;
		}

		@Override
		public void doAction(FileNode node, NTDProject project, Component gui_parent) 
		{
			//Load file
			try
			{
				FileBuffer buffer = NTDProgramFiles.openAndDecompress(node);
				NARC arc = NARC.readNARC(buffer, 0);
				
				DirectoryNode root = arc.getArchiveTree();
				notateTree(root, node);
				
				root.setFileName(node.getFileName());
				DirectoryNode arc_parent = node.getParent();
				arc_parent.removeChild(node);
				
				root.setParent(arc_parent);
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
	
}
