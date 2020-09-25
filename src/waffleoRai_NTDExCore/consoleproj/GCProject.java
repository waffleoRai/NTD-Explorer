package waffleoRai_NTDExCore.consoleproj;

import java.awt.Frame;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.OffsetDateTime;

import waffleoRai_Containers.nintendo.GCMemCard;
import waffleoRai_Containers.nintendo.GCWiiDisc;
import waffleoRai_Containers.nintendo.GCWiiHeader;
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
import waffleoRai_NTDExGUI.dialogs.progress.ProgressListeningDialog;
import waffleoRai_NTDExGUI.panels.AbstractGameOpenButton;
import waffleoRai_NTDExGUI.panels.DefaultGameOpenButton;
import waffleoRai_Files.tree.DirectoryNode;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

/*
 * UPDATES
 * 
 * 2020.06.25 | 1.0.0
 * 	Initial Documentation
 * 
 * 2020.07.03 | 1.1.0
 * 	Added observer param for tree reset method (compatibility with Wii import)
 * 
 * 2020.07.22 | 1.1.1
 * 	Specified icon size for display button
 * 
 * 2020.08.16 | 1.1.1 -> 1.2.0
 * 	Added low-level FS method
 * 
 */

/**
 * NTDProject implementation for a GameCube disk image.
 * @author Blythe Hospelhorn
 * @version 1.2.0
 * @since August 16, 2020
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
		/*FileNode fn = root.getNodeAt("/sys/boot.bin");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getHeaderDef()));
		fn = root.getNodeAt("/sys/bi2.bin");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getBi2Def()));
		fn = root.getNodeAt("/sys/fst.bin");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getFSTDef()));
		fn = root.getNodeAt("/sys/apploader.img");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getApploaderDef()));
		fn = root.getNodeAt("/sys/main.dol");
		fn.setTypeChainHead(new FileTypeDefNode(DolExe.getDefinition()));*/
		
		return proj;
	}
	
	/*----- Alt Methods -----*/
	
	public void resetTree(ProgressListeningDialog observer) throws IOException{
		GCWiiDisc gcimg = new GCWiiDisc(getROMPath());
		DirectoryNode root = gcimg.getDiscTree();
		scanTreeDir(getROMPath(), root);
		super.setTreeRoot(root);
		
		/*FileNode fn = root.getNodeAt("/sys/boot.bin");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getHeaderDef()));
		fn = root.getNodeAt("/sys/bi2.bin");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getBi2Def()));
		fn = root.getNodeAt("/sys/fst.bin");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getFSTDef()));
		fn = root.getNodeAt("/sys/apploader.img");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getApploaderDef()));
		fn = root.getNodeAt("/sys/main.dol");
		fn.setTypeChainHead(new FileTypeDefNode(DolExe.getDefinition()));*/
		
		NTDTools.doTypeScan(root, null);
		stampModificationTime();
	}
	
	public void resetTreeFSDetail(ProgressListeningDialog observer) throws IOException{
		//TODO
		resetTree(observer);
	}
	
	public String[] getBannerLines(){

		//Get main banner
		String title = super.getBannerTitle();
		if(title == null){
			title = "GameCube Software " + getGameCode4() + getMakerCode();
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
		DefaultGameOpenButton gamepnl = new DefaultGameOpenButton(DefaultGameOpenButton.ICONSZ_32);
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
		Animation anim = super.getBannerIcon();
		if(anim == null) return null;
		
		if(anim.getNumberFrames() == 1) return new Unanimator(anim.getFrameImage(0));
		if(anim.getAnimationMode() == Animation.ANIM_MODE_PINGPONG) return new PingpongAnimator(anim, GCMemCard.ICO_FRAME_MILLIS << 2, l);
		
		return new StandardAnimator(anim, GCMemCard.ICO_FRAME_MILLIS << 2, l);
	}
	
}
