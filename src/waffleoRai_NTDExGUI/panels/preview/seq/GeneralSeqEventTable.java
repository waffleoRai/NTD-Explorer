package waffleoRai_NTDExGUI.panels.preview.seq;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JScrollPane;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.List;

import javax.swing.border.SoftBevelBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.BevelBorder;
import javax.swing.JTable;
import java.awt.Font;

public class GeneralSeqEventTable extends JPanel{

	private static final long serialVersionUID = -182208350479396642L;
	
	private JTable table;

	public GeneralSeqEventTable(GenSeqTrack track){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);
		
		table = new JTable();
		table.setFont(new Font("Courier New", Font.PLAIN, 10));
		scrollPane.setViewportView(table);
		
		updateTable(track);
	}
	
	private void updateTable(GenSeqTrack track){
		
		String[] cols = {"Tick", "Event", "Binary"};
		
		if(track == null){
			String[][] data = new String[0][3];
			data[0][0] = ""; data[0][1] = ""; data[0][2] = "";
			DefaultTableModel model = new DefaultTableModel(data, cols);
			table.setModel(model);
			table.repaint();
			return;
		}
		
		List<GenSeqEvent> elist = track.getEventList();
		if(elist.isEmpty()) return;
		
		String[][] data = new String[elist.size()][3];
		int i = 0;
		for(GenSeqEvent e : elist){
			data[i][0] = Long.toString(e.getTick());
			data[i][1] = e.getName();
			data[i][2] = e.getByteString();
			i++;
		}
		
		DefaultTableModel model = new DefaultTableModel(data, cols);
		table.setModel(model);
		table.repaint();
	}

	public void setTrack(GenSeqTrack track){
		updateTable(track);
	}
	
}
