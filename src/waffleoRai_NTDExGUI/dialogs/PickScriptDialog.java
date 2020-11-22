package waffleoRai_NTDExGUI.dialogs;

import javax.swing.JDialog;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_NTDScriptAPI.NTDScript;

import javax.swing.JList;
import javax.swing.JTextPane;
import javax.swing.JTextArea;
import javax.swing.DefaultListModel;
import javax.swing.JButton;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.ListSelectionModel;

public class PickScriptDialog extends JDialog{

	private static final long serialVersionUID = 6275262874809795916L;
	
	public static final int WIDTH = 550;
	public static final int HEIGHT = 365;
	
	protected static final String FONTNAME = "Courier New";
	protected static final int FONTSIZE = 11;
	
	private ComponentGroup disable_group;
	
	private JList<String> lstScripts;
	private JTextPane txtArgs;
	private JTextArea txtDoc;
	
	private boolean selection;
	private Map<String, NTDScript> scriptMap;
	
	public PickScriptDialog(Frame parent){
		super(parent, true);
		setLocationRelativeTo(parent);
		
		disable_group = new ComponentGroup();
		scriptMap = new HashMap<String, NTDScript>();
		initGUI();
		
		loadList();
	}

	private void initGUI(){
		setResizable(false);
		setTitle("Select Script");
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 4.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 2.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JScrollPane spScriptList = new JScrollPane();
		spScriptList.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spScriptList = new GridBagConstraints();
		gbc_spScriptList.insets = new Insets(5, 5, 5, 5);
		gbc_spScriptList.fill = GridBagConstraints.BOTH;
		gbc_spScriptList.gridx = 0;
		gbc_spScriptList.gridy = 0;
		getContentPane().add(spScriptList, gbc_spScriptList);
		disable_group.addComponent("spScriptList", spScriptList);
		
		lstScripts = new JList<String>();
		lstScripts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		spScriptList.setViewportView(lstScripts);
		disable_group.addComponent("lstScripts", lstScripts);
		
		JScrollPane spDoc = new JScrollPane();
		spDoc.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spDoc = new GridBagConstraints();
		gbc_spDoc.gridheight = 2;
		gbc_spDoc.insets = new Insets(5, 5, 5, 5);
		gbc_spDoc.fill = GridBagConstraints.BOTH;
		gbc_spDoc.gridx = 1;
		gbc_spDoc.gridy = 0;
		getContentPane().add(spDoc, gbc_spDoc);
		disable_group.addComponent("spDoc", spDoc);
		
		txtArgs = new JTextPane();
		spDoc.setViewportView(txtArgs);
		disable_group.addComponent("txtArgs", txtArgs);
		
		JPanel pnlArgs = new JPanel();
		GridBagConstraints gbc_pnlArgs = new GridBagConstraints();
		gbc_pnlArgs.insets = new Insets(5, 5, 5, 5);
		gbc_pnlArgs.fill = GridBagConstraints.BOTH;
		gbc_pnlArgs.gridx = 0;
		gbc_pnlArgs.gridy = 1;
		getContentPane().add(pnlArgs, gbc_pnlArgs);
		GridBagLayout gbl_pnlArgs = new GridBagLayout();
		gbl_pnlArgs.columnWidths = new int[]{0, 0};
		gbl_pnlArgs.rowHeights = new int[]{0, 0, 0};
		gbl_pnlArgs.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlArgs.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		pnlArgs.setLayout(gbl_pnlArgs);
		
		JLabel lblNewLabel = new JLabel("Args:");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(5, 5, 5, 0);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		pnlArgs.add(lblNewLabel, gbc_lblNewLabel);
		
		JScrollPane spArgs = new JScrollPane();
		spArgs.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spArgs = new GridBagConstraints();
		gbc_spArgs.fill = GridBagConstraints.BOTH;
		gbc_spArgs.gridx = 0;
		gbc_spArgs.gridy = 1;
		pnlArgs.add(spArgs, gbc_spArgs);
		disable_group.addComponent("spArgs", spArgs);
		
		txtDoc = new JTextArea();
		spArgs.setViewportView(txtDoc);
		disable_group.addComponent("txtDoc", txtDoc);
		
		JPanel pnlButtons = new JPanel();
		pnlButtons.setBorder(null);
		GridBagConstraints gbc_pnlButtons = new GridBagConstraints();
		gbc_pnlButtons.insets = new Insets(5, 5, 5, 5);
		gbc_pnlButtons.gridwidth = 2;
		gbc_pnlButtons.fill = GridBagConstraints.BOTH;
		gbc_pnlButtons.gridx = 0;
		gbc_pnlButtons.gridy = 2;
		getContentPane().add(pnlButtons, gbc_pnlButtons);
		GridBagLayout gbl_pnlButtons = new GridBagLayout();
		gbl_pnlButtons.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlButtons.rowHeights = new int[]{0, 0};
		gbl_pnlButtons.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlButtons.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		pnlButtons.setLayout(gbl_pnlButtons);
		
		JButton btnRun = new JButton("Run");
		btnRun.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_btnRun = new GridBagConstraints();
		gbc_btnRun.insets = new Insets(0, 0, 0, 5);
		gbc_btnRun.gridx = 1;
		gbc_btnRun.gridy = 0;
		pnlButtons.add(btnRun, gbc_btnRun);
		disable_group.addComponent("btnRun", btnRun);
		btnRun.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				onRunSelected();
			}
			
		});
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 0;
		pnlButtons.add(btnCancel, gbc_btnCancel);
		disable_group.addComponent("btnCancel", btnCancel);
		btnCancel.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				onCancelSelected();
			}
			
		});
		
	}
	
	/*----- Getters -----*/
	
	public boolean getSelection(){
		return selection;
	}
	
	public NTDScript getSelectedScript(){
		String key = lstScripts.getSelectedValue();
		if(key == null) return null;
		return scriptMap.get(key);
	}
	
	public String[] getArgs(){
		List<String> arglist = new LinkedList<String>();
		String fullstr = txtArgs.getText();
		if(fullstr == null){
			return new String[]{""};
		}
		
		StringBuilder sb = new StringBuilder(1024);
		int slen = fullstr.length();
		int pos = 0;
		boolean inquotes = false;
		while(pos < slen){
			char c = fullstr.charAt(pos++);
			
			switch(c){
			case ' ':
				if(inquotes) sb.append(c);
				else{
					//Add to list
					arglist.add(sb.toString());
					sb = new StringBuilder(1024);
				}
				break;
			case '\"':
				if(inquotes){
					inquotes = false;
					arglist.add(sb.toString());
					sb = new StringBuilder(1024);
				}
				else inquotes = true;
				break;
			default:
				sb.append(c);
				break;
			}
			
		}
		if(sb.length() > 0) arglist.add(sb.toString());
		
		return arglist.toArray(new String[arglist.size()]);
	}
	
	/*----- Setters -----*/
	
	public void addScripts(Collection<NTDScript> scripts){
		setWait();
		for(NTDScript s : scripts){
			scriptMap.put(s.getDisplayName(), s);
		}
		loadList();
		unsetWait();
	}
	
	public void clearScripts(){
		setWait();
		scriptMap.clear();
		loadList();
		unsetWait();
	}
	
	/*----- GUI Updates -----*/
	
	private StyledDocument getEmptyDoc(){
		SimpleAttributeSet as = new SimpleAttributeSet();
		StyleConstants.setFontFamily(as, FONTNAME);
		StyleConstants.setFontSize(as, FONTSIZE);
		StyleConstants.setForeground(as, Color.black);
		StyleConstants.setBold(as, false);
		
		StyledDocument doc = new DefaultStyledDocument();
		try {
			doc.insertString(0, "[Documentation Unavailable]", as);
		} 
		catch (BadLocationException e) {
			e.printStackTrace();
		}
		
		return doc;
	}
	
	private void loadList(){
		DefaultListModel<String> mdl = new DefaultListModel<String>();
		if(scriptMap == null || scriptMap.isEmpty()){
			lstScripts.setModel(mdl);
			lstScripts.repaint();
			return;
		}
		
		List<String> klist = new LinkedList<String>();
		klist.addAll(scriptMap.keySet());
		Collections.sort(klist);
		
		for(String k : klist) mdl.addElement(k);
		lstScripts.setModel(mdl);
		lstScripts.repaint();
	}
	
	public void setWait(){
		disable_group.setEnabling(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		repaint();
	}
	
	public void unsetWait(){
		disable_group.setEnabling(true);
		setCursor(null);
		repaint();
	}
	
	public void updateDoc(){
		NTDScript script = this.getSelectedScript();
		if(script == null){
			txtDoc.setDocument(getEmptyDoc());
			repaint();
			return;
		}
		else{
			StyledDocument d = script.getUsageInfo();
			if(d != null) txtDoc.setDocument(d);
			else txtDoc.setDocument(getEmptyDoc());
			repaint();
		}
	}
	
	/*----- Actions -----*/
	
	public void onScriptSelection(){
		setWait();
		updateDoc();
		unsetWait();
	}
	
	public void onRunSelected(){
		selection = true;
		closeMe();
	}
	
	public void onCancelSelected(){
		selection = false;
		closeMe();
	}
	
	public void closeMe(){
		this.setVisible(false);
		//DOES NOT dispose!!
	}
	
}
