package waffleoRai_NTDExCore.consoleproj;

import java.awt.Frame;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.OffsetDateTime;
import java.util.GregorianCalendar;

import waffleoRai_Containers.ISO;
import waffleoRai_Containers.ISOXAImage;
import waffleoRai_Containers.CDTable.CDInvalidRecordException;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Image.Animation;
import waffleoRai_NTDExCore.Console;
import waffleoRai_NTDExCore.DefoLanguage;
import waffleoRai_NTDExCore.GameRegion;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.NTDTools;
import waffleoRai_NTDExGUI.banners.Animator;
import waffleoRai_NTDExGUI.banners.StandardAnimator;
import waffleoRai_NTDExGUI.banners.Unanimator;
import waffleoRai_NTDExGUI.dialogs.progress.ProgressListeningDialog;
import waffleoRai_NTDExGUI.panels.AbstractGameOpenButton;
import waffleoRai_NTDExGUI.panels.DefaultGameOpenButton;
import waffleoRai_Utils.DirectoryNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileNode;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_fdefs.psx.PSXSysDefs;

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
 */

/**
 * NTDProject implementation for a PlayStation 1 software disk image.
 * @author Blythe Hospelhorn
 * @version 1.1.1
 * @since July 22, 2020
 */
public class PSXProject extends NTDProject{
	
	/*----- Construction -----*/
	
	/**
	 * Create an empty PSXProject.
	 * @since 1.0.0
	 */
	public PSXProject(){super(); super.setConsole(Console.PS1);}
	
