package waffleoRai_NTDExGUI.panels.preview;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JScrollPane;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.IOException;
import java.io.Writer;

import javax.swing.JTextPane;

import java.awt.Color;
import java.awt.Font;
import javax.swing.border.BevelBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class WriterPanel extends JPanel{
	
	/*----- Constants -----*/
	
	private static final long serialVersionUID = -7621394337907525073L;
	
	/*----- Instance Variables -----*/
	
	private JScrollPane scrollPane;
	private JTextPane textPane;
	
	private StyledDocument doc;
	private SimpleAttributeSet defo_attr;
	
	/*----- Subclasses -----*/
	
	public class PanelWriter extends Writer{

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			String s = String.valueOf(cbuf, off, len);
			try {
				doc.insertString(doc.getLength(), s, defo_attr);
			} catch (BadLocationException e) {
				e.printStackTrace();
				throw new IOException("Document: Bad location");
			}
		}

		@Override
		public void flush() throws IOException {
			refreshPanel();
		}

		@Override
		public void close() throws IOException {
			flush();
		}
		
	}
	
	/*----- Construction -----*/
	
	public WriterPanel() {
		initGUI();
		resetTextPane();
	}
	
	private void initGUI(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);
		
		textPane = new JTextPane();
		textPane.setEditable(false);
		textPane.setFont(new Font("Courier New", Font.PLAIN, 11));
		scrollPane.setViewportView(textPane);
	}
	
	private void resetTextPane(){
		doc = new DefaultStyledDocument();
		textPane.setDocument(doc);
		
		defo_attr = new SimpleAttributeSet();
		StyleConstants.setFontSize(defo_attr, 11);
		StyleConstants.setForeground(defo_attr, Color.black);
		StyleConstants.setFontFamily(defo_attr, "Courier New");
		
		textPane.repaint();
		scrollPane.repaint();
	}
	
	/*----- Writer -----*/
	
	public Writer getWriter(){
		return new PanelWriter();
	}
	
	/*----- Textpane -----*/
	
	public StyledDocument getDocument(){
		return doc;
	}
	
	public void setDocument(StyledDocument doc){
		this.doc = doc;
		textPane.setDocument(doc);
		refreshPanel();
	}
	
	public void refreshPanel(){
		textPane.repaint();
		scrollPane.repaint();
	}
	
	public void clear(){
		resetTextPane();
	}

}
