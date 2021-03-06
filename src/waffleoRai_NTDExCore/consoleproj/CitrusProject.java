package waffleoRai_NTDExCore.consoleproj;

import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import waffleoRai_Containers.nintendo.WiiSaveBannerFile;
import waffleoRai_Containers.nintendo.citrus.CitrusCrypt;
import waffleoRai_Containers.nintendo.citrus.CitrusNCC;
import waffleoRai_Containers.nintendo.citrus.CitrusNCSD;
import waffleoRai_Containers.nintendo.citrus.CitrusSMDH;
import waffleoRai_Containers.nintendo.citrus.CitrusUtil;
import waffleoRai_Encryption.nintendo.NinCryptTable;
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
import waffleoRai_Files.tree.DirectoryNode;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_fdefs.nintendo.CitrusAESCTRDef;

/*
 * UPDATES
 * 
 * 2020.07.16 | 1.0.0
 * 	Initial Documentation
 * 
 * 2020.07.22 | 1.0.0 -> 1.0.1
 * 	Specified icon size for display button
 * 
 * 2020.08.16 | 1.0.1 -> 1.1.0
 * 	Added low-level FS method
 * 
 * 2020.09.27 | 1.1.0 -> 2.0.0
 * 	Update to read directly from encrypted ROM w/o dec buffer
 * 
 * 2020.11.20 | 2.0.0 -> 2.0.1
 * 	Added scan for empty dirs on import/tree reset
 * 
 * 2021.06.23 | 2.0.1 -> 2.0.2
 * 	Default banner image is set to logo
 */

/**
 * NTDProject implementation for a 3DS CCI image.
 * @author Blythe Hospelhorn
 * @version 2.0.2
 * @since June 23, 2021
 */
public class CitrusProject extends NTDProject{
	
	/*----- Constant -----*/
	
	/*----- Instance Variables -----*/
	
	private NinCryptTable crypt_table;
	
	/*----- Construction -----*/	
	
	/**
	 * Generate an empty CitrusProject (3DS).
	 * @since 1.0.0
	 */
	public CitrusProject(){super(); super.setConsole(Console._3DS);}
	
