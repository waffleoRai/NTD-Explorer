package waffleoRai_NTDExCore.filetypes;

import java.io.IOException;

import waffleoRai_Compression.definitions.AbstractCompDef;
import waffleoRai_Compression.definitions.CompDefNode;
import waffleoRai_Compression.nintendo.DSCompHeader;
import waffleoRai_Compression.nintendo.DSRLE;
import waffleoRai_Compression.nintendo.NinHuff;
import waffleoRai_Compression.nintendo.NinLZ;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBufferStreamer;
import waffleoRai_Utils.FileNode;
import waffleoRai_Utils.StreamWrapper;

/*
 * UPDATES
 * 
 * 1.0.0 | June 7, 2020
 * 	Initial writing/documentation
 * 
 */

/**
 * A utility class for common Nitro (Nintendo DS) file processing.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since June 7, 2020
 */
public class NitroFiles {
	
	/**
	 * Check the beginning of the incoming stream for the provided magic number string.
	 * @param in Input stream
	 * @param magic Magic number as a string. Usually four characters, such as "NARC" or "SDAT"
	 * @return True if given magic number was found at the beginning of the stream. False otherwise.
	 * @since 1.0.0
	 */
	public static boolean checkNitroMagic(StreamWrapper in, String magic){
		
		if(in == null) return false;
		byte[] magic_bytes = magic.getBytes();
		for(int i = 0; i < magic_bytes.length; i++)
		{
			if(in.get() != magic_bytes[i]) return false;
		}
		return true;
		
	}
	
	/**
	 * Check whether the data referenced by the provided FileNode is of the given file type (specified by
	 * the definition parameter), and return a type chain referring to the detected type.
	 * <br>Compression is common in DS files, so detection and type chain addition of common DS compression
	 * routines will be run as well.
	 * @param node FileNode linking data to run detection on.
	 * @param magic Magic number of file type to detect as a 4 character string.
	 * @param def Definition of file type to detect.
	 * @param size_strict DS files include the length of the file in their headers, however sometimes these values are smaller
	 * than the actual file size. To force detector to reject mismatches (in the case of something at the head of a much larger
	 * archive), set this to true. Set false to ignore the file size parameter and only check compression and magic number.
	 * @return FileTypeNode chain describing encoding of the file referenced by the FileNode.
	 * @since 1.0.0
	 */
	public static FileTypeNode detectNitroFile(FileNode node, String magic, FileTypeDefinition def, boolean size_strict){

		String path = node.getSourcePath();
		long offset = node.getOffset();
		
		//If file size is less than 16, then it's probably not what we're looking for...
		if(node.getLength() < 16) return null;
		
		try 
		{
			FileBuffer buffer = new FileBuffer(path, offset, offset+16, false);
			long magicoff = buffer.findString(0, 0x10, magic);
			if(magicoff == 0)
			{
				if(size_strict){
					//Make sure size is the same as the header says...
					int expsize = buffer.intFromFile(8);
					if(node.getLength() != expsize) return null;	
				}
				return new FileTypeDefNode(def);
			}
			else
			{
				//Look for DS compression header...

				DSCompHeader chead = DSCompHeader.read(buffer, 0);
				CompDefNode cnode = null;
				switch(chead.getType())
				{
				case DSCompHeader.TYPE_LZ77: 
					AbstractCompDef lzdef = NinLZ.getDefinition();
					cnode = new CompDefNode(lzdef);
					String buffpath = lzdef.decompressToDiskBuffer(new FileBufferStreamer(node.loadData()));
					StreamWrapper decomp = new FileBufferStreamer(new FileBuffer(buffpath, 0, 0x10));
					if(checkNitroMagic(decomp, magic)){
						cnode.setChild(new FileTypeDefNode(def));
						return cnode;
					}
					else return null;
				case DSCompHeader.TYPE_HUFFMAN: return new CompDefNode(NinHuff.getDefinition());
				case DSCompHeader.TYPE_RLE: 
					AbstractCompDef rle = DSRLE.getDefinition();
					cnode = new CompDefNode(rle);
					String buffpath_rle = rle.decompressToDiskBuffer(new FileBufferStreamer(node.loadData()));
					StreamWrapper decomp_rle = new FileBufferStreamer(new FileBuffer(buffpath_rle, 0, 0x10));
					if(checkNitroMagic(decomp_rle, magic)){
						cnode.setChild(new FileTypeDefNode(def));
						return cnode;
					}
					else return null;
				default:
					return null;
				}
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return null;
		}

	}

}
