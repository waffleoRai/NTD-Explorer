package waffleoRai_NTDExCore.consoleproj;

import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import waffleoRai_Containers.nintendo.GCWiiHeader;
import waffleoRai_Containers.nintendo.WiiDisc;
import waffleoRai_Containers.nintendo.WiiSaveBannerFile;
import waffleoRai_Containers.nintendo.wiidisc.WiiCrypt;
import waffleoRai_Containers.nintendo.wiidisc.WiiPartition;
import waffleoRai_Containers.nintendo.wiidisc.WiiPartitionGroup;
import waffleoRai_Encryption.nintendo.NinCryptTable;
import waffleoRai_Files.tree.DirectoryNode;
import waffleoRai_Image.Animation;
import waffleoRai_NTDExCore.Console;
import waffleoRai_NTDExCore.DefoLanguage;
import waffleoRai_NTDExCore.EncryptionRegion;
import waffleoRai_NTDExCore.GameRegion;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.NTDTools;
import waffleoRai_NTDExGUI.banners.Animator;
import waffleoRai_NTDExGUI.banners.PingpongAnimator;
import waffleoRai_NTDExGUI.banners.StandardAnimator;
import waffleoRai_NTDExGUI.banners.Unanimator;
import waffleoRai_NTDExGUI.dialogs.progress.ProgressListeningDialog;
import waffleoRai_NTDExGUI.panels.AbstractGameOpenButton;
import waffleoRai_NTDExGUI.panels.DefaultGameOpenButton;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_fdefs.nintendo.WiiAESDef;


/*
 * UPDATES
 * 
 * 2020.06.28 | 1.0.0
 * 	Initial Documentation
 * 
 * 2020.07.03 | 1.0.0 -> 1.1.0
 * 	Added observer parameters to import, tree reset, and decrypt methods.
 * 
 * 2020.07.04 | 1.1.0 -> 1.1.1
 * 	Mark encryption regions, dispose of temp buffers when done decrypting
 * 
 * 2020.07.22 | 1.1.1 -> 1.1.2
 * 	Specified icon size for display button
 * 
 * 2020.08.16 | 1.1.2 -> 1.2.0
 * 	Added low-level FS method
 * 
 * 2020.09.26 | 1.2.0 -> 1.2.1
 * 	supportsSaveBannerImport() (yes)
 * 
 * 2020.10.28 | 1.2.1 -> 2.0.0
 * 	update to read directly from image without
 * 		generating decrypted files first
 * 
 * 2020.11.20 | 2.0.0 -> 2.0.1
 * 	Added scan for empty dirs on import/tree reset
 * 
 */

/**
 * NTDProject implementation for a Wii disc image.
 * @author Blythe Hospelhorn
 * @version 2.0.1
 * @since November 20, 2020
 */
public class WiiProject extends NTDProject{
	
	/*----- Constant -----*/
	
	/*----- Instance Variables -----*/
	
	private NinCryptTable crypt_table;
	
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
		WiiDisc img = WiiDisc.parseFromData(FileBuffer.createBuffer(imgpath, true), obs, false);
		
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
		
		//String decdir = proj.getDecryptedDataDir();
		//if(!FileBuffer.directoryExists(decdir)) Files.createDirectories(Paths.get(decdir));
		
		//Mark encrypted regions
		List<EncryptionRegion> encregs = proj.getEncRegListReference();
		
