package waffleoRai_NTDTypes.procyon;

import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_NTDExCore.NTDTypeLoader;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_SeqSound.misc.SMD;
import waffleoRai_soundbank.procyon.SWD;

public class ProcyonLoaders {
	
	//Audio
	
	public static class SMDLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_ProcyonSMD();}
		public FileTypeDefinition getDefinition() {return SMD.getDefinition();}
	}
		
	public static class SWDLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_ProcyonSWD();}
		public FileTypeDefinition getDefinition() {return SWD.getDefinition();}
	}

}
