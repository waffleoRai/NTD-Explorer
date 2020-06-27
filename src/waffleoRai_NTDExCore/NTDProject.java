package waffleoRai_NTDExCore;

import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import waffleoRai_Files.EncryptionDefinitions;
import waffleoRai_Image.Animation;
import waffleoRai_Image.AnimationFrame;
import waffleoRai_Image.SimpleAnimation;
import waffleoRai_NTDExCore.consoleproj.DSProject;
import waffleoRai_NTDExCore.consoleproj.GCProject;
import waffleoRai_NTDExCore.consoleproj.PSXProject;
import waffleoRai_NTDExGUI.banners.Animator;
import waffleoRai_NTDExGUI.panels.AbstractGameOpenButton;
import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.DirectoryNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileNode;
import waffleoRai_Utils.FileTreeSaver;
import waffleoRai_Utils.SerializedString;

/*
 * UPDATES
 * 1.0.0
 * 
 * 2020.06.25 | 1.?.? -> 2.0.0
 * 	Initial Documentation
 * 	Moved console specific methods to subclasses (cleans it up)
 *  Also changed banner to Animation which meant parse/serialization overhaul
 */

/**
 * A class containing information for a software file system exploration
 * project. File tree consists of references to location on disk where files
 * can be found allowing for flexibility and memory conservation. Also includes
 * many fields for metadata such as software title and region.
 * @author Blythe Hospelhorn
 * @version 2.0.0
 * @since June 25, 2020
 *
 */
public abstract class NTDProject implements Comparable<NTDProject>{
	
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
	 * (Icon is max 255x255)
	 * Animation Sequence Nodes [1] (V6+) / RESERVED (V5-) [1]
	 * 		(If this is 0 or there is only one image, the animation fields are not present)
	 * Animation Flags [2] (V6+)
	 * 		15 - Is Ping-pong
	 * Animation Nodes [2n] (V6+)
	 * 		Image Index [1]
	 * 		# Frames [1]
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
	
	public static final String MAKERCODE_NINTENDO = "01";
	public static final String MAKERCODE_NINTENDO_DS = "10";
	public static final String MAKERCODE_SQUAREENIX = "DG";
	public static final String MAKERCODE_CAPCOM = "80";
	public static final String MAKERCODE_SEGA = "8P";
	public static final String MAKERCODE_DESTINEER = "RN";
	public static final String MAKERCODE_ALCHEMIST = "AK";
	public static final String MAKERCODE_UBISOFT = "";
	public static final String MAKERCODE_CDPR = "";
	public static final String MAKERCODE_EA = "69"; //lol nice
	
	public static final String MAKERNAME_NINTENDO = "Nintendo";
	public static final String MAKERNAME_SQUAREENIX = "Square Enix";
	public static final String MAKERNAME_CAPCOM = "Capcom";
	public static final String MAKERNAME_SEGA = "SEGA";
	public static final String MAKERNAME_DESTINEER = "Destineer";
	public static final String MAKERNAME_ALCHEMIST = "Alchemist";
	public static final String MAKERNAME_UBISOFT = "Ubisoft";
	public static final String MAKERNAME_CDPR = "CD Projekt Red";
	public static final String MAKERNAME_EA = "Electronic Arts";
	
	public static final String PATH_PLACEHOLDER = "<NA>";

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
	private Animation banner;
	
	private DirectoryNode custom_tree;
	
	private OffsetDateTime volume_time;
	private OffsetDateTime imported_time;
	private OffsetDateTime modified_time;
	
	/*----- Construction -----*/
	
