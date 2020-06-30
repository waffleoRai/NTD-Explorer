package waffleoRai_NTDExGUI.dialogs;

import javax.swing.JDialog;
import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;
import javax.swing.SwingConstants;

import waffleoRai_NTDExCore.DefoLanguage;
import waffleoRai_NTDExCore.GameRegion;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;

import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;

public class BannerEditForm extends JDialog{

	private static final long serialVersionUID = 6447934741978815049L;
	
	public static final int WIDTH = 440;
	public static final int HEIGHT = 300;
	
	private boolean accepted;
	
	private JTextField txtLine1;
	private JTextField txtLine2;
	private JTextField txtLine3;
	private JTextField txtPublisher;
	
	private JComboBox<GameRegion> cmbxReg;
	private JComboBox<DefoLanguage> cmbxLan;
	
	public BannerEditForm(Frame parent){
		super(parent, true);
		setLocationRelativeTo(parent);
		initGUI();
	}
	
	private void initGUI(){
		setResizable(false);
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		setTitle("Edit Project Info");
		getContentPane().setLayout(null);
		
		JLabel lblBannerTitle = new JLabel("Banner Title");
		lblBannerTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblBannerTitle.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblBannerTitle.setBounds(176, 11, 80, 14);
		getContentPane().add(lblBannerTitle);
		
		txtLine1 = new JTextField();
		txtLine1.setFont(NTDProgramFiles.getUnicodeFont(Font.PLAIN, 11));
		txtLine1.setBounds(10, 35, 414, 20);
		getContentPane().add(txtLine1);
		txtLine1.setColumns(10);
		
		txtLine2 = new JTextField();
		txtLine2.setFont(NTDProgramFiles.getUnicodeFont(Font.PLAIN, 11));
		txtLine2.setBounds(10, 60, 414, 20);
		getContentPane().add(txtLine2);
		txtLine2.setColumns(10);
		
		txtLine3 = new JTextField();
		txtLine3.setFont(NTDProgramFiles.getUnicodeFont(Font.PLAIN, 11));
		txtLine3.setBounds(10, 85, 414, 20);
		getContentPane().add(txtLine3);
		txtLine3.setColumns(10);
		
		JLabel lblPublisher = new JLabel("Publisher");
		lblPublisher.setHorizontalAlignment(SwingConstants.CENTER);
		lblPublisher.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblPublisher.setBounds(176, 116, 80, 14);
		getContentPane().add(lblPublisher);
		
		txtPublisher = new JTextField();
		txtPublisher.setFont(NTDProgramFiles.getUnicodeFont(Font.PLAIN, 11));
		txtPublisher.setColumns(10);
		txtPublisher.setBounds(10, 136, 414, 20);
		getContentPane().add(txtPublisher);
		
		cmbxReg = new JComboBox<GameRegion>();
		cmbxReg.setBounds(90, 169, 334, 20);
		getContentPane().add(cmbxReg);
		
		JLabel lblRegion = new JLabel("Region");
		lblRegion.setHorizontalAlignment(SwingConstants.CENTER);
		lblRegion.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblRegion.setBounds(10, 171, 72, 14);
		getContentPane().add(lblRegion);
		
		JLabel lblLanguage = new JLabel("Language");
		lblLanguage.setHorizontalAlignment(SwingConstants.CENTER);
		lblLanguage.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblLanguage.setBounds(10, 199, 72, 14);
		getContentPane().add(lblLanguage);
		
		cmbxLan = new JComboBox<DefoLanguage>();
		cmbxLan.setBounds(90, 197, 334, 20);
		getContentPane().add(cmbxLan);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setBounds(335, 237, 89, 23);
		getContentPane().add(btnCancel);
		btnCancel.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				accepted = false;
				closeMe();
			}
			
		});
		
		JButton btnOk = new JButton("OK");
		btnOk.setBounds(241, 237, 89, 23);
		getContentPane().add(btnOk);
		btnOk.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				accepted = true;
				closeMe();
			}
			
		});

		loadComboBoxes();
	}
	
	private void loadComboBoxes(){
		DefaultComboBoxModel<GameRegion> m1 = new DefaultComboBoxModel<GameRegion>();
		GameRegion[] all = GameRegion.values();
		for(GameRegion r : all) m1.addElement(r);
		cmbxReg.setModel(m1);
		cmbxReg.repaint();
		
		DefaultComboBoxModel<DefoLanguage> m2 = new DefaultComboBoxModel<DefoLanguage>();
		DefoLanguage[] alllan = DefoLanguage.values();
		for(DefoLanguage l : alllan) m2.addElement(l);
		cmbxLan.setModel(m2);
		cmbxLan.repaint();
	}
	
	public void loadProjectInfo(NTDProject proj){
		String btitle = proj.getBannerTitle();
		if(btitle != null && !btitle.isEmpty()){
			String[] lines = btitle.split("\n");
			
			txtLine1.setText(lines[0]); txtLine1.repaint();
			
			if(lines.length >= 2){
				txtLine2.setText(lines[1]); txtLine2.repaint();
			}
			if(lines.length >= 3){
				txtLine3.setText(lines[2]); txtLine3.repaint();
			}	
		}
		
		String pub = proj.getPublisherTag();
		if(pub != null && !pub.isEmpty()){
			txtPublisher.setText(pub);
			txtPublisher.repaint();
		}
		cmbxReg.setSelectedItem(proj.getRegion());
		cmbxLan.setSelectedItem(proj.getDefoLanguage());
		
	}
	
	public boolean selectionApproved(){return accepted;}
	
	public String getBannerTitle(){
		String line1 = txtLine1.getText();
		String line2 = txtLine2.getText();
		String line3 = txtLine3.getText();
		
		String out = "";
		if(line1 != null && !line1.isEmpty()) out += line1;
		if(line2 != null && !line2.isEmpty()) out += "\n" + line2;
		if(line3 != null && !line3.isEmpty()) out += "\n" + line3;
		
		return out;
	}
	
	public String getPublisherName(){
		return txtPublisher.getText();
	}
	
	public GameRegion getSelectedRegion(){
		return cmbxReg.getItemAt(cmbxReg.getSelectedIndex());
	}
	
	public DefoLanguage getSelectedLanguage(){
		return cmbxLan.getItemAt(cmbxLan.getSelectedIndex());
	}
	
	public void closeMe(){
		this.setVisible(false);
	}
	
}