		//If decryption cannot be done, set icon and flash notice accordingly.
		//Otherwise, save decryption buffers
		if(key == null){
			BufferedImage lockico = NTDProgramFiles.scaleDefaultImage_lock(48, 48);
			proj.setBannerIcon(new BufferedImage[]{lockico});
			
			//This is an artifact, but I'll leave it for noting encryption regions
			for(int i = 0; i < 4; i++){
				WiiPartitionGroup grp = img.getPartition(i);
				if(grp == null) continue;
				int j = 0;
				List<WiiPartition> parts = grp.getSubPartitions();
				for(WiiPartition part : parts){
					String decpath = proj.getDecryptedPartitionPath(i,j++);
					long off = part.getAddress() + part.getDataOffset();
					
					EncryptionRegion ereg = new EncryptionRegion(WiiAESDef.getDefinition(), off, part.getDataSize(), decpath);
					encregs.add(ereg);
				}
			}
		}
		else{
			proj.setBannerIcon(new BufferedImage[]{NTDProgramFiles.scaleDefaultImage_unknown(48, 48)});
			proj.crypt_table = img.generateCryptTable();
			proj.saveCryptTable();
			WiiCrypt.initializeDecryptorState(proj.crypt_table);
			
			for(int i = 0; i < 4; i++){
				WiiPartitionGroup grp = img.getPartition(i);
				if(grp == null) continue;
				int j = 0;
				List<WiiPartition> parts = grp.getSubPartitions();
				for(WiiPartition part : parts){
					String decpath = proj.getDecryptedPartitionPath(i,j++);
					//part.writeDecryptedRaw(decpath);
					
					long off = part.getAddress() + part.getDataOffset();
					EncryptionRegion ereg = new EncryptionRegion(WiiAESDef.getDefinition(), off, part.getDataSize(), decpath);
					encregs.add(ereg);
				}
			}
			
			
		}
		
		//Load tree
		//proj.setTreeRoot(img.getDiscTree(imgpath, decdir + File.separator + NTDProgramFiles.DECSTEM_WII_PART));
		proj.setTreeRoot(img.buildDirectTree(imgpath, false));
		NTDTools.doTypeScan(proj.getTreeRoot(), observer);
		
		//img.deleteParsingTempFiles();
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
		
		try{
			resetTree(false, observer);
		}
		catch(UnsupportedFileTypeException x){
			x.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/*----- Alt Methods -----*/
	
	private void resetTree(boolean low_fs, ProgressListeningDialog observer) throws IOException, UnsupportedFileTypeException{

		byte[] key = NTDProgramFiles.getKey(NTDProgramFiles.KEYNAME_WII_COMMON);
		WiiDisc.setCommonKey(key);
		
		String imgpath = super.getROMPath();
		if(observer != null){
			observer.setPrimaryString("Reading");
			observer.setSecondaryString("Reading disk structure");
		}
		WiiDisc img = WiiDisc.parseFromData(FileBuffer.createBuffer(imgpath, true), null, false);
		img.unlock();
		
		if(observer != null){observer.setSecondaryString("Generating crypt table");}
		crypt_table = img.generateCryptTable();
		
		if(observer != null){observer.setSecondaryString("Processing file tree");}
		DirectoryNode tree = img.buildDirectTree(imgpath, low_fs);
		
		if(observer != null){
			observer.setPrimaryString("Updating");
			observer.setSecondaryString("Updating project tree and table");
		}
		super.setTreeRoot(tree);
		saveCryptTable();
		
		//Set crypto state
		WiiCrypt.initializeDecryptorState(crypt_table);
		
		NTDTools.doTypeScan(getTreeRoot(), observer);
		setModifiedTime(OffsetDateTime.now());
	}
	
	public void resetTree(ProgressListeningDialog observer) throws IOException{
		try{
			resetTree(false, observer);
		}
		catch(UnsupportedFileTypeException x){
			x.printStackTrace();
			throw new IOException("Error parsing Wii disc image");
		}	
	}
	
	public void resetTreeFSDetail(ProgressListeningDialog observer) throws IOException{
		try{
			resetTree(true, observer);
		}
		catch(UnsupportedFileTypeException x){
			x.printStackTrace();
			throw new IOException("Error parsing Wii disc image");
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
		DefaultGameOpenButton gamepnl = new DefaultGameOpenButton(DefaultGameOpenButton.ICONSZ_48);
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
		/*int fcount = anim_raw.getNumberFrames();
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
		}*/
		Animation anim = anim_raw;
		
		if(anim.getNumberFrames() == 1) return new Unanimator(anim.getFrameImage(0));
		if(anim.getAnimationMode() == Animation.ANIM_MODE_PINGPONG) return new PingpongAnimator(anim, WiiSaveBannerFile.ANIM_SPEED_CONST, l);
		
		return new StandardAnimator(anim, WiiSaveBannerFile.ANIM_SPEED_CONST, l);
	}
	
	public boolean supportsSaveBannerImport(){return true;}
	
	public void onProjectOpen(){
		try {
			loadCryptTable();
			if(crypt_table != null) WiiCrypt.initializeDecryptorState(crypt_table);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void onProjectClose(){
		WiiCrypt.clearDecryptorState();
	}
	
}
