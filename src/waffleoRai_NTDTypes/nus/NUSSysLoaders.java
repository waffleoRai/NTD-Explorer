package waffleoRai_NTDTypes.nus;

import java.awt.Frame;
import java.io.IOException;
import java.util.List;

import waffleoRai_Containers.nintendo.nus.N64ROMImage;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Files.WriterPrintable;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.NTDTypeLoader;
import waffleoRai_NTDExCore.filetypes.TM_BIN;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDTypes.WriterPanelManager;
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
				if(word == N64ROMImage.MAGIC_LE){
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
			
		}

		@Override
		public void setString(String s) {
			name = s;
		}
		
		public String toString(){return name;}
		
	}

}
