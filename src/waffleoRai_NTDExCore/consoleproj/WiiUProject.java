package waffleoRai_NTDExCore.consoleproj;

import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import waffleoRai_Containers.nintendo.cafe.CafeCrypt;
import waffleoRai_Containers.nintendo.cafe.WiiUDisc;
import waffleoRai_Encryption.nintendo.NinCryptTable;
import waffleoRai_Image.Animation;
import waffleoRai_Image.files.TGAFile;
import waffleoRai_NTDExCore.Console;
import waffleoRai_NTDExCore.DefoLanguage;
import waffleoRai_NTDExCore.EncryptionRegion;
import waffleoRai_NTDExCore.GameRegion;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.NTDTools;
import waffleoRai_NTDExGUI.banners.Animator;
import waffleoRai_NTDExGUI.banners.Unanimator;
import waffleoRai_NTDExGUI.dialogs.progress.ProgressListeningDialog;
import waffleoRai_NTDExGUI.panels.AbstractGameOpenButton;
import waffleoRai_NTDExGUI.panels.DefaultGameOpenButton;
import waffleoRai_Files.tree.DirectoryNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Files.tree.FileNode;

/*
 * UPDATES
 * 
 * 2020.07.25 | 1.0.0
 * 	Initial documentation
 * 
 * 2020.08.16 | 1.0.0 -> 1.1.0
 * 	Added low-level FS method
 * 
 * 2020.10.29 | 1.1.0 -> 2.0.0
 * 	Updated for direct image reference (no dec buffer :3)
 * 
 * 2020.11.20 | 2.0.0 -> 2.0.1
 * 	Added scan for empty dirs on import/tree reset
 * 
 * 2021.06.23 | 2.0.1 -> 2.0.2
 * 	Default icon on import is logo
 * 
 */

/**
 * A project implementation for Wii U WUD images.
 * @author Blythe Hospelhorn
 * @version 2.0.2
 * @since June 23, 2021
 */
public class WiiUProject extends NTDProject{
	
	/*----- Constant -----*/
	
	/*----- Instance Variables -----*/
	
	private NinCryptTable crypt_table;
	
	/*----- Construction -----*/
	
	/**
	 * Generate an empty WiiUProject.
	 * @since 1.0.0
	 */
	public WiiUProject(){super(); super.setConsole(Console.WIIU);}
	
	/**
	 * Generate a WiiUProject from a WUD image.
	 * @param imgpath Path to WUD file on local disk.
	 * @param gamekey 128 bit (16 byte) game disk AES key. Usually found dumped alongside disc image.
	 * @param reg Region associated with software.
	 * @param observer Dialog for monitoring decryption progress. May be left null.
	 * @return WiiUProject generated from the provided disk image, or null if import is unsuccessful.
	 * @throws IOException If a file cannot be read or written to disk.
	 * @throws UnsupportedFileTypeException If the image or a file within cannot be properly parsed.
	 * @since 1.0.0
	 */
	public static WiiUProject createFromWUD(String imgpath, byte[] gamekey, GameRegion reg, ProgressListeningDialog observer) throws IOException, UnsupportedFileTypeException{
		//(Try to) Load common key
		byte[] ckey = NTDProgramFiles.getKey(NTDProgramFiles.KEYNAME_WIIU_COMMON);
		if(ckey != null) WiiUDisc.setCommonKey(ckey);
		
		//Initial disc read...
		if(observer != null){
			observer.setSecondaryString("Parsing disc structure");
		}
		WiiUDisc disc = WiiUDisc.readWUD(imgpath, gamekey);
		String discidlong = disc.getLongGamecode();
		
		//Spawn project and set basic fields...
		if(observer != null){
			observer.setSecondaryString("Initializing project");
		}
		WiiUProject proj = new WiiUProject();
		proj.setImportedTime(OffsetDateTime.now());
		proj.setModifiedTime(OffsetDateTime.now());
		proj.setRegion(reg);
		
		proj.setGameCode(discidlong.substring(6, 10));
		proj.setMakerCode(discidlong.substring(11, 13));
		proj.setDefoLanguage(DefoLanguage.getLanReg(proj.getGameCode4().charAt(3)));
		proj.setFullCode("WUP_" + proj.getGameCode4() + "_" + reg.getShortCode());
		proj.setROMPath(imgpath);
		
		proj.setBannerTitle(discidlong);
		proj.setPublisherName(NTDProject.getPublisherName(proj.getMakerCode()));
		//The title can be pulled from meta.xml
		//longname_en (or language), shortname_en (language), publisher_en (language)
		
		//Save key and get dec dir
		proj.saveGameKey(gamekey);
		String decdir = proj.getDecryptedDataDir();
		//if(!FileBuffer.directoryExists(decdir)) Files.createDirectories(Paths.get(decdir));
		
		//Mark encrypted regions
		int pcount = disc.getPartitionCount();
		List<EncryptionRegion> encregs = proj.getEncRegListReference();
		for(int i = 0; i < pcount; i++){
			long poff = disc.getPartitionOffset(i);
			long psz = disc.getPartitionSize(i);
			
			EncryptionRegion ereg = new EncryptionRegion(CafeCrypt.getStandardAESDef(), poff, psz, decdir);
			encregs.add(ereg);
		}
		EncryptionRegion ereg = new EncryptionRegion(CafeCrypt.getStandardAESDef(), 0x18000, 0x8000, decdir); //Partition table
		encregs.add(ereg);
		
		//Set default icon...
		if(ckey == null || gamekey == null){
			//Lock
			BufferedImage defo_ico = NTDProgramFiles.getConsoleDefaultImage(Console.WIIU, true);
			if(defo_ico != null) proj.setBannerIcon(new BufferedImage[]{defo_ico});
			else proj.setBannerIcon(new BufferedImage[]{NTDProgramFiles.getDefaultImage_lock()});
		}
		else{
			//Default
			//proj.setBannerIcon(new BufferedImage[]{NTDProgramFiles.getDefaultImage_unknown()});
			BufferedImage defo_ico = NTDProgramFiles.getConsoleDefaultImage(Console.WIIU, false);
			if(defo_ico != null) proj.setBannerIcon(new BufferedImage[]{defo_ico});
		}
		
		proj.resetTree(observer, false, true);
		
		return proj;
	}
	
