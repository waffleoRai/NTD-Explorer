package waffleoRai_NTDExCore.filetypes.archive;

import java.awt.Component;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import waffleoRai_Compression.definitions.AbstractCompDef;
import waffleoRai_Compression.definitions.CompDefNode;
import waffleoRai_Compression.nintendo.DSCompHeader;
import waffleoRai_Compression.nintendo.DSRLE;
import waffleoRai_Compression.nintendo.NinHuff;
import waffleoRai_Compression.nintendo.NinLZ;
import waffleoRai_Containers.nintendo.NARC;
import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ArcToTree;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_DumpArc;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ViewHex;
import waffleoRai_NTDExGUI.panels.preview.ArchivePreviewPanel;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileBufferStreamer;
import waffleoRai_Utils.FileNode;
import waffleoRai_Utils.StreamWrapper;

public class TM_NARC extends TypeManager{
	
	private boolean checkDecompMagic(StreamWrapper in)
	{
		if(in == null) return false;
		byte[] magic_bytes = NARC.MAGIC.getBytes();
		for(int i = 0; i < magic_bytes.length; i++)
		{
			if(in.get() != magic_bytes[i]) return false;
		}
		return true;
	}

	@Override
	public FileTypeNode detectFileType(FileNode node) 
	{
		String path = node.getSourcePath();
		long offset = node.getOffset();
		
		
		try 
		{
			FileBuffer buffer = new FileBuffer(path, offset, offset+32, false);
			long magicoff = buffer.findString(0, 0x10, NARC.MAGIC);
			if(magicoff == 0)
			{
				//Should be good as is?
				return new FileTypeDefNode(NARC.getTypeDef());
			}
			else
			{
				//Look for DS compression header...

				DSCompHeader chead = DSCompHeader.read(buffer, 0);
				CompDefNode cnode = null;
				switch(chead.getType())
				{
				case DSCompHeader.TYPE_LZ77: 
					AbstractCompDef def = NinLZ.getDefinition();
					cnode = new CompDefNode(def);
					String buffpath = def.decompressToDiskBuffer(new FileBufferStreamer(node.loadData()));
					StreamWrapper decomp = new FileBufferStreamer(new FileBuffer(buffpath, 0, 0x10));
					if(checkDecompMagic(decomp)) cnode.setChild(new FileTypeDefNode(NARC.getTypeDef()));
					return cnode;
				case DSCompHeader.TYPE_HUFFMAN: return new CompDefNode(NinHuff.getDefinition());
				case DSCompHeader.TYPE_RLE: 
					AbstractCompDef rle = DSRLE.getDefinition();
					cnode = new CompDefNode(rle);
					String buffpath_rle = rle.decompressToDiskBuffer(new FileBufferStreamer(node.loadData()));
					StreamWrapper decomp_rle = new FileBufferStreamer(new FileBuffer(buffpath_rle, 0, 0x10));
					if(checkDecompMagic(decomp_rle)) cnode.setChild(new FileTypeDefNode(NARC.getTypeDef()));
					return cnode;
				default: return null;
				}
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) 
	{
		//Read in
		try 
		{
			FileBuffer buffer = NTDProgramFiles.openAndDecompress(node);
			
			//Parse
			NARC arc = NARC.readNARC(buffer, 0);
			
			//Return panel
			return new ArchivePreviewPanel(arc.getArchiveTree());
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
