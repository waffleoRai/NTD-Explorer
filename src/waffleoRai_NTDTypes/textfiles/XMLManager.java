package waffleoRai_NTDTypes.textfiles;

import java.awt.Color;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import waffleoRai_Files.Converter;
import waffleoRai_Files.FileBufferInputStream;
import waffleoRai_Files.FileClass;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDTypeLoader;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ViewHex;
import waffleoRai_NTDExGUI.panels.preview.WriterPanel;
import waffleoRai_Utils.FileBuffer;

public class XMLManager extends TypeManager{
	
	
	//Definition
	public static final int DEF_ID = 0x3f786d6c;
	public static final String DEFO_ENG_NAME = "eXtended Markup Language Document";
	
	private static XMLFileDefinition stat_def;
	
	public static class XMLFileDefinition implements FileTypeDefinition{

		private String str = DEFO_ENG_NAME;
		
		public Collection<String> getExtensions() {
			List<String> list = new ArrayList<String>(1);
			list.add("xml");
			return list;
		}

		public String getDescription() {return str;}
		public FileClass getFileClass() {return FileClass.XML;}
		public int getTypeID() {return DEF_ID;}
		public void setDescriptionString(String s) {str = s;}
		public String getDefaultExtension() {return "xml";}
		public String toString(){return FileTypeDefinition.stringMe(this);}
		
	}

	public static XMLFileDefinition getDefinition(){
		if(stat_def == null) stat_def = new XMLFileDefinition();
		return stat_def;
	}
	
	//Loader
	public static class XMLFileDefLoader implements NTDTypeLoader{

		public TypeManager getTypeManager() {return new XMLManager();}
		public FileTypeDefinition getDefinition() {return XMLManager.getDefinition();}
			
	}
	
	//Panel Formatting
	
	protected static final int ATTRIDX_DEFO = 0;
	protected static final int ATTRIDX_TAG = 1;
	protected static final int ATTRIDX_KEY = 2;
	protected static final int ATTRIDX_KEYVAL = 3;
	protected static final int ATTRIDX_TAGVAL = 4;
	
	protected static final String FONTNAME = "Courier New";
	protected static final int FONTSIZE = 11;
	
	protected static final int BUFFSIZE = 1024;
	protected static final int TNAMEBUFFSIZE = 256;
	
	private static SimpleAttributeSet[] generateAttributes(){
		//NP++ color scheme
		SimpleAttributeSet[] attrs = new SimpleAttributeSet[5];
		
		//Main font
		SimpleAttributeSet as = new SimpleAttributeSet();
		StyleConstants.setFontFamily(as, FONTNAME);
		StyleConstants.setFontSize(as, FONTSIZE);
		StyleConstants.setForeground(as, Color.black);
		StyleConstants.setBold(as, false);
		attrs[ATTRIDX_DEFO] = as;
		
		//Inside tag brackets
		as = new SimpleAttributeSet();
		StyleConstants.setFontFamily(as, FONTNAME);
		StyleConstants.setFontSize(as, FONTSIZE);
		StyleConstants.setForeground(as, Color.blue);
		StyleConstants.setBold(as, false);
		attrs[ATTRIDX_TAG] = as;
		
		//Key in tag
		as = new SimpleAttributeSet();
		StyleConstants.setFontFamily(as, FONTNAME);
		StyleConstants.setFontSize(as, FONTSIZE);
		StyleConstants.setForeground(as, Color.red);
		StyleConstants.setBold(as, false);
		attrs[ATTRIDX_KEY] = as;
		
		//Key value in tag
		as = new SimpleAttributeSet();
		StyleConstants.setFontFamily(as, FONTNAME);
		StyleConstants.setFontSize(as, FONTSIZE);
		StyleConstants.setForeground(as, Color.magenta);
		StyleConstants.setBold(as, true);
		attrs[ATTRIDX_KEYVAL] = as;
		
		//Value
		as = new SimpleAttributeSet();
		StyleConstants.setFontFamily(as, FONTNAME);
		StyleConstants.setFontSize(as, FONTSIZE);
		StyleConstants.setForeground(as, Color.black);
		StyleConstants.setBold(as, true);
		attrs[ATTRIDX_TAGVAL] = as;
		
		return attrs;
	}
	
