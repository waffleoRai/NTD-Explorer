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

import waffleoRai_Containers.nintendo.GCWiiHeader;
import waffleoRai_Containers.nintendo.WiiDisc;
import waffleoRai_Containers.nintendo.WiiSaveBannerFile;
import waffleoRai_Containers.nintendo.wiidisc.WiiPartition;
import waffleoRai_Containers.nintendo.wiidisc.WiiPartitionGroup;
import waffleoRai_Image.Animation;
import waffleoRai_Image.AnimationFrame;
import waffleoRai_Image.SimpleAnimation;
import waffleoRai_NTDExCore.Console;
import waffleoRai_NTDExCore.DefoLanguage;
import waffleoRai_NTDExCore.GameRegion;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;

import waffleoRai_NTDExGUI.banners.Animator;
import waffleoRai_NTDExGUI.banners.PingpongAnimator;
import waffleoRai_NTDExGUI.banners.StandardAnimator;
import waffleoRai_NTDExGUI.banners.Unanimator;
import waffleoRai_NTDExGUI.dialogs.progress.ProgressListeningDialog;
import waffleoRai_NTDExGUI.panels.AbstractGameOpenButton;
import waffleoRai_NTDExGUI.panels.DefaultGameOpenButton;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;


/*
 * UPDATES
 * 
 * 2020.06.28 | 1.0.0
 * 	Initial Documentation
 * 
 * 2020.07.03 | 1.0.0 -> 1.1.0
 * 	Added observer parameters to import, tree reset, and decrypt methods.
 */

/**
 * NTDProject implementation for a Wii disc image.
 * @author Blythe Hospelhorn
 * @version 1.1.0
 * @since July 3, 2020
 */
public class WiiProject extends NTDProject{
	
/*----- Constant -----*/
	
	/*----- Instance Variables -----*/
	
	/*----- Construction -----*/
	
	/**
	 * Generate an empty WiiProject.
	 * @since 1.0.0
	 */
	public WiiProject(){super(); super.setConsole(Console.WII);}
	
	/**
	 * Create a WiiProject object by reading in a raw Wii disk image.
	 * This parses header information, partition tree, and file systems within each partition
	 * if the common key can be found. Otherwise, it just parses the partition information
	 * and leaves the partitions as encrypted files.
	 * <br>This method does not work for WBFS images, only raw.
	 * @param imgpath Path to raw disk image file to read. Usually has .wii or .iso extension.
	 * @param reg Region of the software the image contains. 
	 * @param observer Progress dialog to send progress update information to. 
	 * This parameter may be left null, in which case progress updates will not be visible.
	 * @return NTDProject to use in NTD Explorer.
	 * @throws IOException If file cannot be read from disk.
	 * @throws UnsupportedFileTypeException If the file cannot be parsed as a Wii/GameCube disc image.
	 * @since 1.0.0
	 */
	public static WiiProject createFromWiiImage(String imgpath, GameRegion reg, ProgressListeningDialog observer) throws IOException, UnsupportedFileTypeException{
	
		//Load key, if present
		byte[] key = NTDProgramFiles.getKey(NTDProgramFiles.KEYNAME_WII_COMMON);
		if(key != null){
			WiiDisc.setCommonKey(key);
		}
		
		//Try to read disc
		WiiDecObserver obs = null;
		if(observer != null) obs = new WiiDecObserver(observer);
		WiiDisc img = WiiDisc.parseFromData(FileBuffer.createBuffer(imgpath, true), obs);
		
		//Set primary disc data
		WiiProject proj = new WiiProject();
		proj.setImportedTime(OffsetDateTime.now());
		proj.setModifiedTime(OffsetDateTime.now());
		proj.setRegion(reg);
		
		GCWiiHeader header = img.getHeader();
		proj.setGameCode(header.get4DigitGameCode());
		proj.setMakerCode(header.getMakerCode());
		proj.setDefoLanguage(DefoLanguage.getLanReg(proj.getGameCode4().charAt(3)));
		proj.setFullCode("RVL_" + proj.getGameCode4() + "_" + reg.getShortCode());
		proj.setROMPath(imgpath);
		
		proj.setBannerTitle(header.getGameTitle());
		proj.setPublisherName(NTDProject.getPublisherName(proj.getMakerCode()));
		
		String decdir = proj.getDecryptedDataDir();
		if(!FileBuffer.directoryExists(decdir)) Files.createDirectories(Paths.get(decdir));
		
		//If decryption cannot be done, set icon and flash notice accordingly.
		//Otherwise, save decryption buffers
		if(key == null){
			BufferedImage lockico = NTDProgramFiles.scaleDefaultImage_lock(48, 48);
			proj.setBannerIcon(new BufferedImage[]{lockico});
		}
		else{
			proj.setBannerIcon(new BufferedImage[]{NTDProgramFiles.scaleDefaultImage_unknown(48, 48)});
			
			for(int i = 0; i < 4; i++){
				WiiPartitionGroup grp = img.getPartition(i);
				if(grp == null) continue;
				int j = 0;
				List<WiiPartition> parts = grp.getSubPartitions();
				for(WiiPartition part : parts){
					String decpath = proj.getDecryptedPartitionPath(i,j++);
					part.writeDecryptedRaw(decpath);
				}
			}
		}
		
		//Load tree
		proj.setTreeRoot(img.getDiscTree(imgpath, decdir + File.separator + NTDProgramFiles.DECSTEM_WII_PART));
		
		return proj;
	}
	
