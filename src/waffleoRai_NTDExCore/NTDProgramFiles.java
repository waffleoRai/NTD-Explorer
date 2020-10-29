package waffleoRai_NTDExCore;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.DataFormatException;

import javax.imageio.ImageIO;

import waffleoRai_Compression.definitions.AbstractCompDef;
import waffleoRai_Compression.definitions.CompDefNode;
import waffleoRai_Compression.definitions.CompressionDefs;
import waffleoRai_Containers.nintendo.citrus.CitrusCrypt;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBufferStreamer;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_Utils.StreamWrapper;


/*
 * proj.bin format
 * 	Format Version [4]
 * 	# of blocks [4]
 * 	Blocks...
 * 		Import Time [8]
 * 		Modify Time [8]
 * 		Block Size [4]
 * 		BLOCK (See NTDProject.java for block format)
 */

public class NTDProgramFiles {
	
	
	public static final String ENCODING = "UTF8";
	public static final byte[] KEY_PLACEHOLDER = {0};
	
	/*----- Temp Stems -----*/
	
	public static final String DECSTEM_DSI_MC = "twl_modcryptreg"; //Eg. mydir/TWL-IWAO-USA/twl_modcryptreg01.bin
	public static final String DECSTEM_WII_PART = "rvl_aes_part"; //Eg. mydir/RVL-RC3E-USA/rvl_aes_part00_01.bin
	public static final String DECSTEM_CTR_PART = "ctr_ncch_part"; //Eg. mydir/CTR-AJRE-USA/ctr_ncch_part4000000125500.bin
	
	/*----- Key Paths -----*/
	
	public static final String KEYNAME_DS_BLOWFISH = "ntr_blowfish";
	public static final String KEYNAME_DSI_BLOWFISH = "twl_blowfish";
	public static final String KEYNAME_DSI_COMMON = "twl_common";
	
	public static final String KEYNAME_WII_COMMON = "rvl_common";
	public static final String KEYNAME_WII_SD = "rvl_sd_common";
	public static final String KEYNAME_WII_SD_IV = "rvl_sd_iv";
	public static final String KEYNAME_WII_SD_MD5 = "rvl_sd_md5blanker";
	public static final String KEYNAME_WIIU_COMMON = "wup_common";
	
	public static final String KEYNAME_CTR_COMMON9 = "ctr_common9";
	public static final String KEYNAME_CTR_CARD1 = "ctr_25X";
	public static final String KEYNAME_CTR_CARDA = "ctr_18X";
	public static final String KEYNAME_CTR_CARDB = "ctr_1BX";
	
	public static final String KEYNAME_HAC_COMMON = "hac_common";
	public static final String KEYNAME_HAC_TITLE = "hac_title";
	
	public static final String[] ALL_KEYKEYS = {KEYNAME_DSI_COMMON, KEYNAME_WII_COMMON, KEYNAME_WII_SD, KEYNAME_WII_SD_IV, 
												KEYNAME_CTR_COMMON9, KEYNAME_CTR_CARD1, KEYNAME_CTR_CARDA, KEYNAME_CTR_CARDB,
												KEYNAME_WIIU_COMMON, KEYNAME_HAC_COMMON, KEYNAME_HAC_TITLE};
	
	/*----- Various Ini Keys -----*/
	
	public static final String INIKEY_LAST_ARCDUMP = "LAST_ARCDUMP_PATH";
	public static final String INIKEY_LAST_EXPORTED = "LAST_EXPORT_PATH";
	public static final String INIKEY_LAST_EXTRACTED = "LAST_EXTRACT_PATH";
	public static final String INIKEY_LAST_SAVEICONPATH = "LAST_SAVE_BANNERIMPORT_PATH";
	public static final String INIKEY_LAST_PCICONPATH = "LAST_PC_BANNERIMPORT_PATH";
	