	/**
	 * Construct a default NTDProject superclass instance.
	 * This instance is generated with the default single frame banner
	 * icon and enums being set to their "unknown" values.
	 */
	protected NTDProject()
	{
		console = Console.UNKNOWN;
		region = GameRegion.UNKNOWN;
		language = DefoLanguage.UNKNOWN;
		
		gamecode = "????";
		makercode = "??";
		fullcode = "UNK_XXXX_UNK";
		
		localName = "unknown game";
		banner = new SimpleAnimation(1);
		try{
			banner.setFrame(NTDProgramFiles.scaleDefaultImage_unknown(32, 32), 0);
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		encrypted_regs = new LinkedList<EncryptionRegion>();
	}
	
	/*----- Console Specific Project Creators -----*/
	
	/**
	 * Scan through the provided directory recursively and set all
	 * source path links for all nodes to the provided String if there
	 * is no source path already set for a node.
	 * @param defo Path to set as "default" source path.
	 * @param dn Topmost directory to scan.
	 */
	protected static void scanTreeDir(String defo, DirectoryNode dn)
	{
		List<FileNode> children = dn.getChildren();
		for(FileNode child : children){
			if(!child.isDirectory()){
				if(child.getSourcePath() == null || child.getSourcePath().isEmpty()){
					child.setSourcePath(defo);
				}
			}
			else{
				if(child instanceof DirectoryNode)scanTreeDir(defo, (DirectoryNode)child);
			}
		}
	}
	
	/*----- Paths -----*/
	
	private String my_dir_path;
	private String tdecrypt_path;
	
	/**
	 * Get the path on local system to the directory storing the save
	 * data for this project external to <i>proj.bin</i>.
	 * @return Path to project directory, or null if there is an error in derivation.
	 */
	public String getCustomDataDirPath(){
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
	
	/**
	 * Get the path on local system to the file containing the file tree
	 * for this project.
	 * @return Path to file tree as a String, or null if there is an error deriving path.
	 */
	public String getCustomTreeSavePath(){
		return getCustomDataDirPath() + File.separator + NTDProgramFiles.TREE_FILE_NAME;
	}
	
	/**
	 * Get the path on local system to the directory that stores decrypted
	 * data buffers for this project.
	 * @return Path to decrypted data directory, or null if there is an error in derivation.
	 */
	public String getDecryptedDataDir(){
		if(tdecrypt_path != null) return tdecrypt_path;
		tdecrypt_path = NTDProgramFiles.getDecryptTempDir() + File.separator + fullcode;
		return tdecrypt_path;
	}
	
	/*----- Parsing -----*/
	
	private static Animation readBannerIconData(FileBuffer file, long stoff, int version) throws DataFormatException, IOException{
		long cpos = stoff;
		int iframes = Byte.toUnsignedInt(file.getByte(cpos++));
		int iwidth = Byte.toUnsignedInt(file.getByte(cpos++));
		int iheight = Byte.toUnsignedInt(file.getByte(cpos++));
		int nframes = Byte.toUnsignedInt(file.getByte(cpos++));

		//Animation data (if applicable)
		long anim_offset = cpos;
		if(version < 6) nframes = iframes;
		else{
			//Skip past animation data for now
			if(iframes > 1) cpos += 2 + (nframes << 1);
		}
		Animation icon = new SimpleAnimation(nframes);
		
		//Image data...
		if(iframes == 0){
			//Load the question mark image instead...
			icon.setFrame(NTDProgramFiles.scaleDefaultImage_unknown(32, 32), 0);
			return icon;
		}

		BufferedImage[] imgarr = new BufferedImage[iframes];
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
			imgarr[z] = buff;
		}
		
		//Animation Data (if present)
		if(version >= 6 && iframes > 1){
			file.setCurrentPosition(anim_offset);
			int aflags = Short.toUnsignedInt(file.nextShort());
			if((aflags & 0x8000) != 0) icon.setAnimationMode(Animation.ANIM_MODE_PINGPONG);
			for(int i = 0; i < nframes; i++){
				int idx = Byte.toUnsignedInt(file.nextByte());
				int flen = Byte.toUnsignedInt(file.nextByte());
				icon.setFrame(new AnimationFrame(imgarr[idx], flen), i);
			}
		}
		else{
			//Just copy frame for frame
			for(int i = 0; i < iframes; i++){
				icon.setFrame(new AnimationFrame(imgarr[i], 1), i);
			}
		}
		
		return icon;
	}
	
	/**
	 * Create an NTDProject from data stored in a <code>proj.bin</code> block. This includes metadata and
	 * banner data, but does not include the tree, which must be loaded from the project's save
	 * data directory.
	 * @param file FileBuffer to read data from.
	 * @param stoff Start offset to begin reading as project block. 
	 * @param version Format version of <code>proj.bin</code>. This is very important as fields have
	 * been added and removed over the versions.
	 * @return The explorer project if read is successful, null if read is unsuccessful for an unknown readon.
	 * @throws IOException If there is an I/O error in reading the file.
	 * @throws DataFormatException If the DEFLATEd image data for the banner cannot be read.
	 */
	public static NTDProject readProject(FileBuffer file, long stoff, int version) throws IOException, DataFormatException
	{
		//NTDProject proj = new NTDProject();
		
		long cpos = stoff;
		
		int flag = Byte.toUnsignedInt(file.getByte(cpos)); cpos++;
		int cenum = (int)file.getByte(cpos); cpos++;
		int renum = Byte.toUnsignedInt(file.getByte(cpos)); cpos++;
		int lenum = Byte.toUnsignedInt(file.getByte(cpos)); cpos++;
		
		//Need to resolve up here as needed for game code...
		Console c = Console.getConsoleFromIntCode(cenum);
		NTDProject proj = null;
		//proj.console = Console.getConsoleFromIntCode(cenum);
		
		//Instantiate proj
		switch(c){
		case DS:
		case DSi:
			proj = new DSProject();
			proj.console = c;
			break;
		case GAMECUBE:
			proj = new GCProject();
			break;
		case PS1:
			proj = new PSXProject();
			break;
		case SWITCH:
		case WII:
		case WIIU:
		case _3DS:
		default:
			return null;
		}
		
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
		
		proj.banner = readBannerIconData(file, cpos, version);
		
		return proj;
	}
	
	/**
	 * Load the currently saved file tree for this project from the project save directory.
	 * @throws IOException If the tree file cannot be found or read.
	 * @throws UnsupportedFileTypeException If the tree file cannot be parsed.
	 */
	public void loadSavedTree() throws IOException, UnsupportedFileTypeException{
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
		if(encrypted_regs != null){
			for(EncryptionRegion reg : encrypted_regs){
				size += reg.getApproximateSerializedSize(false);
			}
		}
		
		//Calculate maximum image size...
		//Number of frames...
		if(banner != null){
			int fcount = banner.getNumberFrames();
			//Assume all frames have unique image.
			//Pull first image to see size...
			BufferedImage img = banner.getFrameImage(0);
			int w = img.getWidth(); int h = img.getHeight();
			int idatsize = (w*h) << 2; //Size of one image decomp in bytes
			
			size += 8; //Image lead-in data + compression size
			if(fcount > 1){
				//Animation data
				size += 2 + (fcount << 2);
			}
			size += idatsize * fcount;
		}
		else{
			size += 8;
		}
		
		return size;
	}
	
	private FileBuffer serializeBannerIcon(){
		if(banner == null) return null;
		
		//Get unique images
		int afcount = banner.getNumberFrames();
		List<BufferedImage> imgset = new LinkedList<BufferedImage>();
		for(int i = 0; i < afcount; i++){
			BufferedImage img = banner.getFrameImage(i);
			if(!imgset.contains(img)) imgset.add(img);
		}
		int icount = imgset.size();
		BufferedImage example = banner.getFrameImage(0);
		int width = example.getWidth();
		int height = example.getHeight();
		
		int idecompsize = ((width * height) << 2) * icount;
		int sz = 4 + 4 + idecompsize; //Overestimate for decomped
		if(afcount > 1){
			//Count animation data
			sz += 2 + (afcount << 1);
		}
		else afcount = 0;
		
		FileBuffer imgdat = new FileBuffer(sz, true);
		
		imgdat.addToFile((byte)icount);
		imgdat.addToFile((byte)width);
		imgdat.addToFile((byte)height);
		imgdat.addToFile((byte)afcount);
		
		if(afcount > 1){
			int flags = 0;
			if(banner.getAnimationMode() == Animation.ANIM_MODE_PINGPONG) flags |= 0x8000;
			imgdat.addToFile((short)flags);
			
			for(int i = 0; i < afcount; i++){
				//Match image...
				int idx = 0;
				BufferedImage fimg = banner.getFrameImage(i);
				for(BufferedImage comp : imgset){
					if(fimg == comp) break;
					if(fimg.equals(comp)) break;
					idx++;
				}
				imgdat.addToFile((byte)idx);
				imgdat.addToFile((byte)banner.getFrame(i).getLengthInFrames());
			}
		}
		
		//Image data
		byte[] rawimg = new byte[idecompsize];
		int p = 0;
		for(BufferedImage frame : imgset)
		{
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < height; x++)
				{
					int argb = frame.getRGB(x, y);
					int alpha = (argb >>> 24) & 0xFF;
					int red = (argb >>> 16) & 0xFF;
					int green = (argb >>> 8) & 0xFF;
					int blue = argb & 0xFF;
					
					rawimg[p++] = (byte)red; rawimg[p++] = (byte)green; 
					rawimg[p++] = (byte)blue; rawimg[p++] = (byte)alpha;
				}
			}
		}
		
