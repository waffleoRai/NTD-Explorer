package waffleoRai_NTDExCore.filetypes.sound;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import waffleoRai_Compression.definitions.AbstractCompDef;
import waffleoRai_Compression.definitions.CompDefNode;
import waffleoRai_Compression.nintendo.DSCompHeader;
import waffleoRai_Compression.nintendo.DSRLE;
import waffleoRai_Compression.nintendo.NinHuff;
import waffleoRai_Compression.nintendo.NinLZ;
import waffleoRai_Containers.nintendo.sar.DSSoundArchive;
import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeDefNode;
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
import waffleoRai_Utils.FileBufferStreamer;
import waffleoRai_Utils.FileNode;
import waffleoRai_Utils.StreamWrapper;

public class TM_SDAT extends TypeManager{
	
	private boolean checkDecompMagic(StreamWrapper in)
	{
		if(in == null) return false;
		byte[] magic_bytes = DSSoundArchive.MAGIC.getBytes();
		for(int i = 0; i < magic_bytes.length; i++)
		{
			if(in.get() != magic_bytes[i]) return false;
		}
		return true;
	}

	@Override
	public FileTypeNode detectFileType(FileNode node) {
		String path = node.getSourcePath();
		long offset = node.getOffset();
		
		
		try 
		{
			FileBuffer buffer = new FileBuffer(path, offset, offset+32, false);
			long magicoff = buffer.findString(0, 0x10, DSSoundArchive.MAGIC);
			if(magicoff == 0)
			{
				//Make sure size is the same as the header says...
				int bom = Short.toUnsignedInt(buffer.shortFromFile(4));
				if(bom == 0xFEFF) buffer.setEndian(true); //Big endian
				int expsize = buffer.intFromFile(8);
				if(node.getLength() != expsize) return null;
				return new FileTypeDefNode(DSSoundArchive.getTypeDef());
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
					if(checkDecompMagic(decomp)) cnode.setChild(new FileTypeDefNode(DSSoundArchive.getTypeDef()));
					return cnode;
				case DSCompHeader.TYPE_HUFFMAN: return new CompDefNode(NinHuff.getDefinition());
				case DSCompHeader.TYPE_RLE: 
					AbstractCompDef rle = DSRLE.getDefinition();
					cnode = new CompDefNode(rle);
					String buffpath_rle = rle.decompressToDiskBuffer(new FileBufferStreamer(node.loadData()));
					StreamWrapper decomp_rle = new FileBufferStreamer(new FileBuffer(buffpath_rle, 0, 0x10));
					if(checkDecompMagic(decomp_rle)) cnode.setChild(new FileTypeDefNode(DSSoundArchive.getTypeDef()));
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
	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {

		try 
		{
			FileBuffer buffer = node.loadDecompressedData();
			//System.err.println("Buffer size: 0x" + Long.toHexString(buffer.getFileSize()));
			
			//Parse
			DSSoundArchive arc = DSSoundArchive.readSDAT(buffer);
			DirectoryNode root = arc.getArchiveView();
			
			//Scan for known types (icons r sxy)
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