	public static final String INIKEY_LAST_MIDI_EXPORT = "LAST_MIDIEXP_PATH";
	public static final String INIKEY_LAST_SEQWAV_EXPORT = "LAST_SEQWAVEXP_PATH";
	
	/*----- Font -----*/
	
	private static final String[] TRYFONTS = {"Arial Unicode MS", "MS PGothic", "MS Gothic", 
			"AppleGothic", "Takao PGothic",
			"Hiragino Maru Gothic Pro", "Hiragino Kaku Gothic Pro"};
	
	/*----- Metadata Tags -----*/
	
	public static final String FN_METAKEY_TITLE = "TITLE";
	
	/*----- Paths -----*/
	
	public static final String INI_FILE_NAME = "ntde.ini";
	
	public static final String DIRNAME_CRYPTO = "crypto";
	public static final String DIRNAME_CRYPTO_KEYS = "keys";
	public static final String DIRNAME_CRYPTO_DECRYPT = "decbuffer";
	
	public static final String DIRNAME_PROJECTS = "projects";
	public static final String DIRNAME_TEMP = "temp";
	public static final String DIRNAME_PLUGINS = "plugins";
	
	public static final String PROJECTS_FILE_NAME = "proj.bin";
	
	public static final String TREE_FILE_NAME = "customtree.bin";
	
	private static String ini_path;
	
	public static String getIniPath()
	{
		if(ini_path != null) return ini_path;
		
		String osname = System.getProperty("os.name");
		osname = osname.toLowerCase();
		String username = System.getProperty("user.name");
		
		if(osname.startsWith("win"))
		{
			//Assumed windows
			String dir = "C:\\Users\\" + username;
			dir += "\\AppData\\Local\\waffleorai\\NTDExplorer";
			dir += "\\" + INI_FILE_NAME;
			ini_path = dir;
			return dir;
		}
		else
		{
			//Assumed Unix like
			String dir = System.getProperty("user.home");
			char sep = File.separatorChar;
			dir += "appdata" + sep + "local" + sep + "waffleorai" + sep + "NTDExplorer";
			dir += sep + INI_FILE_NAME;
			ini_path = dir;
			return dir;
		}
	}
	
	public static String getUsername()
	{
		return System.getProperty("user.name");
	}
	
	public static String getPluginsDirectoryPath(){
		return getInstallDir() + File.separator + DIRNAME_PLUGINS;
	}
	
	/*----- Crypto -----*/
	
	private static String dir_keys;
	//private static String tempdir_decrypt;
	
	private static Map<String, byte[]> keymap;
	
	public static String getKeyDir()
	{
		if(dir_keys != null) return dir_keys;
		dir_keys = getInstallDir() + File.separator + DIRNAME_CRYPTO + File.separator + DIRNAME_CRYPTO_KEYS;
		return dir_keys;
	}
	
	public static String getDecryptTempDir()
	{
		String tempdir_decrypt = getIniValue(IKEY_DEC_DIR);
		if(tempdir_decrypt == null)
		{
			tempdir_decrypt = getInstallDir() + File.separator + DIRNAME_CRYPTO + File.separator + DIRNAME_CRYPTO_DECRYPT;
			setIniValue(IKEY_DEC_DIR, tempdir_decrypt);
		}
		return tempdir_decrypt;
	}
	
	public static boolean moveDecryptTempDir(String path) throws IOException
	{
		String olddir = getDecryptTempDir();
		setIniValue(IKEY_DEC_DIR, path);
		moveDir(getDecryptTempDir(), path);
		Collection<NTDProject> allproj = getAllProjects();
		for(NTDProject p : allproj) p.moveDecryptPath(olddir);
		return true;
	}
	
	public static String getKeyFilePath(String keyname){
		return getKeyDir() + File.separator + keyname + ".bin";
	}
	
