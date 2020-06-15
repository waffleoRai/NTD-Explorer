package waffleoRai_NTDExCore;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;

import waffleoRai_Files.EncryptionDefinition;
import waffleoRai_Files.EncryptionDefinitions;

public interface NTDEncTypeLoader {

	public EncryptionDefinition getDefinition();
	
	public static void registerType(NTDEncTypeLoader loader){
		EncryptionDefinition def = loader.getDefinition();
		if(def == null) return;
		EncryptionDefinitions.registerDefinition(def);
	}
	
	public static boolean registerType(Class<?> loaderdef) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		if(loaderdef == null) return false;
		//Make sure it's not abstract...
		if(loaderdef.isInterface()) return false;

		Object instance = loaderdef.getConstructor().newInstance();
		if(instance instanceof NTDEncTypeLoader){
			registerType((NTDEncTypeLoader)instance);
		}
		else return false;
		
		return true;
	}
	
	public static void registerClasses(List<Class<?>> clist, boolean verbose){
		for(Class<?> c : clist){
			try {
				if(registerType(c)){
					if(verbose) System.err.println("Encryption Type Loader Registered: " + c.getName());
				}
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				System.err.println("EncryptionTypeLoader " + c.getName() + " could not be instantiated!");
				e.printStackTrace();
			}
		}
	}
	
	public static void registerTypes(List<String> plugin_dirs, boolean verbose) throws IOException{

		//Scan local JAR/tree
		URL maindir = NTDEncTypeLoader.class.getResource("..");
		String dirpath = maindir.getFile();
		
		String osname = System.getProperty("os.name").toLowerCase();
		if (osname.startsWith("win")){
			if(dirpath.startsWith("/")) dirpath = dirpath.substring(1);
			dirpath = dirpath.replace('/', '\\');
			dirpath = dirpath.replace("%20", " ");
		}
		if(verbose) System.err.println("Local root: " + dirpath);
		
		if(maindir.getProtocol().equals("file")){
			List<Class<?>> passlist = NTDTypeLoader.scanFileSystemDirectory(dirpath, "", NTDEncTypeLoader.class, verbose);
			registerClasses(passlist, verbose);
		}
		else if(maindir.getProtocol().equals("jar")){
			List<Class<?>> passlist = NTDTypeLoader.scanJAR(dirpath, NTDEncTypeLoader.class, verbose);
			registerClasses(passlist, verbose);
		}
		
		//Scan plugin directories for either class or JAR files
		if(plugin_dirs != null){
			if(verbose) System.err.println("Scanning plugins for type definitions...");
			for(String pdir : plugin_dirs){
				List<Class<?>> passlist = NTDTypeLoader.scanFileSystemDirectory(pdir, "", NTDEncTypeLoader.class, verbose);
				registerClasses(passlist, verbose);
			}
		}
		
	}
	
	
}
