package waffleoRai_NTDExCore.consoleproj;

import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import waffleoRai_Containers.nintendo.nx.NXCartImage;
import waffleoRai_Containers.nintendo.nx.NXCrypt;
import waffleoRai_Containers.nintendo.nx.NXPFS;
import waffleoRai_Containers.nintendo.nx.NXPatcher;
import waffleoRai_Containers.nintendo.nx.NXPatcher.PatchedInfo;
import waffleoRai_Containers.nintendo.nx.NXUtils;
import waffleoRai_Encryption.nintendo.NinCryptTable;
import waffleoRai_Image.Animation;
import waffleoRai_NTDExCore.Console;
import waffleoRai_NTDExCore.DefoLanguage;
import waffleoRai_NTDExCore.EncryptionRegion;
import waffleoRai_NTDExCore.GameRegion;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.NTDTools;
import waffleoRai_NTDExCore.importer.addons.AddonImporter;
import waffleoRai_NTDExGUI.banners.Animator;
import waffleoRai_NTDExGUI.banners.Unanimator;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_NTDExGUI.dialogs.progress.ProgressListeningDialog;
import waffleoRai_NTDExGUI.panels.AbstractGameOpenButton;
import waffleoRai_NTDExGUI.panels.DefaultGameOpenButton;
import waffleoRai_Files.tree.DirectoryNode;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_fdefs.nintendo.NXSysDefs;

/*
 * UPDATES
 * 
 * 2020.08.09 | 1.0.0
 * 	Initial documentation
 * 
 * 2020.08.16 | 1.0.0 -> 1.1.0
 * 	Added low-level FS method
 * 
 * 2020.08.18 | 1.1.0 -> 1.1.1
 * 	Importer now takes null/empty code5. Generates from packageID instead.
 * 
 * 2020.09.24 | 1.1.1 -> 2.0.0
 * 	Better compatibility with simplified/debugged NX util methods
 * 	Patch/DLC import
 * 
 * 2020.11.20 | 2.0.0 -> 2.0.1
 * 	Added scan for empty dirs on import/tree reset
 * 
 */

/**
 * A project implementation for Switch XCI images.
 * @author Blythe Hospelhorn
 * @version 2.0.1
 * @since November 20, 2020
 */
public class NXProject extends NTDProject{
	
	/*----- Constant -----*/
	
	/*----- Instance Variables -----*/
	
	private NinCryptTable crypt_table;
	
	/*----- Construction -----*/
	
	/**
	 * Generate an empty NXProject.
	 * @since 1.0.0
	 */
	public NXProject(){super(); super.setConsole(Console.SWITCH);}
	