	private static void formatXMLDisplay(BufferedReader in, StyledDocument out) throws IOException, BadLocationException{
		//Do line by line mostly for an even splitting unit...
		boolean intag = false; //Ends with >, starts with <
		boolean inkey = false; //Ends with =, starts with space
		boolean inkval = false; //Ends with ", starts with ="
		//boolean intval = false; //Ends with </, starts with >
		boolean tagend = false; //If true, then it's the end marker with the </
		SimpleAttributeSet currentas = null;
		
		SimpleAttributeSet[] attrs = generateAttributes();
		currentas = attrs[ATTRIDX_DEFO];
		
		LinkedList<String> tagstack = new LinkedList<String>();
		StringBuilder tagbuff = null;
		
		String line = null;
		StringBuilder sb = null;
		char lastchar = '\0';
		while((line = in.readLine()) != null){
			int llen = line.length();
			//if(sb == null) sb = new StringBuilder(1024);
			
			for(int ci = 0; ci < llen; ci++){
				char ch = line.charAt(ci);
				
				switch(ch){
				case '<':
					//Tag start. Overrides other flags.
					if(sb != null) out.insertString(out.getLength(), sb.toString(), currentas);
					intag = true;
					currentas = attrs[ATTRIDX_TAG];
					sb = new StringBuilder(BUFFSIZE);
					tagbuff = new StringBuilder(TNAMEBUFFSIZE);
					sb.append(ch);
					break;
				case '>':
					//If in tag, tag end. Otherwise just append?
					sb.append(ch);
					if(intag){
						out.insertString(out.getLength(), sb.toString(), currentas);
						sb = new StringBuilder(BUFFSIZE);
						//how to choose new style? Need to know if start or end tag...
						//Also don't forget to push or pop stack
						if(tagend){
							tagstack.pop();
							tagbuff = null;
							tagend = false;
							if(tagstack.isEmpty()){
								//Not inside any tags right now
								currentas = attrs[ATTRIDX_DEFO];
							}
							else{
								//Assumed still inside a tag pair
								currentas = attrs[ATTRIDX_TAGVAL];
							}
						}
						else{
							//It's a start tag. Push.
							String s = tagbuff.toString();
							tagbuff = null;
							tagstack.push(s);
							currentas = attrs[ATTRIDX_TAGVAL];
						}
						intag = false;
					}
					break;
				case '/':
					//Used to determine if end tag?
					sb.append(ch);
					if(lastchar == '<'){
						//End tag
						tagend = true;
					}
					break;
				case ' ':
					//If in tag, new key/val pair?
					//Otherwise, just append
					sb.append(ch);
					if(intag){
						if(!inkval){
							inkey = true;
							out.insertString(out.getLength(), sb.toString(), currentas);
							sb = new StringBuilder(BUFFSIZE);
							currentas = attrs[ATTRIDX_KEY];
						}
					}
					break;
				case '\"':
					//If in tag, start/end of value
					//Otherwise, just append
					if(intag){
						if(inkval){
							//End
							sb.append(ch);
							out.insertString(out.getLength(), sb.toString(), currentas);
							sb = new StringBuilder(BUFFSIZE);
							currentas = attrs[ATTRIDX_TAG];
							inkval = false;
						}
						else{
							//Start
							out.insertString(out.getLength(), sb.toString(), currentas);
							sb = new StringBuilder(BUFFSIZE);
							currentas = attrs[ATTRIDX_KEYVAL];
							sb.append(ch);
							inkval = true;
						}
					}
					else sb.append(ch);
					break;
				case '=':
					if(inkey){
						inkey = false;
						out.insertString(out.getLength(), sb.toString(), currentas);
						sb = new StringBuilder(BUFFSIZE);
						currentas = attrs[ATTRIDX_DEFO];
						sb.append(ch);
					}
					else sb.append(ch);
					break;
				default: //Just put
					if(sb == null) sb = new StringBuilder(BUFFSIZE);
					sb.append(ch);
					break;
				}
				lastchar = ch;
			}
			//Append a newline...
			sb.append('\n');
		}
		
		//If there's anything unwritten, finish up.
		if(sb != null && sb.length() > 0) out.insertString(out.getLength(), sb.toString(), currentas);
		
	}
	
	//Manager
	
	public FileTypeNode detectFileType(FileNode node) {
		//Check the beginning for "<?xml"
		
		try{
			FileBuffer dat = null;
			if(node.hasCompression()) dat = node.loadDecompressedData();
			else{
				long end = node.getLength();
				if(end > 0x10) end = 0x10;
				dat = node.loadData(0, end);
			}
			
			long cend = 0x10;
			if(dat.getFileSize() < cend) cend = dat.getFileSize();
			long spos = dat.findString(0, cend, "<?xml");
			if(spos >= 0 && spos < 0x10) return new FileTypeDefNode(getDefinition());
		}
		catch(Exception x){
			x.printStackTrace();
			return null;
		}
		
		return null;
	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {

		try{
			WriterPanel pnl = new WriterPanel();
			StyledDocument doc = pnl.getDocument();
			FileBufferInputStream is = new FileBufferInputStream(node.loadDecompressedData());
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"));
			formatXMLDisplay(reader, doc);
			reader.close();
			
			return pnl;
		}
		catch(IOException x){
			x.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, "I/O Error: File could not be loaded for preview!", 
					"Preview Failed", JOptionPane.ERROR_MESSAGE);
		}
		catch(BadLocationException x){
			x.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, "Rendering Error: File preview could not be rendered.", 
					"Preview Failed", JOptionPane.ERROR_MESSAGE);
		}
		
		return null;
	}

	public List<FileAction> getFileActions() {
		//Extract, View Hex
		List<FileAction> list = new ArrayList<FileAction>(2);
		list.add(FA_ExtractFile.getAction());
		list.add(FA_ViewHex.getAction());
		return list;
	}

	public Converter getStandardConverter() {return null;}
	public boolean isOfType(FileNode node) {return (detectFileType(node) != null);}
	

}