	/*----- Decryption -----*/
	
	private String getCryptTablePath(){
		return super.getCustomDataDirPath() + File.separator + "ctbl.bin";
	}
	
	private void saveCryptTable() throws IOException{
		if(crypt_table != null){
			crypt_table.exportToFile(getCryptTablePath());
		}
	}
	
	private void loadCryptTable() throws IOException{
		String path = getCryptTablePath();
		if(!FileBuffer.fileExists(path)) return;
		crypt_table = new NinCryptTable();
		crypt_table.importFromFile(path);
	}
	
	/**
	 * Load the game/disk key associated with this software from the project save directory.
	 * @return Loaded game key as an array of 16 bytes, or null if file is not present.
	 * @throws IOException If file cannot be read.
	 * @since 1.0.0
	 */
	public byte[] loadGameKey() throws IOException{
		String path = getGameKeyPath();
		if(!FileBuffer.fileExists(path)) return null;
		return FileBuffer.createBuffer(path).getBytes();
	}
	
	/**
	 * Save the game/disk key associated with this software to the project save directory.
	 * @param key Key to save. If this parameter is null, nothing will be written.
	 * @throws IOException If the file cannot be written.
	 * @since 1.0.0
	 */
	public void saveGameKey(byte[] key) throws IOException{
		if(key == null) return;
		String path = getGameKeyPath();
		FileBuffer.wrap(key).writeFile(path);
	}
	
	/**
	 * Get the path on local disk to the file containing the game/disk key
	 * in the project save directory.
	 * @return The local key path, or null if there is an error generating the path.
	 * @since 1.0.0
	 */
	public String getGameKeyPath(){
		return this.getCustomDataDirPath() + File.separator + "gamekey.bin";
	}
	
	public boolean decrypt(ProgressListeningDialog observer) throws IOException{
		//Load things...
		resetTree(observer, false, true);
		return true;
	}
	
	/*----- Banner -----*/
	
	private void readBanner(WiiUDisc disc, ProgressListeningDialog observer) throws IOException{
		
		if(observer != null){
			observer.setPrimaryString("Importing Data");
			observer.setSecondaryString("Reading banner data");
		}
		String icopath = disc.getIconPath();
		String metapath = disc.getMetaXMLPath();
		
		DirectoryNode root = super.getTreeRoot();
		
		FileNode iconode = root.getNodeAt(icopath);
		BufferedImage icon = null;
		if(iconode != null){
			icon = TGAFile.readTGA(iconode).getImage();
			setBannerIcon(new BufferedImage[]{icon});
		}
		
		FileNode metanode = root.getNodeAt(metapath);
		Map<String, String[]> bannerstr = null;
		if(metanode != null){
			try {bannerstr = WiiUDisc.readMetaXML(metanode);} 
			catch (XMLStreamException e) {e.printStackTrace();}
		}
		
		//TODO for now, just default to English
		if(bannerstr != null){
			String[] bnr = bannerstr.get("en");
			if(bnr != null){
				setBannerTitle(bnr[1]);
				setPublisherName(bnr[2]);
			}	
		}
	}
	
	/*----- Alt Methods -----*/
	