	/**
	 * Create a project referencing a Switch cartridge image (XCI).
	 * @param xci_path Path to xci file - dump of a Switch cart image.
	 * @param code5 5 character game code of cart. This is found on the cartridge
	 * label and the game case, but unlike with older Nintendo consoles, does
	 * not appear to be embedded in the image. 
	 * @param reg Distribution region for the game image to import.
	 * @return NXProject for use exploring the referenced XCI.
	 * @throws IOException If there is an error reading the source file or writing any intermediate
	 * files.
	 * @throws UnsupportedFileTypeException If there is a parsing error somewhere along the line.
	 * @since 1.0.0
	 */
	public static NXProject createFromXCI(String xci_path, String code5, GameRegion reg) throws IOException, UnsupportedFileTypeException{
		//Load NXCrypt
		NXCrypt crypto = NTDTools.loadNXCrypt();

		//Load XCI
		NXCartImage xci = NXCartImage.readXCI(xci_path);
		xci.unlock(crypto);
		
		//Generate a code5 from package ID if null
		if(code5 == null || code5.isEmpty()){
			long pid = xci.getPackageID();
			String hexpid = Long.toHexString(pid);
			int slen = hexpid.length();
			code5 = hexpid.substring(slen-5, slen);
		}
		
		//Spawn NXProject and start loading basic info
		NXProject proj = new NXProject();
		proj.setImportedTime(OffsetDateTime.now());
		proj.setModifiedTime(OffsetDateTime.now());
		proj.setRegion(reg);
		
		proj.setGameCode(code5);
		proj.setMakerCode("00");
		proj.setDefoLanguage(DefoLanguage.ANY);
		proj.setROMPath(xci_path);
		
		proj.setBannerTitle("HAC Cartridge Image " + Long.toHexString(xci.getPackageID()));
		proj.setPublisherName("Unknown Publisher");
		
		//Save tree
		DirectoryNode tree = xci.getFileTree(NXUtils.TREEBUILD_COMPLEXITY_MERGED);
		proj.setTreeRoot(tree);
		
		//Save crypt table
		NinCryptTable ctbl = xci.generateCryptTable();
		proj.crypt_table = ctbl;
				
		//Extract banner
		NXUtils.setActiveCryptTable(ctbl);
		String[] bdat = NXUtils.getControlStrings(tree, NXUtils.LANIDX_AMENG);
		if(bdat == null){
			//Assume encrypted.
			BufferedImage lock_ico = NTDProgramFiles.getDefaultImage_lock();
			proj.setBannerIcon(new BufferedImage[]{lock_ico});
		}
		else{
			//Save that info!
			proj.setBannerTitle(bdat[NXUtils.CTRLSTR_IDX_TITLE]);
			proj.setPublisherName(bdat[NXUtils.CTRLSTR_IDX_PUBLISHER]);
			if(!bdat[NXUtils.CTRLSTR_IDX_ERRCODE].isEmpty()) proj.setGameCode(bdat[NXUtils.CTRLSTR_IDX_ERRCODE]);
			proj.setBaseVersionString(bdat[NXUtils.CTRLSTR_IDX_VERSION]);
			
			//Get icon image
			BufferedImage ico = NXUtils.getBannerIcon(tree, NXUtils.LANIDX_AMENG);
			if(ico == null) ico = NTDProgramFiles.getDefaultImage_unknown();
			proj.setBannerIcon(new BufferedImage[]{ico});
		}
		
		proj.setFullCode("HAC_" + proj.getGameCode4() + "_" + reg.getShortCode());
		NXUtils.clearActiveCryptTable();
		
		//For encrypted regions, I'm just going to be lazy and mark the whole main HFS
		List<EncryptionRegion> encregs = proj.getEncRegListReference();
		long hfsoff = 0xf000;
		EncryptionRegion ereg = new EncryptionRegion(NXSysDefs.getXTSCryptoDef(), hfsoff, FileBuffer.fileSize(xci_path) - hfsoff, proj.getDecryptedDataDir()); //Partition table
		encregs.add(ereg);
		
		NTDTools.scanForEmptyDirectories(proj.getTreeRoot());
		proj.saveCryptTable();
		proj.saveTree();
		
		return proj;
	}
	
	/*----- Decryption -----*/
	
	/**
	 * Get the path to the saved crypt table for this project. This
	 * should be in the project directory.
	 * <br>The crypt table is used to store the locations, keys,
	 * and base CTRs/sector indices for encrypted regions in the source
	 * file(s) so that files can be easily read directly off the raw image instead
	 * of from a decrypted copy.
	 * @return Crypt table path on local file system for this project as a string.
	 * @since 1.0.0
	 */
	public String getCryptTablePath(){
		return super.getCustomDataDirPath() + File.separator + "ctbl.bin";
	}
	
	/**
	 * Save the project crypt table to project directory.
	 * <br>The crypt table is used to store the locations, keys,
	 * and base CTRs/sector indices for encrypted regions in the source
	 * file(s) so that files can be easily read directly off the raw image instead
	 * of from a decrypted copy.
	 * @throws IOException If there is an error writing the file.
	 * @since 1.0.0
	 */
	public void saveCryptTable() throws IOException{
		String cpath = getCryptTablePath();
		if(crypt_table != null) crypt_table.exportToFile(cpath);
	}
	
