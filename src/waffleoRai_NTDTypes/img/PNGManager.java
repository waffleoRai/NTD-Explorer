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

public class PNGManager extends ImagePanelManager{
	
	//Definition
	public static final int DEF_ID = 0x706e6749;
	public static final String DEFO_ENG_NAME = "Portable Network Graphics Image";
	public static final int PNG_MAGIC = (int)0x89504e47;
	
	private static PNGImageDef stat_def;
	
	public static class PNGImageDef extends RasterImageDef{

		private String desc = DEFO_ENG_NAME;
		
		@Override
		public Collection<String> getExtensions() {
			List<String> list = new ArrayList<String>(1);
			list.add("png");
			return list;
		}

		@Override
		public String getDescription() {return desc;}

		@Override
		public FileClass getFileClass() {return FileClass.IMG_IMAGE;}

		@Override
		public int getTypeID() {return DEF_ID;}

		@Override
		public void setDescriptionString(String s) {desc = s;}

		@Override
		public String getDefaultExtension() {return "png";}

		@Override
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
	
	public static PNGImageDef getDefinition(){
		if(stat_def == null) stat_def = new PNGImageDef();
		return stat_def;
	}
	
	//Loader
	public static class PNGDefLoader implements NTDTypeLoader{

		public TypeManager getTypeManager() {return new PNGManager();}
		public FileTypeDefinition getDefinition() {return PNGManager.getDefinition();}
		
	}
	
	//Manager
	@Override
	protected BufferedImage renderImage(FileNode node) throws IOException {
		try {
			FileBuffer dat = node.loadDecompressedData();
			ByteArrayInputStream is = new ByteArrayInputStream(dat.getBytes());
			return ImageIO.read(is);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public FileTypeNode detectFileType(FileNode node) {
		//Check for the magic number at the beginning...
		try {
			FileBuffer dat = null;
			if(node.hasCompression()) dat = node.loadDecompressedData();
			else dat = node.loadData(0, 0x10);
			
			dat.setEndian(true);
			int check = dat.intFromFile(0);
			if(check != PNG_MAGIC) return null;
			
			return new FileTypeDefNode(getDefinition());
		} 
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	public Converter getStandardConverter() {return null;} //PNG is already defo output image type!
	public boolean isOfType(FileNode node) {return (detectFileType(node) != null);}

}
