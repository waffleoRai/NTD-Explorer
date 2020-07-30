package waffleoRai_NTDExCore.memcard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import waffleoRai_Containers.nintendo.WiiSaveBannerFile;
import waffleoRai_Containers.nintendo.WiiSaveDataFile;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExGUI.dialogs.BannerButton;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class WiiSaveBannerImporter implements BannerImporter{
	
	private static String FF_RAW_DESC = "Wii Raw Save File Data Package";
	private static String FF_BANNER_DESC = "Wii Save File Banner";
	
	public Collection<BannerStruct> findBanner(String mcpath, String gamecode) throws UnsupportedFileTypeException, IOException {
		List<BannerStruct> matches = new LinkedList<BannerStruct>();
		
		//Load save
		try {
			if(mcpath.endsWith("data.bin")){
				//Check keys
				WiiSaveDataFile.set_sdKey(NTDProgramFiles.getKey(NTDProgramFiles.KEYNAME_WII_SD));
				WiiSaveDataFile.set_sdIV(NTDProgramFiles.getKey(NTDProgramFiles.KEYNAME_WII_SD_IV));
				WiiSaveDataFile datfile = WiiSaveDataFile.readDataBin(FileBuffer.createBuffer(mcpath, true), true);
				if(!datfile.isDecryptable()){
					throw new UnsupportedFileTypeException("Save file cannot be decrypted with Wii SD keys!");
				}
				
				WiiSaveBannerFile bnr = datfile.getBanner();
				BannerStruct bs = new BannerStruct();
				bs.framemillis = WiiSaveBannerFile.ANIM_SPEED_CONST;
				bs.title = bnr.getTitle();
				bs.icon = bnr.getIcon();
				
				matches.add(bs);
			}
			else if(mcpath.endsWith("banner.bin")){
				WiiSaveBannerFile bnr = WiiSaveBannerFile.readBannerBin(FileBuffer.createBuffer(mcpath, true));
				BannerStruct bs = new BannerStruct();
				bs.framemillis = WiiSaveBannerFile.ANIM_SPEED_CONST;
				bs.title = bnr.getTitle();
				bs.icon = bnr.getIcon();
				
				matches.add(bs);
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
				if(p.endsWith("data.bin")) return true;
				return false;
			}

			public String getDescription() {
				return FF_RAW_DESC + " (.bin)";
			}
			
		});
		
		list.add(new FileFilter(){

			public boolean accept(File f) {
				String p = f.getAbsolutePath().toString().toLowerCase();
				if(p.endsWith("banner.bin")) return true;
				return false;
			}

			public String getDescription() {
				return FF_BANNER_DESC + " (.bin)";
			}
			
		});
		
		return list;
	}
	
	public int getImportDialogButtonSizeEnum(){return BannerButton.SIZE_MEDSMALL;}
	public String getImportDialogButtonColor(){return BannerButton.COLOR_GREY;}
	public String getImportDialogBackgroundColor(){return BannerButton.COLOR_BLUE;}
	

}