	/**
	 * Load the project crypt table from the project directory.
	 * <br>The crypt table is used to store the locations, keys,
	 * and base CTRs/sector indices for encrypted regions in the source
	 * file(s) so that files can be easily read directly off the raw image instead
	 * of from a decrypted copy.
	 * @throws IOException If the target file does not exist or there is an error
	 * reading the file off disk.
	 * @since 1.0.0
	 */
	public void loadCryptTable() throws IOException{
		String cpath = getCryptTablePath();
		crypt_table = new NinCryptTable();
		crypt_table.importFromFile(cpath);
	}
	
	public boolean decrypt(ProgressListeningDialog observer) throws IOException{
		//This one will pretty much just try to reload the tree....
		
		//Load NXCrypt
		NXCrypt crypto = new NXCrypt();
		String cpath = NTDProgramFiles.getKeyFilePath(NTDProgramFiles.KEYNAME_HAC_COMMON);
		if(FileBuffer.fileExists(cpath)) crypto.loadCommonKeys(cpath);
		else{
			JOptionPane.showMessageDialog(observer, "HAC common keys were not found!\n"
					+ "Cannot decrypt XCI internals without common keys!", 
					"NX Common Keys", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		cpath = NTDProgramFiles.getKeyFilePath(NTDProgramFiles.KEYNAME_HAC_TITLE);
		if(FileBuffer.fileExists(cpath)) crypto.loadTitleKeys(cpath);
		
		try{
			//Read XCI
			NXCartImage xci = NXCartImage.readXCI(getROMPath());
			xci.unlock(crypto);	
			
			//Save tree
			DirectoryNode tree = xci.getFileTree(NXUtils.TREEBUILD_COMPLEXITY_MERGED);
			setTreeRoot(tree);
			
			//Save crypt table
			NinCryptTable ctbl = xci.generateCryptTable();
			crypt_table = ctbl;
			saveCryptTable();
			
			//Try again to extract banner
			NXUtils.setActiveCryptTable(ctbl);
			String[] bdat = NXUtils.getControlStrings(tree, NXUtils.LANIDX_AMENG);
			if(bdat == null){
				//Assume encrypted.
				BufferedImage lock_ico = NTDProgramFiles.getDefaultImage_lock();
				setBannerIcon(new BufferedImage[]{lock_ico});
			}
			else{
				//Save that info!
				setBannerTitle(bdat[NXUtils.CTRLSTR_IDX_TITLE]);
				setPublisherName(bdat[NXUtils.CTRLSTR_IDX_PUBLISHER]);
				setGameCode(bdat[NXUtils.CTRLSTR_IDX_ERRCODE]);
				
				//Get icon image
				BufferedImage ico = NXUtils.getBannerIcon(tree, NXUtils.LANIDX_AMENG);
				if(ico == null) ico = NTDProgramFiles.getDefaultImage_unknown();
				setBannerIcon(new BufferedImage[]{ico});
			}
			
			
			NXUtils.clearActiveCryptTable();
			
		}
		catch(UnsupportedFileTypeException x){
			x.printStackTrace();
			throw new IOException("Parsing error (see previous stack trace)");
		}
		
		NTDTools.doTypeScan(getTreeRoot(), observer);
		setModifiedTime(OffsetDateTime.now());
		return true;
	}
	
	/*----- Alt Methods -----*/
	
	public void onProjectOpen(){
		try {
			loadCryptTable();
			
			String tempdir = NTDProgramFiles.getTempDir();
			NXUtils.setDecryptTempDir(tempdir);
			NXUtils.setActiveCryptTable(crypt_table);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void onProjectClose(){
		try {
			saveCryptTable();
			
			NXUtils.clearActiveCryptTable();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void treeResetCore(int complex, ProgressListeningDialog observer) throws IOException{
		
		//Load NXCrypt
		NXCrypt crypto = NTDTools.loadNXCrypt();
		
		try{
			//Main tree.
			AddOnRecord prec = super.getCurrentPatchState();
			if(prec == null){
				//Read XCI
				NXCartImage xci = NXCartImage.readXCI(getROMPath());
				xci.unlock(crypto);	
				
				//Save tree
				DirectoryNode tree = xci.getFileTree(complex);
				setTreeRoot(tree);	
			}
			else{
				super.removePatchState(prec.getKey());
				boolean lowfs = (complex == NXUtils.TREEBUILD_COMPLEXITY_ALL);
				importPatch(prec.getPath(), lowfs);
			}
			
			//DLC
			Collection<String> d_keys = super.getAllDLCKeys();
			if(d_keys != null && !d_keys.isEmpty()){
				super.dismountAllDLC();
				for(String key : d_keys){
					AddOnRecord drec = super.removeDLCRecord(key);
					if(drec != null){
						importDLC(drec.getPath(), drec.getDisplayString());
					}
				}
			}			
			
		}
		catch(UnsupportedFileTypeException x){
			x.printStackTrace();
			throw new IOException("Parsing error (see previous stack trace)");
		}
		
		NTDTools.doTypeScan(getTreeRoot(), observer);
		setModifiedTime(OffsetDateTime.now());
	}
	
	public void resetTree(ProgressListeningDialog observer) throws IOException{
		treeResetCore(NXUtils.TREEBUILD_COMPLEXITY_MERGED, observer);
	}
	
	public void resetTreeFSDetail(ProgressListeningDialog observer) throws IOException{
		treeResetCore(NXUtils.TREEBUILD_COMPLEXITY_ALL, observer);
	}
	
	public String[] getBannerLines(){
		String title = super.getBannerTitle();
		if(title == null){
			title = "Switch Software " + getGameCode4();
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
	
	/*----- Extensions -----*/
	
	/**
	 * Import a Switch software patch package into this project. The
	 * patch can be set as an alternate state for project browsing.
	 * @param patch_path Path to file containing patch data. This must be
	 * the actual patch data file (PFS), not an .nsp directory.
	 * @param lowfs Whether to import tree under low-level file system specifications
	 * (ie. split by NCA and partition etc) or high-level. If true, import low-level.
	 * @return Key string used to reference added patch state in this project.
	 * @throws IOException If there is an error reading or writing any file.
	 * @throws UnsupportedFileTypeException If there is an error importing the patch.
	 * @since 2.0.0
	 */
	public String importPatch(String patch_path, boolean lowfs) throws IOException, UnsupportedFileTypeException{
		NXCrypt crypto = NTDTools.loadNXCrypt();
		
		PatchedInfo pinfo = null;
		if(!lowfs) pinfo = NXPatcher.patchXCI(super.getROMPath(), patch_path, crypto, true);
		else pinfo = NXPatcher.patchXCI_lowfs(super.getROMPath(), patch_path, crypto);
		AddOnRecord aor = new AddOnRecord(true);
		
		crypt_table = pinfo.crypt_table;
		saveCryptTable();
		aor.setPath(patch_path);
		aor.setDisplayString(pinfo.patched_ver);
		
		NTDTools.scanForEmptyDirectories(pinfo.newroot);
		String k = super.addPatchState(aor, pinfo.newroot);
		super.setPatchState(aor.getKey());
		return k;
	}
	
	/**
	 * Import a Switch software DLC/add-on package into this project and
	 * mount it to the current tree.
	 * @param dlc_path Path to file containing add-on data. This must be
	 * the actual patch data file (PFS), not an .nsp directory.
	 * @param dlc_name Display name to use for DLC package.
	 * @return Key string used to reference added DLC module in this project.
	 * @throws IOException If there is an error reading or writing any file.
	 * @throws UnsupportedFileTypeException If there is an error importing the add-on data.
	 * @since 2.0.0
	 */
	public String importDLC(String dlc_path, String dlc_name) throws IOException, UnsupportedFileTypeException{

		NXCrypt crypto = NTDTools.loadNXCrypt();
		
		String tag = dlc_name.replace(" ", "-");
		DirectoryNode root = super.getTreeRoot();
		NXPatcher.mountDLC(root, crypt_table, dlc_path, crypto, tag);
		AddOnRecord aor = new AddOnRecord(false);
		
		Collection<FileNode> dnodes = NXUtils.unmountDLCNodes(root, tag);
		aor.setPath(dlc_path);
		aor.setDisplayString(dlc_name);
		
		String k = super.addDLCRecord(aor, dnodes);
		super.mountDLC(k);
		
		return k;
	}
	
	public boolean supportsAddOnImport(){return true;}
	
	public AddonImporter getAddOnImporter(){
		//Allows you to import an NSP
		//Auto-detects patch or DLC, I guess.
		
		AddonImporter importer = new AddonImporter(){

			public void importAddOn(Frame gui) {
				//File browser
				JFileChooser fc = new JFileChooser(getROMPath());
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int retVal = fc.showOpenDialog(gui);
				
				if (retVal != JFileChooser.APPROVE_OPTION) return;
				File f = fc.getSelectedFile();
				String fpath = f.getAbsolutePath();
				
				//Dialog
				IndefProgressDialog dialog = new IndefProgressDialog(gui, "Importing Add-On");
				dialog.setPrimaryString("Initializing");
				dialog.setSecondaryString("Checking import target");
				
				//Task
				SwingWorker<Void, Void> task = new SwingWorker<Void, Void>(){

					protected Void doInBackground() throws Exception 
					{
						try{
							//Load NX crypto
							NXCrypt crypto = NTDTools.loadNXCrypt();
							
							//Scan to determine if patch or DLC...
							boolean ispatch = NXUtils.isNSPPatch(fpath, crypto);
							
							//Attempt import...
							DirectoryStream<Path> dstr = Files.newDirectoryStream(Paths.get(fpath));
							if(ispatch){
								dialog.setPrimaryString("Importing Patch");
								for(Path p : dstr){
									//Load PFS
									String pstr = p.toAbsolutePath().toString();
									dialog.setSecondaryString("Initializing " + pstr);
									
									//Check if PFS
									FileBuffer prev = new FileBuffer(pstr, 0L, 0x10, true);
									if(prev.findString(0L, 0x10, NXPFS.MAGIC) != 0L){
										dialog.setSecondaryString("Skipping " + pstr);
										continue;
									}
									
									//Do patch...
									dialog.setSecondaryString("Importing " + pstr);
									importPatch(pstr, false);
								}
							}
							else{
								dialog.setPrimaryString("Importing DLC");
								
								//Derive DLC name...
								int slash = fpath.lastIndexOf(File.separator);
								String dlc_name = null;
								if(slash >= 0) dlc_name = fpath.substring(slash+1);
								int dot = dlc_name.lastIndexOf('.');
								if(dot >= 0) dlc_name = dlc_name.substring(0, dot);
								
								int ctr = 0;
								for(Path p : dstr){
									//Load PFS
									String pstr = p.toAbsolutePath().toString();
									dialog.setSecondaryString("Initializing " + pstr);
									
									//Check if PFS
									FileBuffer prev = new FileBuffer(pstr, 0L, 0x10, true);
									if(prev.findString(0L, 0x10, NXPFS.MAGIC) != 0L){
										dialog.setSecondaryString("Skipping " + pstr);
										continue;
									}
									
									//Do patch...
									dialog.setSecondaryString("Importing " + pstr);
									String mname = dlc_name;
									if(ctr > 0) mname += String.format("_%02d", ctr++);
									else ctr++;
									importDLC(pstr, mname);
								}
							}
							dstr.close();
						}
						catch (Exception e){
							e.printStackTrace();
							dialog.showWarningMessage("ERROR: Add-on import failed! See stderr for details.");
						}
						return null;
					}
					
					public void done(){
						dialog.closeMe();
					}
					
				};
				
				//Go
				task.execute();
				dialog.render();
				
			}
			
		};
		
		
		return importer;
	}
}
