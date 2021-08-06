package waffleoRai_NTDTypes.nus;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import waffleoRai_Containers.nintendo.nus.N64ROMImage;
import waffleoRai_Containers.nintendo.nus.N64ZFileTable;
import waffleoRai_Containers.nintendo.nus.NUSDescrambler;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Files.WriterPrintable;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.NTDTypeLoader;
import waffleoRai_NTDExCore.filetypes.TM_BIN;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_NTDTypes.WriterPanelManager;
import waffleoRai_Utils.EncryptedFileBuffer;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_fdefs.nintendo.NUSSysDefs;

public class NUSSysLoaders {
	
	//Loaders
	
	public static class NUSHeaderDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new NUSROMHeaderManager();}
		public FileTypeDefinition getDefinition() {return NUSSysDefs.getNUSHeaderDef();}
	}
	
	public static class NUSBootcodeLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return NUSSysDefs.getBootCodeDef();}
	}
	
	public static class NUSGameDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new NUSGameDatManager();}
		public FileTypeDefinition getDefinition() {return NUSSysDefs.getGameROMDef();}
	}
	
	public static class NUSCodeDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return NUSSysDefs.getGameCodeDef();}
	}
	
	public static class NUSDMATableDef implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return N64ZFileTable.getDefinition();}
	}
	
	//Managers
	
	public static class NUSROMHeaderManager extends WriterPanelManager{

		@Override
		protected WriterPrintable toPrintable(FileNode node) {
			if(node == null) return null;
			try{
				return N64ROMImage.readROMHeader(node.getSourcePath());
			}
			catch(IOException ex){
				ex.printStackTrace();
				return null;
			}
		}

		@Override
		public FileTypeNode detectFileType(FileNode node) {
			if(node == null || node.getLength() < 16) return null;
			try{
				FileBuffer dat = node.loadData(0, 16);
				dat.setEndian(false);
				int word = dat.intFromFile(0L);
				if(word == N64ROMImage.MAGIC_REVERSE){
					return new FileTypeDefNode(NUSSysDefs.getNUSHeaderDef());
				}
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			
			return null;
		}

		@Override
		public boolean isOfType(FileNode node) {
			return detectFileType(node) != null;
		}
		
	}
	
	public static class NUSGameDatManager extends TM_BIN{

		@Override
		public List<FileAction> getFileActions() {
			List<FileAction> list = super.getFileActions();
			list.add(new FA_ExtractAllNUS());
			return list;
		}
		
		@Override
		public boolean isOfType(FileNode node) {
			//No auto-detect
			return false;
		}
		
	}
	
	public static class FA_ExtractAllNUS implements FileAction{

		private String name = "Extract with Header";
		
		@Override
		public void doAction(FileNode node, NTDProject project, Frame gui_parent) {
			// TODO Auto-generated method stub
			JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(NTDProgramFiles.INIKEY_LAST_EXTRACTED));
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			int select = fc.showSaveDialog(gui_parent);
			
			if(select != JFileChooser.APPROVE_OPTION) return;
			String dir = fc.getSelectedFile().getAbsolutePath();
			
			String targetpath = dir + File.separator + project.getGameCode12() + ".p64";
			NTDProgramFiles.setIniValue(NTDProgramFiles.INIKEY_LAST_EXTRACTED, targetpath);
			IndefProgressDialog dialog = new IndefProgressDialog(gui_parent, "File Extraction");
			dialog.setPrimaryString("Extracting Data");
			dialog.setSecondaryString("Extracting to " + targetpath);
			
			SwingWorker<Void, Void> task = new SwingWorker<Void, Void>(){
				protected Void doInBackground() throws Exception {
					try{
						String srcpath = project.getROMPath();
						N64ROMImage rom = N64ROMImage.readROMHeader(srcpath);
						FileBuffer buff = FileBuffer.createBuffer(srcpath, false);
						switch(rom.getOrdering()){
						case N64ROMImage.ORDER_Z64:
							buff = new EncryptedFileBuffer(buff, new NUSDescrambler.NUS_Z64_ByteswapMethod());
							break;
						case N64ROMImage.ORDER_N64:
							buff = new EncryptedFileBuffer(buff, new NUSDescrambler.NUS_N64_ByteswapMethod());
							break;
						}
						buff.writeFile(targetpath);
					}
					catch(Exception x){
						x.printStackTrace();
						JOptionPane.showMessageDialog(gui_parent, 
								"Unknown Error: Extraction Failed! See stderr for details.", 
								"File Extraction Error", JOptionPane.ERROR_MESSAGE);
					}
					
					return null;
				}
				
				public void done(){
					dialog.closeMe();
				}
			};
			
			task.execute();
			dialog.render();
		}

		@Override
		public void setString(String s) {
			name = s;
		}
		
		public String toString(){return name;}
		
	}

}
