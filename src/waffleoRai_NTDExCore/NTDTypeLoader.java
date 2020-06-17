package waffleoRai_NTDExCore;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import waffleoRai_Files.FileDefinitions;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_Utils.FileBuffer;

public interface NTDTypeLoader {
	
	public TypeManager getTypeManager();
	public FileTypeDefinition getDefinition();
	
	public static void registerType(NTDTypeLoader loader){
		//System.err.println("--DEBUG-- NTDTypeLoader.registerType called || loader null? " + (loader == null));
		FileTypeDefinition def = loader.getDefinition();
		if(def == null) return;
		//System.err.println("--DEBUG-- NTDTypeLoader.registerType || definition is not null!");
		FileDefinitions.registerDefinition(def);
		TypeManager.registerTypeManager(def.getTypeID(), 
				def.getExtensions(), loader.getTypeManager());
	}
	
	public static boolean registerType(Class<?> loaderdef) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		if(loaderdef == null) return false;
		//Make sure it's not abstract...
		if(loaderdef.isInterface()) return false;

		Object instance = loaderdef.getConstructor().newInstance();
		if(instance instanceof NTDTypeLoader){
			registerType((NTDTypeLoader)instance);
		}
		else return false;
		
		return true;
	}
	
	public static void registerClasses(List<Class<?>> clist, boolean verbose){
		for(Class<?> c : clist){
			//We're assuming now that these are NTDTypeLoader implementations
			//So, load instance and do things.
			try {
				if(registerType(c)){
					if(verbose) System.err.println("Type Loader Registered: " + c.getName());
				}
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				System.err.println("TypeLoader " + c.getName() + " could not be instantiated!");
				e.printStackTrace();
			}
		}
	}
	
	public static List<Class<?>> loadAndCheckClasses(Map<String, URL> classes, Class<?> superclass, boolean verbose){
		
		int ucount = classes.size();
		URL[] urllist = new URL[ucount];
		int i = 0;
		for(URL u : classes.values()) urllist[i++] = u;
		
		ClassLoader cl = URLClassLoader.newInstance(urllist, NTDTypeLoader.class.getClassLoader());
		List<Class<?>> loaders = new LinkedList<Class<?>>();
		for(String cname : classes.keySet()){
			try {
				Class<?> c = cl.loadClass(cname);
				if(superclass.isAssignableFrom(c)){
					loaders.add(c);
					//if(verbose)System.err.println(superclass.getName() + " implementation found: " + c.getName());
				}
			} 
			catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
		}
		
		return loaders;
	}
	
	public static List<Class<?>> scanFileSystemDirectory(String dirpath, String prefix, Class<?> superclass, boolean verbose) throws IOException{
		//For class files
		if(verbose)System.err.println("Scanning " + dirpath);
		
		//Get dirname (for package path)
		if(dirpath.endsWith(File.separator)) dirpath = dirpath.substring(0, dirpath.length()-1);
		String dirname = null;
		int lastslash = dirpath.lastIndexOf(File.separator);
		if(lastslash >= 0) dirname = dirpath.substring(lastslash+1);
		if(dirname != null && !dirname.equals("bin")){
			if(prefix == null || prefix.isEmpty()) prefix = dirname;
			else prefix = prefix + "." + dirname;
		}
		
		List<Class<?>> passlist = new LinkedList<Class<?>>();
		Map<String, URL> urlmap = new HashMap<String, URL>();
		DirectoryStream<Path> dirstr = Files.newDirectoryStream(Paths.get(dirpath));
		for(Path p : dirstr){
			String childpath = p.toAbsolutePath().toString();
			if(FileBuffer.directoryExists(childpath)) passlist.addAll(scanFileSystemDirectory(childpath, prefix, superclass, verbose));
			else{
				//See if it's a .class or a .jar
				if(childpath.endsWith(".class")){
					URL url = p.toUri().toURL();
					//Derive proper class name
					lastslash = childpath.lastIndexOf(File.separator);
					String classname = null;
					if(prefix != null && !prefix.isEmpty()) classname = prefix + "." + childpath.substring(lastslash+1, childpath.length()-6);
					else classname = childpath.substring(lastslash+1, childpath.length()-6);
					//Add to map
					urlmap.put(classname, url);
				}
				else if(childpath.endsWith(".jar")) passlist.addAll(scanJAR(childpath, superclass, verbose));
			}
		}
		
		//Load and check all classes found (if any)
		passlist.addAll(loadAndCheckClasses(urlmap, superclass, verbose));
		//System.err.println("--DEBUG-- NTDTypeLoader.scanFileSystemDirectory || match count: " + passlist.size());
		return passlist;
		
	}
	
	public static List<Class<?>> scanJAR(String jarpath, Class<?> superclass, boolean verbose) throws IOException{
		//https://stackoverflow.com/questions/11016092/how-to-load-classes-at-runtime-from-a-folder-or-jar
		if(verbose)System.err.print("Scanning JAR: " + jarpath);
		
		JarFile myjar = new JarFile(jarpath);
		Enumeration<JarEntry> elist = myjar.entries();
		
		Map<String, URL> urlmap = new HashMap<String, URL>();
		while(elist.hasMoreElements()){
			JarEntry e = elist.nextElement();
			if(e.isDirectory()) continue; //Skip if dir
			if(!e.getName().endsWith(".class")) continue; //Skip if not class
			
			//Derive class name
			String ename = e.getName();
			String classname = ename.substring(0, ename.length()-6).replace('/', '.');
			
			//Derive URL
			URL url = new URL("jar:file:" + jarpath + "!/" + ename);
			urlmap.put(classname, url);
		}
		myjar.close();
		
		List<Class<?>> passlist = loadAndCheckClasses(urlmap, superclass, verbose);
		return passlist;
	}
	
	public static void registerTypes(List<String> plugin_dirs, boolean verbose) throws IOException{

		//Scan local JAR/tree
		URL maindir = NTDTypeLoader.class.getResource("..");
		String dirpath = maindir.getFile();
		
		String osname = System.getProperty("os.name").toLowerCase();
		if (osname.startsWith("win")){
			if(dirpath.startsWith("/")) dirpath = dirpath.substring(1);
			dirpath = dirpath.replace('/', '\\');
			dirpath = dirpath.replace("%20", " ");
		}
		if(verbose) System.err.println("Local root: " + dirpath);
		
		if(maindir.getProtocol().equals("file")){
			List<Class<?>> passlist = scanFileSystemDirectory(dirpath, "", NTDTypeLoader.class, verbose);
			registerClasses(passlist, verbose);
		}
		else if(maindir.getProtocol().equals("jar")){
			List<Class<?>> passlist = scanJAR(dirpath, NTDTypeLoader.class, verbose);
			registerClasses(passlist, verbose);
		}
		
		//Scan plugin directories for either class or JAR files
		if(plugin_dirs != null){
			if(verbose) System.err.println("Scanning plugins for type definitions...");
			for(String pdir : plugin_dirs){
				List<Class<?>> passlist = scanFileSystemDirectory(pdir, "", NTDTypeLoader.class, verbose);
				registerClasses(passlist, verbose);
			}
		}
		
	}

}
