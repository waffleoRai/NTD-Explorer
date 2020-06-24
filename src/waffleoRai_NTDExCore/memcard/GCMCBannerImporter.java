package waffleoRai_NTDExCore.memcard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import waffleoRai_Containers.nintendo.GCMemCard;
import waffleoRai_Containers.nintendo.GCMemCardFile;
import waffleoRai_NTDExGUI.dialogs.BannerButton;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class GCMCBannerImporter implements BannerImporter{
	
	private static String FF_RAW_DESC = "Raw GameCube Memory Card Image";
	
	public Collection<BannerStruct> findBanner(String mcpath, String gamecode) throws UnsupportedFileTypeException, IOException {
		List<BannerStruct> matches = new LinkedList<BannerStruct>();
		
		//Load MC
		try {
			gamecode = gamecode.replace("DOL_", "").substring(0, 4);
			GCMemCard mc = GCMemCard.readRawMemoryCardFile(mcpath);
			Collection<GCMemCardFile> files = mc.getFilesFromGame(gamecode);
			for(GCMemCardFile mcf : files){
				BannerStruct banner = new BannerStruct();
				banner.framemillis = GCMemCard.ICO_FRAME_MILLIS << 2;
				banner.icon = mcf.getCleanIcon();
				banner.title = mcf.getComment1() + "\n" + mcf.getComment2();
				matches.add(banner);
			}
			
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
				String p = f.getAbsolutePath().toString().toLowerCase();
				if(p.endsWith(".raw") || p.endsWith(".gcp")) return true;
				return false;
			}

			public String getDescription() {
				return FF_RAW_DESC + "(.raw, .gcp)";
			}
			
		});
		
		return list;
	}
	
	public int getImportDialogButtonSizeEnum(){return BannerButton.SIZE_SMALL;}
	public String getImportDialogButtonColor(){return BannerButton.COLOR_BLUE;}
	public String getImportDialogBackgroundColor(){return BannerButton.COLOR_PURPLE;}
	
	
}
