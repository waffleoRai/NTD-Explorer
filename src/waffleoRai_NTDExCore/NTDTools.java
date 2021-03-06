package waffleoRai_NTDExCore;

import java.awt.Frame;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import waffleoRai_Containers.ArchiveDef;
import waffleoRai_Containers.nintendo.nx.NXCrypt;
import waffleoRai_Files.Converter;
import waffleoRai_Files.FileClass;
import waffleoRai_Files.FileNodeModifierCallback;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.memcard.BannerImporter;
import waffleoRai_NTDExCore.memcard.BannerImporter.BannerStruct;
import waffleoRai_NTDExCore.memcard.GCMCBannerImporter;
import waffleoRai_NTDExCore.memcard.PSXMCBannerImporter;
import waffleoRai_NTDExCore.memcard.WiiSaveBannerImporter;
import waffleoRai_NTDExGUI.ExplorerForm;
import waffleoRai_NTDExGUI.dialogs.AddHACKeyDialog;
import waffleoRai_NTDExGUI.dialogs.BannerImportDialog;
import waffleoRai_NTDExGUI.dialogs.SetTextDialog;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_NTDExGUI.dialogs.progress.ProgressListeningDialog;
import waffleoRai_Files.tree.DirectoryNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_Files.tree.LinkNode;

public class NTDTools {
	
	public static void processArchiveContents(DirectoryNode contents, FileNode archive, boolean addArchive){
		if(archive == null) return;
		if(contents == null) return;
		
		//Sets the archive node to the container for all tree members (if compressed)
		if(archive.hasCompression()){
			contents.doForTree(new FileNodeModifierCallback(){

				public void doToNode(FileNode node) {
					if(node.isDirectory()) return;
					node.setContainerNode(archive);
				}
				
			});
		}
		else{
			//Update offsets
			contents.incrementTreeOffsetsBy(archive.getOffset());
		}
		
		//Also adds the archive node to the tree so it can be examined even
		//after the archive has been mounted. (Optional)
		if(addArchive){
			FileNode acopy = archive.copy(null);
			acopy.setFileName("~" + archive.getFileName());
			acopy.setParent(contents);
		}
		
	}
	
	public static void extractArchivesToTree(DirectoryNode dir, ProgressListeningDialog listener, List<FileNode> failed)
	{
		listener.setPrimaryString("Scanning directory");
		listener.setSecondaryString("Scanning " + dir.getFullPath());
		//System.err.println("Scanning " + dir.getFullPath());
		List<FileNode> children = dir.getChildren();
		for(FileNode child : children){
			if(child instanceof DirectoryNode){
				extractArchivesToTree((DirectoryNode)child, listener, failed);
			}
			else if(child instanceof LinkNode){}
			else{
				listener.setPrimaryString("Scanning files");
				listener.setSecondaryString("Scanning " + child.getFullPath());
				
				//Skip if file name starts with ~
				//Likely a previously mounted arch node inside itself.
				//Unless you want a recursive nightmare to descend upon you.
				//Of course if your ROM has a bunch of archives that start with ~ then TFB I guess
				if(child.getFileName().startsWith("~")) continue;
				
				//System.err.println("Scanning " + child.getFullPath());
				//Check type...
				FileTypeNode head = child.getTypeChainHead();
				if(head == null){
					//Run detection...
					listener.setPrimaryString("Checking file type");
					head = TypeManager.detectType(child);
					child.setTypeChainHead(head);
					//if(head != null) System.err.println("Type head: " + head.toString());
				}
				if(head == null) continue; //Still unknown
				
				//Go down to tail
				FileTypeNode tail = head;
				while(tail.getChild() != null) tail = tail.getChild();
				//if(tail != null) System.err.println("Type tail: " + tail.toString());
				if(tail != null && (tail.getFileClass() == FileClass.ARCHIVE)){
					//System.err.println("Tail is an archive!");
					listener.setPrimaryString("Reading archive");
					listener.setSecondaryString("Parsing " + child.getFullPath());
					try{
						//Need to nab the definition
						ArchiveDef def = (ArchiveDef)(tail.getTypeDefinition());
						DirectoryNode arcroot = def.getContents(child);
						
						listener.setPrimaryString("Copying archive tree");
						listener.setSecondaryString("Copying " + child.getFullPath());
						
						//Clean up extracted tree
						//NTDTools.notateTree(arcroot, child);
						NTDTools.processArchiveContents(arcroot, child, true);
						arcroot.setFileName(child.getFileName());
						doTypeScan(arcroot, listener);
						//arcroot.setFileClass(tail.getFileClass());
						
						listener.setPrimaryString("Copying archive tree");
						listener.setSecondaryString("Copying " + child.getFullPath());
						
						//Add to tree
						dir.removeChild(child);
						arcroot.setParent(dir);
						
						//In case this archive contains more archives
						extractArchivesToTree(arcroot, listener, failed);
					}
					catch(Exception x){
						x.printStackTrace();
						failed.add(child);
					}
				}
			}
		}
		
	}

