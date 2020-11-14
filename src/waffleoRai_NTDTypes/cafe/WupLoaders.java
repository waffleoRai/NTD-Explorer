package waffleoRai_NTDTypes.cafe;

import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_NTDExCore.NTDTypeLoader;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_fdefs.nintendo.PowerGCSysFileDefs;

public class WupLoaders {
	
	//Sys
	
	public static class WiiUPartTableDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return PowerGCSysFileDefs.getWiiUPartTableDef();}
	}
	
	public static class WiiUDiscHeaderDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return PowerGCSysFileDefs.getWiiUDiscHeaderDef();}
	}
	
	public static class WiiUPartHeaderDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return PowerGCSysFileDefs.getWiiUPartHeaderDef();}
	}
	
	public static class WiiUFSTDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return PowerGCSysFileDefs.getWiiUFSTDef();}
	}

}
