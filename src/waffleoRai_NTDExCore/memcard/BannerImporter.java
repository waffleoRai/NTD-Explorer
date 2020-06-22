package waffleoRai_NTDExCore.memcard;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import waffleoRai_Image.Animation;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public interface BannerImporter {

	public static class BannerStruct{
		public String title;
		public Animation icon;
		public int framemillis;
	}
	
	public Collection<BannerStruct> findBanner(String mcpath, String gamecode) throws IOException, UnsupportedFileTypeException;
	public List<FileFilter> getFileFilters();
	
}
