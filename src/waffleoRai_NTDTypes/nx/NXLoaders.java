package waffleoRai_NTDTypes.nx;

import waffleoRai_Containers.nintendo.nx.NXContentMeta;
import waffleoRai_Containers.nintendo.nx.NXNACP;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_NTDExCore.NTDTypeLoader;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_fdefs.nintendo.NXSysDefs;

public class NXLoaders {
	
	//Sys
	public static class NXNCAHeaderDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return NXSysDefs.getNCAHeaderDef();}
	}
	
	public static class NXXCIHeaderDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return NXSysDefs.getXCIHeaderDef();}
	}
	
	public static class NXRomFSHeaderDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return NXSysDefs.getRomFSHeaderDef();}
	}
	
	public static class NXRomFSTDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return NXSysDefs.getRomFSTableDef();}
	}
	
	public static class NXPFSHeaderDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return NXSysDefs.getPFSHeaderDef();}
	}
	
	public static class NXHFSHeaderDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return NXSysDefs.getHFSHeaderDef();}
	}
	
	public static class NXCNMTDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new NXSysTypes.NXCNMTManager();}
		public FileTypeDefinition getDefinition() {return NXContentMeta.getDefinition();}
	}
	
	public static class NXNACPDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new NXSysTypes.NXNACPManager();}
		public FileTypeDefinition getDefinition() {return NXNACP.getDefinition();}
	}
	
}
