package waffleoRai_NTDExCore;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import waffleoRai_Containers.CDTable.CDInvalidRecordException;
import waffleoRai_Containers.ISO;
import waffleoRai_Containers.ISOXAImage;
import waffleoRai_Containers.nintendo.GCWiiDisc;
import waffleoRai_Containers.nintendo.GCWiiHeader;
import waffleoRai_Containers.nintendo.NDS;
import waffleoRai_Executable.nintendo.DolExe;
import waffleoRai_Files.EncryptionDefinitions;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Image.Animation;
import waffleoRai_Image.AnimationFrame;
import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.DirectoryNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileBufferStreamer;
import waffleoRai_Utils.FileNode;
import waffleoRai_Utils.FileTreeSaver;
import waffleoRai_Utils.SerializedString;
import waffleoRai_fdefs.nintendo.DSSysFileDefs;
import waffleoRai_fdefs.nintendo.PowerGCSysFileDefs;
import waffleoRai_fdefs.psx.PSXSysDefs;

public class NTDProject implements Comparable<NTDProject>{
	
	/*
	 * Block Format
	 * 
	 * Flags [1]
	 * 	7 - ROM has encrypted regions
	 * Console Enum[1] - Determines length of game codes (V3+)
	 * Region Enum [1]
	 * Language Enum [1]
	 * 
	 * Game Code...
	 * 	For all except PSX and HAC...
	 * 		GameCode [4]
	 * 		MakerCode [2]
	 * 		FullCode [10] (No dashes/underscores...)
	 * 	For PSX (v3+)
	 * 		GameCode [10] 
	 * 	For HAC (v3+)
	 * 		GameCode [6] (5 + null)
	 * 		MakerCode [2]
	 * 		FullCode [12] (11 + null)		
	 * 
	 * ROM Path [VLS 2x2]
	 * Decrypted ROM Path [VLS 2x2] (This is just a short 0 if N/A) (V1 only)
	 * Encrypted Region count [2] (If applicable) (V2+)
	 * Encrypted Regions (If applicable) (V2+)
	 * 		Definition [4]
	 * 		Offset [8]
	 * 		Size [8]
	 * 		Buffer path [VLS 2x2]
	 * 		#KeyDat Entries [2]
	 * 		KeyDat Entries
	 * 			KeyDat Len[2]
	 * 			KeyDat [Len]
	 * 
	 * (For Banner)
	 * Local Game Name [VLS 2x2] 
	 * Publisher String [VLS 2x2] (V4+)
	 * Publish Date [8] (V4+)
	 * # of image frames [1]
	 * Image width [1]
	 * Image height [1]
	 * RESERVED[1] 
	 * (Icon is max 255x255)
	 * CompData Size [4] (V5+)
	 * Image Data (DEFLATEd if V5+ -- Will add but haven't yet) - Decomp size can be calculated
	 * 	Each pixel is 32 bits (RGBA)
	 * Padding to 2-bytes (V5+)
	 * 
	 * 
	 * Exported File (ntdpj)
	 * 	MAGIC [8] "ntd PROJ"
	 * 	Version [2]
	 *  Flags [2]
	 *  Offset to Tree [4]
	 */
	
	/*----- Constant -----*/
	
	public static final String EXPORT_MAGIC = "ntd PROJ";
	public static final short CURRENT_VERSION = 1;
	
	public static final String MAKERDS_NINTENDO = "01";
	public static final String MAKERDS_SQUAREENIX = "DG";
	public static final String MAKERDS_CAPCOM = "80";
	public static final String MAKERDS_SEGA = "8P";

	/*----- Instance Variables -----*/
	
	private Console console;
	private GameRegion region;
	private DefoLanguage language;
	
	private String gamecode;
	private String makercode;
	private String fullcode;
	
	private String rom_path;
	private boolean is_encrypted; //Deprecated
	private String decrypted_rom_path; //Deprecated
	private List<EncryptionRegion> encrypted_regs;
	
	private String localName;
	private String pubName;
	private BufferedImage[] banner;
	
	private DirectoryNode custom_tree;
	
	private OffsetDateTime volume_time;
	private OffsetDateTime imported_time;
	private OffsetDateTime modified_time;
	
	/*----- Construction -----*/
	
	private NTDProject()
	{
		console = Console.UNKNOWN;
		region = GameRegion.UNKNOWN;
		language = DefoLanguage.UNKNOWN;
		
		gamecode = "????";
		makercode = "??";
		fullcode = "UNK_XXXX_UNK";
		
		localName = "unknown game";
		banner = new BufferedImage[1];
		try{
		banner[0] = NTDProgramFiles.scaleDefaultImage_unknown(32, 32);}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		encrypted_regs = new LinkedList<EncryptionRegion>();
	}
	
	public static NTDProject createEmptyProject()
	{
		return new NTDProject();
	}
	
	/*----- Console Specific Project Creators -----*/
	
	private static void scanTreeDir(String defo, DirectoryNode dn)
	{
		List<FileNode> children = dn.getChildren();
		for(FileNode child : children)
		{
			if(!child.isDirectory())
			{
				if(child.getSourcePath() == null || child.getSourcePath().isEmpty())
				{
					child.setSourcePath(defo);
				}
			}
			else
			{
				if(child instanceof DirectoryNode)scanTreeDir(defo, (DirectoryNode)child);
			}
		}
	}
	
