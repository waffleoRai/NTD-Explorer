package waffleoRai_NTDScriptAPI;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;

import waffleoRai_Files.tree.FileNode;
import waffleoRai_GUITools.MarkdownReader;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExGUI.ExplorerForm;
import waffleoRai_NTDExGUI.dialogs.PickScriptDialog;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_Reflection.ReflectionUtils;

public class NTDScripts {

	public static final String INIKEY_LAST_LASTSCRIPTPATH = "LAST_SCRIPT_PATH";
	
	private static ExplorerForm active_gui;
	private static List<Class<?>> class_list; //available scripts
	
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
			//if(myclass.isInterface()) return null;
			Object instance = myclass.getConstructor().newInstance();
			if(instance instanceof NTDScript){
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
	
	public static void loadScriptsDirectory(String scripts_dir) throws IOException{
		class_list = new LinkedList<Class<?>>();
		//ReflectionUtils.loadClassesFromDir(scripts_dir, NTDScripts.class);
		ReflectionUtils.loadClassesFromDir(scripts_dir);
		class_list.addAll(ReflectionUtils.findSubclassesOf(NTDScript.class, false));
		
		//Debug
		/*for(Class<?> c : class_list){
			System.err.println("Added to class list: " + c.getName());
		}*/
	}
	
	private static List<NTDScript> instantiateScripts(){
		List<NTDScript> slist = new LinkedList<NTDScript>();
		if(class_list == null) return slist;
		
		for(Class<?> c : class_list){
			NTDScript s = loadFromClass(c, false);
			if(s != null) slist.add(s);
		}
		
		return slist;
	}
	
	public static NTDScript gui_run_script(){
		if(active_gui == null) return null;
		
		//Choose file path
		/*JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(INIKEY_LAST_LASTSCRIPTPATH));
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
		NTDProgramFiles.setIniValue(INIKEY_LAST_LASTSCRIPTPATH, f.getAbsolutePath());*/
		
		//Check for scripts at path
		try{
			List<NTDScript> scripts = instantiateScripts();	
			if(scripts == null || scripts.isEmpty()){
				JOptionPane.showMessageDialog(active_gui, "No compatible NTDScript classes were found!", 
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
			int op = JOptionPane.showConfirmDialog(active_gui, "Run the following script: " + script.getDisplayName() + "?", 
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
					pd.closeMe();
					if(result == 0){
						JOptionPane.showMessageDialog(active_gui, "Script completed successfully with return value 0.", 
								"Script Complete", JOptionPane.INFORMATION_MESSAGE);
					}
					else{
						JOptionPane.showMessageDialog(active_gui, "Script aborted with return value " + result + ".\n"
								+ "Error Message: " + script.getErrorMessage(), 
								"Script Error", JOptionPane.ERROR_MESSAGE);
					}
					pd.dispose();
				}
				
			};
			task.execute();
			pd.render();
			
			return script;
		}
		catch(Exception x){
			x.printStackTrace();
			JOptionPane.showMessageDialog(active_gui, "No compatible NTDScript classes were found!", 
					"No Scripts Found", JOptionPane.WARNING_MESSAGE);
			return null;
		}

	}
	
	public static StyledDocument parseMarkdownDocument(InputStream indoc) throws IOException{
		DefaultStyledDocument sdoc = new DefaultStyledDocument();
		MarkdownReader.parseStream(indoc, sdoc);
		return sdoc;
	}
	
}
