package waffleoRai_NTDExGUI.panels.preview.seq;

public class GenSeqEvent {

	private String name;
	private long tick;
	
	private boolean byte_order;
	private byte[] binary;
	
	public GenSeqEvent(){
		name = "NOP";
	}
	
	public String getName(){return name;}
	public long getTick(){return tick;}
	public byte[] getBytes(){return binary;}
	public boolean getByteOrder(){return byte_order;}
	
	public String getByteString(){
		if(binary == null) return "";
		StringBuilder sb = new StringBuilder(binary.length*3);
		
		if(byte_order){
			for(int i = 0; i < binary.length; i++){
				sb.append(String.format("%02x", binary[i]) + " ");
			}
		}
		else{
			for(int i = binary.length-1; i >= 0; i--){
				sb.append(String.format("%02x", binary[i]) + " ");
			}
		}
		
		return sb.toString();
	}
	
	public void setName(String s){name = s;}
	public void setTick(long t){tick = t;}
	public void setByteOrder(boolean bo){byte_order = bo;}
	public void setBytes(byte[] b){binary = b;}
	
	public String toString(){
		return name;
	}
}
