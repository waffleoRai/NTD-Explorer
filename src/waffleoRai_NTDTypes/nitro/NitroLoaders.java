package waffleoRai_NTDTypes.nitro;

import waffleoRai_Containers.nintendo.NARC;
import waffleoRai_Containers.nintendo.sar.DSSoundArchive;
import waffleoRai_Executable.nintendo.DSExeDefs;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_Image.nintendo.nitro.NCGR;
import waffleoRai_Image.nintendo.nitro.NCLR;
import waffleoRai_Image.nintendo.nitro.NSCR;
import waffleoRai_NTDExCore.NTDTypeLoader;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_SeqSound.ninseq.DSMultiSeq;
import waffleoRai_SeqSound.ninseq.DSSeq;
import waffleoRai_Sound.nintendo.DSStream;
import waffleoRai_Sound.nintendo.DSWarc;
import waffleoRai_Sound.nintendo.DSWave;
import waffleoRai_fdefs.nintendo.DSSysFileDefs;
import waffleoRai_soundbank.nintendo.DSBank;

public class NitroLoaders {
	
	//Sys
	
	public static class DSHeaderDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return DSSysFileDefs.getHeaderDef();}
	}
	
	public static class DSCertDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return DSSysFileDefs.getRSACertDef();}
	}
	
	public static class DSBannerDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return TypeManager.getDefaultManager();}
		public FileTypeDefinition getDefinition() {return DSSysFileDefs.getBannerDef();}
	}
	
	//Bincode
	public static class DSARM7Loader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_DSARM7();}
		public FileTypeDefinition getDefinition() {return DSExeDefs.getDefARM7();}
	}
	
	public static class DSARM7iLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_DSARM7i();}
		public FileTypeDefinition getDefinition() {return DSExeDefs.getDefARM7i();}
	}
	
	public static class DSARM9Loader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_DSARM9();}
		public FileTypeDefinition getDefinition() {return DSExeDefs.getDefARM9();}
	}
	
	public static class DSARM9iLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_DSARM9i();}
		public FileTypeDefinition getDefinition() {return DSExeDefs.getDefARM9i();}
	}
	
	//Archive
	
	public static class NARCLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_NARC();}
		public FileTypeDefinition getDefinition() {return NARC.getTypeDef();}
	}
	
	public static class SDATLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_SDAT();}
		public FileTypeDefinition getDefinition() {return DSSoundArchive.getTypeDef();}
	}
	
	//Audio
	
	public static class NBNKLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_NitroBNK();}
		public FileTypeDefinition getDefinition() {return DSBank.getDefinition();}
	}
	
	public static class NSEQLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_NitroSEQ();}
		public FileTypeDefinition getDefinition() {return DSSeq.getDefinition();}
	}
	
	public static class NSARLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_NitroSAR();}
		public FileTypeDefinition getDefinition() {return DSMultiSeq.getDefinition();}
	}
	
	public static class NWARLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_NitroWAR();}
		public FileTypeDefinition getDefinition() {return DSWarc.getDefinition();}
	}
	
	public static class NWAVLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_NitroWAV();}
		public FileTypeDefinition getDefinition() {return DSWave.getDefinition();}
	}
	
	public static class NSTMLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_NitroSTM();}
		public FileTypeDefinition getDefinition() {return DSStream.getDefinition();}
	}
	
	//2D Graphics
	
	public static class NCLRLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_NitroCLR();}
		public FileTypeDefinition getDefinition() {return NCLR.getTypeDef();}
	}
	
	public static class NCGRLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_NitroCGR();}
		public FileTypeDefinition getDefinition() {return NCGR.getTypeDef();}
	}
	
	public static class NSCRLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_NitroSCR();}
		public FileTypeDefinition getDefinition() {return NSCR.getTypeDef();}
	}

}
