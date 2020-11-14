package waffleoRai_NTDExGUI;

import java.awt.Dimension;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.SwingWorker;

import waffleoRai_Containers.nintendo.citrus.CitrusCrypt;
import waffleoRai_Files.Converter;
import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.GUITools;
import waffleoRai_NTDExCore.Console;
import waffleoRai_NTDExCore.DefoLanguage;
import waffleoRai_NTDExCore.GameRegion;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.NTDProject.AddOnRecord;
import waffleoRai_NTDExCore.NTDTools;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.importer.addons.AddonImporter;
import waffleoRai_NTDExGUI.dialogs.AddCTRKeyDialog;
import waffleoRai_NTDExGUI.dialogs.AddKeyDialog;
import waffleoRai_NTDExGUI.dialogs.BannerEditForm;
import waffleoRai_NTDExGUI.dialogs.ConvertDumpDialog;
import waffleoRai_NTDExGUI.dialogs.DirSetDialog;
import waffleoRai_NTDExGUI.dialogs.ImportDialog;
import waffleoRai_NTDExGUI.dialogs.OpenDialog;
import waffleoRai_NTDExGUI.dialogs.imageinfo.ImageInfoDialogs;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_NTDExGUI.nightmode.DarkMenu;
import waffleoRai_NTDExGUI.nightmode.DarkMenuBar;
import waffleoRai_Files.tree.DirectoryNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Files.tree.FileNode;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.awt.GridBagConstraints;
import javax.swing.JSeparator;
import java.awt.Color;

public class ExplorerForm extends JFrame {
	
	//TODO Also add detector on project load to see if
			//	existing decryption buffers have been deleted (and regenerate if needed)
	
	/*----- Constants -----*/
	
	private static final long serialVersionUID = -5515199484209320297L;

	public static final int MIN_WIDTH = ImageMasterPanel.MIN_WIDTH;
	public static final int MIN_HEIGHT = ImageMasterPanel.MIN_HEIGHT + 50;
	public static final int PREF_HEIGHT = 350;
	
	public static final int MAX_WIDTH = InfoPanel.MAX_WIDTH;
	public static final int MAX_HEIGHT = 720;
	
	public static final String INIKEY_LASTDUMP = "LAST_RAW_DUMP_PATH";
	
	/*----- Instance Variables -----*/
	
	private ImageMasterPanel pnlMain;
	
	private ComponentGroup always_enabled;
	private ComponentGroup loaded_enabled;
	
	private JMenuItem mntmImportBannerMC;
	private JMenuItem mntmImportCDTrack;
	
	private NTDProject loaded_project;
	
	private JMenu mnSourcesAddons;
	private JMenu mnSourcesUpdates;
	private JMenu mnSourcesDLC;
	
	private AddonCheckbox[] rawsrc_list;
	private AddonRadioButton[] update_list;
	private AddonCheckbox[] dlc_list;
	
	/*----- Component Subclasses -----*/
	
	protected class AddonRadioButton extends JRadioButton{
		
		private static final long serialVersionUID = -2982772178050918259L;
		
		private int index;
		private String addon_key;
		
