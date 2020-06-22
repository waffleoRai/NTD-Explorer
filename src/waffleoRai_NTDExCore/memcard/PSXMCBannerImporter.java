package waffleoRai_NTDExCore.memcard;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import waffleoRai_Containers.psx.PSXMemoryCard;
import waffleoRai_Containers.psx.PSXMemoryCard.PSXMemoryCardReadErrorException;
import waffleoRai_Image.Animation;
import waffleoRai_Image.AnimationFrame;
import waffleoRai_Image.SimpleAnimation;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class PSXMCBannerImporter implements BannerImporter{

	private static String FF_MCR_DESC = "Raw PS1 Memory Card Image";
	
	public static final int FRAME_MILLIS_2 = 500;
	public static final int FRAME_MILLIS_3 = 333;
	
	private Animation scaleIcon(Animation in){

		Animation copy = new SimpleAnimation(in.getNumberFrames());
		for(int f = 0; f < in.getNumberFrames(); f++){
			AnimationFrame src = in.getFrame(f);
			AnimationFrame af = new AnimationFrame(scaleIcon(src.getImage()), src.getLengthInFrames());
			copy.setFrame(af, f);
		}
		
		return copy;
	}
	
	private BufferedImage scaleIcon(BufferedImage in){
		//Input is 16x16, we want 32x32
		BufferedImage out = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
		int x = 0; int y = 0;
		for(int r = 0; r < 16; r++){
			for (int l = 0; l < 16; l++){
				int argb = in.getRGB(l, r);
				
				out.setRGB(x, y, argb);
				out.setRGB(x+1, y, argb);
				out.setRGB(x, y+1, argb);
				out.setRGB(x+1, y+1, argb);
				x+=2;
			}
			y+=2; x = 0;
		}
		
		return out;
	}
	
	public Collection<BannerStruct> findBanner(String mcpath, String gamecode) throws UnsupportedFileTypeException, IOException {
		String gc = gamecode.replace("_", "");
		List<BannerStruct> matches = new LinkedList<BannerStruct>();
		
		//Load MC
		try {
			PSXMemoryCard card = new PSXMemoryCard(mcpath);
			Collection<String> mcnames = card.getPossibleFilenamesForGame(gc);
			
			//If there are any files, scan through them and load the banners
			//Scale icons to 32x32
			for(String fname : mcnames){
				BannerStruct banner = new BannerStruct();
				banner.title = card.getGameName(fname);
				banner.icon = card.getGameIcon(fname);
				banner.icon = scaleIcon(banner.icon);
				if(banner.icon.getNumberFrames() == 2) banner.framemillis = FRAME_MILLIS_2;
				else banner.framemillis = FRAME_MILLIS_3;
				matches.add(banner);
			}
		} 
		catch (PSXMemoryCardReadErrorException e) {
			e.printStackTrace();
			throw new FileBuffer.UnsupportedFileTypeException("PSX Memory Card Read Error");
		} 
		catch (IOException e) {
			throw e;
		}
		
		
		return matches;
	}

	public List<FileFilter> getFileFilters() {
		List<FileFilter> list = new ArrayList<FileFilter>(1);
		
		list.add(new FileFilter(){

			public boolean accept(File f) {
				if(f.getPath().toString().endsWith(".mcr")) return true;
				return false;
			}

			public String getDescription() {
				return FF_MCR_DESC + "(.mcr)";
			}
			
		});
		
		return list;
	}
	

}