	private void resetTree(ProgressListeningDialog observer, boolean fs_raw, boolean resetBanner) throws IOException{
		
		if(observer != null){
			observer.setPrimaryString("Resetting tree");
			observer.setSecondaryString("Loading key data");
		}
		
		byte[] ckey = NTDProgramFiles.getKey(NTDProgramFiles.KEYNAME_WIIU_COMMON);
		if(ckey != null) WiiUDisc.setCommonKey(ckey);
		byte[] gkey = loadGameKey();
		String imgpath = getROMPath();
		
		boolean dec = (ckey != null) && (gkey != null);
		if(!dec){
			try{
				if(observer != null){
					observer.setSecondaryString("Game or common key not found. Loading encrypted partition tree.");
				}
				
				WiiUDisc disc = WiiUDisc.readWUD(imgpath, gkey);
				if(resetBanner){
					BufferedImage defo_ico = NTDProgramFiles.getConsoleDefaultImage(Console.WIIU, true);
					if(defo_ico != null) setBannerIcon(new BufferedImage[]{defo_ico});
					else setBannerIcon(new BufferedImage[]{NTDProgramFiles.getDefaultImage_lock()});
				}
				super.setTreeRoot(disc.getDirectFileTree(fs_raw));
			}
			catch(UnsupportedFileTypeException x){
				x.printStackTrace();
				throw new IOException();
			}	
		}
		else{
			try{
				if(observer != null){
					observer.setSecondaryString("Reading disc structure");
				}		
				WiiUDisc disc = WiiUDisc.readWUD(imgpath, gkey);
				
				if(observer != null){
					observer.setSecondaryString("Generating crypt table");
				}
				crypt_table = disc.genCryptTable();
				saveCryptTable();
				
				if(observer != null){
					observer.setSecondaryString("Generating file tree");
				}
				DirectoryNode root = disc.getDirectFileTree(fs_raw);
				root.setFileName("");
				super.setTreeRoot(root);
				
				//Don't forget to set crypto state!
				CafeCrypt.initCafeCryptState(crypt_table);
				
				//Banner
				if(resetBanner) readBanner(disc, observer);
			}
			catch(UnsupportedFileTypeException x){
				x.printStackTrace();
				throw new IOException();
			}	
		}
		
		NTDTools.doTypeScan(getTreeRoot(), observer);
		setModifiedTime(OffsetDateTime.now());
	}
	
	public void resetTree(ProgressListeningDialog observer) throws IOException{
		resetTree(observer, false, false);
	}
	
	public void resetTreeFSDetail(ProgressListeningDialog observer) throws IOException{
		resetTree(observer, true, false);
	}
	
	public String[] getBannerLines(){
		String title = super.getBannerTitle();
		if(title == null){
			title = "WiiU Software " + getGameCode4() + getMakerCode();
			super.setBannerTitle(title);
		}
		String[] titlelines = title.split("\n");
		String publisher = super.getPublisherTag();
		if(publisher == null){
			publisher = "Unknown Publisher";
			super.setPublisherName(publisher);
		}
		
		switch(titlelines.length){
		case 1:
			//2 lines with publisher
			return new String[]{titlelines[0], publisher};
		case 2:
			//3 lines with publisher
			return new String[]{titlelines[0], titlelines[1], publisher};
		case 3:
		default:
			//3 line title
			return new String[]{titlelines[0], titlelines[1], titlelines[2]};
		}
	}
	
	public AbstractGameOpenButton generateOpenButton(){
		DefaultGameOpenButton gamepnl = new DefaultGameOpenButton(DefaultGameOpenButton.ICONSZ_64);
		gamepnl.loadMe(this);
		
		//Adjust labels
		String[] bnr = getBannerLines();
		switch(bnr.length){
		case 1:
			gamepnl.setLabelsDirect(bnr[0]);
			break;
		case 2:
			gamepnl.setLabelsDirect(bnr[0], bnr[1]);
			break;
		case 3:
		default:
			gamepnl.setLabelsDirect(bnr[0], bnr[1], bnr[2]);
			break;
		}
		
		return gamepnl;
	}
	
	public void showImageInfoDialog(Frame gui){
		//TODO
	}
	
	public Animator getBannerIconAnimator(ActionListener l){
		Animation anim_raw = super.getBannerIcon();
		if(anim_raw == null) return null;
		
		BufferedImage ico = anim_raw.getFrameImage(0);
		
		//Scale down to 64x64
		Image scaled = ico.getScaledInstance(64, 64, Image.SCALE_DEFAULT);
		BufferedImage out = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
		out.getGraphics().drawImage(scaled, 0, 0, null);
			
		return new Unanimator(out);
	}
	
	public void onProjectOpen(){
		try {
			loadCryptTable();
			if(crypt_table != null) CafeCrypt.initCafeCryptState(crypt_table);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void onProjectClose(){
		CafeCrypt.clearCafeCryptState();
	}
	
}
