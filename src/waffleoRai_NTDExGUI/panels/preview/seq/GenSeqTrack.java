package waffleoRai_NTDExGUI.panels.preview.seq;

import java.util.LinkedList;
import java.util.List;

public class GenSeqTrack {

	private String name;
	
	private List<GenSeqEvent> events;
	
	public GenSeqTrack(){
		name = "ANON_TRACK";
		events = new LinkedList<GenSeqEvent>();
	}
	
	public String getName(){return name;}
	public String toString(){return name;}
	public void setName(String n){name = n;}
	
	public void addEvent(GenSeqEvent e){events.add(e);}
	public void clearEvents(){events.clear();}
	public List<GenSeqEvent> getEventList(){return events;}
	
}