	public static void doConversionDump(DirectoryNode dir, ProgressListeningDialog listener, List<FileNode> failed, Collection<Converter> included, String path) throws IOException
	{
		if(!FileBuffer.directoryExists(path)) Files.createDirectories(Paths.get(path));
		listener.setPrimaryString("Scanning directory");
		listener.setSecondaryString("Scanning " + dir.getFullPath());
		List<FileNode> children = dir.getChildren();
		
		for(FileNode child : children)
		{
			String mypath = dir + File.separator + child.getFileName();
			if(child instanceof DirectoryNode)
			{
				doConversionDump((DirectoryNode)child, listener, failed, included, mypath);
			}
			else if(child instanceof LinkNode){}
			else
			{
				listener.setPrimaryString("Scanning files");
				listener.setSecondaryString("Scanning " + child.getFullPath());
				//Check type...
				FileTypeNode head = child.getTypeChainHead();
				if(head == null)
				{
					//Run detection...
					listener.setPrimaryString("Detecting file type");
					head = TypeManager.detectType(child);
					child.setTypeChainHead(head);
				}
				
				listener.setPrimaryString("Checking available conversions");
				if(head != null)
				{
					//See if there is an available converter...
					FileTypeNode tail = head;
					while(tail.getChild() != null) tail = tail.getChild();
					TypeManager manager = TypeManager.getTypeManager(tail.getTypeID());
					Converter con = manager.getStandardConverter();
					if(con != null)
					{
						boolean match = false;
						for(Converter inc : included)
						{
							if(con == inc) {match = true; break;}
						}
						
						if(match)
						{
							//Do conversion... and return
							String npath = con.changeExtension(mypath);
							listener.setPrimaryString("Converting");
							listener.setSecondaryString("Generating " + npath);
							try{con.writeAsTargetFormat(child.loadDecompressedData(), npath);}
							catch(Exception x){x.printStackTrace(); failed.add(child);}
							return;
						}
					}
					//Just copy file
					listener.setPrimaryString("Extracting");
					listener.setSecondaryString("Writing " + mypath);
					try{child.loadDecompressedData().writeFile(mypath);}
					catch(Exception x){x.printStackTrace(); failed.add(child);}
				}
			}
		}
	}

	public static void doTypeScan(DirectoryNode dir, ProgressListeningDialog listener){
		if(listener != null) {
			listener.setPrimaryString("Scanning directory");
			listener.setSecondaryString("Scanning " + dir.getFullPath());
		}
		
		List<FileNode> children = dir.getChildren();
		//Check if empty...
		if(children.isEmpty()) dir.setFileClass(FileClass.EMPTY_DIR);
		
		for(FileNode child : children)
		{
			if(child instanceof DirectoryNode){
				doTypeScan((DirectoryNode)child, listener);
			}
			else{
				if(listener != null) {
					listener.setPrimaryString("Scanning files");
					listener.setSecondaryString("Scanning " + child.getFullPath());	
				}
				if(child.getTypeChainHead() == null){
					//No type set, so scan
					FileTypeNode ftn = TypeManager.detectType(child);
					if(ftn != null) child.setTypeChainHead(ftn);
				}
				else if(child.getTypeChainTail().isCompression()){
					//Try again, maybe the type is known now.
					FileTypeNode ftn = TypeManager.detectType(child);
					if(ftn != null) child.setTypeChainHead(ftn);
				}
			}
		}
	}
	
