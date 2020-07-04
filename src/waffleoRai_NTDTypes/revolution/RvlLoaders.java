package waffleoRai_NTDTypes.revolution;

import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_NTDExCore.NTDTypeLoader;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_fdefs.nintendo.PowerGCSysFileDefs;

public class RvlLoaders {

	//Sys
	
	public static class WiiPartTableDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return PowerGCSysFileDefs.getPartTableDef();}
	}
	
	public static class WiiReginfoDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return PowerGCSysFileDefs.getRegInfoDef();}
	}
	
	public static class WiiTicketDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return PowerGCSysFileDefs.getWiiTicketDef();}
	}
	
	public static class WiiTMDDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return PowerGCSysFileDefs.getWiiTMDDef();}
	}
	
	public static class WiiH3DefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return PowerGCSysFileDefs.getWiiH3Def();}
	}
	
	public static class WiiCertDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return PowerGCSysFileDefs.getRSADef();}
	}
	
}