	public static byte[] getKey(String keyname){
		//System.err.println("Requesting key: " + keyname);
		if(keymap == null) keymap = new TreeMap<String, byte[]>();
		byte[] key = keymap.get(keyname);
		if(key != null) return key;
		
		//Try to read from file
		String fpath = getKeyDir() + File.separator + keyname + ".bin";
		//System.err.println("Key not loaded. Checking " + fpath);
		if(!FileBuffer.fileExists(fpath)) return null;
		try {
			key = (new FileBuffer(fpath, true)).getBytes();
		} 
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		keymap.put(keyname, key);
		return key;
	}

	public static boolean addKey(String keyname, byte[] key)
	{
		keymap.put(keyname, key);
		
		//Save
		String kpath = getKeyDir() + File.separator + keyname + ".bin";
		try
		{
			FileOutputStream out = new FileOutputStream(kpath);
			out.write(key);
			out.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static Collection<String> getAllKeyKeys()
	{
		List<String> list = new ArrayList<String>(ALL_KEYKEYS.length);
		for(String s : ALL_KEYKEYS)list.add(s);
		
		return list;
	}
	
	public static CitrusCrypt load3DSKeystate() throws IOException{
		String key9_path = NTDProgramFiles.getKeyFilePath(NTDProgramFiles.KEYNAME_CTR_COMMON9);
		CitrusCrypt crypto = null;
		if(FileBuffer.fileExists(key9_path)){
			crypto = CitrusCrypt.loadCitrusCrypt(FileBuffer.createBuffer(key9_path));
			
			//Look for additional keys
			byte[] key = NTDProgramFiles.getKey(NTDProgramFiles.KEYNAME_CTR_CARD1);
			if(key != null) crypto.setKeyX(0x25, key);
			key = NTDProgramFiles.getKey(NTDProgramFiles.KEYNAME_CTR_CARDA);
			if(key != null) crypto.setKeyX(0x18, key);
			key = NTDProgramFiles.getKey(NTDProgramFiles.KEYNAME_CTR_CARDB);
			if(key != null) crypto.setKeyX(0x1B, key);
		}
		return crypto;
	}
	
	/*----- Default Images -----*/
	
	private static final String IMG_PATH_LOCK = "res/ndtex_icon_locked.png";
	private static final String IMG_PATH_QUESTION = "res/ndtex_icon_unknown.png";
	
	private static final String IMG_PATH_LOCK_DBG_SUFFIX = "debug" + File.separator + "ndtex_icon_locked.png";
	private static final String IMG_PATH_QUESTION_DBG_SUFFIX = "debug" + File.separator + "ndtex_icon_unknown.png";
	
	private static BufferedImage img_lock;
	private static BufferedImage img_question;
	
	private static BufferedImage loadImage(String pkg_path, String debug_path) throws IOException
	{
		//Check package path first...
		InputStream is = NTDProgramFiles.class.getResourceAsStream(pkg_path);
		
		if(is == null)
		{
			//We'll try to load it from the debug path...
			System.err.println("NTDProgramFiles.loadImage || Image load from JAR failed. Trying debug path...");
			is = new BufferedInputStream(new FileInputStream(debug_path));
		}
		
		BufferedImage myimg = ImageIO.read(is);
		is.close();
		
		return myimg;
	}
	
	public static BufferedImage getDefaultImage_unknown() throws IOException
	{
		if(img_question != null) return img_question;
		String debugpath = System.getProperty("user.home") + File.separator + IMG_PATH_QUESTION_DBG_SUFFIX;
		img_question = loadImage(IMG_PATH_QUESTION, debugpath);
		return img_question;
	}
	
	public static BufferedImage getDefaultImage_lock() throws IOException
	{
		if(img_lock != null) return img_lock;
		String debugpath = System.getProperty("user.home") + File.separator + IMG_PATH_LOCK_DBG_SUFFIX;
		img_lock = loadImage(IMG_PATH_LOCK, debugpath);
		return img_lock;
	}

	public static BufferedImage scaleDefaultImage_unknown(int w, int h) throws IOException{
		BufferedImage in = getDefaultImage_unknown();
		BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Image s = in.getScaledInstance(w, h, Image.SCALE_DEFAULT);
		out.getGraphics().drawImage(s, 0, 0, null);
		return out;
	}
	
	public static BufferedImage scaleDefaultImage_lock(int w, int h) throws IOException{
		BufferedImage in = getDefaultImage_lock();
		BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Image s = in.getScaledInstance(w, h, Image.SCALE_DEFAULT);
		out.getGraphics().drawImage(s, 0, 0, null);
		return out;
	}
	
	/*----- Type Definitions -----*/
	
	public static void registerTypeDefinitions() throws IOException
	{
		List<String> plugin_dirs = new LinkedList<String>();
		plugin_dirs.add(NTDProgramFiles.getPluginsDirectoryPath());
		
		NTDTypeLoader.registerTypes(plugin_dirs, true);
		NTDCompTypeLoader.registerTypes(plugin_dirs, true);
		NTDEncTypeLoader.registerTypes(plugin_dirs, true);
	}
	
	/*----- Init Values -----*/
	
	public static final String IKEY_INSTALL_DIR = "WAFFLEO_NTDAPPDIR";
	public static final String IKEY_UNIFONT_NAME = "UNICODE_FONT";
	public static final String IKEY_DEC_DIR = "DECRYPT_BUFFER_DIR";
	public static final String IKEY_TEMP_DIR = "NTD_TEMP_DIR";
	public static final String IKEY_LANGUAGE = "NTD_LANGUAGE";
	
	private static Map<String, String> init_values;
	private static String my_unifont;
	
	public static boolean readIni() throws IOException
	{
		init_values = new TreeMap<String, String>();
		
		String inipath = getIniPath();
		if(!FileBuffer.fileExists(inipath)) return false;
		
		BufferedReader br = new BufferedReader(new FileReader(inipath));
		String line = null;
		while((line = br.readLine()) != null)
		{
			if(line.isEmpty()) continue;
			if(line.startsWith("#")) continue;
			String[] fields = line.split("=");
			if(fields.length < 2) continue;
			init_values.put(fields[0], fields[1]);
		}
		br.close();
		registerTypeDefinitions();
		
		CompressionDefs.setCompressionTempDir(getTempDir());
		
		return true;
	}
	
	public static String getIniValue(String key)
	{
		if(init_values == null) return null;
		return init_values.get(key);
	}
	
	public static void setIniValue(String key, String value)
	{
		if(init_values == null) init_values = new HashMap<String, String>();
		init_values.put(key, value);
		//System.err.println("Ini Value Set: key = " + key + " | val = " + value);
	}
	
	public static void createIniFile(String installDir) throws IOException
	{
		String inipath = getIniPath();
		String inidir = inipath.substring(0, inipath.lastIndexOf(File.separator));
		
		if(!FileBuffer.directoryExists(inidir)) Files.createDirectories(Paths.get(inidir));
		
		//Set defaults...
		setIniValue(IKEY_INSTALL_DIR, installDir);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(inipath));
		bw.write(IKEY_INSTALL_DIR + "=" + installDir + "\n");
		bw.write(IKEY_DEC_DIR + "=" + getDecryptTempDir() + "\n");
		bw.write(IKEY_TEMP_DIR + "=" + getTempDir() + "\n");
		bw.close();
	}
	
	public static void saveIniFile() throws IOException
	{
		String inipath = getIniPath();
		List<String> keyset = new ArrayList<String>(init_values.size()+1);
		keyset.addAll(init_values.keySet());
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(inipath));
		for(String k : keyset)
		{
			String v = init_values.get(k);
			bw.write(k + "=" + v + "\n");
		}
		bw.close();
	}
	