	public static NTDProject createFromNDSImage(NDS image, GameRegion region)
	{
		//TODO
		NTDProject proj = new NTDProject();
		proj.imported_time = OffsetDateTime.now();
		proj.modified_time = OffsetDateTime.now();
		
		if(image.hasTWL()) proj.console = Console.DSi;
		else proj.console = Console.DS;
		proj.region = region;
		
		proj.gamecode = image.getGameCode();
		proj.language = DefoLanguage.getLanReg(proj.gamecode.charAt(3));
		if(proj.language == null) proj.language = DefoLanguage.UNKNOWN;
		
		proj.makercode = image.getMakerCodeAsASCII();
		if(proj.makercode.equals(MAKERDS_NINTENDO)) proj.pubName = "Nintendo";
		else if(proj.makercode.equals(MAKERDS_CAPCOM)) proj.pubName = "Capcom";
		else if(proj.makercode.equals(MAKERDS_SQUAREENIX)) proj.pubName = "Square Enix";
		
		proj.fullcode = proj.console.getShortCode() + "_" + proj.gamecode + "_" + proj.region.getShortCode();
		
		int lan = NDS.TITLE_LANGUAGE_ENGLISH;
		switch(proj.language)
		{
		case FRENCH: lan = NDS.TITLE_LANGUAGE_FRENCH; break;
		case GERMAN: lan = NDS.TITLE_LANGUAGE_GERMAN; break;
		case ITALIAN: lan = NDS.TITLE_LANGUAGE_ITALIAN; break;
		case JAPANESE: lan = NDS.TITLE_LANGUAGE_JAPANESE; break;
		case KOREAN: lan = NDS.TITLE_LANGUAGE_KOREAN; break;
		case SPANISH: lan = NDS.TITLE_LANGUAGE_SPANISH; break;
		default: lan = NDS.TITLE_LANGUAGE_ENGLISH; break;
		}
		
		proj.rom_path = image.getROMPath();
		proj.is_encrypted = image.hasModcryptRegions();
		
		if(proj.is_encrypted)
		{
			proj.encrypted_regs = new ArrayList<EncryptionRegion>(2);
			
			EncryptionRegion reg = new EncryptionRegion();
			String ddir = proj.getDecryptedDataDir();
			String stem = ddir + File.separator + NTDProgramFiles.DECSTEM_DSI_MC;
			String path = stem + "01.bin";
			
			long off = image.getMC1Offset();
			long size = image.getMC1Size();
			
			reg.setDecryptBufferPath(path);
			reg.setOffset(off);
			reg.setSize(size);
			reg.setDefintion(NDS.getModcryptDef());
			
			//WARNING!! The secure key is NOT currently accurate (20/03/31)!
			//I'm still testing the key derivation!!!!
			byte[] aeskey = null;
			if(image.usesSecureKey()){
				//aeskey = image.getSecureKey();
				aeskey = new byte[16];
			}
			else aeskey = image.getInsecureKey();
			
			reg.addKeyData(aeskey);
			reg.addKeyData(image.getModcryptCTR1());
			proj.encrypted_regs.add(reg);
			
			reg = new EncryptionRegion();
			path = stem + "02.bin";
			reg.setDecryptBufferPath(path);
			reg.setOffset(image.getMC2Offset());
			reg.setSize(image.getMC2Size());
			reg.setDefintion(NDS.getModcryptDef());
			reg.addKeyData(aeskey);
			reg.addKeyData(image.getModcryptCTR2());
			proj.encrypted_regs.add(reg);
		}
		
		proj.localName = image.getBannerTitle(lan);
		proj.banner = image.getBannerIcon();
		
		//Get main tree...
		proj.custom_tree = image.getArchiveTree();
		
		//Scan for empty paths...
		scanTreeDir(proj.rom_path, proj.custom_tree);
		
		//Note encrypted nodes...
		if(proj.is_encrypted) proj.markEncryptedNodes(proj.custom_tree);
		
		//Run initial type scan
		NTDTools.doTypeScan(proj.custom_tree, null);
		
		//Mark system files...
		FileNode sys = proj.custom_tree.getNodeAt("/header.bin");
		if(sys != null) sys.setTypeChainHead(new FileTypeDefNode(DSSysFileDefs.getHeaderDef()));
		sys = proj.custom_tree.getNodeAt("/icon.bin");
		if(sys != null) sys.setTypeChainHead(new FileTypeDefNode(DSSysFileDefs.getBannerDef()));
		if(image.hasTWL()){
			sys = proj.custom_tree.getNodeAt("/rsa.bin");
			if(sys != null) sys.setTypeChainHead(new FileTypeDefNode(DSSysFileDefs.getRSACertDef()));
		}
		
		return proj;
	}
	
