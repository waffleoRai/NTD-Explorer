package waffleoRai_NTDTypes.encryption;

import waffleoRai_Containers.nintendo.NDS;
import waffleoRai_Files.EncryptionDefinition;
import waffleoRai_NTDExCore.NTDEncTypeLoader;

public class NinEncLoaders {
	
	public static class DSModcryptDefLoader implements NTDEncTypeLoader{
		public EncryptionDefinition getDefinition() {return NDS.getModcryptDef();}
	}
	
	public static class DSBlowfishDefLoader implements NTDEncTypeLoader{
		public EncryptionDefinition getDefinition() {return NDS.getBlowfishDef();}
	}

}