	public static String getInstallDir()
	{
		return getIniValue(IKEY_INSTALL_DIR);
	}
	
	public static Font getUnicodeFont(int style, int size)
	{
		if(my_unifont != null) return new Font(my_unifont, style, size);
		
		//Try the key...
		String fontkey = getIniValue(IKEY_UNIFONT_NAME);
		
		if(fontkey != null) my_unifont = fontkey;
		else
		{
			//See what's on this system
			String[] flist = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
			for(String name : TRYFONTS)
			{
				if(my_unifont != null) break;
				for(String f : flist)
				{
					if(f.equalsIgnoreCase(name))
					{
						my_unifont = name;
						System.err.println("Unicode font detected: " + my_unifont);
						break;
					}
				}
			}
			setIniValue(IKEY_UNIFONT_NAME, my_unifont);
		}
		
		return new Font(my_unifont, style, size);
	}
	
	public static void saveProgramInfo() throws IOException
	{
		saveIniFile();
		saveProjectMap();
	}
	
	/*----- Temporary Files -----*/
	
	public static String getTempDir()
	{
		String value = getIniValue(IKEY_TEMP_DIR);
		if(value == null)
		{
			value = getInstallDir() + File.separator + DIRNAME_TEMP;
			setIniValue(IKEY_TEMP_DIR, value);
		}
		return value;
	}
	
