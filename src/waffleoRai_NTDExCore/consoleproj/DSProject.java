package waffleoRai_NTDExCore.consoleproj;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import waffleoRai_Containers.nintendo.NDS;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_NTDExCore.Console;
import waffleoRai_NTDExCore.DefoLanguage;
import waffleoRai_NTDExCore.EncryptionRegion;
import waffleoRai_NTDExCore.GameRegion;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.NTDTools;
import waffleoRai_NTDExGUI.panels.AbstractGameOpenButton;
import waffleoRai_NTDExGUI.panels.DefaultGameOpenButton;
import waffleoRai_Utils.DirectoryNode;
import waffleoRai_Utils.FileNode;
import waffleoRai_fdefs.nintendo.DSSysFileDefs;

/*
 * UPDATES
 * 
 * 2020.06.25 | 1.0.0
 * 	Initial Documentation
 * 
 */

/**
 * NTDProject implementation for a DS or DSi cartridge ROM image.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since June 25, 2020
 *
 */
public class DSProject extends NTDProject{

	/*----- Construction -----*/
	
	/**
	 * Generate an empty DSProject.
	 * @since 1.0.0
	 */
	public DSProject(){super();}
	
	/**
	 * Create a DSProject containing information about the source
	 * cartridge image and file system for use in NTDExplorer.
	 * @param image Preparsed DS image. The source files for these usually have a .nds extension.
	 * @param region Region of the DS software.
	 * @return DSProject for use in NTDExplorer.
	 * @since 1.0.0
	 */
	public static DSProject createFromNDSImage(NDS image, GameRegion region)
	{
		DSProject proj = new DSProject();
		proj.setImportedTime(OffsetDateTime.now());
		proj.setModifiedTime(OffsetDateTime.now());
		
		if(image.hasTWL()) proj.setConsole(Console.DSi);
		else proj.setConsole(Console.DS);
		proj.setRegion(region);
		
		proj.setGameCode(image.getGameCode());
		proj.setDefoLanguage(DefoLanguage.getLanReg(proj.getGameCode4().charAt(3)));
		
		proj.setMakerCode(image.getMakerCodeAsASCII());
		proj.setPublisherName(NTDProject.getPublisherName(proj.getMakerCode()));
		
		proj.setFullCode(proj.getConsole().getShortCode() + "_" + proj.getGameCode4() + "_" + region.getShortCode());
		
		int lan = NDS.TITLE_LANGUAGE_ENGLISH;
		switch(proj.getDefoLanguage())
		{
		case FRENCH: lan = NDS.TITLE_LANGUAGE_FRENCH; break;
		case GERMAN: lan = NDS.TITLE_LANGUAGE_GERMAN; break;
		case ITALIAN: lan = NDS.TITLE_LANGUAGE_ITALIAN; break;
		case JAPANESE: lan = NDS.TITLE_LANGUAGE_JAPANESE; break;
		case KOREAN: lan = NDS.TITLE_LANGUAGE_KOREAN; break;
		case SPANISH: lan = NDS.TITLE_LANGUAGE_SPANISH; break;
		default: lan = NDS.TITLE_LANGUAGE_ENGLISH; break;
		}
		
		proj.setROMPath(image.getROMPath());
		//proj.is_encrypted = image.hasModcryptRegions();
		boolean enc = image.hasModcryptRegions();
		
		if(enc)
		{
			proj.reinstantiateEncryptedRegionsList(2);
			List<EncryptionRegion> encrypted_regs = proj.getEncRegListReference();
			
			EncryptionRegion reg = new EncryptionRegion();
			String ddir = proj.getDecryptedDataDir();
			String stem = ddir + File.separator + NTDProgramFiles.DECSTEM_DSI_MC;
			String path = stem + "01.bin";
			
			long off = image.getMC1Offset();
			long size = image.getMC1Size();
			
			reg.setDecryptBufferPath(path);
			reg.setOffset(off);
			reg.setSize(size);
			reg.setDefintion(NDS.getModcryptDef());
			
			//WARNING!! The secure key is NOT currently accurate (20/03/31)!
			//I'm still testing the key derivation!!!!
			byte[] aeskey = null;
			if(image.usesSecureKey()){
				//aeskey = image.getSecureKey();
				aeskey = new byte[16];
			}
			else aeskey = image.getInsecureKey();
			
			reg.addKeyData(aeskey);
			reg.addKeyData(image.getModcryptCTR1());
			encrypted_regs.add(reg);
			
			reg = new EncryptionRegion();
			path = stem + "02.bin";
			reg.setDecryptBufferPath(path);
			reg.setOffset(image.getMC2Offset());
			reg.setSize(image.getMC2Size());
			reg.setDefintion(NDS.getModcryptDef());
			reg.addKeyData(aeskey);
			reg.addKeyData(image.getModcryptCTR2());
			encrypted_regs.add(reg);
		}
		
		proj.setBannerTitle(image.getBannerTitle(lan));
		proj.setBannerIcon(image.getBannerIcon());
		
		//Get main tree...
		DirectoryNode root = image.getArchiveTree();
		proj.setTreeRoot(root);
		
		//Scan for empty paths...
		scanTreeDir(proj.getROMPath(), root);
		
		//Note encrypted nodes...
		if(enc) proj.markEncryptedNodes(root);
		
		//Run initial type scan
		NTDTools.doTypeScan(root, null);
		
		//Mark system files...
		FileNode sys = root.getNodeAt("/header.bin");
		if(sys != null) sys.setTypeChainHead(new FileTypeDefNode(DSSysFileDefs.getHeaderDef()));
		sys = root.getNodeAt("/icon.bin");
		if(sys != null) sys.setTypeChainHead(new FileTypeDefNode(DSSysFileDefs.getBannerDef()));
		if(image.hasTWL()){
			sys = root.getNodeAt("/rsa.bin");
			if(sys != null) sys.setTypeChainHead(new FileTypeDefNode(DSSysFileDefs.getRSACertDef()));
		}
		
		return proj;
	}
	
	/*----- Encryption -----*/
	
	
	/*----- Alt Methods -----*/
	
	public void resetTree() throws IOException{
		NDS nds = NDS.readROM(super.getROMPath(), 0);
		DirectoryNode root = nds.getArchiveTree();
		super.setTreeRoot(root);
		
		//Mark system files...
		FileNode sys = root.getNodeAt("/header.bin");
		if(sys != null) sys.setTypeChainHead(new FileTypeDefNode(DSSysFileDefs.getHeaderDef()));
		sys = root.getNodeAt("/icon.bin");
		if(sys != null) sys.setTypeChainHead(new FileTypeDefNode(DSSysFileDefs.getBannerDef()));
		if(nds.hasTWL()){
			sys = root.getNodeAt("/rsa.bin");
			if(sys != null) sys.setTypeChainHead(new FileTypeDefNode(DSSysFileDefs.getRSACertDef()));
		}
		
		NTDTools.doTypeScan(root, null);
		stampModificationTime();
	}
	
	public AbstractGameOpenButton generateOpenButton(){
		DefaultGameOpenButton gamepnl = new DefaultGameOpenButton();
		int millis = 0;
		if(getBannerIcon() != null && getBannerIcon().length > 1){
			millis = (int)Math.round(1000.0/30.0);
		}
		gamepnl.loadMe(this, millis);
		return gamepnl;
	}
	
	public void showImageInfoDialog(Frame gui){
		//TODO
	}
	
	
}
