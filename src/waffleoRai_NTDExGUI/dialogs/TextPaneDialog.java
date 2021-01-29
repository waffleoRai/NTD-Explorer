package waffleoRai_NTDExGUI.dialogs;

import javax.swing.JDialog;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import javax.swing.JScrollPane;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextPane;
import javax.swing.border.BevelBorder;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import javax.swing.JButton;

public class TextPaneDialog extends JDialog{

	private static final long serialVersionUID = -1229717669437245201L;
	
	private JTextPane textPane;
	
	public TextPaneDialog(Frame parent){
		super(parent, true);
		setLocationRelativeTo(parent);
		initGUI();
	}
	
	private void initGUI(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		getContentPane().add(scrollPane, gbc_scrollPane);
		
		textPane = new JTextPane();
		scrollPane.setViewportView(textPane);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(5, 5, 5, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		getContentPane().add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 90, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JButton btnOk = new JButton("OK");
		GridBagConstraints gbc_btnOk = new GridBagConstraints();
		gbc_btnOk.insets = new Insets(0, 5, 0, 0);
		gbc_btnOk.fill = GridBagConstraints.BOTH;
		gbc_btnOk.gridx = 3;
		gbc_btnOk.gridy = 0;
		panel.add(btnOk, gbc_btnOk);
		btnOk.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				closeMe();
			}
			
		});
	}
	
	public void clearTextPane(){
		textPane.setStyledDocument(new DefaultStyledDocument());
		repaint();
	}
	
	public StyledDocument getTextPaneDoc(){
		StyledDocument doc = textPane.getStyledDocument();
		if(doc == null) clearTextPane();
		return doc;
	}
	
	public void closeMe(){
		this.setVisible(false);
		this.dispose();
	}

}
