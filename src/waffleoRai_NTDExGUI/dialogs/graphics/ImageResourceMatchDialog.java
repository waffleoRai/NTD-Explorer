package waffleoRai_NTDExGUI.dialogs.graphics;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JDialog;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;

import waffleoRai_GUITools.CheckeredImagePane;
import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_Utils.FileNode;

import javax.swing.DefaultListModel;
import javax.swing.JButton;

public class ImageResourceMatchDialog extends JDialog{

	private static final long serialVersionUID = -8110432981582977095L;
	
	public static final int MIN_WIDTH = 350;
	public static final int MIN_HEIGHT = 250;
	
	//private Frame parent;
	private ImageResourceMatcher src;
	
	private ComponentGroup disableable;
	private CheckeredImagePane imgpane;
	
	private JList<FileNode> list;
	
	public ImageResourceMatchDialog(Frame parent, ImageResourceMatcher obj){
		super(parent, true);
		//this.parent = parent;
		src = obj;
		disableable = new ComponentGroup();
		
		initGUI();
		initList();
		drawImage();
	}
	
	private void initGUI(){
		
		this.setTitle(src.getDialogTitle());
		this.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		this.setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 40, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 2.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JScrollPane spList = new JScrollPane();
		spList.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spList = new GridBagConstraints();
		gbc_spList.weightx = 0.5;
		gbc_spList.insets = new Insets(0, 0, 5, 5);
		gbc_spList.fill = GridBagConstraints.BOTH;
		gbc_spList.gridx = 0;
		gbc_spList.gridy = 0;
		getContentPane().add(spList, gbc_spList);
		
		list = new JList<FileNode>();
		spList.setViewportView(list);
		disableable.addComponent("list", list);
		
		JScrollPane spImage = new JScrollPane();
		spImage.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spImage = new GridBagConstraints();
		gbc_spImage.weightx = 1.0;
		gbc_spImage.gridheight = 2;
		gbc_spImage.insets = new Insets(0, 0, 5, 0);
		gbc_spImage.fill = GridBagConstraints.BOTH;
		gbc_spImage.gridx = 1;
		gbc_spImage.gridy = 0;
		getContentPane().add(spImage, gbc_spImage);
		
		imgpane = new CheckeredImagePane();
		spImage.setViewportView(imgpane);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.weightx = 0.5;
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		getContentPane().add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JButton btnApply = new JButton("Apply");
		GridBagConstraints gbc_btnApply = new GridBagConstraints();
		gbc_btnApply.insets = new Insets(5, 5, 5, 5);
		gbc_btnApply.gridx = 1;
		gbc_btnApply.gridy = 0;
		panel.add(btnApply, gbc_btnApply);
		disableable.addComponent("btnApply", btnApply);
		btnApply.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {onApply();}
			
		});
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(5, 5, 5, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 0;
		panel.add(btnCancel, gbc_btnCancel);
		disableable.addComponent("btnCancel", btnCancel);
		btnCancel.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {onCancel();}
			
		});
		
	}
	
	private void initList(){
		List<FileNode> rlist = src.getResourceList();
		if(rlist == null) return;
		
		DefaultListModel<FileNode> model = new DefaultListModel<FileNode>();
		for(FileNode fn : rlist){
			model.addElement(fn);
		}
		
		list.setModel(model);
		list.setSelectedIndex(0);
		list.repaint();
	}
	
	private void drawImage(){		
		setWait();
		
		JDialog me = this;
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>(){

			protected Void doInBackground() throws Exception {
				try{
					imgpane.clearItems();
					FileNode select = list.getSelectedValue();
					src.drawSelected(select, imgpane);
					imgpane.repaint();
				}
				catch(Exception x){
					x.printStackTrace();
					JOptionPane.showMessageDialog(me, "Draw operation failed!", "Error", JOptionPane.ERROR_MESSAGE);
				}
				return null;
			}
			
			public void done(){
				unsetWait();
			}
		};
		
		task.execute();
		
	}
	
	public void setWait(){
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		disableable.setEnabling(false);
	}
	
	public void unsetWait(){
		setCursor(null);
		disableable.setEnabling(true);
	}

	public void onApply(){
		setWait();
		
		JDialog me = this;
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>(){

			protected Void doInBackground() throws Exception {
				try{src.applySelected(list.getSelectedValue());}
				catch(Exception x){
					x.printStackTrace();
					JOptionPane.showMessageDialog(me, "Apply operation failed!", "Error", JOptionPane.ERROR_MESSAGE);
				}
				return null;
			}
			
			public void done(){
				closeMe();
			}
		};
		
		task.execute();
		
	}
	
	public void onCancel(){
		closeMe();
	}
	
	public void closeMe(){
		this.setVisible(false);
		this.dispose();
	}
	
}