	/**
	 * Read a 3DS CCI image file from disc, decrypt (if keys are present) and parse
	 * file tree. Generate an NTDProject from the image data.
	 * @param imgpath Path to image file.
	 * @param reg Game region image is associated with.
	 * @param observer Observing dialog for listening to updates.
	 * @return A 3DS specific NTDProject for use in NTDExplorer.
	 * @throws IOException If there is an error opening or writing any files.
	 * @throws UnsupportedFileTypeException If there is an error parsing any files.
	 * @since 1.0.0
	 */
	public static CitrusProject create3DSProject(String imgpath, GameRegion reg, ProgressListeningDialog observer) throws IOException, UnsupportedFileTypeException{
		//First, check for keys...
		CitrusCrypt crypto = NTDProgramFiles.load3DSKeystate();
		
		//Load Image...
		FileBuffer buffer = FileBuffer.createBuffer(imgpath, false);
		CitrusNCSD ncsd = CitrusNCSD.readNCSD(buffer, 0, true);
		
		//Load initial project stuff
		CitrusProject proj = new CitrusProject();
		proj.setImportedTime(OffsetDateTime.now());
		proj.setModifiedTime(OffsetDateTime.now());
		proj.setRegion(reg);
		proj.setROMPath(imgpath);
		
		//Flag partition 0. This is our friend. We'll use this one to generate gamecode data (and eventually nab icon)
		CitrusNCC part0 = ncsd.getPartition(0);
		String productid = part0.getProductID();
		proj.setMakerCode(part0.getMakerCode());
		int slen = productid.length();
		String code4 = productid.substring(slen-4, slen);
		proj.setGameCode(code4);
		proj.setDefoLanguage(DefoLanguage.getLanReg(code4.charAt(3)));
		proj.setFullCode("CTR_" + code4 + "_" + reg.getShortCode());
		
		//Get decdir and mark encrypted regions
		/*String decdir = proj.getDecryptedDataDir();
		if(!FileBuffer.directoryExists(decdir)) Files.createDirectories(Paths.get(decdir));*/
		List<EncryptionRegion> encregs = proj.getEncRegListReference(); //Eh, lazy. So just do exheader, ExeFS(as one), and RomFS
		for(int i = 0; i < 8; i++){
			CitrusNCC part = ncsd.getPartition(i);
			if(part != null){
				long part_off = ncsd.getPartitionOffset(i);
				String buffpath = proj.generatePartitionBufferPath(part.getPartitionID());
				if(part.isCXI()){
					EncryptionRegion r = new EncryptionRegion(CitrusAESCTRDef.getDefinition(), part_off + 0x200, 0x400, buffpath);
					encregs.add(r);
				}
				
				//ExeFS
				long off = part.getExeFSOffset();
				long len = part.getExeFSSize();
				EncryptionRegion r = new EncryptionRegion(CitrusAESCTRDef.getDefinition(), part_off + off, len, buffpath);
				encregs.add(r);
				
				//RomFS
				off = part.getRomFSOffset();
				len = part.getRomFSSize();
				r = new EncryptionRegion(CitrusAESCTRDef.getDefinition(), part_off + off, len, buffpath);
				encregs.add(r);
				
				//Run decryption (if possible)
				/*if(crypto != null){
					observer.setSecondaryString("Decrypting partition " + i);
					part.setDecBufferLocation(buffpath);
					part.refreshDecBuffer(crypto, true);
				}*/
			}
		}
		
		//Parse what can be parsed.
		observer.setSecondaryString("Attempting unlock");
		if(crypto != null) ncsd.unlock(crypto);
		proj.crypt_table = ncsd.generateCryptTable();
		proj.saveCryptTable();
		
		observer.setSecondaryString("Parsing file tree");
		//DirectoryNode root = ncsd.getFileTree();
		DirectoryNode root = ncsd.getFileTreeDirect(false);
		//root.printMeToStdErr(0);
		//proj.crypt_table.printMe();
		NTDTools.scanForEmptyDirectories(root);
		proj.setTreeRoot(root);
		
		//Try to nab icon/banner
		CitrusUtil.setActiveCryptTable(proj.crypt_table);
		observer.setSecondaryString("Parsing banner");
		boolean ico_found = false;
		if(crypto != null){
			//May have worked? Check tree.
			String ico_path = "/" + Long.toHexString(part0.getPartitionID()) + "/ExeFS/icon";
			FileNode ico_node = root.getNodeAt(ico_path);
			//System.err.println("Icon found at: " + ico_path +" ? " + (ico_node != null));
			if(ico_node != null){
				CitrusSMDH smdh = CitrusSMDH.readSMDH(ico_node.loadData(), 0);
				proj.setBannerIcon(new BufferedImage[]{smdh.getIcon()});
				
				//Banner strings...
				proj.setBannerTitle(smdh.getShortDescription(CitrusSMDH.LANIDX_ENG));
				proj.setPublisherName(smdh.getPublisherName(CitrusSMDH.LANIDX_ENG));
				ico_found = true;
			}
			else{
				BufferedImage defo_ico = NTDProgramFiles.getConsoleDefaultImage(Console._3DS, false);
				if(defo_ico != null) proj.setBannerIcon(new BufferedImage[]{defo_ico});
			}
		}
		if(!ico_found){
			//Set the lock icon.
			//proj.setBannerIcon(new BufferedImage[]{NTDProgramFiles.scaleDefaultImage_lock(48, 48)});
			BufferedImage defo_ico = NTDProgramFiles.getConsoleDefaultImage(Console._3DS, true);
			if(defo_ico != null) proj.setBannerIcon(new BufferedImage[]{defo_ico});
			else proj.setBannerIcon(new BufferedImage[]{NTDProgramFiles.scaleDefaultImage_lock(48, 48)});
			proj.setBannerTitle("3DS Software " + part0.getProductID());
			proj.setPublisherName("Publisher Unknown");
		}
		CitrusUtil.clearActiveCryptTable();
		
		return proj;
	}
	
	/*----- Decryption -----*/
	
	/**
	 * Get the path to the saved crypt table for this project. This
	 * should be in the project directory.
	 * <br>The crypt table is used to store the locations, keys,
	 * and base CTRs for encrypted regions in the source
	 * file(s) so that files can be easily read directly off the raw image instead
	 * of from a decrypted copy.
	 * @return Crypt table path on local file system for this project as a string.
	 * @since 2.0.0
	 */
	public String getCryptTablePath(){
		return super.getCustomDataDirPath() + File.separator + "ctbl.bin";
	}
	
	/**
	 * Save the project crypt table to project directory.
	 * <br>The crypt table is used to store the locations, keys,
	 * and base CTRs for encrypted regions in the source
	 * file(s) so that files can be easily read directly off the raw image instead
	 * of from a decrypted copy.
	 * @throws IOException If there is an error writing the file.
	 * @since 2.0.0
	 */
	public void saveCryptTable() throws IOException{
		String cpath = getCryptTablePath();
		if(crypt_table != null) crypt_table.exportToFile(cpath);
	}
	
	/**
	 * Load the project crypt table from the project directory.
	 * <br>The crypt table is used to store the locations, keys,
	 * and base CTRs for encrypted regions in the source
	 * file(s) so that files can be easily read directly off the raw image instead
	 * of from a decrypted copy.
	 * @throws IOException If the target file does not exist or there is an error
	 * reading the file off disk.
	 * @since 2.0.0
	 */
	public void loadCryptTable() throws IOException{
		String cpath = getCryptTablePath();
		crypt_table = new NinCryptTable();
		crypt_table.importFromFile(cpath);
	}
	
