package waffleoRai_NTDTypes.img;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_GUITools.AnimatedImagePaneDrawer;
import waffleoRai_Image.files.GIFFile;
import waffleoRai_Image.files.GIFFile.GIFFrame;
import waffleoRai_NTDExCore.NTDTypeLoader;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDTypes.AnimatedImagePanelManager;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class GIFManager extends AnimatedImagePanelManager{

	//Loader
	public static class GIFDefLoader implements NTDTypeLoader{

		public TypeManager getTypeManager() {return new GIFManager();}
		public FileTypeDefinition getDefinition() {return GIFFile.getDefinition();}
		
	}
	
	//Manager
	
	protected Collection<AnimatedImagePaneDrawer> getFrames(FileNode node) throws IOException {
		try {
			GIFFile gif = GIFFile.readGIF(node.loadDecompressedData());
			List<GIFFrame> frames = gif.getFrames();
			List<AnimatedImagePaneDrawer> list = new ArrayList<AnimatedImagePaneDrawer>(frames.size());
			for(GIFFrame f : frames) list.add(f);
			return list;
		} 
		catch (UnsupportedFileTypeException e) {
			e.printStackTrace();
			throw new IOException("GIF parsing failed");
		}
	}

	public FileTypeNode detectFileType(FileNode node) {
		//Look for "GIF8"
		
		try{
			
			FileBuffer dat = null;
			long ed = 0x10;
			if(node.getLength() < ed) ed = node.getLength();
			if(node.hasCompression()) dat = node.loadDecompressedData();
			else dat = node.loadData(0, ed);
			
			
			long sfind = dat.findString(0, ed, "GIF8");
			if(sfind >= 0) return new FileTypeDefNode(GIFFile.getDefinition());
		}
		catch(Exception x){
			x.printStackTrace();
			return null;
		}
		
		return null;
	}

	public Converter getStandardConverter() {return null;}
	public boolean isOfType(FileNode node) {return (detectFileType(node) != null);}

}
