package waffleoRai_NTDTypes.img;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;

import waffleoRai_Files.Converter;
import waffleoRai_Files.FileClass;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_Image.RasterImageDef;
import waffleoRai_NTDExCore.NTDTypeLoader;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDTypes.ImagePanelManager;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class JPEGManager extends ImagePanelManager{

	//Definition
	public static final int DEF_ID = 0x4a504547;
	public static final String DEFO_ENG_NAME = "Joint Photographic Experts Group (JPEG) Image";
		
	private static JPEGImageDef stat_def;
		
	public static class JPEGImageDef extends RasterImageDef{

		private String desc = DEFO_ENG_NAME;
			
		public Collection<String> getExtensions() {
			List<String> list = new ArrayList<String>(6);
			list.add("jpg");
			list.add("jpeg");
			list.add("jpe");
			list.add("jif");
			list.add("jfif");
			list.add("jfi");
			return list;
		}

		public String getDescription() {return desc;}

		public FileClass getFileClass() {return FileClass.IMG_IMAGE;}

		public int getTypeID() {return DEF_ID;}

		public void setDescriptionString(String s) {desc = s;}

		public String getDefaultExtension() {return "jpg";}

		public BufferedImage renderImage(FileNode src) {

			try {
				FileBuffer dat = src.loadDecompressedData();
				ByteArrayInputStream is = new ByteArrayInputStream(dat.getBytes());
				return ImageIO.read(is);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}

		}
			
	}
		
	public static JPEGImageDef getDefinition(){
		if(stat_def == null) stat_def = new JPEGImageDef();
		return stat_def;
	}
		
	//Converter
	public static class JPEG2PNGConv extends PNGConverter{

		public static final String DEFO_ENG_DESC = "Joint Photographic Experts Group (JPEG) Image (.jpg, .jpeg)";
			
		private String desc = DEFO_ENG_DESC;
			
		public String getFromFormatDescription() {return desc;}
		public void setFromFormatDescription(String s) {desc = s;}

		protected BufferedImage loadAndRender(String inpath) throws IOException, UnsupportedFileTypeException {
			FileBuffer buff = FileBuffer.createBuffer(inpath);
			return loadAndRender(buff);
		}

		protected BufferedImage loadAndRender(FileBuffer input) throws IOException, UnsupportedFileTypeException {
			ByteArrayInputStream is = new ByteArrayInputStream(input.getBytes());
			return ImageIO.read(is);
		}

		protected BufferedImage loadAndRender(FileNode node) throws IOException, UnsupportedFileTypeException {
			FileBuffer buff = node.loadDecompressedData();
			return loadAndRender(buff);
		}
			
	}
		
	//Loader
	public static class JPEGDefLoader implements NTDTypeLoader{

		public TypeManager getTypeManager() {return new JPEGManager();}
		public FileTypeDefinition getDefinition() {return JPEGManager.getDefinition();}
			
	}
		
	//Manager
	protected BufferedImage renderImage(FileNode node) throws IOException {
		try {
			FileBuffer dat = node.loadDecompressedData();
			ByteArrayInputStream is = new ByteArrayInputStream(dat.getBytes());
			return ImageIO.read(is);
		} 
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public FileTypeNode detectFileType(FileNode node) {
		//Check for strings "JFIF" or "Exif" near beginning?
		try {
			FileBuffer dat = null;
			if(node.hasCompression()) dat = node.loadDecompressedData();
			else dat = node.loadData(0, 0x10);
			
			long sfind = dat.findString(0x0, 0x10, "JFIF");
			if(sfind < 0){
				sfind = dat.findString(0x0, 0x10, "Exif");
				if(sfind < 0) return null;
			}
			
			return new FileTypeDefNode(getDefinition());
		} 
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Converter getStandardConverter() {return new JPEG2PNGConv();}
	public boolean isOfType(FileNode node) {return (detectFileType(node) != null);}

	
}
