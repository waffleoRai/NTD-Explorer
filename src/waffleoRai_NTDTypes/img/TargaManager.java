package waffleoRai_NTDTypes.img;

import java.awt.image.BufferedImage;
import java.io.IOException;

import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_Image.files.TGAFile;
import waffleoRai_NTDExCore.NTDTypeLoader;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDTypes.ImagePanelManager;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class TargaManager extends ImagePanelManager{
	
	//Put loader in here too
	public static class TargaDefLoader implements NTDTypeLoader{

		public TypeManager getTypeManager() {return new TargaManager();}
		public FileTypeDefinition getDefinition() {return TGAFile.getDefinition();}
		
	}
	
	//Converter
	public static class TGA2PNGConv extends PNGConverter{

		public static final String DEFO_ENG_DESC = "Truevision TARGA Image (.tga)";
		
		private String desc = DEFO_ENG_DESC;
		
		public String getFromFormatDescription() {return desc;}
		public void setFromFormatDescription(String s) {desc = s;}

		@Override
		protected BufferedImage loadAndRender(String inpath) throws IOException, UnsupportedFileTypeException {
			FileBuffer buff = FileBuffer.createBuffer(inpath);
			return loadAndRender(buff);
		}

		@Override
		protected BufferedImage loadAndRender(FileBuffer input) throws IOException, UnsupportedFileTypeException {
			TGAFile tga = TGAFile.readTGA(input);
			if(tga == null) return null;
			return tga.getImage();
		}

		@Override
		protected BufferedImage loadAndRender(FileNode node) throws IOException, UnsupportedFileTypeException {
			FileBuffer buff = node.loadDecompressedData();
			return loadAndRender(buff);
		}
		
	}

	@Override
	protected BufferedImage renderImage(FileNode node) throws IOException {
		FileBuffer dat = node.loadDecompressedData();
		if(dat == null) throw new IOException("Loaded data buffer was null!");
		
		TGAFile tga = TGAFile.readTGA(dat);
		if(tga == null) throw new IOException("TGA parse failed!");
		
		return tga.getImage();
	}

	@Override
	public FileTypeNode detectFileType(FileNode node) {
		//Look for "TRUEVISION" near the end of the file
		//Looks like it usually starts 18 bytes back?
		//https://en.wikipedia.org/wiki/Truevision_TGA
		
		final String str = "TRUEVISION";
		boolean found = false;
		long end = node.getLength();
		long st = end - 32;
		if(st < 0) st = 0;
		if(st >= end) return null;
		if(node.hasCompression()){
			//Gotta load the whole thing :/
			try {
				FileBuffer buff = node.loadDecompressedData();
				long spos = buff.findString(st, end-st, str);
				found = (spos >= 0);
			} 
			catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		else{
			//Just the end
			try {
				FileBuffer buff = node.loadData(st, end-st);
				long spos = buff.findString(0,buff.getFileSize(), str);
				found = (spos >= 0);
			} 
			catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			
		}
		
		if(found) return new FileTypeDefNode(TGAFile.getDefinition());
		return null;
	}

	public Converter getStandardConverter() {return new TGA2PNGConv();}
	public boolean isOfType(FileNode node) {return (detectFileType(node) != null);}
	

}
