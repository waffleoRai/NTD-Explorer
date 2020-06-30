package waffleoRai_NTDExCore.consoleproj;

import java.awt.Frame;
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
import waffleoRai_Containers.nintendo.wiidisc.WiiPartition;
import waffleoRai_Containers.nintendo.wiidisc.WiiPartitionGroup;
import waffleoRai_NTDExCore.Console;
import waffleoRai_NTDExCore.DefoLanguage;
import waffleoRai_NTDExCore.GameRegion;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;

import waffleoRai_NTDExGUI.banners.Animator;
import waffleoRai_NTDExGUI.panels.AbstractGameOpenButton;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;


/*
 * UPDATES
 * 
 * 2020.06.28 | 1.0.0
 * 	Initial Documentation
 * 
 */

/**
 * NTDProject implementation for a Wii disc image.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since June 28, 2020
 *
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
	 * @return NTDProject to use in NTD Explorer.
	 * @throws IOException If file cannot be read from disk.
	 * @throws UnsupportedFileTypeException If the file cannot be parsed as a Wii/GameCube disc image.
	 * @since 1.0.0
	 */
	public static WiiProject createFromWiiImage(String imgpath, GameRegion reg) throws IOException, UnsupportedFileTypeException{
	
		//Load key, if present
		byte[] key = NTDProgramFiles.getKey(NTDProgramFiles.KEYNAME_WII_COMMON);
		if(key != null){
			WiiDisc.setCommonKey(key);
		}
		
		//Try to read disc
		WiiDisc img = WiiDisc.parseFromData(FileBuffer.createBuffer(imgpath, true));
		
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
	
	public boolean decrypt() throws IOException{
		//This also resets the file tree!!
		byte[] key = NTDProgramFiles.getKey(NTDProgramFiles.KEYNAME_WII_COMMON);
		if(key == null) return false;
		
		WiiDisc.setCommonKey(key);
		try{
			WiiDisc img = WiiDisc.parseFromData(FileBuffer.createBuffer(getROMPath(), true));
			
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
	
	public void resetTree() throws IOException{
		
		byte[] key = NTDProgramFiles.getKey(NTDProgramFiles.KEYNAME_WII_COMMON);

		WiiDisc.setCommonKey(key);
		try{
			WiiDisc img = WiiDisc.parseFromData(FileBuffer.createBuffer(getROMPath(), true));
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
		//TODO
		return null;
	}
	
	public void showImageInfoDialog(Frame gui){
		//TODO
	}
	
	public Animator getBannerIconAnimator(ActionListener l){
		//TODO
		return null;
	}
	
}