	public static void clearTempDir() throws IOException
	{
		DirectoryStream<Path> dstr = Files.newDirectoryStream(Paths.get(getTempDir()));
		for(Path p : dstr)
		{
			if(FileBuffer.fileExists(p.toAbsolutePath().toString())) Files.deleteIfExists(p);
		}
		dstr.close();
	}
	
	public static void setTempDir(String path)
	{
		setIniValue(IKEY_TEMP_DIR, path);
	}
	
	public static FileBuffer openFile(FileNode node) throws IOException
	{
		FileBuffer buffer = FileBuffer.createBuffer(node.getSourcePath(), node.getOffset(), node.getOffset() + node.getLength());
		return buffer;
	}
	
	public static FileBuffer openFile(FileNode node, long stpos, long len) throws IOException
	{
		long fst = node.getOffset();
		FileBuffer buffer = FileBuffer.createBuffer(node.getSourcePath(), fst + stpos, fst + stpos + len);
		return buffer;
	}
	
	public static StreamWrapper openFileStreamer(FileNode node) throws IOException
	{
		FileBuffer buffer = FileBuffer.createBuffer(node.getSourcePath(), node.getOffset(), node.getOffset() + node.getLength());
		FileBufferStreamer streamer = new FileBufferStreamer(buffer);
		return streamer;
	}
	
	public static FileBuffer openAndDecompress(FileNode node) throws IOException
	{
		FileBuffer buffer = node.loadData();
		
		FileTypeNode typechain = node.getTypeChainHead();
		while(typechain != null)
		{
			if(typechain.isCompression())
			{
				if(typechain instanceof CompDefNode)
				{
					AbstractCompDef def = ((CompDefNode)typechain).getDefinition();
					
					String tpath = def.decompressToDiskBuffer(new FileBufferStreamer(buffer));
					buffer = FileBuffer.createBuffer(tpath);
				}
				
				typechain = typechain.getChild();
			}
			else break;
		}
		
		return buffer;
	}
	
	/*----- Install/Generate -----*/
	