	/**
	 * Generate the path to the decryption buffer file that should be used for 
	 * a partition with the specified ID in this project.
	 * @param part_id Long UID of partition.
	 * @return Project & partition specific decryption buffer path.
	 * @since 1.0.0
	 */
	public String generatePartitionBufferPath(long part_id){
		//Obsolete, but too lazy to delete deps so will keep for now
		String path = getDecryptedDataDir();
		path += File.separator + NTDProgramFiles.DECSTEM_CTR_PART;
		path += Long.toHexString(part_id) + ".bin";
		return path;
	}
	
	public boolean decrypt(ProgressListeningDialog observer) throws IOException{

		//Load keys
		CitrusCrypt crypto = NTDProgramFiles.load3DSKeystate();
		
		if(crypto == null){
			//Decryption cannot be done...
			return false;
		}
		
		//Out of laziness, we'll just reload the NCSD from the ROM path...
		try{
			FileBuffer src = FileBuffer.createBuffer(getROMPath(), false);
			CitrusNCSD ncsd = CitrusNCSD.readNCSD(src, 0, true);
			
			//Unlock & reparse the tree
			ncsd.unlock(crypto);
			crypt_table = ncsd.generateCryptTable();
			setTreeRoot(ncsd.getFileTreeDirect(false));
			NTDTools.scanForEmptyDirectories(super.getTreeRoot());
			saveCryptTable();
			
			//Try again to grab the banner.
			CitrusUtil.setActiveCryptTable(crypt_table);
			CitrusNCC part0 = ncsd.getPartition(0);
			String ico_path = "/" + Long.toHexString(part0.getPartitionID()) + "/ExeFS/icon";
			FileNode ico_node = getTreeRoot().getNodeAt(ico_path);
			//System.err.println("Icon found at: " + ico_path +" ? " + (ico_node != null));
			if(ico_node != null){
				CitrusSMDH smdh = CitrusSMDH.readSMDH(ico_node.loadData(), 0);
				setBannerIcon(new BufferedImage[]{smdh.getIcon()});
				
				//Banner strings...
				setBannerTitle(smdh.getShortDescription(CitrusSMDH.LANIDX_ENG));
				setPublisherName(smdh.getPublisherName(CitrusSMDH.LANIDX_ENG));
			}
			else{
				setBannerIcon(new BufferedImage[]{NTDProgramFiles.scaleDefaultImage_unknown(48, 48)});
				setBannerTitle("3DS Software " + part0.getProductID());
				setPublisherName("Publisher Unknown");
			}
			CitrusUtil.clearActiveCryptTable();
			
		}
		catch(UnsupportedFileTypeException x){
			x.printStackTrace();
			throw new IOException("3DS Image parsing error");
		}
		
		
		return true;
	}
	
	/*----- Alt Methods -----*/
	
	public void onProjectOpen(){
		try {
			loadCryptTable();
			CitrusUtil.setActiveCryptTable(crypt_table);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void onProjectClose(){
		try {
			saveCryptTable();
			CitrusUtil.clearActiveCryptTable();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void resetTreeCore(boolean lowfs) throws IOException{
		CitrusCrypt crypto = NTDProgramFiles.load3DSKeystate();
		
		try{
			FileBuffer src = FileBuffer.createBuffer(getROMPath(), false);
			CitrusNCSD ncsd = CitrusNCSD.readNCSD(src, 0, true);
			
			//Unlock & reparse the tree
			ncsd.unlock(crypto);
			crypt_table = ncsd.generateCryptTable();
			setTreeRoot(ncsd.getFileTreeDirect(lowfs));
			NTDTools.scanForEmptyDirectories(super.getTreeRoot());
			saveCryptTable();			
		}
		catch(UnsupportedFileTypeException x){
			x.printStackTrace();
			throw new IOException("3DS Image parsing error");
		}
	}
	
	public void resetTree(ProgressListeningDialog observer) throws IOException{	
		resetTreeCore(false);
	}
	
	public void resetTreeFSDetail(ProgressListeningDialog observer) throws IOException{
		resetTreeCore(true);
	}
	
	public String[] getBannerLines(){
		//See if banner is multiline
		String bnrraw = this.getBannerTitle();
		String[] bnrlines = bnrraw.split("\n");
		
		String[] out = null;
		switch(bnrlines.length){
			case 1:
				out = new String[]{bnrlines[0], super.getPublisherTag()};
				break;
			case 2:
				out = new String[]{bnrlines[0], bnrlines[1], super.getPublisherTag()};
				break;
			case 3:
				out = new String[]{bnrlines[0], bnrlines[1], bnrlines[2]};
				break;
		}
		
		return out;
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
		//Just copied from Wii version. Icon from NCCH should only be one frame, but NTDE allows ability to set more
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
	

}
