package waffleoRai_NTDExCore.consoleproj;

import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import waffleoRai_Containers.nintendo.cafe.WiiUDisc;
import waffleoRai_Image.Animation;
import waffleoRai_Image.files.TGAFile;
import waffleoRai_NTDExCore.Console;
import waffleoRai_NTDExCore.DefoLanguage;
import waffleoRai_NTDExCore.EncryptionRegion;
import waffleoRai_NTDExCore.GameRegion;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExGUI.banners.Animator;
import waffleoRai_NTDExGUI.banners.Unanimator;
import waffleoRai_NTDExGUI.dialogs.progress.ProgressListeningDialog;
import waffleoRai_NTDExGUI.panels.AbstractGameOpenButton;
import waffleoRai_NTDExGUI.panels.DefaultGameOpenButton;
import waffleoRai_Utils.DirectoryNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileNode;
import waffleoRai_fdefs.nintendo.WiiAESDef;

/*
 * UPDATES
 * 
 * 2020.07.25 | 1.0.0
 * 	Initial documentation
 * 
 */

/**
 * A project implementation for Wii U WUD images.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since July 22, 2020
 */
public class WiiUProject extends NTDProject{
	
	/*----- Constant -----*/
	
	/*----- Instance Variables -----*/
	
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
		if(!FileBuffer.directoryExists(decdir)) Files.createDirectories(Paths.get(decdir));
		
		//Mark encrypted regions
		int pcount = disc.getPartitionCount();
		List<EncryptionRegion> encregs = proj.getEncRegListReference();
		for(int i = 0; i < pcount; i++){
			long poff = disc.getPartitionOffset(i);
			long psz = disc.getPartitionSize(i);
			
			EncryptionRegion ereg = new EncryptionRegion(WiiAESDef.getDefinition(), poff, psz, decdir);
			encregs.add(ereg);
		}
		EncryptionRegion ereg = new EncryptionRegion(WiiAESDef.getDefinition(), 0x18000, 0x8000, decdir); //Partition table
		encregs.add(ereg);
		
		//Set default icon...
		if(ckey == null || gamekey == null){
			//Lock
			proj.setBannerIcon(new BufferedImage[]{NTDProgramFiles.getDefaultImage_lock()});
		}
		else{
			//Default
			proj.setBannerIcon(new BufferedImage[]{NTDProgramFiles.getDefaultImage_unknown()});
		}
		
		//Decryption
		WudDecObserver obs = null;
		if(observer != null) obs = new WudDecObserver(observer);
		disc.decryptPartitionsTo(decdir, obs);
		if(observer != null) obs.dispose();
		DirectoryNode root = disc.getFileTree();
		proj.setTreeRoot(root);
		
		//Get banner data...
		if(observer != null){
			observer.setPrimaryString("Importing Data");
			observer.setSecondaryString("Reading banner data");
		}
		String icopath = disc.getIconPath();
		String metapath = disc.getMetaXMLPath();
		
		FileNode iconode = root.getNodeAt(icopath);
		BufferedImage icon = null;
		if(iconode != null){
			icon = TGAFile.readTGA(iconode).getImage();
			proj.setBannerIcon(new BufferedImage[]{icon});
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
				proj.setBannerTitle(bnr[1]);
				proj.setPublisherName(bnr[2]);
			}	
		}
		
		return proj;
	}
	
	/*----- Decryption -----*/
	
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
		byte[] ckey = NTDProgramFiles.getKey(NTDProgramFiles.KEYNAME_WIIU_COMMON);
		if(ckey != null) WiiUDisc.setCommonKey(ckey);
		byte[] gkey = loadGameKey();
		String imgpath = getROMPath();
		
		//Initial disc read...
		try{
			if(observer != null){
				observer.setSecondaryString("Parsing disc structure");
			}
			WiiUDisc disc = WiiUDisc.readWUD(imgpath, gkey);
			//WiiUDisc disc = WiiUDisc.loadPredecedWUD(imgpath, gkey, getDecryptedDataDir());
			
			String decdir = getDecryptedDataDir();
			if(!FileBuffer.directoryExists(decdir)) Files.createDirectories(Paths.get(decdir));
			
			if(ckey == null || gkey == null){
				setBannerIcon(new BufferedImage[]{NTDProgramFiles.getDefaultImage_lock()});
				return false;
			}
			setBannerIcon(new BufferedImage[]{NTDProgramFiles.getDefaultImage_unknown()});

			//Decryption
			WudDecObserver obs = null;
			if(observer != null) obs = new WudDecObserver(observer);
			disc.decryptPartitionsTo(decdir, obs);
			if(observer != null) obs.dispose();
			DirectoryNode root = disc.getFileTree();
			setTreeRoot(root);
			
			//Get banner data...
			if(observer != null){
				observer.setPrimaryString("Importing Data");
				observer.setSecondaryString("Reading banner data");
			}
			String icopath = disc.getIconPath();
			String metapath = disc.getMetaXMLPath();
			
			//System.err.println("icopath = " + icopath);
			//System.err.println("metapath = " + metapath);
			//root.printMeToStdErr(0);
			//System.err.println("freaking hello??!?!?");
			
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
		catch(UnsupportedFileTypeException x){
			x.printStackTrace();
			return false;
		}
		
		setModifiedTime(OffsetDateTime.now());
		return true;
	}
	
	/*----- Alt Methods -----*/
	
	public void resetTree(ProgressListeningDialog observer) throws IOException{
		byte[] ckey = NTDProgramFiles.getKey(NTDProgramFiles.KEYNAME_WIIU_COMMON);
		if(ckey != null) WiiUDisc.setCommonKey(ckey);
		byte[] gkey = loadGameKey();
		String imgpath = getROMPath();
		
		boolean dec = true;
		String decdir = getDecryptedDataDir();
		if(!FileBuffer.directoryExists(decdir)) dec = false;
		dec = dec && (ckey != null) && (gkey != null);
		
		if(!dec){
			try{
				WiiUDisc disc = WiiUDisc.readWUD(imgpath, gkey);
				setBannerIcon(new BufferedImage[]{NTDProgramFiles.getDefaultImage_lock()});
				setTreeRoot(disc.getFileTree());
			}
			catch(UnsupportedFileTypeException x){
				x.printStackTrace();
				throw new IOException();
			}	
		}
		else{
			try{
				WiiUDisc disc = WiiUDisc.loadPredecedWUD(imgpath, gkey, decdir);
				setTreeRoot(disc.getFileTree());
			}
			catch(UnsupportedFileTypeException x){
				x.printStackTrace();
				throw new IOException();
			}	
		}
		
		setModifiedTime(OffsetDateTime.now());
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
	
	
}