	public static void installNTD(String installDir) throws IOException
	{
		createIniFile(installDir);
		
		String cryptodir = installDir + File.separator + DIRNAME_CRYPTO;
		if(!FileBuffer.directoryExists(cryptodir)) Files.createDirectories(Paths.get(cryptodir));
		
		String ndir = cryptodir + File.separator + DIRNAME_CRYPTO_KEYS;
		if(!FileBuffer.directoryExists(ndir)) Files.createDirectory(Paths.get(ndir));
		
		ndir = cryptodir + File.separator + DIRNAME_CRYPTO_DECRYPT;
		if(!FileBuffer.directoryExists(ndir)) Files.createDirectory(Paths.get(ndir));
		
		ndir = installDir + File.separator + DIRNAME_PROJECTS;
		if(!FileBuffer.directoryExists(ndir)) Files.createDirectory(Paths.get(ndir));
		
		ndir = installDir + File.separator + DIRNAME_TEMP;
		if(!FileBuffer.directoryExists(ndir)) Files.createDirectory(Paths.get(ndir));
		
		ndir = installDir + File.separator + DIRNAME_PLUGINS;
		if(!FileBuffer.directoryExists(ndir)) Files.createDirectory(Paths.get(ndir));
		
		readIni();
	}

	public static void moveInstallation(String newDir) throws IOException
	{
		String nowDir = getInstallDir();
		moveDir(nowDir, newDir);
		setIniValue(IKEY_INSTALL_DIR, newDir);
	}
	
	private static void moveDir(String src, String targ) throws IOException
	{
		if(!FileBuffer.directoryExists(targ)) Files.createDirectories(Paths.get(targ));
		
		DirectoryStream<Path> dstr = Files.newDirectoryStream(Paths.get(src));
		for(Path p : dstr)
		{
			String fpath = p.toAbsolutePath().toString();
			int lastslash = fpath.lastIndexOf(File.separator);
			String myname = fpath.substring(lastslash+1);
			String tpath = targ + File.separator + myname;
			if(FileBuffer.fileExists(fpath))
			{
				Files.deleteIfExists(p);
				Files.move(p, Paths.get(tpath));
			}
			else moveDir(fpath, tpath);
		}
		//Delete me
		Files.delete(Paths.get(src));
	}
	
	private static void deleteDir(Path dir) throws IOException
	{
		DirectoryStream<Path> dstr = Files.newDirectoryStream(dir);
		for(Path p : dstr)
		{
			if(FileBuffer.directoryExists(p.toAbsolutePath().toString())) deleteDir(p);
			else Files.deleteIfExists(p);
		}
		dstr.close();
		Files.deleteIfExists(dir);
	}
	
	public static void uninstallNTD() throws IOException
	{
		boolean read = readIni();
		if(read)
		{
			String ipath = getIniPath();
			Files.deleteIfExists(Paths.get(ipath));
			String idir = getInstallDir();
			//Delete
			if(FileBuffer.directoryExists(idir)) deleteDir(Paths.get(idir));
			init_values.clear();
			keymap.clear();
			project_map.clear();
		}
	}
	
	/*----- Load Project Info -----*/
	
	private static String project_list_path;
	private static Map<Console, Collection<NTDProject>> project_map;
	public static final int CURRENT_VERSION_PROJBIN = 8;
	
	public static String getProjectBinPath()
	{
		if(project_list_path != null) return project_list_path;
		project_list_path = getInstallDir() + File.separator + DIRNAME_PROJECTS + File.separator + PROJECTS_FILE_NAME;
		return project_list_path;
	}
	
	private static void readProjectBin() throws IOException, DataFormatException
	{
		String path = getProjectBinPath();
		project_map = new TreeMap<Console, Collection<NTDProject>>();
		
		if(!FileBuffer.fileExists(path)) return;
		FileBuffer projbin = FileBuffer.createBuffer(path, true);
		
		long cpos = 0;
		int version = projbin.intFromFile(cpos); cpos+=4;
		int bcount = projbin.intFromFile(cpos); cpos+=4;
		
		//System.err.println("--DEBUG-- NTDProgramFiles.readProjectBin || Project count: " + bcount);
		for(int i = 0; i < bcount; i++)
		{
			//System.err.println("--DEBUG-- NTDProgramFiles.readProjectBin || Reading project: " + i + " (cpos = 0x" + Long.toHexString(cpos) + ")");
			long time_import = projbin.longFromFile(cpos); cpos+=8;
			long time_modify = projbin.longFromFile(cpos); cpos+=8;
			int bsz = projbin.intFromFile(cpos); cpos+=4;
			
			NTDProject proj = NTDProject.readProject(projbin, cpos, version);
			cpos += bsz;
			proj.loadImportTime(time_import);
			proj.loadModifyTime(time_modify);
			
			Console c = proj.getConsole();
			if(c == null) proj.setConsole(Console.UNKNOWN);
			Collection<NTDProject> list = project_map.get(c);
			if(list == null)
			{
				list = new LinkedList<NTDProject>();
				project_map.put(c, list);
			}
			list.add(proj);
		}
		
	}
	
