package waffleoRai_NTDExCore.consoleproj;

import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.OffsetDateTime;

import waffleoRai_Containers.nintendo.nus.N64ROMImage;
import waffleoRai_Containers.nintendo.nus.NUSDescrambler;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Files.tree.DirectoryNode;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_Image.Animation;
import waffleoRai_NTDExCore.Console;
import waffleoRai_NTDExCore.DefoLanguage;
import waffleoRai_NTDExCore.GameRegion;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExGUI.banners.Animator;
import waffleoRai_NTDExGUI.banners.Unanimator;
import waffleoRai_NTDExGUI.dialogs.progress.ProgressListeningDialog;
import waffleoRai_NTDExGUI.panels.AbstractGameOpenButton;
import waffleoRai_NTDExGUI.panels.DefaultGameOpenButton;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_fdefs.nintendo.NUSSysDefs;

/*
 * UPDATES
 * 
 * 2021.06.24 | 1.0.0
 * 	Initial Documentation
 * 
 */

/**
 * NTDProject implementation for an N64 cart ROM image.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since June 24, 2021
 */
public class NUSProject extends NTDProject{

	/*----- Constants -----*/
	
	/*----- Instance Variables -----*/
	
	/*----- Initialization -----*/
	
	/**
	 * @since 1.0.0
	 */
	public NUSProject(){
		super(); 
		super.setConsole(Console.N64);
		NUSDescrambler.registerByteswapMethods();
	}
	
	/**
	 * 
	 * @param imgpath
	 * @param region
	 * @return
	 * @throws IOException
	 * @since 1.0.0
	 */
	public static NUSProject createFromROMImage(String imgpath, GameRegion region) throws IOException{
		//Autodetects byte ordering in image
		N64ROMImage rom = N64ROMImage.readROMHeader(imgpath);
		if(rom == null) return null;
		
		NUSProject proj = new NUSProject();
		proj.setImportedTime(OffsetDateTime.now());
		proj.setModifiedTime(OffsetDateTime.now());
		
		proj.setRegion(region);
		proj.setGameCode(rom.getGamecode());
		proj.setDefoLanguage(DefoLanguage.getLanReg(rom.getGamecode().charAt(3)));
		proj.setFullCode("NUS_" + rom.getGamecode() + "_" + region.getShortCode());
		proj.setROMPath(imgpath);
		
		proj.setBannerTitle(rom.getName());
		proj.setPublisherName("NINTENDO 64");
		
		BufferedImage defo_ico = NTDProgramFiles.getConsoleDefaultImage(Console.N64, false);
		if(defo_ico != null) proj.setBannerIcon(new BufferedImage[]{defo_ico});
		
		proj.resetTree(null);
		
		return proj;
	}
	
	/*----- Tree Reset -----*/
	
	public void resetTree(ProgressListeningDialog observer) throws IOException{
		String imgpath = super.getROMPath();
		N64ROMImage rom = N64ROMImage.readROMHeader(imgpath);
		
		//afaik there isn't a standard fs
		//So you get this.
		DirectoryNode root = new DirectoryNode(null, "");
		
		FileNode fn = new FileNode(root, "gphdr.bin");
		fn.setSourcePath(imgpath); fn.setOffset(0L); fn.setLength(N64ROMImage.OFFSET_BOOTCODE);
		fn.addTypeChainNode(new FileTypeDefNode(NUSSysDefs.getNUSHeaderDef()));
		if(rom.getOrdering() == N64ROMImage.ORDER_Z64){
			fn.addEncryption(new NUSDescrambler.NUS_Z64_ByteswapDef());
		}
		else if(rom.getOrdering() == N64ROMImage.ORDER_N64){
			fn.addEncryption(new NUSDescrambler.NUS_N64_ByteswapDef());
		}
		
		fn = new FileNode(root, "boot.mips");
		fn.setSourcePath(imgpath); fn.setOffset(N64ROMImage.OFFSET_BOOTCODE); fn.setLength(N64ROMImage.OFFSET_GAMECODE - N64ROMImage.OFFSET_BOOTCODE);
		fn.addTypeChainNode(new FileTypeDefNode(NUSSysDefs.getBootCodeDef()));
		if(rom.getOrdering() == N64ROMImage.ORDER_Z64){
			fn.addEncryption(new NUSDescrambler.NUS_Z64_ByteswapDef());
		}
		else if(rom.getOrdering() == N64ROMImage.ORDER_N64){
			fn.addEncryption(new NUSDescrambler.NUS_N64_ByteswapDef());
		}
		
		fn = new FileNode(root, "game.bin");
		fn.setSourcePath(imgpath); fn.setOffset(N64ROMImage.OFFSET_GAMECODE); 
		fn.setLength(FileBuffer.fileSize(imgpath) - N64ROMImage.OFFSET_GAMECODE);
		fn.addTypeChainNode(new FileTypeDefNode(NUSSysDefs.getGameROMDef()));
		if(rom.getOrdering() == N64ROMImage.ORDER_Z64){
			fn.addEncryption(new NUSDescrambler.NUS_Z64_ByteswapDef());
		}
		else if(rom.getOrdering() == N64ROMImage.ORDER_N64){
			fn.addEncryption(new NUSDescrambler.NUS_N64_ByteswapDef());
		}
		
		setTreeRoot(root);
	}
	
	public void resetTreeFSDetail(ProgressListeningDialog observer) throws IOException{
		resetTree(observer);
	}
	
	/*----- GUI Interface -----*/
	
	public String[] getBannerLines(){
		String title = super.getBannerTitle();
		if(title == null){
			title = "N64 Software " + getGameCode4();
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
		
		return new Unanimator(anim.getFrameImage(0));
	}
	
	public boolean supportsSaveBannerImport(){return false;} 
	//Though can import from computer - will switch to true when this is ready
	
}
