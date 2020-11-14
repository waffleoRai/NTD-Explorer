package waffleoRai_NTDExGUI.panels.preview.text;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.border.EtchedBorder;

import waffleoRai_Files.FileBufferInputStream;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_NTDExGUI.panels.preview.WriterPanel;
import waffleoRai_NTDTypes.textfiles.TXTManager;
import waffleoRai_Utils.FileBuffer;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import java.awt.Font;

public class EncodedTextPanel extends JPanel{

	private static final long serialVersionUID = 7408181375176472840L;
	
	private JComboBox<String> comboBox;
	private WriterPanel pnlText;
	
	private FileNode node;
	private String[] lookup;
	
	public EncodedTextPanel(){
		initGUI();
	}
	
	/*--- GUI Gen ---*/
	
	private void initGUI(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{50, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel pnlEnc = new JPanel();
		pnlEnc.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlEnc = new GridBagConstraints();
		gbc_pnlEnc.insets = new Insets(0, 0, 5, 0);
		gbc_pnlEnc.fill = GridBagConstraints.BOTH;
		gbc_pnlEnc.gridx = 0;
		gbc_pnlEnc.gridy = 0;
		add(pnlEnc, gbc_pnlEnc);
		GridBagLayout gbl_pnlEnc = new GridBagLayout();
		gbl_pnlEnc.columnWidths = new int[]{0, 200, 0, 0};
		gbl_pnlEnc.rowHeights = new int[]{0, 0, 0};
		gbl_pnlEnc.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlEnc.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		pnlEnc.setLayout(gbl_pnlEnc);
		
		JLabel lblDummy = new JLabel("");
		GridBagConstraints gbc_lblDummy = new GridBagConstraints();
		gbc_lblDummy.insets = new Insets(0, 0, 5, 0);
		gbc_lblDummy.gridx = 2;
		gbc_lblDummy.gridy = 0;
		pnlEnc.add(lblDummy, gbc_lblDummy);
		
		JLabel lblEncoding = new JLabel("Encoding: ");
		lblEncoding.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblEncoding = new GridBagConstraints();
		gbc_lblEncoding.anchor = GridBagConstraints.EAST;
		gbc_lblEncoding.insets = new Insets(0, 5, 0, 5);
		gbc_lblEncoding.gridx = 0;
		gbc_lblEncoding.gridy = 1;
		pnlEnc.add(lblEncoding, gbc_lblEncoding);
		
		comboBox = new JComboBox<String>();
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 0, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 1;
		pnlEnc.add(comboBox, gbc_comboBox);
		populateCombobox();
		comboBox.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				String s = comboBox.getItemAt(comboBox.getSelectedIndex());
				onEncodingSelect(s);
			}
			
		});
		
		pnlText = new WriterPanel();
		GridBagConstraints gbc_pnlText = new GridBagConstraints();
		gbc_pnlText.fill = GridBagConstraints.BOTH;
		gbc_pnlText.gridx = 0;
		gbc_pnlText.gridy = 1;
		add(pnlText, gbc_pnlText);
	}

	private void populateCombobox(){

		Map<String, Charset> charsets = Charset.availableCharsets();
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
		
		List<String> list = new ArrayList<String>(charsets.size()+1);
		list.addAll(charsets.keySet());
		Collections.sort(list);
		if(!list.isEmpty()) lookup = new String[list.size()];
		
		int i = 0;
		for(String s : list){
			model.addElement(s);
			lookup[i++] = s;
		}
		
		comboBox.setModel(model);
		comboBox.repaint();
		
	}
	
	private void refreshTextPanel(String enc){
		try{
			FileBuffer buff = node.loadDecompressedData();
		
			pnlText.clear();
			Writer w = pnlText.getWriter();
		
			//Read in
			InputStream is = new FileBufferInputStream(buff);
			BufferedReader br = new BufferedReader(new InputStreamReader(is, enc));
			String line = null;
			while((line = br.readLine()) != null){
				w.write(line + "\n");
			}
			br.close();
			w.close();
			
			pnlText.refreshPanel();
		}
		catch(Exception x){
			x.printStackTrace();
			JOptionPane.showMessageDialog(this, "File could not be loaded!", "Loading Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/*--- Getters ---*/
	
	public WriterPanel getWriterPanel(){return pnlText;}
	
	/*--- Setters ---*/
	
	public void setNode(FileNode n){
		node = n;
		
		//Get current encoding
		String enc = n.getMetadataValue(TXTManager.METAKEY_ENCODING);
		if(enc == null) enc = "UTF-8";
		
		//Set the encoding in the combobox
		int idx = Arrays.binarySearch(lookup, enc);
		if(idx < 0){
			enc = "UTF-8";
			idx = Arrays.binarySearch(lookup, enc);
		}
		
		//Debug
		/*for(int i = 0; i < lookup.length; i++){
			System.out.println(lookup[i]);
		}*/
		
		if(idx < 0) idx = -1;
		comboBox.setSelectedIndex(idx); //Should call onEncodingSelect()?
		
	}
	
	/*--- Actions ---*/
	
	private void onEncodingSelect(String s){

		if(node == null) return;
		node.setMetadataValue(TXTManager.METAKEY_ENCODING, s);
		refreshTextPanel(s);
	}
	
}
