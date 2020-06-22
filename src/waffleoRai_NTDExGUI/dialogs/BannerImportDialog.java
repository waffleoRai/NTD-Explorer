package waffleoRai_NTDExGUI.dialogs;

import javax.swing.JDialog;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.memcard.BannerImporter.BannerStruct;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;

import java.awt.FlowLayout;
import java.awt.Font;

public class BannerImportDialog extends JDialog{
	
	private static final long serialVersionUID = 7850904122752257173L;
	
	public static final int MIN_WIDTH = 150;
	public static final int MIN_HEIGHT = 200;
	
	private boolean approved = false;
	private int selected_idx;
	
	private int iWidth;
	private int iHeight;
	
	private JLabel lblTitle;
	private JPanel pnlIcons;
	
	private BannerStruct[] banners;
	private BannerButton[] buttons;
	
 	public BannerImportDialog(Frame parent){
		super(parent, true);
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
		iWidth = 64;
		iHeight = 64;
		
		initGUI();
	}

	private void initGUI(){
		setTitle("Select Banner");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		getContentPane().add(scrollPane, gbc_scrollPane);
		
		pnlIcons = new JPanel();
		pnlIcons.setBackground(Color.WHITE);
		scrollPane.setViewportView(pnlIcons);
		pnlIcons.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		lblTitle = new JLabel("Selected: [None]");
		lblTitle.setFont(NTDProgramFiles.getUnicodeFont(Font.PLAIN, 11));
		GridBagConstraints gbc_lblTitle = new GridBagConstraints();
		gbc_lblTitle.anchor = GridBagConstraints.WEST;
		gbc_lblTitle.insets = new Insets(5, 15, 5, 5);
		gbc_lblTitle.gridx = 0;
		gbc_lblTitle.gridy = 1;
		getContentPane().add(lblTitle, gbc_lblTitle);
		
		JButton btnApply = new JButton("Apply");
		GridBagConstraints gbc_btnApply = new GridBagConstraints();
		gbc_btnApply.insets = new Insets(5, 5, 5, 5);
		gbc_btnApply.gridx = 1;
		gbc_btnApply.gridy = 2;
		getContentPane().add(btnApply, gbc_btnApply);
		btnApply.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onApply();
			}
		});
		//btnApply.setEnabled(false);
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(5, 5, 5, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 2;
		getContentPane().add(btnCancel, gbc_btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
	}
	
	public void loadBannerOptions(Collection<BannerStruct> bannerlist){
		//selected = null;
		selected_idx = 0;
		
		if(buttons != null){
			for(BannerButton b : buttons)b.stopAnimationTimer();
		}
		
		int count = bannerlist.size();
		if(count == 0){
			banners = null;
			buttons = null;
		}
		else{
			banners = new BannerStruct[count];
			buttons = new BannerButton[count];
			
			int i = 0;
			for(BannerStruct b : bannerlist){
				banners[i] = b;
				buttons[i] = new BannerButton(b.icon, b.framemillis, iWidth, iHeight);
				int idx = i;
				buttons[i].startAnimationTimer();
				buttons[i].addActionListener(new ActionListener(){

					public void actionPerformed(ActionEvent e) {
						onIconSelected(idx);
					}
					
				});
				i++;
			}
		}
		
		refreshGUI();
	}
	
	public void setIconButtonSize(int w, int h){
		iWidth = w;
		iHeight = h;
		refreshGUI();
	}
	
	public void refreshGUI(){
		pnlIcons.removeAll();
		
		if(buttons != null){
			for(BannerButton b : buttons)pnlIcons.add(b);
			BannerStruct b = banners[selected_idx];
			lblTitle.setText("Selected: " + b.title);
		}
		else{
			lblTitle.setText("Selected: (None)");
		}
		
		lblTitle.repaint();
		pnlIcons.repaint();
	}
	
	private void onIconSelected(int idx){
		//Update selected index
		//Deselect those not selected.
		if(buttons == null) return;
		if(idx < 0 || idx >= buttons.length) return;
		
		selected_idx = idx;
		for(int i = 0; i < buttons.length; i++){
			if(i == idx) buttons[i].setSelected(true);
			else buttons[i].setSelected(false);
		}
		
		BannerStruct b = banners[selected_idx];
		lblTitle.setText("Selected: " + b.title);
		lblTitle.repaint();
		
		//refreshGUI();
	}
	
	public void onApply(){
		approved = true;
		closeMe();
	}
	
	public void onCancel(){
		approved = false;
		closeMe();
	}
	
	public boolean selectionApproved(){
		return approved;
	}
	
	public BannerStruct getSelected(){
		return banners[selected_idx];
	}
	
	public void closeMe(){
		if(buttons != null){
			for(BannerButton b : buttons)b.stopAnimationTimer();
			buttons = null;
		}
		
		this.setVisible(false);
		this.dispose();
	}
	
}