	public static NTDProject createFromPSXTrack(String imgpath, GameRegion region) throws CDInvalidRecordException, IOException, UnsupportedFileTypeException{

		ISOXAImage image = new ISOXAImage(new ISO(FileBuffer.createBuffer(imgpath), false));
		NTDProject proj = new NTDProject();
		proj.imported_time = OffsetDateTime.now();
		proj.modified_time = OffsetDateTime.now();
		proj.rom_path = imgpath;
		
		proj.console = Console.PS1;
		proj.region = region;
		//Guess language from region...
		switch(proj.region){
		case JPN: proj.language =DefoLanguage.JAPANESE; break;
		case NOE: proj.language = DefoLanguage.ENGLISH; break;
		case UNKNOWN: proj.language = DefoLanguage.JAPANESE; break;
		case USA: proj.language = DefoLanguage.ENGLISH; break;
		case USZ: proj.language = DefoLanguage.ENGLISH; break;
		default: proj.language = DefoLanguage.JAPANESE; break;
		}
		
		//Nab tree (will need config and exe for auto-extracting more info
		proj.custom_tree = image.getRootNode();
		scanTreeDir(proj.rom_path, proj.custom_tree); //Set path for all nodes...
		
		//Nab the gamecode from the volume ident
		String volident = proj.custom_tree.getMetadataValue(ISOXAImage.METAKEY_VOLUMEIDENT);
		proj.gamecode = volident;
		proj.custom_tree.setFileName(volident);
		proj.fullcode = volident;
		
		proj.pubName = image.getPublisherIdent().replace(" ", "");
		GregorianCalendar date = image.getDateCreated();
		proj.volume_time = OffsetDateTime.ofInstant(date.toInstant(), date.getTimeZone().toZoneId());
		
		//Look for SYSTEM.CNF
		FileNode cnf = proj.custom_tree.getNodeAt("/" + volident + "/SYSTEM.CNF");
		if(cnf == null) cnf = proj.custom_tree.getNodeAt("/" + volident + "/system.cnf");
		if(cnf != null){
			cnf.setTypeChainHead(new FileTypeDefNode(PSXSysDefs.getConfigDef()));
			//Load and get more data
			String exepath = null;
			BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cnf.loadData().getBytes())));
			//Look for a line that starts with "BOOT"
			String line = null;
			while((line = br.readLine()) != null){
				//System.err.println(line);
				if(!line.startsWith("BOOT")) continue;
				String[] fields = line.replace(" ", "").split("=");
				if(fields.length < 2) break;
				String val = fields[1];
				val = val.substring(val.indexOf('\\') + 1);
				val = val.substring(0, val.lastIndexOf(';'));
				exepath = "/" + volident + "/" + val;
			}
			br.close();
			
			if(exepath != null){
				//Should be able to take out underscores and dots to get game code.
				//proj.gamecode = exepath.substring(1).replace(".", "").replace("_", "");
				FileNode exe = proj.custom_tree.getNodeAt(exepath);
				if(exe != null){
					System.err.println("Executable " + exepath + " found!");
					exe.setTypeChainHead(new FileTypeDefNode(PSXSysDefs.getExeDef()));
				}
				else{
					System.err.println("Executable " + exepath + " not found!");
				}
			}
			else{
				//Warn and set defaults
				System.err.println("PS1 ISO import error: executable not found!");
			}
		}
		else{
			//Will have to fill in with dummies...
			System.err.println("PS1 ISO import error: SYSTEM.CNF not found!");
		}
		
		proj.localName = "PS1Software " + proj.gamecode;
		
		return proj;
	}
	
	public static NTDProject createFromGCM(String imgpath, GameRegion region) throws IOException, UnsupportedFileTypeException{
		GCWiiDisc gcimg = new GCWiiDisc(imgpath);
		GCWiiHeader header = gcimg.getHeader();
		
		NTDProject proj = new NTDProject();
		proj.imported_time = OffsetDateTime.now();
		proj.modified_time = OffsetDateTime.now();
		
		proj.console = Console.GAMECUBE;
		proj.region = region;
		proj.gamecode = header.get4DigitGameCode();
		proj.makercode = header.getMakerCode();
		proj.language = DefoLanguage.getLanReg(proj.gamecode.charAt(3));
		proj.fullcode = "DOL_" + proj.gamecode + "_" + region.getShortCode();
		proj.rom_path = imgpath;
		
		proj.localName = header.getGameTitle();
		switch(proj.makercode){
		case MAKERDS_NINTENDO: proj.pubName = "Nintendo"; break;
		case MAKERDS_SQUAREENIX: proj.pubName = "SquareEnix"; break;
		case MAKERDS_CAPCOM: proj.pubName = "Capcom"; break;
		case MAKERDS_SEGA: proj.pubName = "Sega"; break;
		}
		
		proj.custom_tree = gcimg.getDiscTree();
		scanTreeDir(proj.rom_path, proj.custom_tree);
		
		//Type tags
		FileNode fn = proj.custom_tree.getNodeAt("/sys/boot.bin");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getHeaderDef()));
		fn = proj.custom_tree.getNodeAt("/sys/bi2.bin");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getBi2Def()));
		fn = proj.custom_tree.getNodeAt("/sys/fst.bin");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getFSTDef()));
		fn = proj.custom_tree.getNodeAt("/sys/apploader.img");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getApploaderDef()));
		fn = proj.custom_tree.getNodeAt("/sys/main.dol");
		fn.setTypeChainHead(new FileTypeDefNode(DolExe.getDefinition()));
		
		return proj;
	}
	
	/*----- Paths -----*/
	
	private String my_dir_path;
	private String tdecrypt_path;
	
	public String getCustomDataDirPath()
	{
		if(my_dir_path != null) return my_dir_path;
		my_dir_path = NTDProgramFiles.getInstallDir() + File.separator + 
				NTDProgramFiles.DIRNAME_PROJECTS + File.separator + fullcode;
		if(!FileBuffer.directoryExists(my_dir_path))
		{
			try
			{
				Files.createDirectory(Paths.get(my_dir_path));
			}
			catch(Exception x){x.printStackTrace();}
		}
		return my_dir_path;
	}
	
	public String getCustomTreeSavePath()
	{
		return getCustomDataDirPath() + File.separator + NTDProgramFiles.TREE_FILE_NAME;
	}
	
	public String getDecryptedDataDir()
	{
		if(tdecrypt_path != null) return tdecrypt_path;
		tdecrypt_path = NTDProgramFiles.getDecryptTempDir() + File.separator + fullcode;
		return tdecrypt_path;
	}
	
	/*----- Parsing -----*/
	
	public static NTDProject readProject(FileBuffer file, long stoff, int version) throws IOException, DataFormatException
	{
		NTDProject proj = new NTDProject();
		
		long cpos = stoff;
		
		int flag = Byte.toUnsignedInt(file.getByte(cpos)); cpos++;
		int cenum = (int)file.getByte(cpos); cpos++;
		int renum = Byte.toUnsignedInt(file.getByte(cpos)); cpos++;
		int lenum = Byte.toUnsignedInt(file.getByte(cpos)); cpos++;
		
		//Need to resolve up here as needed for game code...
		proj.console = Console.getConsoleFromIntCode(cenum);
		
		if(version >= 3){
			if(proj.console == Console.PS1){
				proj.gamecode = file.getASCII_string(cpos, 10); cpos+=10;
				proj.makercode = proj.gamecode.substring(0,2);
				proj.fullcode = proj.gamecode;
			}
			else if(proj.console == Console.SWITCH){
				proj.gamecode = file.getASCII_string(cpos, 6); cpos+=6;
				proj.makercode = file.getASCII_string(cpos, 2); cpos += 2;
				String fullcode_raw = file.getASCII_string(cpos, 12); cpos+=12;
				
				proj.fullcode = fullcode_raw.substring(0,3) + "_";
				proj.fullcode += fullcode_raw.substring(3,8) + "_";
				proj.fullcode += fullcode_raw.substring(8);
			}
			else{
				proj.gamecode = file.getASCII_string(cpos, 4); cpos+=4;
				proj.makercode = file.getASCII_string(cpos, 2); cpos += 2;
				String fullcode_raw = file.getASCII_string(cpos, 10); cpos+=10;
				
				proj.fullcode = fullcode_raw.substring(0,3) + "_";
				proj.fullcode += fullcode_raw.substring(3,7) + "_";
				proj.fullcode += fullcode_raw.substring(7);
			}
		}
		else{
			proj.gamecode = file.getASCII_string(cpos, 4); cpos+=4;
			proj.makercode = file.getASCII_string(cpos, 2); cpos += 2;
			String fullcode_raw = file.getASCII_string(cpos, 10); cpos+=10;
			
			proj.fullcode = fullcode_raw.substring(0,3) + "_";
			proj.fullcode += fullcode_raw.substring(3,7) + "_";
			proj.fullcode += fullcode_raw.substring(7);
		}
		
		SerializedString ss = file.readVariableLengthString(NTDProgramFiles.ENCODING, cpos, BinFieldSize.WORD, 2);
		proj.rom_path = ss.getString();
		cpos += ss.getSizeOnDisk();
		
		if(version < 2)
		{
			ss = file.readVariableLengthString(NTDProgramFiles.ENCODING, cpos, BinFieldSize.WORD, 2);
			proj.decrypted_rom_path = ss.getString();
			cpos += ss.getSizeOnDisk();	
		}
		else
		{
			if((flag & 0x80) != 0)
			{
				proj.is_encrypted = true;
				int rcount = file.shortFromFile(cpos); cpos += 2;
				proj.encrypted_regs = new ArrayList<EncryptionRegion>(rcount+1);
				
				for(int r = 0; r < rcount; r++)
				{
					int id = file.intFromFile(cpos); cpos+=4;
					long off = file.longFromFile(cpos); cpos += 8;
					long sz = file.longFromFile(cpos); cpos += 8;
					ss = file.readVariableLengthString(NTDProgramFiles.ENCODING, cpos, BinFieldSize.WORD, 2);
					String dpath = ss.getString();
					cpos += ss.getSizeOnDisk();
					EncryptionRegion reg = new EncryptionRegion(EncryptionDefinitions.getByID(id), off, sz, dpath);
					int keycount = file.shortFromFile(cpos); cpos += 2;
					for(int k = 0; k < keycount; k++)
					{
						int keysz = Short.toUnsignedInt(file.shortFromFile(cpos)); cpos+=2;
						byte[] barr = new byte[keysz];
						for(int b = 0; b < keysz; b++) {barr[b] = file.getByte(cpos); cpos++;}
						reg.addKeyData(barr);
					}
					proj.encrypted_regs.add(reg);
				}
			}
		}
		
		
		//Resolve enums and flags...
		proj.is_encrypted = (flag & 0x80) != 0;
		proj.region = GameRegion.getRegion(renum);
		proj.language = DefoLanguage.getLanReg((char)lenum);
		
		//Read banner data...
		ss = file.readVariableLengthString(NTDProgramFiles.ENCODING, cpos, BinFieldSize.WORD, 2);
		proj.localName = ss.getString();
		cpos += ss.getSizeOnDisk();
		
		if(version >= 4){
			ss = file.readVariableLengthString(NTDProgramFiles.ENCODING, cpos, BinFieldSize.WORD, 2);
			proj.pubName = ss.getString();
			cpos += ss.getSizeOnDisk();
			
			long time = file.longFromFile(cpos); cpos+=8;
			proj.volume_time = OffsetDateTime.ofInstant(Instant.ofEpochSecond(time), ZoneId.systemDefault());
		}
		
		int iframes = Byte.toUnsignedInt(file.getByte(cpos)); cpos++;
		int iwidth = Byte.toUnsignedInt(file.getByte(cpos)); cpos++;
		int iheight = Byte.toUnsignedInt(file.getByte(cpos)); cpos++;
		cpos++;
		
		//Image data...
		if(iframes == 0)
		{
			//Load the question mark image instead...
			proj.banner = new BufferedImage[1];
			proj.banner[0] = NTDProgramFiles.getDefaultImage_unknown();
		}
		else
		{
			proj.banner = new BufferedImage[iframes];	
			FileBuffer imgdat = null;
			
			int imgdat_len = iframes * iwidth * iheight * 4;
			
			//If v5+, INFLATE
			if(version >= 5){
				int complen = file.intFromFile(cpos); cpos+=4;
				byte[] compdat = file.getBytes(cpos, cpos+complen);
				Inflater dec = new Inflater();
				dec.setInput(compdat);
				byte[] result = new byte[imgdat_len+16];
				dec.inflate(result);
				dec.end();
				
				imgdat = new FileBuffer(imgdat_len, true);
				for(int i = 0; i < imgdat_len; i++) imgdat.addToFile(result[i]);
			}
			else{
				imgdat = file.createReadOnlyCopy(cpos, cpos+imgdat_len);
			}
			
			cpos = 0;
			for(int z = 0; z < iframes; z++)
			{
				BufferedImage buff = new BufferedImage(iwidth, iheight, BufferedImage.TYPE_INT_ARGB);
				for(int y = 0; y < iheight; y++)
				{
					for(int x = 0; x < iwidth; x++)
					{
						int red = Byte.toUnsignedInt(imgdat.getByte(cpos)); cpos++;
						int green = Byte.toUnsignedInt(imgdat.getByte(cpos)); cpos++;
						int blue = Byte.toUnsignedInt(imgdat.getByte(cpos)); cpos++;
						int alpha = Byte.toUnsignedInt(imgdat.getByte(cpos)); cpos++;
						
						int argb = (alpha << 24) | (red << 16) | (green << 8) | blue;
						buff.setRGB(x, y, argb);
					}
				}
				proj.banner[z] = buff;
			}
		}
		
		return proj;
	}
	
	public void loadSavedTree() throws IOException, UnsupportedFileTypeException
	{
		String tpath = getCustomTreeSavePath();
		try{
			if(FileBuffer.fileExists(tpath)) custom_tree = FileTreeSaver.loadTree(tpath);
		}
		catch(Exception x){
			System.err.println("Corrupted tree found. Resetting...");
			x.printStackTrace();
			resetTree();
		}
	}
	
	/*----- Serialization -----*/
	
	private int calculatePrelimSerializedSize()
	{
		//Omits VLSs since those need to be serialized first if UTF8
		int size = 28;
		
		//Approximate encryption info data size
		size += 2; //For enc reg count
		if(encrypted_regs != null)
		{
			for(EncryptionRegion reg : encrypted_regs)
			{
				size += reg.getApproximateSerializedSize(false);
			}
		}
		
		//Add image size...
		int pix = 0;
		if(banner == null || banner.length > 0)
		{
			pix = 128*128;
		}
		else
		{
			BufferedImage banner0 = banner[0];
			pix = banner.length * banner0.getHeight() * banner0.getWidth();
		}
		size += pix << 2;
		
		return size;
	}
	
	public FileBuffer serializeProjectBlock()
	{
		return serializeProjectBlock(false);
	}
	
	public FileBuffer serializeProjectBlock(boolean scrubPaths)
	{
		//Calculate Size
		int sz = calculatePrelimSerializedSize();
		
		//Serialize strings
		FileBuffer str_rom = null;
		if(!scrubPaths)
		{
			str_rom = new FileBuffer(3+(rom_path.length() << 1), true);
			str_rom.addVariableLengthString(NTDProgramFiles.ENCODING, rom_path, BinFieldSize.WORD, 2);
			sz += str_rom.getFileSize();
		}
		else
		{
			str_rom = new FileBuffer(6, true);
			str_rom.addVariableLengthString(NTDProgramFiles.ENCODING, "<NA>", BinFieldSize.WORD, 2);
			sz += str_rom.getFileSize();
		}
		//FileBuffer str_dec = null;
		/*if(is_encrypted){
			str_dec = new FileBuffer(3+(decrypted_rom_path.length() << 1), true);
			str_dec.addVariableLengthString(NTDProgramFiles.ENCODING, decrypted_rom_path, BinFieldSize.WORD, 2);
			sz += str_dec.getFileSize();
		}
		else{
			sz += 2;
			str_dec = new FileBuffer(2, true);
			str_dec.addToFile((short)0);
		}*/
		FileBuffer str_name = new FileBuffer(3+(localName.length() << 1), true);
		str_name.addVariableLengthString(NTDProgramFiles.ENCODING, localName, BinFieldSize.WORD, 2);
		sz += str_name.getFileSize();
		
		FileBuffer str_pub = null;
		if(pubName == null){
			str_pub = new FileBuffer(2, true);
			str_pub.addToFile((short)0);
			sz += 2;
		}
		else{
			str_pub = new FileBuffer(3+(pubName.length() << 1), true);
			str_pub.addVariableLengthString(NTDProgramFiles.ENCODING, pubName, BinFieldSize.WORD, 2);
			sz += str_pub.getFileSize();	
		}
		sz+=8; //Volume creation time
		
		FileBuffer[] epathnames = null;
		if(encrypted_regs != null)
		{
			epathnames = new FileBuffer[encrypted_regs.size()];
			int i = 0;
			for(EncryptionRegion reg : encrypted_regs)
			{
				FileBuffer reg_path = new FileBuffer(3+((reg.getDecryptBufferPath().length() << 1)), true);
				reg_path.addVariableLengthString(NTDProgramFiles.ENCODING, reg.getDecryptBufferPath(), BinFieldSize.WORD, 2);
				sz+=reg_path.getFileSize();
				epathnames[i] = reg_path;
				i++;
			}
		}
		
		//
		FileBuffer out = new FileBuffer(sz, true);
		
		//
		int flags = 0;
		if(is_encrypted) flags |= 0x80;
		out.addToFile((byte)flags);
		out.addToFile((byte)console.getIntValue());
		out.addToFile((byte)region.getIntValue());
		out.addToFile((byte)language.getCharCode());
		
		if(console == Console.PS1){
			if(gamecode.length() > 10) gamecode = gamecode.substring(0,10);
			out.printASCIIToFile(gamecode);
		}
		else if(console == Console.SWITCH){
			if(gamecode.length() > 6) gamecode = gamecode.substring(0,6);
			out.printASCIIToFile(gamecode);
			if(makercode.length() > 2) makercode = makercode.substring(0,2);
			out.printASCIIToFile(makercode);
			String fcode = fullcode.replace("_", "");
			if(fcode.length() > 12) fcode = fcode.substring(0,12);
			out.printASCIIToFile(fcode);
		}
		else{
			if(gamecode.length() > 4) gamecode = gamecode.substring(0,4);
			out.printASCIIToFile(gamecode);
			if(makercode.length() > 2) makercode = makercode.substring(0,2);
			out.printASCIIToFile(makercode);
			String fcode = fullcode.replace("_", "");
			if(fcode.length() > 10) fcode = fcode.substring(0,10);
			out.printASCIIToFile(fcode);	
		}
		
		
		out.addToFile(str_rom);
		//out.addToFile(str_dec);
		
		//Encryption info
		if(encrypted_regs != null && is_encrypted)
		{
			out.addToFile((short)encrypted_regs.size());
			int i = 0;
			for(EncryptionRegion reg : encrypted_regs)
			{
				out.addToFile(reg.getDefintion().getID());
				out.addToFile(reg.getOffset());
				out.addToFile(reg.getSize());
				out.addToFile(epathnames[i]);
				List<byte[]> keylist = reg.getKeyData();
				out.addToFile((short)keylist.size());
				for(byte[] key : keylist)
				{
					out.addToFile((short)key.length);
					for(byte b : key)out.addToFile(b);
				}
				i++;
			}
		}
		
		out.addToFile(str_name);
		out.addToFile(str_pub);
		if(volume_time == null) out.addToFile(0L);
		else out.addToFile(volume_time.toEpochSecond());
		
		//Banner
		if(banner == null) out.addToFile(0);
		else
		{
			out.addToFile((byte)banner.length);
			BufferedImage banner0 = banner[0];
			if(banner0 == null){
				out.add24ToFile(0);
			}
			else
			{
				int width = banner0.getWidth();
				int height = banner0.getHeight();
				out.addToFile((byte)width);
				out.addToFile((byte)height);
				out.addToFile((byte)0);
				
				//DEFLATE!
				int img_dat_len = width * height * banner.length * 4;
				byte[] in = new byte[img_dat_len];
				int i = 0;
				
				for(int z = 0; z < banner.length; z++)
				{
					BufferedImage frame = banner[z];
					for(int y = 0; y < height; y++)
					{
						for(int x = 0; x < height; x++)
						{
							int argb = frame.getRGB(x, y);
							int alpha = (argb >>> 24) & 0xFF;
							int red = (argb >>> 16) & 0xFF;
							int green = (argb >>> 8) & 0xFF;
							int blue = argb & 0xFF;
							
							//out.addToFile((byte)red);
							//out.addToFile((byte)green);
							//out.addToFile((byte)blue);
							//out.addToFile((byte)alpha);
							in[i++] = (byte)red; in[i++] = (byte)green; in[i++] = (byte)blue; in[i++] = (byte)alpha;
						}
					}
				}
				
				Deflater comp = new Deflater();
				comp.setInput(in);
				comp.finish();
				byte[] compbytes = new byte[img_dat_len + 16];
				int complen = comp.deflate(compbytes);
				comp.end();
				
				out.addToFile(complen);
				for(int j = 0; j < complen; j++){
					out.addToFile(compbytes[j]);
				}
				if(complen % 2 != 0) out.addToFile((byte)0);
			}
			
		}
		
		return out;
	}
	
	public void saveTree() throws IOException
	{
		String tpath = getCustomTreeSavePath();
		FileTreeSaver.saveTree(custom_tree, tpath);
	}
	
	public void exportProject(String outpath) throws IOException
	{
		//Do header
		FileBuffer header = new FileBuffer(16, true);
		header.printASCIIToFile(EXPORT_MAGIC);
		header.addToFile(CURRENT_VERSION);
		header.addToFile((short)0);
		
		//Serialize block
		FileBuffer pblock = serializeProjectBlock(true);
		int blocksize = (int)pblock.getFileSize();
		header.addToFile(16+blocksize);
		
		//Serialize tree
		String temppath = FileBuffer.generateTemporaryPath("exportntdproj");
		FileTreeSaver.saveTree(custom_tree, temppath, true);
		
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outpath));
		header.writeToStream(bos);
		pblock.writeToStream(bos);
		FileBuffer tree = FileBuffer.createBuffer(temppath);
		tree.writeToStream(bos);
		bos.close();
		
		Files.delete(Paths.get(temppath));
	}
	
	/*----- Getters -----*/
	
	public String getROMPath(){return this.rom_path;}
	public String getDecryptedROMPath(){return this.decrypted_rom_path;}
	public String getGameCode4(){return this.gamecode;}
	public String getGameCode12(){return this.fullcode;}
	public String getBannerTitle(){return this.localName;}
	public BufferedImage[] getBannerIcon(){return this.banner;}
	public Console getConsole(){return this.console;}
	public GameRegion getRegion(){return this.region;}
	//public boolean isEncrypted(){return this.is_encrypted;}
	public OffsetDateTime getImportTime(){return this.imported_time;}
	public OffsetDateTime getModifyTime(){return this.modified_time;}
	public OffsetDateTime getVolumeTime(){return this.volume_time;}
	public String getPublisherTag(){return this.pubName;}
	
	public boolean isEncrypted()
	{
		if(encrypted_regs == null) return false;
		if(encrypted_regs.isEmpty()) return false;
		return true;
	}
	
	public DirectoryNode getTreeRoot()
	{
		if(this.custom_tree == null)
		{
			try {loadSavedTree();} 
			catch (IOException e) {e.printStackTrace();} 
			catch (UnsupportedFileTypeException e) 
			{
				System.err.println(e.getErrorMessage());
				e.printStackTrace();
			}
		}
		return this.custom_tree;
	}
	
	/*----- Setters -----*/
	
	public void setDecryptStem(String path)
	{
		decrypted_rom_path = path;
	}
	
	protected void loadImportTime(long raw)
	{
		this.imported_time = OffsetDateTime.ofInstant(Instant.ofEpochSecond(raw), ZoneId.systemDefault());
	}

	protected void loadModifyTime(long raw)
	{
		this.modified_time = OffsetDateTime.ofInstant(Instant.ofEpochSecond(raw), ZoneId.systemDefault());
	}
	
	public void stampModificationTime(){modified_time = OffsetDateTime.now();}
	
	public void setConsole(Console c)
	{	
		console = c;
	}
	
	private void resetTree_gcm() throws IOException{
		GCWiiDisc gcimg = new GCWiiDisc(rom_path);
		custom_tree = gcimg.getDiscTree();
		//scanTreeDir(proj.rom_path, proj.custom_tree);
		FileNode fn = custom_tree.getNodeAt("/sys/boot.bin");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getHeaderDef()));
		fn = custom_tree.getNodeAt("/sys/bi2.bin");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getBi2Def()));
		fn = custom_tree.getNodeAt("/sys/fst.bin");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getFSTDef()));
		fn = custom_tree.getNodeAt("/sys/apploader.img");
		fn.setTypeChainHead(new FileTypeDefNode(PowerGCSysFileDefs.getApploaderDef()));
		fn = custom_tree.getNodeAt("/sys/main.dol");
		fn.setTypeChainHead(new FileTypeDefNode(DolExe.getDefinition()));
	}
	
	public void resetTree() throws IOException
	{
		if(console == Console.DS || console == Console.DSi)
		{
			NDS nds = NDS.readROM(rom_path, 0);
			custom_tree = nds.getArchiveTree();
			
			//Mark system files...
			FileNode sys = custom_tree.getNodeAt("/header.bin");
			if(sys != null) sys.setTypeChainHead(new FileTypeDefNode(DSSysFileDefs.getHeaderDef()));
			sys = custom_tree.getNodeAt("/icon.bin");
			if(sys != null) sys.setTypeChainHead(new FileTypeDefNode(DSSysFileDefs.getBannerDef()));
			if(nds.hasTWL()){
				sys = custom_tree.getNodeAt("/rsa.bin");
				if(sys != null) sys.setTypeChainHead(new FileTypeDefNode(DSSysFileDefs.getRSACertDef()));
			}
		}
		else if (console == Console.PS1){
			try {
				ISOXAImage image = new ISOXAImage(new ISO(FileBuffer.createBuffer(rom_path), false));
				custom_tree = image.getRootNode();
				String volident = custom_tree.getMetadataValue(ISOXAImage.METAKEY_VOLUMEIDENT);
				
				FileNode cnf = custom_tree.getNodeAt("/" + volident + "/SYSTEM.CNF");
				if(cnf == null) cnf = custom_tree.getNodeAt("/" + volident + "/system.cnf");
				if(cnf != null){
					cnf.setTypeChainHead(new FileTypeDefNode(PSXSysDefs.getConfigDef()));
					//Load and get more data
					String exepath = null;
					BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cnf.loadData().getBytes())));
					//Look for a line that starts with "BOOT"
					String line = null;
					while((line = br.readLine()) != null){
						//System.err.println(line);
						if(!line.startsWith("BOOT")) continue;
						String[] fields = line.replace(" ", "").split("=");
						if(fields.length < 2) break;
						String val = fields[1];
						val = val.substring(val.indexOf('\\') + 1);
						val = val.substring(0, val.lastIndexOf(';'));
						exepath = "/" + volident + "/" + val;
					}
					br.close();
					
					if(exepath != null){
						//Should be able to take out underscores and dots to get game code.
						//proj.gamecode = exepath.substring(1).replace(".", "").replace("_", "");
						FileNode exe = custom_tree.getNodeAt(exepath);
						if(exe != null){
							System.err.println("Executable " + exepath + " found!");
							exe.setTypeChainHead(new FileTypeDefNode(PSXSysDefs.getExeDef()));
						}
						else{
							System.err.println("Executable " + exepath + " not found!");
						}
					}
					else{
						//Warn and set defaults
						System.err.println("PS1 ISO import error: executable not found!");
					}
				}
				else{
					//Will have to fill in with dummies...
					System.err.println("PS1 ISO import error: SYSTEM.CNF not found!");
				}
			} 
			catch (CDInvalidRecordException e) {
				e.printStackTrace();
			} 
			catch (UnsupportedFileTypeException e) {
				e.printStackTrace();
			}
		}
		else if (console == Console.GAMECUBE){
			resetTree_gcm();
		}
		
		//Note encrypted nodes...
		scanTreeDir(rom_path, custom_tree);
		if(is_encrypted) markEncryptedNodes(custom_tree);
				
		//Run initial type scan
		NTDTools.doTypeScan(custom_tree, null);
		
		stampModificationTime();
	}
	
	public void setBannerIcon(BufferedImage[] img){
		banner = img;
	}
	
	public void setBannerIcon(Animation a){
		int fcount = 0;
		for(int i = 0; i < a.getNumberFrames(); i++) fcount += a.getFrame(i).getLengthInFrames();
		banner = new BufferedImage[fcount];
		int j = 0;
		for(int i = 0; i < a.getNumberFrames(); i++){
			AnimationFrame f = a.getFrame(i);
			for(int k = 0; k < f.getLengthInFrames(); k++){
				banner[j++] = f.getImage();
			}
		}
	}
	
	public void setBannerTitle(String str){
		this.localName = str;
	}
	
	/*----- Decryption -----*/
	
	private void markEncryptedNodes(DirectoryNode dir)
	{
		if(encrypted_regs == null) return;
		List<FileNode> children = dir.getChildren();
		for(FileNode child : children)
		{
			if(child instanceof DirectoryNode) markEncryptedNodes((DirectoryNode)child);
			else
			{
				for(EncryptionRegion reg : encrypted_regs)
				{
					if(reg.inRegion(child.getOffset(), child.getLength()))
					{
						long off = 0;
						long sz = child.getLength();
						if(reg.getOffset() > child.getOffset()) off = child.getOffset() - reg.getOffset();
						if(off != 0 || reg.getSize() < child.getLength()){
							long end = reg.getOffset() + reg.getSize();
							long cend = child.getOffset() + child.getLength();
							if(end < cend){
								if(off != 0) sz = end - reg.getOffset();
								else sz = end - child.getOffset();
							}
						}
						child.setEncryption(reg.getDefintion(), off, sz);
						break;
					}
				}
			}
		}
		
	}
	
	private void updateDecryptPaths(String oldpath, String newpath, DirectoryNode dir)
	{
		//Just substitute the strings in the paths...
		List<FileNode> children = dir.getChildren();
		for(FileNode child : children)
		{
			if(child instanceof DirectoryNode)
			{
				updateDecryptPaths(oldpath, newpath, (DirectoryNode)child);
			}
			else
			{
				String cpath = child.getSourcePath();
				if(cpath.startsWith(oldpath))
				{
					String npath = cpath.replace(oldpath, newpath);
					child.setSourcePath(npath);
				}
			}
		}
		
	}
	
	public void moveDecryptPath(String oldpath)
	{
		String newdir = NTDProgramFiles.getDecryptTempDir();
		updateDecryptPaths(oldpath, newdir, custom_tree);
		if(this.encrypted_regs != null)
		{
			for(EncryptionRegion reg : encrypted_regs)
			{
				String p = reg.getDecryptBufferPath();
				if(p.contains(oldpath)) p = p.replace(oldpath, newdir);
				reg.setDecryptBufferPath(p);
			}
		}
	}
	
	private void rerefDecryptedNodes(DirectoryNode dir, EncryptionRegion reg)
	{
		if(dir == null) return;
		if(reg == null) return;
		
		List<FileNode> children = dir.getChildren();
		for(FileNode child : children)
		{
			if(child instanceof DirectoryNode) rerefDecryptedNodes((DirectoryNode)child, reg);
			else
			{
				if(reg.inRegion(child.getOffset(), child.getLength()))
				{
					long r_off = child.getOffset() - reg.getOffset();
					child.setSourcePath(reg.getDecryptBufferPath());
					child.setOffset(r_off);
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	@Deprecated
	private boolean decryptDSi() throws IOException
	{
		//Scan regions
		if(encrypted_regs == null || encrypted_regs.isEmpty()) return true;
		NDS nds = null;
		byte[] securekey = null;
		
		for(EncryptionRegion reg : encrypted_regs)
		{
			//See if the key is available
			//Key, then ctr
			List<byte[]> keydat = reg.getKeyData();
			byte[] aeskey = keydat.get(0);
			if(aeskey.length < 16)
			{
				//Didn't have the DSi common before. Try to load now.
				if(securekey != null) aeskey = securekey;
				else
				{
					if(nds == null) nds = NDS.readROM(rom_path, 0);
					securekey = nds.getSecureKey();
					aeskey = securekey;
				}
				//if we get this far, then we should have the key.
				keydat.set(0, aeskey);
			}
			//Check if decrypted file exists
			String decpath = reg.getDecryptBufferPath();
			if(FileBuffer.fileExists(decpath)) continue;
			
			//Do decryption
			FileBuffer inbuff = FileBuffer.createBuffer(rom_path, reg.getOffset(), reg.getOffset() + reg.getSize());
			FileBufferStreamer streamer = new FileBufferStreamer(inbuff);
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(decpath));
			reg.getDefintion().decrypt(streamer, bos, keydat);
			bos.close();
			
			//Change references in nodes...
			rerefDecryptedNodes(custom_tree, reg);
		}
		
		//We'll have to reload the ROM image
		nds = NDS.readROM(rom_path, 0);
		
		return true;
	}
	
	public boolean decrypt() throws IOException
	{
		//Will redo decryption if already done...
		//if(console == Console.DSi) return decryptDSi();
		return false;
	}
	
	/*----- Tree Manipulation -----*/
	
	public FileNode getNodeAt(String treepath)
	{
		if(custom_tree == null) return null;
		return custom_tree.getNodeAt(treepath);
	}
	
	public DirectoryNode generateDirectoryTree()
	{
		if(custom_tree == null) return null;
		return custom_tree.copyDirectoryTree();
	}
	
	public boolean moveNode(FileNode node, String targetpath)
	{
		//Get the target...
		FileNode target = getNodeAt(targetpath);
		if(target == null) return false;
		if(!(target instanceof DirectoryNode)) return false;
		
		DirectoryNode newparent = (DirectoryNode)target;
		node.setParent(newparent);
		
		return true;
	}
	
	/*----- Misc Utility -----*/
	
	public static String getDateTimeString(OffsetDateTime timestamp){
		if(timestamp == null) return null;
		StringBuilder sb = new StringBuilder(512);
		sb.append(String.format("%02d ", timestamp.getDayOfMonth()));
		sb.append(timestamp.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + " ");
		sb.append(timestamp.getYear() + " ");
		sb.append(String.format("%02d:%02d:%02d", timestamp.getHour(), timestamp.getMinute(), timestamp.getSecond()));
		sb.append(" " + timestamp.getOffset().getDisplayName(TextStyle.NARROW, Locale.getDefault()));
		return sb.toString();
	}
	
	/*----- Comparison -----*/
	
	public boolean equals(Object o){
		return (this == o);
	}
	
	public int hashCode(){
		return fullcode.hashCode();
	}

	@Override
	public int compareTo(NTDProject o) {
		if(o == null) return 1;
		
		//Compare by console..
		Console tcon = this.getConsole();
		Console ocon = o.getConsole();
		if(tcon != ocon){
			if(tcon == null) return -1;
			if(ocon == null) return 1;
			return tcon.getIntValue() - ocon.getIntValue();
		}
		
		//Compare by full code...
		String tcode = this.getGameCode12();
		String ocode = o.getGameCode12();
		if(tcode == null) return -1;
		
		return tcode.compareTo(ocode);
	}
	
}