	/*----- Decryption -----*/
	
	/**
	 * Generate the path to the decrypted buffer file on disk for the specified partition.
	 * @param pgroup The partition group index.
	 * @param p Index of the partition within the group.
	 * @return Path that should be used for decrypted buffer file given the specified
	 * partition, or null if generation failed.
	 * @since 1.0.0
	 */
	public String getDecryptedPartitionPath(int pgroup, int p){
		String stem = this.getDecryptedDataDir() + File.separator + NTDProgramFiles.DECSTEM_WII_PART;
		return WiiDisc.generateDecryptedPartitionPath(stem, pgroup, p);
	}
	
	public boolean decrypt(ProgressListeningDialog observer) throws IOException{
		//This also resets the file tree!!
		byte[] key = NTDProgramFiles.getKey(NTDProgramFiles.KEYNAME_WII_COMMON);
		if(key == null) return false;
		
		WiiDisc.setCommonKey(key);
		try{
			//Add observer
			WiiDecObserver obs = null;
			if(observer != null) obs = new WiiDecObserver(observer);
			WiiDisc img = WiiDisc.parseFromData(FileBuffer.createBuffer(getROMPath(), true), obs);
			
			setBannerIcon(new BufferedImage[]{NTDProgramFiles.scaleDefaultImage_unknown(48, 48)});
			
			for(int i = 0; i < 4; i++){
				WiiPartitionGroup grp = img.getPartition(i);
				if(grp == null) continue;
				int j = 0;
				List<WiiPartition> parts = grp.getSubPartitions();
				for(WiiPartition part : parts){
					String decpath = getDecryptedPartitionPath(i,j++);
					part.writeDecryptedRaw(decpath);
				}
			}
			
			setTreeRoot(img.getDiscTree(getROMPath(), getDecryptedDataDir() + File.separator + NTDProgramFiles.DECSTEM_WII_PART));
		}
		catch(UnsupportedFileTypeException x){
			x.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/*----- Alt Methods -----*/
	
	public void resetTree(ProgressListeningDialog observer) throws IOException{
		
		byte[] key = NTDProgramFiles.getKey(NTDProgramFiles.KEYNAME_WII_COMMON);

		WiiDisc.setCommonKey(key);
		try{
			WiiDecObserver obs = null;
			if(observer != null) obs = new WiiDecObserver(observer);
			WiiDisc img = WiiDisc.parseFromData(FileBuffer.createBuffer(getROMPath(), true), obs);
			setTreeRoot(img.getDiscTree(getROMPath(), getDecryptedDataDir() + File.separator + NTDProgramFiles.DECSTEM_WII_PART));
		}
		catch(UnsupportedFileTypeException x){
			x.printStackTrace();
			return;
		}	
	}
	
	public String[] getBannerLines(){
		String title = super.getBannerTitle();
		if(title == null){
			title = "Wii Software " + getGameCode4() + getMakerCode();
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
		DefaultGameOpenButton gamepnl = new DefaultGameOpenButton();
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
		
		//Scale down to 32x32
		int fcount = anim_raw.getNumberFrames();
		Animation anim = new SimpleAnimation(fcount);
		anim.setAnimationMode(anim_raw.getAnimationMode());
		for(int f = 0; f < fcount; f++){
			AnimationFrame frame = anim_raw.getFrame(f);
			BufferedImage in = anim_raw.getFrameImage(f);
			Image scaled = in.getScaledInstance(32, 32, Image.SCALE_DEFAULT);
			BufferedImage out = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
			out.getGraphics().drawImage(scaled, 0, 0, null);
			
			AnimationFrame frame2 = new AnimationFrame(out, frame.getLengthInFrames());
			anim.setFrame(frame2, f);
		}
		
		if(anim.getNumberFrames() == 1) return new Unanimator(anim.getFrameImage(0));
		if(anim.getAnimationMode() == Animation.ANIM_MODE_PINGPONG) return new PingpongAnimator(anim, WiiSaveBannerFile.ANIM_SPEED_CONST, l);
		
		return new StandardAnimator(anim, WiiSaveBannerFile.ANIM_SPEED_CONST, l);
	}
	
}