	public static Map<Console, Collection<NTDProject>> getProjectMap()
	{
		if(project_map != null) return project_map;
		try {readProjectBin();} 
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		catch(DataFormatException e){
			e.printStackTrace();
			return null;
		}
		
		return project_map;
	}
	
	public static void saveProjectMap() throws IOException
	{
		if(project_map == null) return;
		
		String path = getProjectBinPath();
		
		//Prepare queue...
		List<NTDProject> queue = new LinkedList<NTDProject>();
		Console[] consoles = {Console.PS1, Console.GAMECUBE, Console.DS, Console.DSi, Console.WII, Console._3DS,
							Console.NEW_3DS, Console.WIIU, Console.SWITCH};
		for(Console c : consoles)
		{
			Collection<NTDProject> list = project_map.get(c);
			if(list != null) queue.addAll(list);
		}
		
		FileBuffer header = new FileBuffer(8, true);
		header.addToFile(CURRENT_VERSION_PROJBIN);
		header.addToFile(queue.size());
		
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(path));
		out.write(header.getBytes());
		for(NTDProject proj : queue)
		{
			//System.err.println("Saving project: " + proj.getGameCode12());
			header = new FileBuffer(20, true);
			header.addToFile(proj.getImportTime().toEpochSecond());
			header.addToFile(proj.getModifyTime().toEpochSecond());
			
			FileBuffer serial = proj.serializeProjectBlock();
			header.addToFile((int)serial.getFileSize());
			out.write(header.getBytes());
			out.write(serial.getBytes(), 0, (int)serial.getFileSize());
		}
		out.close();
		
	}
	
	public static void addProject(NTDProject proj)
	{
		if(proj == null) return;
		Console c = proj.getConsole();
		if(c == null) c = Console.UNKNOWN;
		Collection<NTDProject> list = getProjectMap().get(c);
		if(list == null)
		{
			list = new LinkedList<NTDProject>();
			project_map.put(c, list);
		}
		list.add(proj);
		try 
		{
			proj.saveTree();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static boolean gameHasBeenLoaded(Console c, String code4, GameRegion reg)
	{
		if(c == null) return false;
		getProjectMap();
		
		Collection<NTDProject> list = project_map.get(c);
		if(list == null) return false;
		for(NTDProject p : list)
		{
			if(p.getGameCode4().equals(code4) && (reg == p.getRegion())) return true;
		}
		
		return false;
	}
	
	public static Collection<NTDProject> getAllProjects()
	{
		if(project_map == null) return new LinkedList<NTDProject>();
		List<NTDProject> list = new LinkedList<NTDProject>();
		for(Console c : project_map.keySet())
		{
			Collection<NTDProject> col = project_map.get(c);
			if(col != null) list.addAll(col);
		}
		return list;
	}
	
	public static boolean removeProject(NTDProject proj){
		Console c = proj.getConsole();
		Collection<NTDProject> pcoll = project_map.get(c);
		
		List<NTDProject> newcoll = new LinkedList<NTDProject>();
		boolean found = false;
		for(NTDProject p : pcoll){
			if(p!= proj) newcoll.add(p);
			else found = true;
		}
		
		project_map.put(c, newcoll);
		
		return found;
	}
	
}
