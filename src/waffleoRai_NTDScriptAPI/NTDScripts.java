package waffleoRai_NTDScriptAPI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.StyledDocument;

import waffleoRai_Files.tree.FileNode;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.NTDTypeLoader;
import waffleoRai_NTDExGUI.ExplorerForm;
import waffleoRai_NTDExGUI.dialogs.PickScriptDialog;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;

public class NTDScripts {

	public static final String INIKEY_LAST_LASTSCRIPTPATH = "LAST_SCRIPT_PATH";
	
	private static ExplorerForm active_gui;
	
	public static void setActiveForm(ExplorerForm form){
		active_gui = form;
	}
	
	public static ExplorerForm getActiveForm(){
		return active_gui;
	}
	
	public static NTDProject getCurrentProject(){
		//Check active form.
		if(active_gui == null) return null;
		return active_gui.getLoadedProject();
	}
	
	public static FileNode getSelectedNode(){
		//Check active form
		if(active_gui == null) return null;
		return active_gui.getSelectedNode();
	}
	
	public static NTDScript loadFromClass(Class<?> myclass, boolean verbose){

		try {
			if(myclass == null) return null;
			//Make sure it's not abstract...
			if(myclass.isInterface()) return null;

			Object instance = myclass.getConstructor().newInstance();
			if(instance instanceof NTDTypeLoader){
				if(verbose) System.err.println("Script Class Found: " + myclass.getName());
				return ((NTDScript)instance);
			}
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			System.err.println("NTDScript Class " + myclass.getName() + " could not be instantiated!");
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<NTDScript> loadScriptsFrom(String path) throws IOException{
		List<NTDScript> list = new LinkedList<NTDScript>();
		if(path == null) return list;
		
		if(path.endsWith(".jar")){
			List<Class<?>> clist = NTDTypeLoader.scanJAR(path, NTDScript.class, true);
			if(clist != null){
				for(Class<?> c : clist){
					if(NTDScript.class.isAssignableFrom(c)){
						NTDScript s = loadFromClass(c, true);
						if(s != null) list.add(s);
					}
				}	
			}
		}
		else if(path.endsWith(".class")){
			URL url = Paths.get(path).toUri().toURL();
			int lastslash = path.lastIndexOf(File.separator);
			String classname = path.substring(lastslash+1, path.length()-6);
			ClassLoader cl = URLClassLoader.newInstance(new URL[]{url}, NTDScript.class.getClassLoader());
			try {
				Class<?> c = cl.loadClass(classname);
				if(NTDScript.class.isAssignableFrom(c)){
					NTDScript s = loadFromClass(c, true);
					if(s != null) list.add(s);
				}
			} 
			catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		return list;
	}
	
	public static NTDScript gui_run_script(){
		if(active_gui == null) return null;
		
		//Choose file path
		JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(INIKEY_LAST_LASTSCRIPTPATH));
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.addChoosableFileFilter(new FileFilter(){

			public boolean accept(File f) {
				if(f.isDirectory()) return true;
				return f.getAbsolutePath().endsWith(".class");
			}

			public String getDescription() {
				return "Java Compiled Class (.class)";
			}});
		
		fc.addChoosableFileFilter(new FileFilter(){

			public boolean accept(File f) {
				if(f.isDirectory()) return true;
				return f.getAbsolutePath().endsWith(".jar");
			}

			public String getDescription() {
				return "Java Archive (.jar)";
			}});
		int op = fc.showOpenDialog(getActiveForm());
		
		if(op != JFileChooser.APPROVE_OPTION) return null;
		File f = fc.getSelectedFile();
		NTDProgramFiles.setIniValue(INIKEY_LAST_LASTSCRIPTPATH, f.getAbsolutePath());
		
		//Check for scripts at path
		try{
			List<NTDScript> scripts = loadScriptsFrom(f.getAbsolutePath());	
			if(scripts == null || scripts.isEmpty()){
				JOptionPane.showMessageDialog(active_gui, "No compatible NTDScript classes were found in the provided file!", 
						"No Scripts Found", JOptionPane.WARNING_MESSAGE);
				return null;
			}
			
			//Script select dialog
			PickScriptDialog dialog = new PickScriptDialog(active_gui);
			dialog.addScripts(scripts);
			dialog.setVisible(true);
			
			//Dispose of dialog
			if(!dialog.getSelection()){dialog.dispose(); return null;}
			NTDScript script = dialog.getSelectedScript();
			String[] args = dialog.getArgs();
			dialog.dispose();
			
			//Double check script
			if(script == null){
				JOptionPane.showMessageDialog(active_gui, "Selected script is null! No script executed.", 
						"Null Script", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			op = JOptionPane.showConfirmDialog(active_gui, "Run the following script: " + script.getDisplayName() + "?", 
					"Confirm Script", JOptionPane.YES_NO_OPTION);
			
			//Run Script...
			if(op != JOptionPane.YES_OPTION) return null;
			IndefProgressDialog pd = new IndefProgressDialog(active_gui, "Executing Script");
			pd.setPrimaryString("Initializing");
			pd.setSecondaryString("Preparing to run " + script.getDisplayName());
			
			SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
			{
				
				private int result = -1;

				protected Void doInBackground() throws Exception 
				{
					try
					{
						//Load project
						pd.setPrimaryString("Executing");
						pd.setSecondaryString("Running " + script.getDisplayName());
						
						result = script.run(args, pd);
						
					}
					catch (Exception e)
					{
						e.printStackTrace();
						JOptionPane.showMessageDialog(active_gui, "An exception was thrown during script execution.\n"
								+ "See stderr for details.", 
								"Exception Caught", JOptionPane.ERROR_MESSAGE);
					}
					return null;
				}
				
				public void done(){
					dialog.closeMe();
					if(result == 0){
						JOptionPane.showMessageDialog(active_gui, "Script completed successfully with return value 0.", 
								"Script Complete", JOptionPane.INFORMATION_MESSAGE);
					}
					else{
						JOptionPane.showMessageDialog(active_gui, "Script aborted with return value " + result + ".", 
								"Script Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				
			};
			task.execute();
			pd.render();
			
			return script;
		}
		catch(Exception x){
			x.printStackTrace();
			JOptionPane.showMessageDialog(active_gui, "No compatible NTDScript classes were found in the provided file!", 
					"No Scripts Found", JOptionPane.WARNING_MESSAGE);
			return null;
		}

	}
	
	public static StyledDocument parseMarkdownDocument(InputStream indoc){
		//TODO
		return null;
	}
	
}
