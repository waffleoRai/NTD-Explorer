package waffleoRai_NTDTypes;

import waffleoRai_Containers.NonspecificArchiveDef;
import waffleoRai_Files.FileDefinitions;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_NTDExCore.NTDTypeLoader;
import waffleoRai_NTDExCore.filetypes.TM_EmptyFile;
import waffleoRai_NTDExCore.filetypes.TypeManager;

public class MiscLoaders {

	public static class EmptyFileDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_EmptyFile();}
		public FileTypeDefinition getDefinition() {return FileDefinitions.getEmptyFileDef();}
	}
	
	public static class GenArcDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return NonspecificArchiveDef.getDefinition();}
	}
	
}
