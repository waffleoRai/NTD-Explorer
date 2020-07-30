package waffleoRai_NTDTypes.citrus;

import waffleoRai_Containers.nintendo.citrus.CitrusEXEFS;
import waffleoRai_Containers.nintendo.citrus.CitrusNCC;
import waffleoRai_Containers.nintendo.citrus.CitrusSMDH;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_NTDExCore.NTDTypeLoader;
import waffleoRai_NTDExCore.filetypes.TypeManager;

public class CitrusLoaders {
	
	//Sys
	
	public static class NCCHeaderDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return CitrusNCC.getHeaderDef();}
	}
	
	public static class NCCExHeaderDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return CitrusNCC.getExHeaderDef();}
	}
	
	public static class NCCAccessDescDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return CitrusNCC.getAccessDescDef();}
	}
	
	public static class NCCPlainRegDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return CitrusNCC.getPlainRegDef();}
	}
	
	public static class CXIBannerDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return CitrusEXEFS.getBannerDef();}
	}
	
	public static class CXIIconDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return CitrusSMDH.getIconDef();}
	}
	
	//Exe
	
	public static class CXIMainExeDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return CitrusEXEFS.getMainExeDef();}
	}

}
