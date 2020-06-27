package waffleoRai_NTDExCore.consoleproj;

import java.awt.Frame;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.OffsetDateTime;

import waffleoRai_Containers.nintendo.GCMemCard;
import waffleoRai_Containers.nintendo.GCWiiDisc;
import waffleoRai_Containers.nintendo.GCWiiHeader;
import waffleoRai_Executable.nintendo.DolExe;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Image.Animation;
import waffleoRai_NTDExCore.Console;
import waffleoRai_NTDExCore.DefoLanguage;
import waffleoRai_NTDExCore.GameRegion;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.NTDTools;
import waffleoRai_NTDExGUI.banners.Animator;
import waffleoRai_NTDExGUI.banners.PingpongAnimator;
import waffleoRai_NTDExGUI.banners.StandardAnimator;
import waffleoRai_NTDExGUI.banners.Unanimator;
import waffleoRai_NTDExGUI.panels.AbstractGameOpenButton;
import waffleoRai_NTDExGUI.panels.DefaultGameOpenButton;
import waffleoRai_Utils.FileNode;
import waffleoRai_Utils.DirectoryNode;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_fdefs.nintendo.PowerGCSysFileDefs;

/*
 * UPDATES
 * 
 * 2020.06.25 | 1.0.0
 * 	Initial Documentation
 * 
 */

/**
 * NTDProject implementation for a GameCube disk image.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since June 25, 2020
 *
 */
public class GCProject extends NTDProject{

	/*----- Constant -----*/
	
	/*----- Instance Variables -----*/
	
	/*----- Construction -----*/
	
	/**
	 * Generate an empty GCProject.
	 * @since 1.0.0
	 */
	public GCProject(){super(); super.setConsole(Console.GAMECUBE);}
	
	/**
	 * Create a GCProject object by reading in a GameCube disk image.
	 * This parses the header information and file system tree.
	 * @param imgpath Path to raw disk image file to read. Usually has .gcm or .iso extension.
	 * @param region Region of the software the image contains. 
	 * @return NTDProject to use in NTD Explorer.
	 * @throws IOException If file cannot be read from disk.
	 * @throws UnsupportedFileTypeException If the file cannot be parsed as a GameCube disc image.
	 * @since 1.0.0
	 */
	public static GCProject createFromGCM(String imgpath, GameRegion region) throws IOException, UnsupportedFileTypeException{
		GCWiiDisc gcimg = new GCWiiDisc(imgpath);
		GCWiiHeader header = gcimg.getHeader();
		
		GCProject proj = new GCProject();
		proj.setImportedTime(OffsetDateTime.now());
		proj.setModifiedTime(OffsetDateTime.now());
		
		proj.setRegion(region);
		proj.setGameCode(header.get4DigitGameCode());
		proj.setMakerCode(header.getMakerCode());
		proj.setDefoLanguage(DefoLanguage.getLanReg(proj.getGameCode4().charAt(3)));
		proj.setFullCode("DOL_" + proj.getGameCode4() + "_" + region.getShortCode());
		proj.setROMPath(imgpath);
		
		proj.setBannerTitle(header.getGameTitle());
		proj.setPublisherName(NTDProject.getPublisherName(proj.getMakerCode()));
		
		DirectoryNode root = gcimg.getDiscTree();
		scanTreeDir(imgpath, root);
		proj.setTreeRoot(root);
		
		//Type tags
		FileNode fn = root.getNodeAt("/sys/boot.bin");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getHeaderDef()));
		fn = root.getNodeAt("/sys/bi2.bin");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getBi2Def()));
		fn = root.getNodeAt("/sys/fst.bin");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getFSTDef()));
		fn = root.getNodeAt("/sys/apploader.img");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getApploaderDef()));
		fn = root.getNodeAt("/sys/main.dol");
		fn.setTypeChainHead(new FileTypeDefNode(DolExe.getDefinition()));
		
		return proj;
	}
	
	/*----- Alt Methods -----*/
	
	public void resetTree() throws IOException{
		GCWiiDisc gcimg = new GCWiiDisc(getROMPath());
		DirectoryNode root = gcimg.getDiscTree();
		scanTreeDir(getROMPath(), root);
		super.setTreeRoot(root);
		
		FileNode fn = root.getNodeAt("/sys/boot.bin");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getHeaderDef()));
		fn = root.getNodeAt("/sys/bi2.bin");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getBi2Def()));
		fn = root.getNodeAt("/sys/fst.bin");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getFSTDef()));
		fn = root.getNodeAt("/sys/apploader.img");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getApploaderDef()));
		fn = root.getNodeAt("/sys/main.dol");
		fn.setTypeChainHead(new FileTypeDefNode(DolExe.getDefinition()));
		
		NTDTools.doTypeScan(root, null);
		stampModificationTime();
	}
	
	public AbstractGameOpenButton generateOpenButton(){
		DefaultGameOpenButton gamepnl = new DefaultGameOpenButton();
		int millis = GCMemCard.ICO_FRAME_MILLIS * 4;
		gamepnl.loadMe(this, millis);
		
		//TODO
		//Maybe adjust labels to something specific...
		
		return gamepnl;
	}
	
	public void showImageInfoDialog(Frame gui){
		//TODO
	}
	
	public Animator getBannerIconAnimator(ActionListener l){
		Animation anim = super.getBannerIcon();
		if(anim == null) return null;
		
		if(anim.getNumberFrames() == 1) return new Unanimator(anim.getFrameImage(0));
		if(anim.getAnimationMode() == Animation.ANIM_MODE_PINGPONG) return new PingpongAnimator(anim, GCMemCard.ICO_FRAME_MILLIS << 2, l);
		
		return new StandardAnimator(anim, GCMemCard.ICO_FRAME_MILLIS << 2, l);
	}
	
}
