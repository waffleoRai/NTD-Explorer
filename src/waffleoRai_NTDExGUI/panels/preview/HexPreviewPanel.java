package waffleoRai_NTDExGUI.panels.preview;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileNode;

public class HexPreviewPanel extends JPanel{
	
	private static final long serialVersionUID = 4490768211073360323L;
	
	public static final int MAX_LOAD_SIZE = 0x4000000; //64MB
	
	public static final int MIN_COL_WIDTH_OFFSET = 40;
	public static final int MIN_COL_WIDTH_BYTE = 5;
	public static final int MIN_COL_WIDTH_ASCII = 100;
	
	private JScrollPane scrollPane;
	private JTable table;
	
	public HexPreviewPanel()
	{
		//super(parent, true);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {0};
		gridBagLayout.rowHeights = new int[] {0};
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.rowWeights = new double[]{1.0};
		setLayout(gridBagLayout);
		
		scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);
		
		table = new JTable();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		table.setFont(new Font("Courier New", Font.PLAIN, 10));
		table.setFillsViewportHeight(true);
		scrollPane.setViewportView(table);
		
		setMinimumSize(new Dimension(400, 200));
		setPreferredSize(new Dimension(400, 200));
	}

	public void load(FileNode node, long offset, boolean decomp) throws IOException
	{
		FileBuffer buffer = null;
		if(decomp) buffer = node.loadDecompressedData();
		else buffer = node.loadData();
		long edpos = offset + MAX_LOAD_SIZE;
		if(edpos > buffer.getFileSize()) edpos = buffer.getFileSize();
		
		loadTable(buffer.createCopy(offset, edpos), offset);
	}
	
	private void loadTable(FileBuffer data, long stoff)
	{
		String[] cols = new String[18];
		cols[0] = "Offset";
		for(int i = 0; i < 16; i++) cols[i+1] = String.format("0x%02x", i);
		cols[17] = "ASCII";
		
		int row_count = (int)(data.getFileSize() >>> 4);
		if(data.getFileSize() % 0x10 != 0) row_count++;
		
		String[][] tbl = new String[row_count][cols.length];
		
		long cpos = 0;
		long off = stoff;
		for(int i = 0; i < row_count; i++)
		{
			StringBuilder ascii = new StringBuilder(16);
			tbl[i][0] = "0x" + Long.toHexString(off); off+=16;
			for(int j = 0; j < 16; j++)
			{
				if(cpos < data.getFileSize())
				{
					byte b = data.getByte(cpos);
					tbl[i][j+1] = String.format("%02x", b);
					if(b >= 32)
					{
						char c = (char)b;
						ascii.append(c);
					}
					else ascii.append('.');
				}
				else
				{
					tbl[i][j+1] = ".";
					ascii.append('.');
				}
				cpos++;
			}
			tbl[i][17] = ascii.toString();
		}
		
		DefaultTableModel tmodel = new DefaultTableModel(tbl, cols);
		table.setModel(tmodel);
		
		TableColumnModel tcm = table.getColumnModel();
		tcm.getColumn(0).setMinWidth(MIN_COL_WIDTH_OFFSET);
		tcm.getColumn(0).setPreferredWidth(MIN_COL_WIDTH_OFFSET);
		for(int i = 1; i <= 16; i++){
			tcm.getColumn(i).setMinWidth(MIN_COL_WIDTH_BYTE);
			tcm.getColumn(i).setPreferredWidth(MIN_COL_WIDTH_BYTE);
		}
		tcm.getColumn(17).setMinWidth(MIN_COL_WIDTH_ASCII);
		tcm.getColumn(17).setPreferredWidth(MIN_COL_WIDTH_ASCII);
		
		table.repaint();
		scrollPane.repaint();
		
	}
	

}
