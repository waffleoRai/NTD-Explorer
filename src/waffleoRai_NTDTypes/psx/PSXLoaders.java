package waffleoRai_NTDTypes.psx;

import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_NTDExCore.NTDTypeLoader;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_fdefs.psx.PSXSysDefs;

public class PSXLoaders {
	
	//System
	
	public static class PSXEXELoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return PSXSysDefs.getExeDef();}
	}
	
	public static class PSXCFGLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_PSXCFG();}
		public FileTypeDefinition getDefinition() {return PSXSysDefs.getConfigDef();}
	}

}
