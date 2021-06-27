package waffleoRai_NTDTypes.encryption;

import waffleoRai_Containers.nintendo.NDS;
import waffleoRai_Containers.nintendo.cafe.CafeCrypt;
import waffleoRai_Containers.nintendo.nus.NUSDescrambler;
import waffleoRai_Files.EncryptionDefinition;
import waffleoRai_NTDExCore.NTDEncTypeLoader;
import waffleoRai_fdefs.nintendo.CitrusAESCTRDef;
import waffleoRai_fdefs.nintendo.NXSysDefs;
import waffleoRai_fdefs.nintendo.WiiAESDef;

public class NinEncLoaders {
	
	public static class NUSZ64ByteSwapDefLoader implements NTDEncTypeLoader{
		public EncryptionDefinition getDefinition() {return new NUSDescrambler.NUS_Z64_ByteswapDef();}
	}
	
	public static class NUSN64ByteSwapDefLoader implements NTDEncTypeLoader{
		public EncryptionDefinition getDefinition() {return new NUSDescrambler.NUS_N64_ByteswapDef();}
	}
	
	public static class DSModcryptDefLoader implements NTDEncTypeLoader{
		public EncryptionDefinition getDefinition() {return NDS.getModcryptDef();}
	}
	
	public static class DSBlowfishDefLoader implements NTDEncTypeLoader{
		public EncryptionDefinition getDefinition() {return NDS.getBlowfishDef();}
	}
	
	public static class WiiDiscAESDefLoader implements NTDEncTypeLoader{
		public EncryptionDefinition getDefinition() {return WiiAESDef.getDefinition();}
	}
	
	public static class CitrusAESDefLoader implements NTDEncTypeLoader{
		public EncryptionDefinition getDefinition() {return CitrusAESCTRDef.getDefinition();}
	}
	
	public static class WUPAESDefLoader implements NTDEncTypeLoader{
		public EncryptionDefinition getDefinition() {return CafeCrypt.getStandardAESDef();}
	}
	
	public static class WUPSecAESDefLoader implements NTDEncTypeLoader{
		public EncryptionDefinition getDefinition() {return CafeCrypt.getSectoredAESDef();}
	}
	
	public static class NXCTRDefLoader implements NTDEncTypeLoader{
		public EncryptionDefinition getDefinition() {return NXSysDefs.getCTRCryptoDef();}
	}
	
	public static class NXXTSDefLoader implements NTDEncTypeLoader{
		public EncryptionDefinition getDefinition() {return NXSysDefs.getXTSCryptoDef();}
	}
	

}
