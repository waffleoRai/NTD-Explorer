package waffleoRai_NTDTypes.psx;

import waffleoRai_Compression.definitions.AbstractCompDef;
import waffleoRai_Compression.lz77.LZMu;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_NTDExCore.NTDCompTypeLoader;
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
	
	//Compression
	public static class MuLZDefLoader implements NTDCompTypeLoader{
		public AbstractCompDef getDefinition() {return LZMu.getDefinition();}
	}

}
