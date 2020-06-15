package waffleoRai_NTDTypes.compression;

import waffleoRai_Compression.definitions.AbstractCompDef;
import waffleoRai_Compression.nintendo.DSRLE;
import waffleoRai_Compression.nintendo.NinLZ;
import waffleoRai_NTDExCore.NTDCompTypeLoader;

public class NinCompLoaders {
	
	public static class NinLZDefLoader implements NTDCompTypeLoader{
		public AbstractCompDef getDefinition() {return NinLZ.getDefinition();}
	}
	
	public static class DSRLEDefLoader implements NTDCompTypeLoader{
		public AbstractCompDef getDefinition() {return DSRLE.getDefinition();}
	}

}