	public static void clearTypeMarkers(DirectoryNode dir, ProgressListeningDialog listener){
		
		listener.setPrimaryString("Scanning directory");
		listener.setSecondaryString("Scanning " + dir.getFullPath());
		
		List<FileNode> children = dir.getChildren();
		for(FileNode child : children)
		{
			if(child instanceof DirectoryNode){
				clearTypeMarkers((DirectoryNode)child, listener);
			}
			else{
				listener.setPrimaryString("Scanning files");
				listener.setSecondaryString("Scanning " + child.getFullPath());
				child.clearTypeChain();
			}
		}
		
	}
	
	private static void scanForType(DirectoryNode dir, FileClass type, List<FileNode> list){

		List<FileNode> children = dir.getChildren();
		for(FileNode child : children){
			if(child instanceof DirectoryNode){
				scanForType((DirectoryNode)child, type, list);
			}
			else{
				if(child.getTypeChainTail().getFileClass() == type) list.add(child);
			}
		}
		
	}
	
	public static List<FileNode> scanForType(FileNode node, FileClass type){
		//Backtrack to root.
		DirectoryNode dn = node.getParent();
		if(dn == null && (node instanceof DirectoryNode)){
			dn = (DirectoryNode)node;
		}
		else{
			while(dn.getParent() != null) dn = dn.getParent();
		}
		
		List<FileNode> list = new LinkedList<FileNode>();
		scanForType(dn, type, list);
		
		return list;
	}
	
	public static void matchExtensionsToType(DirectoryNode dir, ProgressListeningDialog listener){
		
		listener.setSecondaryString("Scanning " + dir.getFullPath());
		List<FileNode> children = dir.getChildren();
		for(FileNode child : children){
			if(child instanceof DirectoryNode){
				matchExtensionsToType((DirectoryNode)child, listener);
			}
			else{
				listener.setSecondaryString("Scanning " + child.getFullPath());
				FileTypeNode head = child.getTypeChainHead();
				if(head == null) continue;
				//Generate extension from chain of type definitions
				String newext = "";
				while(head != null){
					FileTypeDefinition def = head.getTypeDefinition();
					if(def != null){
						String dext = def.getDefaultExtension();
						if(dext != null && !dext.isEmpty()){
							newext = newext + "." + dext;		
						}
					}
					head = head.getChild();
				}
				//Replace extension
				int lastdot = child.getFileName().lastIndexOf('.');
				String newname = null;
				if(lastdot < 0) newname = child.getFileName();
				else newname = child.getFileName().substring(0, lastdot);
				newname += newext;
				child.setFileName(newname);
			}
		}
		
	}
	
	public static void scanForEmptyDirectories(DirectoryNode root){

		if(root == null) return;
		root.doForTree(new FileNodeModifierCallback(){

			public void doToNode(FileNode node) {
				if(node.isDirectory()){
					if(node.getChildCount() <= 0 && node instanceof DirectoryNode){
						DirectoryNode dn = (DirectoryNode)node;
						dn.setFileClass(FileClass.EMPTY_DIR);
					}
				}
			}
			
		});
	}
	
	public static void runExport(Frame gui_parent, ExportAction exp, String failmsg){
		
		JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(NTDProgramFiles.INIKEY_LAST_EXPORTED));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		int select = fc.showSaveDialog(gui_parent);
		
		if(select != JFileChooser.APPROVE_OPTION) return;
		String dir = fc.getSelectedFile().getAbsolutePath();
		NTDProgramFiles.setIniValue(NTDProgramFiles.INIKEY_LAST_EXPORTED, dir);
		
		IndefProgressDialog dialog = new IndefProgressDialog(gui_parent, "Data Export");
		dialog.setPrimaryString("Exporting Data");
		dialog.setSecondaryString("Writing to " + dir);
		
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{

			protected Void doInBackground() throws Exception 
			{
				try{
					exp.doExport(dir, dialog);
				}
				catch(Exception x)
				{
					x.printStackTrace();
					JOptionPane.showMessageDialog(gui_parent, failmsg, 
							"Export Failed", JOptionPane.ERROR_MESSAGE);
				}
				
				return null;
			}
			
			public void done()
			{
				dialog.closeMe();
			}
		};
		
