package waffleoRai_NTDTypes.img;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import waffleoRai_Files.Converter;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public abstract class PNGConverter implements Converter{
	
	public static final String DEFO_ENG_DESC = "Portable Network Graphics Image";
	
	protected String desc_str = DEFO_ENG_DESC;
	
	public String getToFormatDescription(){
		return desc_str;
	}
	
	public void setToFormatDescription(String s){
		desc_str = s;
	}
	
	protected abstract BufferedImage loadAndRender(String inpath) throws IOException, UnsupportedFileTypeException;
	protected abstract BufferedImage loadAndRender(FileBuffer input) throws IOException, UnsupportedFileTypeException;
	protected abstract BufferedImage loadAndRender(FileNode node) throws IOException, UnsupportedFileTypeException;
	
	public void writeAsTargetFormat(String inpath, String outpath) throws IOException, UnsupportedFileTypeException{
		BufferedImage img = loadAndRender(inpath);
		ImageIO.write(img, "png", new File(outpath));
	}
	
	public void writeAsTargetFormat(FileBuffer input, String outpath) throws IOException, UnsupportedFileTypeException{
		BufferedImage img = loadAndRender(input);
		ImageIO.write(img, "png", new File(outpath));
	}
	
	public void writeAsTargetFormat(FileNode node, String outpath) throws IOException, UnsupportedFileTypeException{
		BufferedImage img = loadAndRender(node);
		ImageIO.write(img, "png", new File(outpath));
	}
	
	public String changeExtension(String path){
		//Remove existing extension
		int lastdot = path.lastIndexOf('.');
		if(lastdot >= 0) path = path.substring(0, lastdot);
		
		return path + ".png";
	}

}
