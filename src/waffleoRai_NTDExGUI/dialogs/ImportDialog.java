package waffleoRai_NTDExGUI.dialogs;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import waffleoRai_NTDExCore.GameRegion;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.importer.CCIParserOption;
import waffleoRai_NTDExCore.importer.DSParserOption;
import waffleoRai_NTDExCore.importer.GCParserOption;
import waffleoRai_NTDExCore.importer.PSXParserOption;
import waffleoRai_NTDExCore.importer.ParserOption;
import waffleoRai_NTDExCore.importer.WUDParserOption;
import waffleoRai_NTDExCore.importer.WiiParserOption;
import waffleoRai_NTDExCore.importer.XCIParserOption;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.Font;

public class ImportDialog extends JDialog{

	private static final long serialVersionUID = 4556346183393170132L;
	
	public static final int WIDTH = 360;
	public static final int HEIGHT = 210;
	
	public static final String LAST_BROWSE_PATH_KEY = "LAST_IMPORT_BROWSE_PATH";
	
	private JTextField txtPath;
	private JComboBox<GameRegion> cmbxRegion;
	private JComboBox<ParserOption> cmbxParser;
	
	private NTDProject project;
	
	private Frame myparent;
	
	public ImportDialog(Frame parent)
	{
		super(parent, true);
		setLocationRelativeTo(parent);
		initGUI();
		populateComboboxes();
		myparent = parent;
	}
	
	private void initGUI()
	{
		setResizable(false);
		setTitle("Import ROM");
		getContentPane().setLayout(null);
		
		Dimension sz = new Dimension(WIDTH, HEIGHT);
		setMinimumSize(sz);
		setPreferredSize(sz);
		
		cmbxParser = new JComboBox<ParserOption>();
		cmbxParser.setBounds(10, 28, 328, 20);
		getContentPane().add(cmbxParser);
		
		txtPath = new JTextField();
		txtPath.setBounds(10, 124, 328, 20);
		getContentPane().add(txtPath);
		txtPath.setColumns(10);
		
		JButton btnBrowse = new JButton("Browse...");
		btnBrowse.setBounds(10, 148, 89, 23);
		getContentPane().add(btnBrowse);
		btnBrowse.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {onBrowse();}
			
		});
		
		JButton btnImport = new JButton("Import");
		btnImport.setBounds(249, 148, 89, 23);
		getContentPane().add(btnImport);
		btnImport.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {onImport(); closeMe();}
			
		});
		
		JLabel lblImageType = new JLabel("Image Type:");
		lblImageType.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblImageType.setBounds(10, 11, 71, 14);
		getContentPane().add(lblImageType);
		
		JLabel lblPath = new JLabel("Path:");
		lblPath.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblPath.setBounds(10, 108, 46, 14);
		getContentPane().add(lblPath);
		
		JLabel lblRegionifKnown = new JLabel("Region (If Known):");
		lblRegionifKnown.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblRegionifKnown.setBounds(10, 59, 99, 14);
		getContentPane().add(lblRegionifKnown);
		
		cmbxRegion = new JComboBox<GameRegion>();
		cmbxRegion.setBounds(10, 77, 328, 20);
		getContentPane().add(cmbxRegion);
	}

	private void populateComboboxes()
	{
		//Regions (easier)
		DefaultComboBoxModel<GameRegion> model = new DefaultComboBoxModel<GameRegion>();
		model.addElement(GameRegion.UNKNOWN);
		for(GameRegion r : GameRegion.values())
		{
			if(r == GameRegion.UNKNOWN) continue;
			model.addElement(r);
		}
		cmbxRegion.setModel(model);
		cmbxRegion.setSelectedIndex(0);
		
		//Parsers
		DefaultComboBoxModel<ParserOption> model2 = new DefaultComboBoxModel<ParserOption>();
		
		model2.addElement(new DSParserOption());
		model2.addElement(new PSXParserOption());
		model2.addElement(new GCParserOption());
		model2.addElement(new WiiParserOption());
		model2.addElement(new CCIParserOption());
		model2.addElement(new WUDParserOption());
		model2.addElement(new XCIParserOption());
		
		cmbxParser.setModel(model2);
		cmbxParser.setSelectedIndex(0);
	}

	public void setWait()
	{
		this.setEnabled(false);
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public void unsetWait()
	{
		this.setEnabled(true);
		this.setCursor(null);
	}
	
	public NTDProject getImport()
	{
		return project;
	}
	
	public void onBrowse()
	{
		String lastpath = NTDProgramFiles.getIniValue(LAST_BROWSE_PATH_KEY);
		
		//Get parser...
		ParserOption pop = (ParserOption)cmbxParser.getSelectedItem();
		List<FileFilter> fflist = pop.getExtFilters();
		
		JFileChooser fc = new JFileChooser(lastpath);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		for(FileFilter ff : fflist) fc.addChoosableFileFilter(ff);
		
		int retVal = fc.showOpenDialog(this);
		if (retVal == JFileChooser.APPROVE_OPTION)
		{
			File f = fc.getSelectedFile();
			String p = f.getAbsolutePath();
			
			txtPath.setText(p);
			txtPath.repaint();
			NTDProgramFiles.setIniValue(LAST_BROWSE_PATH_KEY, p);
		}
		
	}
	
	public void onImport()
	{
		setWait();
		int sidx = cmbxParser.getSelectedIndex();
		if(sidx < 0)
		{
			showWarning("Please select a ROM type!");
			return;
		}
		
		ParserOption parser = cmbxParser.getItemAt(sidx);
	
		sidx = cmbxRegion.getSelectedIndex();
		GameRegion reg = GameRegion.UNKNOWN;
		if(sidx >= 0) reg = cmbxRegion.getItemAt(sidx);
		
		if(reg == GameRegion.UNKNOWN)
		{
			showWarning("Game region is set to UNKNOWN!\n "
					+ "Parser will attempt to detect region.\n"
					+ "Please note that this doesn't currently work for all ROM types!");
		}
		
		try 
		{
			project = parser.generateProject(txtPath.getText(), reg, myparent);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			showError("Import failed! See stderr for details!");
		}
		if(project != null)
		{
			JOptionPane.showMessageDialog(this, "Import was successful!", "Import Succeeded", JOptionPane.INFORMATION_MESSAGE);
		}
		else showError("Import failed! See stderr for details!");
		unsetWait();
	}

	public void closeMe()
	{
		setVisible(false);
		WindowListener[] listeners = this.getWindowListeners();
		for(WindowListener l : listeners) l.windowClosing(new WindowEvent(this, 0));
		dispose();
	}
	
	public void showWarning(String text)
	{
		JOptionPane.showMessageDialog(this, text, "Warning", JOptionPane.WARNING_MESSAGE);
	}
	
	public void showError(String text)
	{
		JOptionPane.showMessageDialog(this, text, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
}