		task.execute();
		dialog.render();
		
		
	}
	
	public static void importBannerFromSave(ExplorerForm gui, NTDProject proj){

		Console c = proj.getConsole();
		BannerImporter importer = null;
		switch(c){
		case PS1: 
			importer = new PSXMCBannerImporter();
			break;
		case GAMECUBE:
			importer = new GCMCBannerImporter();
			break;
		case WII:
			importer = new WiiSaveBannerImporter();
			break;
		default: 
			gui.showWarning("Save data support not available for this console!");
			return;
		}
		
		//JFileChooser
		JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(NTDProgramFiles.INIKEY_LAST_SAVEICONPATH));
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		List<FileFilter> filters = importer.getFileFilters();
		for(FileFilter f : filters) fc.addChoosableFileFilter(f);
		
		int select = fc.showOpenDialog(gui);
		
		if(select != JFileChooser.APPROVE_OPTION) return;
		String abspath = fc.getSelectedFile().getAbsolutePath();
		NTDProgramFiles.setIniValue(NTDProgramFiles.INIKEY_LAST_SAVEICONPATH, abspath);
		
		//Create dialog
		IndefProgressDialog dialog = new IndefProgressDialog(gui, "Banner Import");
		dialog.setPrimaryString("Reading");
		dialog.setSecondaryString("Scanning save data from " + abspath);
		
		//Create task for banner retrieval...
		Collection<BannerStruct> bannerlist = null;
		BannerImporter fimporter = importer;
		SwingWorker<Collection<BannerStruct>, Collection<BannerStruct>> task = new SwingWorker<Collection<BannerStruct>, Collection<BannerStruct>>()
		{

			protected Collection<BannerStruct> doInBackground() throws Exception{
				Collection<BannerStruct> blist = null;
				try{
					blist = fimporter.findBanner(abspath, proj.getGameCode12());
				}
				catch (IOException e){
					e.printStackTrace();
					gui.showError("I/O Error: File \"" + abspath + "\" could not be loaded!");
				}
				catch (UnsupportedFileTypeException e){
					e.printStackTrace();
					gui.showError("Parsing Error: File \"" + abspath + "\" could not be read!");
				}
				return blist;
			}
			
			public void done(){
				dialog.closeMe();
			}
			
		};
		
		//Run banner retrieval (and wait)...
		task.execute();
		dialog.render();
		try {
			bannerlist = task.get();
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
			gui.showError("Task Execution Error: Import task was interrupted! Try again later.");
			return;
		} 
		catch (ExecutionException e) {
			e.printStackTrace();
			gui.showError("Task Execution Error: Import task could not be executed! Try again later.");
			return;
		}
		
		if(bannerlist == null){
			gui.showError("Unknown Error: Import task failed! See stderr.");
			return;
		}
		
		//Display icon selection panel
		BannerImportDialog bidialog = new BannerImportDialog(gui, importer.getImportDialogButtonSizeEnum(), importer.getImportDialogButtonColor());
		bidialog.setLocationRelativeTo(gui);
		bidialog.setBackgroundColor(importer.getImportDialogBackgroundColor());
		bidialog.loadBannerOptions(bannerlist);
		bidialog.setVisible(true);
		
		//Set title (if applicable)
		if(!bidialog.selectionApproved()) return;
		BannerStruct banner = bidialog.getSelected();
		if(banner == null){
			gui.showWarning("No banner selected!");
			return;
		}
		String title = null;
		if(c == Console.PS1){
			//Confirm title to load in
			SetTextDialog textdialog = new SetTextDialog(gui, "Set Banner Title", banner.title);
			textdialog.setLocationRelativeTo(gui);
			textdialog.setVisible(true);
			if(!textdialog.okSelected()) return;
			title = textdialog.getText();
		}
		
		//Apply to project
		proj.setBannerIcon(banner.icon);
		if(title!=null) proj.setBannerTitle(title);
		
	}
	
	public static void importBannerFromLocalFS(ExplorerForm gui, NTDProject proj){
		//Only a single image.
		
		//JFileChooser
		String[] imgexts = {"png", "jpg", "jpeg", "bmp"};
		JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(NTDProgramFiles.INIKEY_LAST_PCICONPATH));
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.addChoosableFileFilter(new FileFilter(){

			public boolean accept(File f) {
				String fname = f.getAbsolutePath();
				for(String ext : imgexts){
					if(fname.endsWith("." + ext)) return true;
				}
				return false;
			}

			public String getDescription() {
				String extlist = "";
				boolean first = true;
				for(String ext : imgexts){
					if(!first)extlist += ",";
					first = false;
					extlist += "." + ext;
				}
				return "Image File (" + extlist + ")";
			}
			
		});
		
		int select = fc.showOpenDialog(gui);
		
		if(select != JFileChooser.APPROVE_OPTION) return;
		String abspath = fc.getSelectedFile().getAbsolutePath();
		NTDProgramFiles.setIniValue(NTDProgramFiles.INIKEY_LAST_PCICONPATH, abspath);
		
		//Create dialog
		IndefProgressDialog dialog = new IndefProgressDialog(gui, "Banner Icon Import");
		dialog.setPrimaryString("Reading");
		dialog.setSecondaryString("Loading image from " + abspath);
		
		//Create task
		BufferedImage img = null;
		SwingWorker<BufferedImage, BufferedImage> task = new SwingWorker<BufferedImage, BufferedImage>()
		{

			protected BufferedImage doInBackground() throws Exception{
				BufferedImage i = null;
				try{
					i = ImageIO.read(new File(abspath));
				}
				catch (IOException e){
					e.printStackTrace();
					gui.showError("I/O Error: File \"" + abspath + "\" could not be read!");
				}
				return i;
			}
			
			public void done(){
				dialog.closeMe();
			}
			
		};
		
		//Wait for task to finish
		task.execute();
		dialog.render();
		try {
			img = task.get();
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
			gui.showError("Task Execution Error: Import task was interrupted! Try again later.");
			return;
		} 
		catch (ExecutionException e) {
			e.printStackTrace();
			gui.showError("Task Execution Error: Import task could not be executed! Try again later.");
			return;
		}
		
		if(img == null){
			gui.showError("Error: Import task failed! See stderr.");
			return;
		}
		
		//Rescale to 32x32
		BufferedImage scaled = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
		Image simg = img.getScaledInstance(32, 32, Image.SCALE_DEFAULT);
		scaled.getGraphics().drawImage(simg, 0, 0, null);
		
		//Apply to project
		BufferedImage[] ico = new BufferedImage[1];
		ico[0] = scaled;
		proj.setBannerIcon(ico);
	}
	
	public static void importSwitchKeysFromDump(ExplorerForm gui){
		
		//Show path selection dialog...
		AddHACKeyDialog dialog = new AddHACKeyDialog(gui);
		dialog.setVisible(true);
		
		if(!dialog.confirmSelected()) return;
		String prod_path = dialog.getProductKeysPath();
		String titl_path = dialog.getTitleKeysPath();
		
		//Load into an NXCrypt
		NXCrypt nxcrypt = new NXCrypt();
		try{
			if(FileBuffer.fileExists(prod_path)) nxcrypt.importCommonKeys(prod_path);
			if(FileBuffer.fileExists(titl_path)) nxcrypt.importTitleKeys(titl_path);
		}
		catch(IOException x){
			x.printStackTrace();
			gui.showError("I/O Error: Key file could not be read!");
			return;
		}
		
		//Save
		try{
			nxcrypt.saveCommonKeys(NTDProgramFiles.getKeyFilePath(NTDProgramFiles.KEYNAME_HAC_COMMON));
			nxcrypt.saveTitleKeys(NTDProgramFiles.getKeyFilePath(NTDProgramFiles.KEYNAME_HAC_TITLE));
		}
		catch(IOException x){
			x.printStackTrace();
			gui.showError("I/O Error: Keys could not be saved!");
			return;
		}
	}

	public static NXCrypt loadNXCrypt() throws IOException{
		NXCrypt crypto = new NXCrypt();
		String cpath = NTDProgramFiles.getKeyFilePath(NTDProgramFiles.KEYNAME_HAC_COMMON);
		if(FileBuffer.fileExists(cpath)) crypto.loadCommonKeys(cpath);
		cpath = NTDProgramFiles.getKeyFilePath(NTDProgramFiles.KEYNAME_HAC_TITLE);
		if(FileBuffer.fileExists(cpath)) crypto.loadTitleKeys(cpath);
		return crypto;
	}
	
}