		//Compressed image data
		Deflater comp = new Deflater();
		comp.setInput(rawimg);
		comp.finish();
		byte[] compbytes = new byte[idecompsize + 16];
		int complen = comp.deflate(compbytes);
		comp.end();
		
		imgdat.addToFile(complen);
		for(int i = 0; i < complen; i++)imgdat.addToFile(compbytes[i]);
		if(complen % 2 != 0) imgdat.addToFile(FileBuffer.ZERO_BYTE);
		
		return imgdat;
	}
	
	/**
	 * Generate a serialization of the metadata and banner data contained within
	 * this project to store as a <code>proj.bin</code> block.
	 * Path links are not scrubbed from output. This overload is intended
	 * for local saves, not export.
	 * @return FileBuffer containing serialized data. FileBuffer is allocated to be
	 * larger than actual data, so be careful calling methods that lay bare any underlying arrays.
	 */
	public FileBuffer serializeProjectBlock(){
		return serializeProjectBlock(false);
	}
	
	/**
	 * Generate a serialization of the metadata and banner data contained within
	 * this project to store as a <code>proj.bin</code> block or project export file.
	 * @param scrubPaths Whether to scrub linked file paths from output. Set to true
	 * for export file, set to false for local save.
	 * @return FileBuffer containing serialized data. FileBuffer is allocated to be
	 * larger than actual data, so be careful calling methods that lay bare any underlying arrays.
	 */
	public FileBuffer serializeProjectBlock(boolean scrubPaths)
	{
		//Calculate Size
		int sz = calculatePrelimSerializedSize();
		
		//Serialize strings
		FileBuffer str_rom = null;
		if(!scrubPaths){
			str_rom = new FileBuffer(3+(rom_path.length() << 1), true);
			str_rom.addVariableLengthString(NTDProgramFiles.ENCODING, rom_path, BinFieldSize.WORD, 2);
			sz += str_rom.getFileSize();
		}
		else{
			str_rom = new FileBuffer(6, true);
			str_rom.addVariableLengthString(NTDProgramFiles.ENCODING, PATH_PLACEHOLDER, BinFieldSize.WORD, 2);
			sz += str_rom.getFileSize();
		}

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
		if(encrypted_regs != null){
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
		if(encrypted_regs != null && is_encrypted){
			out.addToFile((short)encrypted_regs.size());
			int i = 0;
			for(EncryptionRegion reg : encrypted_regs){
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
		out.addToFile(serializeBannerIcon());
		
		return out;
	}
	
	/**
	 * Save the custom file tree loaded to this project in its current state.
	 * The tree file is saved to the save directory for this project. Tree target
	 * path can be obtained by calling <i>getCustomTreeSavePath()</i>.
	 * @throws IOException If there is an error writing to the target file.
	 */
	public void saveTree() throws IOException{
		String tpath = getCustomTreeSavePath();
		FileTreeSaver.saveTree(custom_tree, tpath);
	}
	
	/**
	 * Save the project metadata, banner data, and tree to an export file for sharing.
	 * This only saves the project structure, it should go without saying that this does
	 * not export any linked data.
	 * @param outpath Path to write export file to as a string.
	 * @throws IOException If the target file cannot be written.
	 */
	public void exportProject(String outpath) throws IOException{
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
	
	/**
	 * Get the path (as a string representation of a local file system path)
	 * to the primary ROM image file linked to this project.
	 * @return Currently linked ROM file path, or null if none.
	 */
	public String getROMPath(){return this.rom_path;}
	
	/**
	 * Get the path (as a string representation of a local file system path)
	 * to the decrypted buffer file linked to this project.
	 * <br>This method has been deprecated as instead I have decided
	 * to mark and only decrypt specific regions, each into their own 
	 * buffer (thus taking up less space).
	 * @return Path to decrypted ROM buffer, if applicable.
	 */
	@Deprecated
	public String getDecryptedROMPath(){return this.decrypted_rom_path;}
	
	/**
	 * Get the two-character maker code associated with the linked
	 * software image. For Nintendo systems, this is indicative
	 * of the software publisher.
	 * @return Maker code (2 ASCII character string) if present. Null
	 * if not present or image is not software for a Nintendo system.
	 */
	public String getMakerCode(){return this.makercode;}
	
	/**
	 * Get the unique ASCII gamecode for this software. For most Nintendo software,
	 * this code is 4 ASCII characters, where the final character is indicative
	 * of the software distribution region/language.
	 * <br>For Nintendo Switch software, this code is 5 characters with no regional
	 * indicator character.
	 * <br>For PS1 software, this method returns the full 10 character code. <code>getGameCode12()</code>
	 * returns the same value.
	 * @return Short gamecode for the software image associated with this project.
	 */
	public String getGameCode4(){return this.gamecode;}
	
	/**
	 * Get the formatted long ASCII gamecode for this software. For most Nintendo software,
	 * this follows the 12 character pattern CCC_GGGG_RRR where CCC refers to the console
	 * short code (eg. NTR for "Nitro", or RVL for "Revolution"), GGGG refers to the game code,
	 * and RRR refers to the region (eg. "USA" or "JPN"). This is the code seen on game cases 
	 * and cartridges.
	 * <br>Nintendo Switch software has 5 character game codes and thus the full code is 13 
	 * characters following the pattern HAC_GGGGG_RRR.
	 * <br>For PS1 software, this method returns the full 10 character code. <code>getGameCode4()</code>
	 * returns the same value.
	 * @return Long formatted gamecode for the software image associated with this project.
	 */
	public String getGameCode12(){return this.fullcode;}
	
	/**
	 * Get the banner title for the software associated with this project.
	 * For some images, the banner string can be extracted from the ROM data.
	 * For some, the title can be obtained from a memory card/save file.
	 * <br>This field can be multiple lines (delimited by a single newline
	 * character, linux style).
	 * @return Currently set banner title for this project.
	 */
	public String getBannerTitle(){return this.localName;}
	
	/**
	 * Get the frames of the banner icon as an image array. For images that
	 * are held over multiple frames, the image will be copied multiple times
	 * so that if a playback of the icon animation was to proceed over the array
	 * one array member per frame, the animation would play back correctly.
	 * @return Banner icon as an array of images.
	 */
	public BufferedImage[] getBannerIconImages(){return animationToImageArray(banner);}
	
	/**
	 * Get the banner icon as an Animation. If the banner is not animated, the
	 * Animation object will contain only one frame and the frame length
	 * can be disregarded.
	 * @return The banner icon Animation.
	 */
	public Animation getBannerIcon(){return banner;}
	
	/**
	 * Get the console enum value associated with this project. This should
	 * be indicative of what system the linked software is for.
	 * @return Console enum, or null if unset.
	 */
	public Console getConsole(){return this.console;}
	
	/**
	 * Get the region associated with this project. This may be read off the software
	 * ROM image or entered manually. This field is handy as many software/games can have regional
	 * differences or are region locked.
	 * @return Enum representing the region associated with linked software, or null if unset.
	 */
	public GameRegion getRegion(){return this.region;}
	
	/**
	 * Get the language setting for software associated with this project.
	 * This may be obtained from the ROM data, software code, or set manually.
	 * @return Enum representing software language, or null if unset.
	 */
	public DefoLanguage getDefoLanguage(){return this.language;}
	
	/**
	 * Get the timestamp describing when the project was created (the software
	 * image was imported).
	 * @return OffsetDateTime containing import timestamp.
	 */
	public OffsetDateTime getImportTime(){return this.imported_time;}
	
	/**
	 * Get the timestamp describing when the project was last modified.
	 * @return OffsetDateTime containing import timestamp.
	 */
	public OffsetDateTime getModifyTime(){return this.modified_time;}
	
	/**
	 * Get the manufacturer timestamp found on the associated data ROM.
	 * If no timestamp was found, this method returns either null or the
	 * zero value of OffsetDateTime.
	 * @return OffsetDateTime containing software timestamp, or null if not set.
	 * If not set, this may also return the OffsetDateTime zero value.
	 */
	public OffsetDateTime getVolumeTime(){return this.volume_time;}
	
	/**
	 * Get the publisher name associated with this project. This may
	 * be detected from a Nintendo maker code, or ISO volume info struct,
	 * but can also be set manually.
	 * @return Publisher name associated with project.
	 */
	public String getPublisherTag(){return this.pubName;}
	
	/**
	 * Get whether the software referenced by this project has encrypted sections.
	 * @return True if associated software image has encrypted portions. False if
	 * associated software is fully plaintext.
	 */
	public boolean isEncrypted()
	{
		if(encrypted_regs == null) return false;
		if(encrypted_regs.isEmpty()) return false;
		return true;
	}
	
	/**
	 * Get the root node of the file tree for this project referencing
	 * the locations and encoding of files in associated software image.
	 * @return Root of the current file tree.
	 */
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
	
	/**
	 * Set the import time field to an OffsetDateTime object reflecting input epoch second.
	 * @param raw Epoch second value as long, loaded from serialized data field.
	 */
	protected void loadImportTime(long raw){
		this.imported_time = OffsetDateTime.ofInstant(Instant.ofEpochSecond(raw), ZoneId.systemDefault());
	}

	/**
	 * Set the modification time field to an OffsetDateTime object reflecting input epoch second.
	 * @param raw Epoch second value as long, loaded from serialized data field.
	 */
	protected void loadModifyTime(long raw){
		this.modified_time = OffsetDateTime.ofInstant(Instant.ofEpochSecond(raw), ZoneId.systemDefault());
	}
	
	/**
	 * Set the project last-modified timestamp to the current time.
	 */
	public void stampModificationTime(){modified_time = OffsetDateTime.now();}
	
	/**
	 * Reset the project file tree to the tree found on the associated
	 * software ROM. This removes any user-made modifications to the tree including
	 * new directories, file renames, archive mounts, splits etc.
	 * @throws IOException If the ROM image file needs to be read an cannot be loaded.
	 */
	public abstract void resetTree() throws IOException;
	
	/**
	 * Set the banner icon by generating a 1 frame/image animation
	 * from an array of images.
	 * @param img Image array containing data to set as animation. If
	 * this parameter is null, the banner will not be changed.
	 */
	public void setBannerIcon(BufferedImage[] img){
		if(img == null) return;
		
		banner = new SimpleAnimation(img.length);
		for(int i = 0; i < img.length; i++){
			AnimationFrame f = new AnimationFrame(img[i], 1);
			banner.setFrame(f, i);
		}
	}
	
	/**
	 * Set the banner icon to the provided animation object.
	 * @param a Animation to set banner icon to. Animation will not be copied - if
	 * the parameter Animation is altered after this method is called, the banner will
	 * also be altered.
	 */
	public void setBannerIcon(Animation a){
		banner = a;
	}
	
	/**
	 * Set the banner title for this project. Up to three newline delimited lines can
	 * be used.
	 * @param str String to set banner title to.
	 */
	public void setBannerTitle(String str){this.localName = str;}
	
	/**
	 * Set the publisher name string for this project.
	 * @param str Publisher name to set.
	 */
	public void setPublisherName(String str){this.pubName = str;}
	
	protected void setConsole(Console c){console = c;}
	protected void setDefoLanguage(DefoLanguage lan){this.language = lan;}
	protected void setRegion(GameRegion r){this.region = r;}
	protected void setGameCode(String code){this.gamecode = code;}
	protected void setMakerCode(String code){this.makercode = code;}
	protected void setFullCode(String code){this.fullcode = code;}
	protected void setROMPath(String path){this.rom_path = path;}
	protected void reinstantiateEncryptedRegionsList(){this.encrypted_regs = new LinkedList<EncryptionRegion>();}
	protected void reinstantiateEncryptedRegionsList(int size){this.encrypted_regs = new ArrayList<EncryptionRegion>(size);}
	protected List<EncryptionRegion> getEncRegListReference(){return this.encrypted_regs;}
	protected void setTreeRoot(DirectoryNode root){this.custom_tree = root;}
	protected void setVolumeTime(OffsetDateTime time){this.volume_time = time;}
	protected void setImportedTime(OffsetDateTime time){this.imported_time = time;}
	protected void setModifiedTime(OffsetDateTime time){this.modified_time = time;}
	
	/*----- Decryption -----*/
	
	/**
	 * Scan through the project tree and mark (with encryption definitions) nodes
	 * that fall into encrypted regions.
	 * @param dir Directory to begin scan on (recursive use)
	 */
	protected void markEncryptedNodes(DirectoryNode dir){
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
	
	/**
	 * Perform a string substitution on node source paths, replacing any instances
	 * of oldpath with newpath.
	 * @param oldpath String to replace in node source paths.
	 * @param newpath String to replace with in node source paths.
	 * @param dir Directory to start scan (recursive use)
	 */
	protected void updateDecryptPaths(String oldpath, String newpath, DirectoryNode dir){
		//Just substitute the strings in the paths...
		List<FileNode> children = dir.getChildren();
		for(FileNode child : children){
			if(child instanceof DirectoryNode){
				updateDecryptPaths(oldpath, newpath, (DirectoryNode)child);
			}
			else
			{
				String cpath = child.getSourcePath();
				if(cpath.startsWith(oldpath)){
					String npath = cpath.replace(oldpath, newpath);
					child.setSourcePath(npath);
				}
			}
		}
		
	}
	
	/**
	 * Update the decrypted file paths (source paths in encrypted nodes) to a new
	 * decrypted buffer directory path.
	 * @param oldpath The previous decryption buffer directory path (the current one is obtained
	 * by calling <code>NTDProgramFiles.getDecryptTempDir()</code>)
	 */
	public void moveDecryptPath(String oldpath)
	{
		String newdir = NTDProgramFiles.getDecryptTempDir();
		updateDecryptPaths(oldpath, newdir, custom_tree);
		if(this.encrypted_regs != null){
			for(EncryptionRegion reg : encrypted_regs){
				String p = reg.getDecryptBufferPath();
				if(p.contains(oldpath)) p = p.replace(oldpath, newdir);
				reg.setDecryptBufferPath(p);
			}
		}
	}
	
	/**
	 * Update the file offsets in nodes that fall into encrypted regions to be
	 * relative to the start of the encrypted region rather than the start of the image.
	 * That way, they can be easily retrieved from the decrypted buffer file.
	 * @param dir Directory to begin scan (recursive use)
	 * @param reg Encrypted region to mark contained nodes relative to.
	 */
	protected void rerefDecryptedNodes(DirectoryNode dir, EncryptionRegion reg)
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
		
	/**
	 * Run full basic decryption routine on this project, generating decrypted
	 * data buffers (files containing individual decrypted regions) 
	 * in the project decryption directory, and adjusting references in tree to
	 * point to these decrypted data instead of the raw encrypted image.
	 * @return True if decryption routine succeeds, false if it fails, decryption cannot
	 * be run at this time, or the image has no encrypted regions.
	 * @throws IOException
	 */
	public boolean decrypt() throws IOException{return false;}
	
	/*----- Tree Manipulation -----*/
	
	/**
	 * Get the file node at the specified absolute path in the project file tree as 
	 * represented by the provided string.
	 * @param treepath Absolute path to desired FileNode.
	 * @return FileNode located at specified path, or null if not found.
	 */
	public FileNode getNodeAt(String treepath)
	{
		if(custom_tree == null) return null;
		return custom_tree.getNodeAt(treepath);
	}
	
	/**
	 * Generate a copy of the project tree culled to only directory nodes.
	 * This can be useful for browsing and selecting directories.
	 * @return Copy of project tree including only directories and no files.
	 */
	public DirectoryNode generateDirectoryTree(){
		if(custom_tree == null) return null;
		return custom_tree.copyDirectoryTree();
	}
	
	/**
	 * Move a node in the project tree to a new location on the project tree
	 * specified by the provided absolute path.
	 * @param node Node to move
	 * @param targetpath Path to move node to.
	 * @return True if the move succeeds, false if it fails.
	 */
	public boolean moveNode(FileNode node, String targetpath){
		//Get the target...
		FileNode target = getNodeAt(targetpath);
		if(target == null) return false;
		if(!(target instanceof DirectoryNode)) return false;
		
		DirectoryNode newparent = (DirectoryNode)target;
		node.setParent(newparent);
		
		return true;
	}
	
	/*----- GUI Interface -----*/
	
	/**
	 * Spawn a button for the NTDExplorer open dialog and load banner information
	 * into the button. 
	 * <br>This method does not load the button into the open dialog panel, it only
	 * spawns and does the initial banner load. This method is provided here to streamline
	 * the OpenDialog loading and allow for different project types (ie. for different consoles)
	 * to instantiate different types of buttons depending on their needs.
	 * @return Button decorated with banner information for use in the open dialog.
	 * @since 2.0.0
	 */
	public abstract AbstractGameOpenButton generateOpenButton();
	
	/**
	 * Display a modal dialog with the software image and project info.
	 * @param gui Parent Frame to spawn dialog relative to.
	 * @since 2.0.0
	 */
	public abstract void showImageInfoDialog(Frame gui);
	
	/**
	 * Generate an Animator containing the banner icon that handles animation 
	 * timing and frame updates when run.
	 * <br>This can be called to simplify icon usage by GUI forms.
	 * @param l Object listening to frame updates.
	 * @return Animator wrapping the banner icon.
	 * @since 2.0.0
	 */
	public abstract Animator getBannerIconAnimator(ActionListener l);
	
	/*----- Misc Utility -----*/
	
	/**
	 * Convert an Animation to an array of image frames. For animation
	 * frames where an image is held over multiple cycles, the image will
	 * be stored to the array that many times producing an array of a length
	 * that equals not the number of images referenced by the animation, but
	 * the number of animation frames.
	 * @param a Animation to simplify.
	 * @return BufferedImage array that mimics the input animation when read at 1 image/frame.
	 * @since 2.0.0
	 */
	public static BufferedImage[] animationToImageArray(Animation a){
		if(a == null) return null;
		
		int fcount = 0;
		int ncount = a.getNumberFrames();
		for(int i = 0; i < ncount; i++){
			AnimationFrame f = a.getFrame(i);
			if(f != null) fcount += f.getLengthInFrames();
		}
		if(a.getAnimationMode() == Animation.ANIM_MODE_PINGPONG){
			int hicount = fcount + (fcount - 1);
			BufferedImage[] arr = new BufferedImage[hicount];
			int j = 0;
			for(int i = 0; i < ncount; i++){
				AnimationFrame f = a.getFrame(i);
				if(f == null) continue;
				for(int k = 0; k < f.getLengthInFrames(); k++){
					arr[hicount - 1 - j] = f.getImage();
					arr[j++] = f.getImage(); 
				}
			}
			return arr;
		}
		else{
			BufferedImage[] arr = new BufferedImage[fcount];
			int j = 0;
			for(int i = 0; i < ncount; i++){
				AnimationFrame f = a.getFrame(i);
				if(f == null) continue;
				for(int k = 0; k < f.getLengthInFrames(); k++){
					arr[j++] = f.getImage(); 
				}
			}
			return arr;
		}
	}
	
	/**
	 * Format an OffsetDateTime timestamp as a neat string to use in a GUI or printout.
	 * @param timestamp Time to format.
	 * @return Time from timestamp as formatted string.
	 */
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
	
	/**
	 * Match a two-character Nintendo maker code to a Publisher name string
	 * by looking up hard-coded maker codes.
	 * @param ninMakerCode Two-character ASCII maker code from a Nintendo software.
	 * @return Publisher name, if matched - null otherwise.
	 * @since 2.0.0
	 */
	public static String getPublisherName(String ninMakerCode){
		switch(ninMakerCode){
		case MAKERCODE_NINTENDO: return MAKERNAME_NINTENDO;
		case MAKERCODE_SQUAREENIX: return MAKERNAME_SQUAREENIX;
		case MAKERCODE_CAPCOM: return MAKERNAME_CAPCOM;
		case MAKERCODE_SEGA: return MAKERNAME_SEGA;
		}
		return null;
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