		public AddonRadioButton(int idx, String key, String display_txt){
			super(display_txt);
			addon_key = key;
			index = idx;
			
			this.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					onSelectUpdate(index);
				}
				
			});
		}
		
		public String getKey(){
			return addon_key;
		}
		
	}
	
	protected class AddonCheckbox extends JCheckBox{

		private static final long serialVersionUID = 7947632154241242893L;
		private String addon_key;
		private boolean for_dlc;
		private int index;
		
		public AddonCheckbox(int idx, String key, String display_txt, boolean dlc){
			super(display_txt);
			addon_key = key;
			for_dlc = dlc;
			index = idx;
			
			this.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					if(for_dlc) onSelectDLC(index);
					else onSelectRawAddon(index);
				}
				
			});
			
		}
		
		public String getKey(){
			return addon_key;
		}
	}
	
	/*----- Build -----*/
	
 	public ExplorerForm()
	{
 		setBackground(Color.DARK_GRAY);
 		always_enabled = new ComponentGroup();
 		loaded_enabled = new ComponentGroup();
 		
		initGUI();
	}
	
	private void initGUI()
	{
		setTitle("NTD Explorer");
		Dimension sz = new Dimension(MIN_WIDTH, MIN_HEIGHT);
		setMinimumSize(sz);
		setMaximumSize(new Dimension(MAX_WIDTH, MAX_HEIGHT));
		sz = new Dimension(MIN_WIDTH, PREF_HEIGHT);
		setPreferredSize(sz);
		setLocation(GUITools.getScreenCenteringCoordinates(this));
		
		JMenuBar menuBar = new DarkMenuBar();
		setJMenuBar(menuBar);
		
		//Menus...
		JMenu mnFile = new DarkMenu("File");
		menuBar.add(mnFile);
		always_enabled.addComponent("mnFile", mnFile);
		
		/*JMenu mnTools = new JMenu("Tools");
		menuBar.add(mnTools);
		loaded_enabled.addComponent("mnTools", mnTools);*/
		
		JMenu mnProject = new DarkMenu("Project");
		menuBar.add(mnProject);
		loaded_enabled.addComponent("mnProject", mnProject);
		
		JMenu mnDecryption = new DarkMenu("Decrypt");
		menuBar.add(mnDecryption);
		always_enabled.addComponent("mnDecryption", mnDecryption);
		
		JMenu mnTypes = new DarkMenu("Types");
		menuBar.add(mnTypes);
		always_enabled.addComponent("mnTypes", mnTypes);
		
		//Menu Options - File
		JMenuItem mntmOpen = new JMenuItem("Open...");
		mnFile.add(mntmOpen);
		mntmOpen.setBackground(Color.darkGray);
		mntmOpen.setOpaque(false);
		always_enabled.addComponent("mntmOpen", mntmOpen);
		mntmOpen.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onFileOpen();}	
		});
		
		JMenuItem mntmImportRom = new JMenuItem("Import...");
		mnFile.add(mntmImportRom);
		always_enabled.addComponent("mntmImportRom", mntmImportRom);
		mntmImportRom.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				onFileImport();
			}
		});
		
		JMenuItem mntmSave = new JMenuItem("Save");
		mnFile.add(mntmSave);
		loaded_enabled.addComponent("mntmSave", mntmSave);
		mntmSave.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				onFileSave();
			}
		});
		
		JMenuItem mntmClose = new JMenuItem("Close");
		mnFile.add(mntmClose);
		always_enabled.addComponent("mntmClose", mntmClose);
		mntmClose.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onFileClose();}
		});
		
		JMenuItem mntmDeleteProject = new JMenuItem("Delete Project");
		mnFile.add(mntmDeleteProject);
		loaded_enabled.addComponent("mntmDeleteProject", mntmDeleteProject);
		mntmDeleteProject.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onFileDeleteProject();}
		});
		
		mnFile.add(new JSeparator());
		
		JMenuItem mntmImportProj = new JMenuItem("Import Project...");
		mnFile.add(mntmImportProj);
		always_enabled.addComponent("mntmImportProj", mntmImportProj);
		mntmImportProj.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onFileImportProject();}
		});
		
		JMenuItem mntmExportProj = new JMenuItem("Export Project...");
		mnFile.add(mntmExportProj);
		loaded_enabled.addComponent("mntmExportProj", mntmExportProj);
		mntmExportProj.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onFileExportProject();}
		});
		
		mnFile.add(new JSeparator());
		
		JMenuItem mntmSetTempDirectory = new JMenuItem("Set Temp Directory...");
		mnFile.add(mntmSetTempDirectory);
		always_enabled.addComponent("mntmSetTempDirectory", mntmSetTempDirectory);
		mntmSetTempDirectory.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onFileSetTemp();}
		});
		
		JMenuItem mntmSetDecryptDir = new JMenuItem("Set Decrypt Directory...");
		mnFile.add(mntmSetDecryptDir);
		always_enabled.addComponent("mntmSetDecryptDir", mntmSetDecryptDir);
		mntmSetDecryptDir.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onDecryptSetDir();}
		});
		
		//Menu Options - Edit
		//Menu Options - Project
		
		JMenuItem mntmEditInfo = new JMenuItem("Edit Banner...");
		mnProject.add(mntmEditInfo);
		loaded_enabled.addComponent("mntmEditInfo", mntmEditInfo);
		mntmEditInfo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onFileEditProjectInfo();}
		});
		
		JMenu mnImportBanner = new JMenu("Import Banner Icon");
		mnProject.add(mnImportBanner);
		loaded_enabled.addComponent("mnImportBanner", mnImportBanner);
		
		mntmImportBannerMC = new JMenuItem("From Save Data...");
		mnImportBanner.add(mntmImportBannerMC);
		mntmImportBannerMC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onToolsImportBannerFromSave();}
		});
		
		JMenuItem mntmImportBannerPC = new JMenuItem("From Computer...");
		mnImportBanner.add(mntmImportBannerPC);
		mntmImportBannerPC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onToolsImportBannerFromComputer();}
		});
		
		mnProject.add(new JSeparator());
		
		JMenu mnManageSources = new JMenu("Sources");
		mnProject.add(mnManageSources);
		loaded_enabled.addComponent("mnManageSources", mnManageSources);
		
		mnSourcesAddons = new JMenu("Base Data");
		mnManageSources.add(mnSourcesAddons);
		mnSourcesAddons.setEnabled(false);
		//loaded_enabled.addComponent("mnSourcesAddons", mnSourcesAddons);
		
		mnSourcesUpdates = new JMenu("Updates");
		mnManageSources.add(mnSourcesUpdates);
		mnSourcesUpdates.setEnabled(false);
		
		mnSourcesDLC = new JMenu("DLC");
		mnManageSources.add(mnSourcesDLC);
		mnSourcesDLC.setEnabled(false);
		
		mntmImportCDTrack = new JMenuItem("Import Data Source...");
		mnManageSources.add(mntmImportCDTrack);
		//loaded_enabled.addComponent("mntmImportCDTrack", mntmImportCDTrack);
		mntmImportCDTrack.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onFileImportAdditionalData();}
		});
		
		mnProject.add(new JSeparator());
		
		JMenuItem mntmDumpImageAs = new JMenuItem("Dump Image To...");
		mnProject.add(mntmDumpImageAs);
		mntmDumpImageAs.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onToolsImgDump();}
		});
		
		JMenuItem mntmDumpAndConvert = new JMenuItem("Dump & Convert To...");
		mnProject.add(mntmDumpAndConvert);
		mntmDumpAndConvert.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onToolsConvDump();}
		});
		
		mnProject.add(new JSeparator());
		
		JMenu mnTreeReset = new JMenu("Reset Tree");
		mnProject.add(mnTreeReset);
		loaded_enabled.addComponent("mnTreeReset", mnTreeReset);
		
		JMenuItem mntmResetImageTree = new JMenuItem("Standard View");
		mnTreeReset.add(mntmResetImageTree);
		mntmResetImageTree.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onToolsTreeReset();}
		});
		
		JMenuItem mntmResetTreeFSDetail = new JMenuItem("FS Detail View");
		mnTreeReset.add(mntmResetTreeFSDetail);
		mntmResetTreeFSDetail.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onTreeResetDetailFS();}
		});
		
		mnProject.add(new JSeparator());
		
		JMenuItem mntmImageInfo = new JMenuItem("Image Info...");
		mnProject.add(mntmImageInfo);
		loaded_enabled.addComponent("mntmImageInfo", mntmImageInfo);
		mntmImageInfo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onFileInfo();}
		});
		
		//Menu Options - Decrypt
		
		JMenu mnAddKey = new JMenu("Add Key");
		mnDecryption.add(mnAddKey);
		always_enabled.addComponent("mnAddKey", mnAddKey);
		
		JMenuItem mntmAddKey = new JMenuItem("Import Key...");
		mnAddKey.add(mntmAddKey);
		always_enabled.addComponent("mntmAddKey", mntmAddKey);
		mntmAddKey.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onDecryptKeyAdd();}
		});
		
		JMenuItem mntmAddCtr9Key = new JMenuItem("Gen CTR Boot9 Keyset...");
		mnAddKey.add(mntmAddCtr9Key);
		always_enabled.addComponent("mntmAddCtr9Key", mntmAddCtr9Key);
		mntmAddCtr9Key.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onDecrypt3DSKeyAdd();}
		});
		
		JMenuItem mntmAddSwitchKeys = new JMenuItem("Import NX Keys...");
		mnAddKey.add(mntmAddSwitchKeys);
		always_enabled.addComponent("mntmAddSwitchKeys", mntmAddSwitchKeys);
		mntmAddSwitchKeys.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onDecryptNXKeyImport();}
		});
		
		JMenuItem mntmSetDecryptedImage = new JMenuItem("Set Decrypt Directory...");
		mnDecryption.add(mntmSetDecryptedImage);
		always_enabled.addComponent("mntmSetDecryptedImage", mntmSetDecryptedImage);
		mntmSetDecryptedImage.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onDecryptSetDir();}
		});
		
		JMenuItem mntmRunRomDecryption = new JMenuItem("Run ROM Decryption...");
		mnDecryption.add(mntmRunRomDecryption);
		mntmRunRomDecryption.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onDecryptRunDecrypt();}
		});
		
		//Menu Options - Types
		
		JMenuItem mntmScanForKnown = new JMenuItem("Scan for Known Types");
		mnTypes.add(mntmScanForKnown);
		mntmScanForKnown.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onToolsScanTypes();}
		});
						
		JMenuItem mntmMatchExts = new JMenuItem("Match Extensions to Types");
		mnTypes.add(mntmMatchExts);
		mntmMatchExts.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onToolsMatchExt();}
		});
		
		JMenuItem mntmClearTypeNotations = new JMenuItem("Clear Type Notations");
		mnTypes.add(mntmClearTypeNotations);
		mntmClearTypeNotations.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onToolsClearTypes();}
		});
		
		JMenuItem mntmAutoArchiveDump = new JMenuItem("Automount Archives...");
		mnTypes.add(mntmAutoArchiveDump);
		mntmAutoArchiveDump.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onToolsArcDump();}
		});
		
		JMenuItem mntmPluginManage = new JMenuItem("Manage Plugins...");
		mnTypes.add(mntmPluginManage);
		mntmPluginManage.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onTypesManagePlugins();}
		});
		
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		pnlMain = new ImageMasterPanel(this);
		pnlMain.setForeground(Color.LIGHT_GRAY);
		pnlMain.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		getContentPane().add(pnlMain, gbc_panel);
		
		loaded_enabled.setEnabling(false);
	}
	
	public void render()
	{
		pack();
		setVisible(true);
	}
	
	/*----- Enable/Disable -----*/
	
	public void disableMenu()
	{
		always_enabled.setEnabling(false);
		loaded_enabled.setEnabling(false);
	}
	
	public void enableMenu()
	{
		always_enabled.setEnabling(true);
		loaded_enabled.setEnabling(loaded_project != null);
	}
	
	/*----- GUI Update -----*/
	
	public void syncAddOnMenus(){
		mnSourcesAddons.removeAll();
		mnSourcesAddons.setEnabled(false);
		mnSourcesUpdates.removeAll();
		mnSourcesUpdates.setEnabled(false);
		mnSourcesDLC.removeAll();
		mnSourcesDLC.setEnabled(false);
		if(loaded_project != null){
			List<AddOnRecord> list = new LinkedList<AddOnRecord>();	
			//Patches
			list.addAll(loaded_project.getAllPatchRecords());
			AddOnRecord pstate = loaded_project.getCurrentPatchState();
			
			if(!list.isEmpty()){
				Collections.sort(list);
				int count = list.size();
				update_list = new AddonRadioButton[count+1]; //Base!
				AddonRadioButton rb = new AddonRadioButton(0, null, loaded_project.getBaseVersionString());
				update_list[0] = rb; mnSourcesUpdates.add(rb);
				if(pstate == null) rb.setSelected(true);
				int i = 1;
				for(AddOnRecord r : list){
					rb = new AddonRadioButton(i, r.getKey(), r.getDisplayString());
					if(pstate != null && pstate.getKey().equals(r.getKey())) rb.setSelected(true);
					update_list[i++] = rb;
					mnSourcesUpdates.add(rb);
				}
				
				mnSourcesUpdates.setEnabled(true);
				
			}
			
			//DLC
			list.clear();
			list.addAll(loaded_project.getAllDLCRecords());
			
			if(!list.isEmpty()){
				Collections.sort(list);
				Collection<String> loaded = loaded_project.getLoadedDLCKeys();
				int count = list.size();
				dlc_list = new AddonCheckbox[count];
				int i = 0;
				for(AddOnRecord r : list){
					AddonCheckbox cb = new AddonCheckbox(i, r.getKey(), r.getDisplayString(), true);
					if(loaded.contains(r.getKey())) cb.setSelected(true);
					dlc_list[i++] = cb;
					mnSourcesDLC.add(cb);
				}
				mnSourcesDLC.setEnabled(true);
			}
			
		}
		else{
			//Clear
			rawsrc_list = null;
			update_list = null;
			dlc_list = null;
		}
	}
	
	public void loadProject(NTDProject project)
	{
		always_enabled.setEnabling(false);
		loaded_enabled.setEnabling(false);
		
		IndefProgressDialog dialog = new IndefProgressDialog(this, "Loading Project");
		dialog.setPrimaryString("Please Wait");
		dialog.setSecondaryString("Loading project into browser");
		
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{

			protected Void doInBackground() throws Exception 
			{
				try{
					if(loaded_project != null) loaded_project.onProjectClose();
					
					loaded_project = project;
					if(loaded_project != null) loaded_project.onProjectOpen();
					
					if(loaded_project != null){
						//Check whether to enable add-on and save banner import...
						mntmImportBannerMC.setEnabled(loaded_project.supportsSaveBannerImport());
						mntmImportCDTrack.setEnabled(loaded_project.supportsAddOnImport());
					}
					
					//Load add-ons
					syncAddOnMenus();
					
					pnlMain.loadProject(project);
					
					always_enabled.setEnabling(true);
					loaded_enabled.setEnabling(project != null);
					
					repaint();
				}
				catch (Exception e){
					e.printStackTrace();
					dialog.showWarningMessage("ERROR: Project load failed! See stderr for details.");
				}
				return null;
			}
			
			public void done(){
				dialog.closeMe();
			}
			
		};
		
		//Execute task...
		task.execute();
		dialog.render();
		
	}
	
	/*----- Actions -----*/
	
	public void onImport(ImportDialog idialog)
	{
		if(idialog == null) return;
		NTDProject p = idialog.getImport();
		if(p == null) return;
		NTDProgramFiles.addProject(p);
		loadProject(p);
	}
	
	public void renderWithImportDialog()
	{
		render();
		ImportDialog dialog = new ImportDialog(this);
		dialog.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e)
			{
				onImport(dialog);
				dialog.dispose();
			}
		});
		dialog.setVisible(true);
	}
	
	public void renderWithOpenDialog()
	{
		render();
		OpenDialog dialog = new OpenDialog(this, NTDProgramFiles.getProjectMap());
		dialog.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.closeMe();
			}
		});
		dialog.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e)
			{
				NTDProject selection = dialog.getSelection();
				loadProject(selection);
			}
		});
		dialog.setVisible(true);
	}
	
	/*----- Menu Actions -----*/
	
	private void onFileOpen()
	{
		OpenDialog dialog = new OpenDialog(this, NTDProgramFiles.getProjectMap());
		dialog.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.closeMe();
			}
		});
		dialog.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e)
			{
				NTDProject selection = dialog.getSelection();
				if(selection != null) loadProject(selection);
			}
		});
		dialog.setVisible(true);
	}
	
	private void onFileImport()
	{
		ImportDialog dialog = new ImportDialog(this);
		dialog.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e)
			{
				onImport(dialog);
				dialog.dispose();
			}
		});
		dialog.setVisible(true);
	}
	
	private void onFileSave()
	{
		IndefProgressDialog dialog = new IndefProgressDialog(this, "Saving Project");
		
		dialog.setPrimaryString("Saving");
		dialog.setSecondaryString("");
		
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{

			protected Void doInBackground() throws Exception 
			{
				try
				{
					loaded_project.saveTree();
					NTDProgramFiles.saveProgramInfo();
				}
				catch(IOException x)
				{
					x.printStackTrace();
					showError("I/O Error: Save failed!");
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
	
	private void onFileInfo()
	{
		if(loaded_project == null) return;
		if(!FileBuffer.fileExists(loaded_project.getROMPath()))
		{
			showError("Source ROM file at " + loaded_project.getROMPath() + "\n"
					+ "was not found!");
			return;
		}
		
		ImageInfoDialogs.showImageInfoDialog(this, loaded_project);
	}
	
	private void onFileSetTemp()
	{
		String tpath = NTDProgramFiles.getTempDir();
		DirSetDialog dialog = new DirSetDialog(this, tpath);
		dialog.setVisible(true);
		
		if(dialog.confirmSelected())
		{
			tpath = dialog.getPath();
			try{NTDProgramFiles.clearTempDir();}
			catch(Exception x){x.printStackTrace();}
			NTDProgramFiles.setTempDir(tpath);
			
			showInfo("Temporary directory set to " + tpath);
		}
		
	}
	
	private void onToolsArcDump()
	{
		if(!checkSourcePath()) return;
		
		//Dumps all known archive formats to tree
		int op = JOptionPane.showConfirmDialog(this, 
				"Scan for and extract archives with known formats"
				+ " to the project tree?", 
				"Archive Extraction", 
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		
		if(op == JOptionPane.YES_OPTION)
		{
			IndefProgressDialog dialog = new IndefProgressDialog(this, "Extracting Archives");
			
			dialog.setPrimaryString("Scanning");
			dialog.setSecondaryString("Scanning for known archive formats...");
			
			SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
			{

				protected Void doInBackground() throws Exception 
				{
					try
					{
						List<FileNode> failed = new LinkedList<FileNode>();
						NTDTools.extractArchivesToTree(loaded_project.getTreeRoot(), dialog, failed);
						if(!failed.isEmpty())
						{
							String s = "Extraction failed for the following nodes: \n";
							for(FileNode f : failed)
							{
								s += "\t"+f.getFullPath()+"\n";
							}
							showError(s);
						}
						loaded_project.stampModificationTime();
					}
					catch(Exception x)
					{
						x.printStackTrace();
						showError("Unknown Error: Operation aborted. See stderr for details.");
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
		
	}
	
	private void onToolsImgDump()
	{
		if(!checkSourcePath()) return;
		
		//This just dumps the image as is.
		JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(INIKEY_LASTDUMP));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int sel = fc.showSaveDialog(this);
		
		if(sel != JFileChooser.APPROVE_OPTION) return;
		
		boolean auto_decomp = false;
		int op = JOptionPane.showConfirmDialog(this, "Automatically recognize and decompress files"
				+ " with known compression schemes?", "Decompression", 
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		
		if(op == JOptionPane.CANCEL_OPTION) return;
		auto_decomp = (op == JOptionPane.YES_OPTION);
		
		IndefProgressDialog dialog = new IndefProgressDialog(this, "Dumping Project Tree");
		
		String path = fc.getSelectedFile().getAbsolutePath();
		dialog.setPrimaryString("Dumping");
		dialog.setSecondaryString("Dumping ROM data to " + path);
		
		final boolean decomp = auto_decomp;
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{

			protected Void doInBackground() throws Exception 
			{
				try
				{
					DirectoryNode root = loaded_project.getTreeRoot();
					root.dumpTo(path, decomp, new DirectoryNode.TreeDumpListener() {
						
						@Override
						public void onStartNodeDump(FileNode node) {
							dialog.setSecondaryString("Extracting " + node.getFullPath());
						}
					});
				}
				catch(IOException x)
				{
					x.printStackTrace();
					showError("I/O Error: Tree dump failed!");
				}
				
				return null;
			}
			
			public void done()
			{
				dialog.closeMe();
				NTDProgramFiles.setIniValue(INIKEY_LASTDUMP, path);
				showInfo("ROM tree dump complete!");
			}
		};
		
		task.execute();
		dialog.render();
		
	}
	
	private void onToolsConvDump()
	{
		if(!checkSourcePath()) return;
		
		//File chooser
		JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(INIKEY_LASTDUMP));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int op = fc.showSaveDialog(this);
		
		if(op != JFileChooser.APPROVE_OPTION) return;
		String rawpath = fc.getSelectedFile().getAbsolutePath();
		
		//Conversion dialog...
		ConvertDumpDialog cdialog = new ConvertDumpDialog(this, TypeManager.getAvailableConversions());
		cdialog.setVisible(true);
		
		if(!cdialog.isConfirmed()) return;
		
		Collection<Converter> valid = cdialog.getIncluded();
		
		IndefProgressDialog dialog = new IndefProgressDialog(this, "Dump and Convert");
		
		dialog.setPrimaryString("Dumping Project");
		dialog.setSecondaryString("Dumping to " + rawpath);
		
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{

			protected Void doInBackground() throws Exception 
			{
				try
				{
					List<FileNode> failed = new LinkedList<FileNode>();
					NTDTools.doConversionDump(loaded_project.getTreeRoot(), dialog, failed, valid, rawpath);
					if(!failed.isEmpty())
					{
						String s = "Extraction failed for the following nodes: \n";
						for(FileNode f : failed)
						{
							s += "\t"+f.getFullPath()+"\n";
						}
						showError(s);
					}
				}
				catch(Exception x)
				{
					x.printStackTrace();
					showError("Unknown Error: Operation aborted. See stderr for details.");
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
	
	private void onToolsScanTypes(){
		if(!checkSourcePath()) return;
		
		//Dumps all known archive formats to tree
		int op = JOptionPane.showConfirmDialog(this, 
				"Scan tree to look for files with known formats?", 
				"File Type Scan", 
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		
		if(op == JOptionPane.YES_OPTION)
		{
			IndefProgressDialog dialog = new IndefProgressDialog(this, "Scanning Files");
			
			dialog.setPrimaryString("Scanning");
			dialog.setSecondaryString("Scanning for known formats...");
			
			SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
			{

				protected Void doInBackground() throws Exception 
				{
					try
					{
						NTDTools.doTypeScan(loaded_project.getTreeRoot(), dialog);
						loaded_project.stampModificationTime();
					}
					catch(Exception x)
					{
						x.printStackTrace();
						showError("Unknown Error: Operation aborted. See stderr for details.");
					}
					
					return null;
				}
				
				public void done()
				{
					pnlMain.updateTree();
					dialog.closeMe();
				}
			};
			
			task.execute();
			dialog.render();
		}
		
	}
	
	private void onToolsClearTypes(){
		if(!checkSourcePath()) return;
		
		//Dumps all known archive formats to tree
		int op = JOptionPane.showConfirmDialog(this, 
				"Clear file type notations from tree?", 
				"File Type Clear", 
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		
		if(op == JOptionPane.YES_OPTION)
		{
			IndefProgressDialog dialog = new IndefProgressDialog(this, "Clearing Type Notations");
			
			dialog.setPrimaryString("Scanning");
			dialog.setSecondaryString("Clearing tree of type notations...");
			
			SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
			{

				protected Void doInBackground() throws Exception 
				{
					try
					{
						NTDTools.clearTypeMarkers(loaded_project.getTreeRoot(), dialog);
						loaded_project.stampModificationTime();
					}
					catch(Exception x)
					{
						x.printStackTrace();
						showError("Unknown Error: Operation aborted. See stderr for details.");
					}
					
					return null;
				}
				
				public void done()
				{
					pnlMain.updateTree();
					dialog.closeMe();
				}
			};
			
			task.execute();
			dialog.render();
		}
	}
	
	private void onToolsTreeReset()
	{
		if(!checkSourcePath()) return;
		//TODO refresh preview panel
		int op = JOptionPane.showConfirmDialog(this, 
				"Are you sure you want to reset the tree to the ROM tree?\n"
				+ "If you do, you won't be able to undo it!", 
				"Tree Reset", 
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		
		if(op != JOptionPane.YES_OPTION) return;
		
		IndefProgressDialog dialog = new IndefProgressDialog(this, "Tree Reset");
		
		dialog.setPrimaryString("Resetting Tree");
		dialog.setSecondaryString("Parsing source ROM");
		
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{

			protected Void doInBackground() throws Exception 
			{
				try
				{
					loaded_project.resetTree(dialog);
				}
				catch(IOException x)
				{
					x.printStackTrace();
					showError("I/O Error: Reset failed!");
				}
				
				return null;
			}
			
			public void done()
			{
				pnlMain.refreshMe();
				dialog.closeMe();
			}
		};
		
		task.execute();
		dialog.render();
		
	}
	
	private void onTreeResetDetailFS(){
		if(!checkSourcePath()) return;
		//TODO refresh preview panel
		int op = JOptionPane.showConfirmDialog(this, 
				"Are you sure you want to reset the tree to the ROM tree?\n"
				+ "If you do, you won't be able to undo it!", 
				"Tree Reset", 
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		
		if(op != JOptionPane.YES_OPTION) return;
		
		IndefProgressDialog dialog = new IndefProgressDialog(this, "Tree Reset");
		
		dialog.setPrimaryString("Resetting Tree");
		dialog.setSecondaryString("Parsing source ROM");
		
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{

			protected Void doInBackground() throws Exception 
			{
				try
				{
					loaded_project.resetTreeFSDetail(dialog);
				}
				catch(IOException x)
				{
					x.printStackTrace();
					showError("I/O Error: Reset failed!");
				}
				
				return null;
			}
			
			public void done()
			{
				pnlMain.refreshMe();
				dialog.closeMe();
			}
		};
		
		task.execute();
		dialog.render();
	}
	
	private void onToolsMatchExt(){
		if(!checkSourcePath()) return;
		
		//Dumps all known archive formats to tree
		int op = JOptionPane.showConfirmDialog(this, 
				"Change file extensions to match type notations?", 
				"Type Extension Match", 
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		
		if(op == JOptionPane.YES_OPTION)
		{
			IndefProgressDialog dialog = new IndefProgressDialog(this, "Type Extension Match");
			
			dialog.setPrimaryString("Scanning");
			dialog.setSecondaryString("Matching file extensions...");
			
			SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
			{

				protected Void doInBackground() throws Exception 
				{
					try
					{
						NTDTools.matchExtensionsToType(loaded_project.getTreeRoot(), dialog);
						loaded_project.stampModificationTime();
					}
					catch(Exception x)
					{
						x.printStackTrace();
						showError("Unknown Error: Operation aborted. See stderr for details.");
					}
					
					return null;
				}
				
				public void done()
				{
					pnlMain.updateTree();
					dialog.closeMe();
				}
			};
			
			task.execute();
			dialog.render();
		}
	}
	
	private void onDecryptKeyAdd()
	{
		AddKeyDialog adialog = new AddKeyDialog(this);
		adialog.setVisible(true);
		
		if(!adialog.saveSelected()) return;
		
		String key = adialog.getKey();
		byte[] value = adialog.getData();
		
		boolean b = NTDProgramFiles.addKey(key, value);
		if(b) showInfo("Key addition succeeded!");
		else showError("Key addition failed!");
		
	}
	
	private void onDecrypt3DSKeyAdd()
	{
		AddCTRKeyDialog dialog = new AddCTRKeyDialog(this, "");
		dialog.setVisible(true);
		
		if(dialog.confirmSelected()){
			String path = dialog.getPath();
			try{
				CitrusCrypt crypto = CitrusCrypt.initFromBoot9(FileBuffer.createBuffer(path, false));
				String common9_path = NTDProgramFiles.getKeyFilePath(NTDProgramFiles.KEYNAME_CTR_COMMON9);
				crypto.saveCitrusCrypt(common9_path);
			}
			catch(Exception x){
				x.printStackTrace();
				showError("Keyset generation failed!");
			}
		}
		
	}
	
	private void onDecryptNXKeyImport(){
		NTDTools.importSwitchKeysFromDump(this);
	}
	
	private void onDecryptSetDir()
	{
		String tpath = NTDProgramFiles.getDecryptTempDir();
		DirSetDialog ddialog = new DirSetDialog(this, tpath);
		ddialog.setVisible(true);
		
		if(ddialog.confirmSelected())
		{
			String npath = ddialog.getPath();
			IndefProgressDialog dialog = new IndefProgressDialog(this, "Moving Directory");
			
			dialog.setPrimaryString("Moving Files");
			dialog.setSecondaryString("Moving all decrypted files to " + npath);
			
			SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
			{

				protected Void doInBackground() throws Exception 
				{
					try
					{
						try{NTDProgramFiles.moveDecryptTempDir(npath);}
						catch(Exception x){x.printStackTrace();}
						showInfo("Decrypt directory set to " + npath);
					}
					catch(Exception x)
					{
						x.printStackTrace();
						showError("Unknown Error: Decrypt directory was not changed!");
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
	}
	
	private void onDecryptRunDecrypt()
	{
		//Check if project has encrypted regions. If not, show
		//message saying it doesn't need decryption and return
		
		if(!checkSourcePath()) return;
		if(!loaded_project.isEncrypted())
		{
			showWarning("Current ROM does not contain encrypted regions!\n"
					+ "No decryption needed!");
			return;
		}
		
		//Confirm dialog (essentially to inform user of what they are doing
		// and where the files will be saved)
		String msgstr = "This process will save a copy of each decrypted region to disk.\n";
		msgstr += "These files can be quite large - often nearly as large as the full image.\n";
		msgstr += "Proceed, writing decrypted data to:\n";
		msgstr += loaded_project.getDecryptedDataDir();
		msgstr += " ?";
		
		int sel = JOptionPane.showConfirmDialog(this, msgstr, "Confirm Decryption", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		
		//Spawn progress dialog and background thread
		if(sel != JOptionPane.YES_OPTION) return;
		
		IndefProgressDialog dialog = new IndefProgressDialog(this, "Decrypt Image");
		
		dialog.setPrimaryString("Decrypting Data");
		dialog.setSecondaryString("Decrypting image data to " + loaded_project.getDecryptedDataDir());
		
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{

			protected Void doInBackground() throws Exception 
			{
				try
				{
					if(!loaded_project.decrypt(dialog))
					{
						showError("Unknown Error: Operation aborted. See stderr for details.");
					}
				}
				catch(Exception x)
				{
					x.printStackTrace();
					showError("Unknown Error: Operation aborted. See stderr for details.");
				}
				
				return null;
			}
			
			public void done(){
				pnlMain.refreshMe();
				dialog.closeMe();
			}
		};
		
		task.execute();
		dialog.render();
		
	}
	
	private void onFileExportProject()
	{
		//TODO
	}
	
	private void onFileImportProject()
	{
		//TODO
	}
	
	private void onFileImportAdditionalData(){

		if(loaded_project == null){
			showError("No project loaded!"); return;
		}
		if(!loaded_project.supportsAddOnImport()){
			showError("Current project does not support add-on import!"); return;
		}
		
		AddonImporter importer = loaded_project.getAddOnImporter();
		if(importer == null){
			showError("Current project does not support add-on import!"); return;
		}
		
		importer.importAddOn(this);
		
		syncAddOnMenus();
		pnlMain.refreshMe();
		repaint();
		
	}
	
	private void onFileDeleteProject(){
		if(loaded_project == null){
			showWarning("There is no loaded project to delete!");
			return;
		}
		
		int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this project?", 
				"Confirm Project Deletion", JOptionPane.YES_NO_OPTION);
		if(choice == JOptionPane.NO_OPTION) return;
		
		if(!NTDProgramFiles.removeProject(loaded_project)){
			this.showError("ERROR: Project deletion was not successful. See stderr for details.");
			return;
		}
		else{
			this.showInfo("Project deletion was successful!");
		}
		
		this.loadProject(null);
		
	}
	
	private void onToolsImportBannerFromSave(){
		if(loaded_project == null){
			showError("No project loaded!");
			return;
		}
		
		NTDTools.importBannerFromSave(this, loaded_project);
		pnlMain.updateBannerPanel();
	}
	
	private void onToolsImportBannerFromComputer(){
		if(loaded_project == null){
			showError("No project loaded!");
			return;
		}
		
		NTDTools.importBannerFromLocalFS(this, loaded_project);
		pnlMain.updateBannerPanel();
	}

	private void onFileEditProjectInfo(){
		if(loaded_project == null){
			showError("No project loaded!");
			return;
		}
		
		BannerEditForm dialog = new BannerEditForm(this);
		dialog.loadProjectInfo(loaded_project);
		dialog.setVisible(true);
		
		if(dialog.selectionApproved()){
			String banner = dialog.getBannerTitle();
			String pub = dialog.getPublisherName();
			GameRegion r = dialog.getSelectedRegion();
			DefoLanguage lan = dialog.getSelectedLanguage();
			
			loaded_project.setBannerTitle(banner);
			loaded_project.setPublisherName(pub);
			loaded_project.setRegion(r);
			loaded_project.setDefoLanguage(lan);
			
			if(loaded_project.getConsole() == Console.SWITCH){
				String code5 = dialog.getMiddleCode();
				if(code5 != null){
					while(code5.length() < 5) code5 += "0";
					if(code5.length() > 5) code5 = code5.substring(0,5);
					code5 = code5.toUpperCase();
					loaded_project.changeShortcode(code5);
				}
			}
		}
		
		dialog.dispose();
		pnlMain.updateBannerPanel();
	}
	
	private void onFileClose(){
		loadProject(null);
	}
	
	private void onTypesManagePlugins(){
		//TODO
	}
	
	/*----- Menu Addon Actions -----*/
	
	private void onSelectRawAddon(int idx){
		//TODO
	}
	
	private void onSelectDLC(int idx){
		//Get key and check if set or unset
		if(loaded_project == null) return;
		
		if(dlc_list == null) return;
		if(dlc_list[idx] == null) return;
		boolean set = dlc_list[idx].isSelected();
		String key = dlc_list[idx].getKey();
		if(key == null) return;
		
		//Generate wait dialog and task...
		IndefProgressDialog dialog = new IndefProgressDialog(this, "Setting DLC Module");
		dialog.setPrimaryString("Reading");
		dialog.setSecondaryString("Loading DLC Module");
		
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{

			protected Void doInBackground() throws Exception 
			{
				try{
					if(set) loaded_project.mountDLC(key);
					else loaded_project.dismountDLC(key);
				}
				catch (Exception e){
					e.printStackTrace();
					dialog.showWarningMessage("ERROR: DLC state change failed! See stderr for details.");
				}
				return null;
			}
			
			public void done(){
				dialog.closeMe();
			}
			
		};
		
		//Execute task...
		task.execute();
		dialog.render();
		
		//Refresh this form
		pnlMain.refreshMe();
	}
	
	private void onSelectUpdate(int idx){
		//Set the radio buttons (and get key)...
		String key = null;
		if(update_list == null) return;
		for(int i = 0; i < update_list.length; i++){
			if(update_list[i] != null){
				if(i == idx){
					update_list[i].setSelected(true);
					key = update_list[i].getKey();
				}
				else{
					update_list[i].setSelected(false);
				}	
			}
		}
		if(loaded_project == null) return;
		
		//Generate wait dialog and task...
		IndefProgressDialog dialog = new IndefProgressDialog(this, "Setting Patch Version");
		dialog.setPrimaryString("Reading");
		dialog.setSecondaryString("Loading patch state");
		
		String fkey = key;
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{

			protected Void doInBackground() throws Exception 
			{
				try{
					if(fkey == null) loaded_project.setToUnpatchedState();
					else loaded_project.setPatchState(fkey);
				}
				catch (Exception e){
					e.printStackTrace();
					dialog.showWarningMessage("ERROR: Patch state change failed! See stderr for details.");
				}
				return null;
			}
			
			public void done(){
				dialog.closeMe();
			}
			
		};
		
		//Execute task...
		task.execute();
		dialog.render();
		
		//Refresh this form
		pnlMain.refreshMe();
		
	}
	
	/*----- Checks -----*/
	
	private boolean checkSourcePath()
	{
		if(loaded_project == null) return false;
		
		Console console = loaded_project.getConsole();
		if(console == Console.DS || console == Console.DSi)
		{
			if(!FileBuffer.fileExists(loaded_project.getROMPath())) {
				showError("Source ROM file at " + loaded_project.getROMPath() + "\n"
						+ "was not found!");
				return false;
			}
		}
		
		return true;
	}
	
	/*----- Messages -----*/
	
	public void showWarning(String text)
	{
		JOptionPane.showMessageDialog(this, text, "Warning", JOptionPane.WARNING_MESSAGE);
	}
	
	public void showError(String text)
	{
		JOptionPane.showMessageDialog(this, text, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public void showInfo(String text)
	{
		JOptionPane.showMessageDialog(this, text, "Notice", JOptionPane.INFORMATION_MESSAGE);
	}
	
}
