package waffleoRai_NTDTypes.dolphin;

import waffleoRai_Executable.nintendo.DolExe;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_NTDExCore.NTDTypeLoader;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_fdefs.nintendo.PowerGCSysFileDefs;

public class DolphinLoaders {
	
	//Sys
	
	public static class GCHeaderDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return PowerGCSysFileDefs.getHeaderDef();}
	}
	
	public static class GCFSTDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return PowerGCSysFileDefs.getFSTDef();}
	}
	
	public static class GCBI2DefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return PowerGCSysFileDefs.getBi2Def();}
	}
	
	public static class GCApplDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return PowerGCSysFileDefs.getApploaderDef();}
	}
	
	//Exe
	
	public static class DolMainDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return DolExe.getDefinition();}
	}

}