	/**
	 * Create a PSXProject from a PlayStation 1 raw disk image. This method only reads a single raw ISO track.
	 * @param imgpath Path on disk to the PS1 image. Usually has a .iso extension.
	 * @param region Software region enum.
	 * @return An NTDProject containing the volume metadata and file system tree from the referenced
	 * file to be used in NTDExplorer.
	 * @throws CDInvalidRecordException If the ISO9660 directory on the image cannot be parsed.
	 * @throws IOException If the provided file cannot be read or accessed.
	 * @throws UnsupportedFileTypeException If there is an issue reading the provided file as a raw ISO9660-XA disk image.
	 * @since 1.0.0
	 */
	public static PSXProject createFromPSXTrack(String imgpath, GameRegion region) throws CDInvalidRecordException, IOException, UnsupportedFileTypeException{

		ISOXAImage image = new ISOXAImage(new ISO(FileBuffer.createBuffer(imgpath), false));
		PSXProject proj = new PSXProject();
		proj.setImportedTime(OffsetDateTime.now());
		proj.setModifiedTime(OffsetDateTime.now());
		proj.setROMPath(imgpath);
		
		proj.setRegion(region);
		//Guess language from region...
		switch(region){
		case JPN: proj.setDefoLanguage(DefoLanguage.JAPANESE); break;
		case NOE: proj.setDefoLanguage(DefoLanguage.ENGLISH); break;
		case UNKNOWN: proj.setDefoLanguage(DefoLanguage.JAPANESE); break;
		case USA: proj.setDefoLanguage(DefoLanguage.ENGLISH); break;
		case USZ: proj.setDefoLanguage(DefoLanguage.ENGLISH); break;
		default: proj.setDefoLanguage(DefoLanguage.JAPANESE); break;
		}
		
		//Nab tree (will need config and exe for auto-extracting more info
		DirectoryNode root = image.getRootNode();
		scanTreeDir(imgpath, root); //Set path for all nodes...
		proj.setTreeRoot(root);
		
		//Nab the gamecode from the volume ident
		String volident = root.getMetadataValue(ISOXAImage.METAKEY_VOLUMEIDENT);
		proj.setGameCode(volident);
		proj.setFullCode(volident);
		
		proj.setPublisherName(image.getPublisherIdent().replace(" ", ""));
		GregorianCalendar date = image.getDateCreated();
		proj.setVolumeTime(OffsetDateTime.ofInstant(date.toInstant(), date.getTimeZone().toZoneId()));
		
		//Look for SYSTEM.CNF
		FileNode cnf = root.getNodeAt("/" + volident + "/SYSTEM.CNF");
		if(cnf == null) cnf = root.getNodeAt("/" + volident + "/system.cnf");
		if(cnf != null){
			cnf.setTypeChainHead(new FileTypeDefNode(PSXSysDefs.getConfigDef()));
			//Load and get more data
			String exepath = null;
			BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cnf.loadData().getBytes())));
			//Look for a line that starts with "BOOT"
			String line = null;
			while((line = br.readLine()) != null){
				//System.err.println(line);
				if(!line.startsWith("BOOT")) continue;
				String[] fields = line.replace(" ", "").split("=");
				if(fields.length < 2) break;
				String val = fields[1];
				val = val.substring(val.indexOf('\\') + 1);
				val = val.substring(0, val.lastIndexOf(';'));
				exepath = "/" + volident + "/" + val;
			}
			br.close();
			
			if(exepath != null){
				//Should be able to take out underscores and dots to get game code.
				//proj.gamecode = exepath.substring(1).replace(".", "").replace("_", "");
				FileNode exe = root.getNodeAt(exepath);
				if(exe != null){
					System.err.println("Executable " + exepath + " found!");
					exe.setTypeChainHead(new FileTypeDefNode(PSXSysDefs.getExeDef()));
				}
				else{
					System.err.println("Executable " + exepath + " not found!");
				}
			}
			else{
				//Warn and set defaults
				System.err.println("PS1 ISO import error: executable not found!");
			}
		}
		else{
			//Will have to fill in with dummies...
			System.err.println("PS1 ISO import error: SYSTEM.CNF not found!");
		}

		proj.setBannerTitle("PS1Software " + proj.getGameCode4());
		
		return proj;
	}
	
	/*----- Alt Methods -----*/
	
	public void resetTree(ProgressListeningDialog observer) throws IOException{
		try {
			ISOXAImage image = new ISOXAImage(new ISO(FileBuffer.createBuffer(super.getROMPath()), false));
			DirectoryNode root = image.getRootNode();
			super.setTreeRoot(root);
			String volident = root.getMetadataValue(ISOXAImage.METAKEY_VOLUMEIDENT);
			
			FileNode cnf = root.getNodeAt("/" + volident + "/SYSTEM.CNF");
			if(cnf == null) cnf = root.getNodeAt("/" + volident + "/system.cnf");
			if(cnf != null){
				cnf.setTypeChainHead(new FileTypeDefNode(PSXSysDefs.getConfigDef()));
				//Load and get more data
				String exepath = null;
				BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cnf.loadData().getBytes())));
				//Look for a line that starts with "BOOT"
				String line = null;
				while((line = br.readLine()) != null){
					//System.err.println(line);
					if(!line.startsWith("BOOT")) continue;
					String[] fields = line.replace(" ", "").split("=");
					if(fields.length < 2) break;
					String val = fields[1];
					val = val.substring(val.indexOf('\\') + 1);
					val = val.substring(0, val.lastIndexOf(';'));
					exepath = "/" + volident + "/" + val;
				}
				br.close();
				
				if(exepath != null){
					//Should be able to take out underscores and dots to get game code.
					//proj.gamecode = exepath.substring(1).replace(".", "").replace("_", "");
					FileNode exe = root.getNodeAt(exepath);
					if(exe != null){
						System.err.println("Executable " + exepath + " found!");
						exe.setTypeChainHead(new FileTypeDefNode(PSXSysDefs.getExeDef()));
					}
					else{
						System.err.println("Executable " + exepath + " not found!");
					}
				}
				else{
					//Warn and set defaults
					System.err.println("PS1 ISO import error: executable not found!");
				}
			}
			else{
				//Will have to fill in with dummies...
				System.err.println("PS1 ISO import error: SYSTEM.CNF not found!");
			}
			
			scanTreeDir(super.getROMPath(), root);
			
			//Run initial type scan
			NTDTools.doTypeScan(root, null);
			stampModificationTime();
		} 
		catch (CDInvalidRecordException e) {
			e.printStackTrace();
		} 
		catch (UnsupportedFileTypeException e) {
			e.printStackTrace();
		}
	}
	
	public String[] getBannerLines(){

		//Get main banner
		String title = super.getBannerTitle();
		if(title == null){
			title = "PS1 Software " + getGameCode12();
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
			return new String[]{titlelines[0], publisher, NTDProject.getDateTimeString(getVolumeTime())};
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
		
		return gamepnl;
	}
	
	public void showImageInfoDialog(Frame gui){
		//TODO
	}

	public Animator getBannerIconAnimator(ActionListener l){
		Animation anim = super.getBannerIcon();
		if(anim == null) return null;
		
		if(anim.getNumberFrames() == 1) return new Unanimator(anim.getFrameImage(0));
		
		int millis = 0;
		if(getBannerIcon() != null){
			if(anim.getNumberFrames() == 2){
				millis = (int)Math.round((16.0/50.0) * 1000.0);
			}
			else if(anim.getNumberFrames() == 3){
				millis = (int)Math.round((11.0/50.0) * 1000.0);
			}
		}
		
		return new StandardAnimator(anim, millis, l);
	}
	
}
