package waffleoRai_NTDExGUI;

import java.awt.Dimension;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import waffleoRai_Files.Converter;
import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.GUITools;
import waffleoRai_NTDExCore.Console;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.NTDTools;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExGUI.dialogs.AddKeyDialog;
import waffleoRai_NTDExGUI.dialogs.ConvertDumpDialog;
import waffleoRai_NTDExGUI.dialogs.DirSetDialog;
import waffleoRai_NTDExGUI.dialogs.ImportDialog;
import waffleoRai_NTDExGUI.dialogs.OpenDialog;
import waffleoRai_NTDExGUI.dialogs.imageinfo.ImageInfoDialogs;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_Utils.DirectoryNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileNode;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.awt.GridBagConstraints;

public class ExplorerForm extends JFrame {
	
	//TODO Also add detector on project load to see if
			//	existing decryption buffers have been deleted (and regenerate if needed)
	
	/*----- Constants -----*/
	
	private static final long serialVersionUID = -5515199484209320297L;

	public static final int MIN_WIDTH = ImageMasterPanel.MIN_WIDTH;
	public static final int MIN_HEIGHT = ImageMasterPanel.MIN_HEIGHT + 50;
	public static final int PREF_HEIGHT = 350;
	
	public static final String INIKEY_LASTDUMP = "LAST_RAW_DUMP_PATH";
	
	/*----- Instance Variables -----*/
	
	private ImageMasterPanel pnlMain;
	
	private ComponentGroup always_enabled;
	private ComponentGroup loaded_enabled;
	
	private NTDProject loaded_project;
	
	/*----- Build -----*/
	
 	public ExplorerForm()
	{
 		always_enabled = new ComponentGroup();
 		loaded_enabled = new ComponentGroup();
 		
		initGUI();
	}
	
	private void initGUI()
	{
		setTitle("NTD Explorer");
		Dimension sz = new Dimension(MIN_WIDTH, MIN_HEIGHT);
		setMinimumSize(sz);
		sz = new Dimension(MIN_WIDTH, PREF_HEIGHT);
		setPreferredSize(sz);
		setLocation(GUITools.getScreenCenteringCoordinates(this));
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		always_enabled.addComponent("mnFile", mnFile);
		
		JMenuItem mntmOpen = new JMenuItem("Open...");
		mnFile.add(mntmOpen);
		always_enabled.addComponent("mntmOpen", mntmOpen);
		mntmOpen.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e)
			{
				onFileOpen();
			}
			
		});
		
		JMenuItem mntmImportRom = new JMenuItem("Import ROM...");
		mnFile.add(mntmImportRom);
		always_enabled.addComponent("mntmImportRom", mntmImportRom);
		mntmImportRom.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e)
			{
				onFileImport();
			}
			
		});
		
		JMenuItem mntmSave = new JMenuItem("Save");
		mnFile.add(mntmSave);
		loaded_enabled.addComponent("mntmSave", mntmSave);
		mntmSave.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e)
			{
				onFileSave();
			}
			
		});
		
		JMenuItem mntmImageInfo = new JMenuItem("Image Info...");
		mnFile.add(mntmImageInfo);
		loaded_enabled.addComponent("mntmImageInfo", mntmImageInfo);
		mntmImageInfo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onFileInfo();}
		});
		
		JMenuItem mntmSetTempDirectory = new JMenuItem("Set Temp Directory...");
		mnFile.add(mntmSetTempDirectory);
		always_enabled.addComponent("mntmSetTempDirectory", mntmSetTempDirectory);
		mntmSetTempDirectory.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onFileSetTemp();}
		});
		
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
		
		
		JMenu mnTools = new JMenu("Tools");
		menuBar.add(mnTools);
		loaded_enabled.addComponent("mnTools", mnTools);
		
		JMenuItem mntmAutoArchiveDump = new JMenuItem("Auto Archive Dump...");
		mnTools.add(mntmAutoArchiveDump);
		mntmAutoArchiveDump.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onToolsArcDump();}
		});
		
		JMenuItem mntmDumpImageAs = new JMenuItem("Dump Image To...");
		mnTools.add(mntmDumpImageAs);
		mntmDumpImageAs.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onToolsImgDump();}
		});
		
		JMenuItem mntmDumpAndConvert = new JMenuItem("Dump and Convert...");
		mnTools.add(mntmDumpAndConvert);
		mntmDumpAndConvert.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onToolsConvDump();}
		});
		
		JMenuItem mntmScanForKnown = new JMenuItem("Scan for Known Types");
		mnTools.add(mntmScanForKnown);
		mntmScanForKnown.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onToolsScanTypes();}
		});
		
		JMenuItem mntmMatchExts = new JMenuItem("Change Extensions to Match Types");
		mnTools.add(mntmMatchExts);
		mntmMatchExts.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onToolsMatchExt();}
		});
		
		JMenuItem mntmClearTypeNotations = new JMenuItem("Clear Type Notations");
		mnTools.add(mntmClearTypeNotations);
		mntmClearTypeNotations.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onToolsClearTypes();}
		});
		
		JMenuItem mntmResetImageTree = new JMenuItem("Reset Image Tree");
		mnTools.add(mntmResetImageTree);
		mntmResetImageTree.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onToolsTreeReset();}
		});
		
		JMenu mnDecryption = new JMenu("Decryption");
		menuBar.add(mnDecryption);
		always_enabled.addComponent("mnDecryption", mnDecryption);
		
		JMenuItem mntmAddKey = new JMenuItem("Add Key...");
		mnDecryption.add(mntmAddKey);
		always_enabled.addComponent("mntmAddKey", mntmAddKey);
		mntmAddKey.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){onDecryptKeyAdd();}
		});
		
		JMenuItem mntmSetDecryptedImage = new JMenuItem("Set Decrypted Image Directory...");
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
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		pnlMain = new ImageMasterPanel(this);
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
	
	public void loadProject(NTDProject project)
	{
		always_enabled.setEnabling(false);
		loaded_enabled.setEnabling(false);
		
		loaded_project = project;
		
		pnlMain.loadProject(project);
		
		always_enabled.setEnabling(true);
		loaded_enabled.setEnabling(project != null);
	}
	
	/*----- Actions -----*/
	
	public void onImport(ImportDialog idialog)
	{
		if(idialog == null) return;
		NTDProject p = idialog.getImport();
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
				loadProject(selection);
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
					loaded_project.resetTree();
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
					if(!loaded_project.decrypt())
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
			
			public void done()
			{
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
