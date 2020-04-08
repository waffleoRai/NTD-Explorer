package waffleoRai_NTDExCore;

import java.util.HashMap;
import java.util.Map;

public enum Console {
	
	UNKNOWN(-1, "UNK"),
	
	PS1(-2, "PSX"),
	
	NES(1, "NES"),
	SNES(2, "SNS"),
	GB(3, "DMG"), //Dot Matrix Game
	GBC(4, "CGB"), //Color Game Boy
	N64(5, "NUS"), //Nintendo Ultra Sixty-Four
	GBA(6, "AGB"), //Advanced Game Boy
	
	GAMECUBE(7, "DOL"), //Dolphin
	DS(8, "NTR"), //Nitro
	WII(9, "RVL"), //Revolution
	DSi(10, "TWL"), //Twilight (?)
	_3DS(11, "CTR"), //Citrus
	NEW_3DS(12, "KTR"), //Kontrolle (?)
	WIIU(13, "WUP"), //Wii U Project
	SWITCH(14, "HAC"); //Handheld and Console (?)
	
	private int int_val;
	private String short_code;
	
	private Console(int ival, String code3)
	{
		int_val = ival;
		short_code = code3;
	}
	
	public int getIntValue()
	{
		return int_val;
	}
	
	public String getShortCode()
	{
		return short_code;
	}
	
	//--------------------------
	
	private static Map<Integer, Console> imap;
	
	public static Console getConsoleFromIntCode(int icode)
	{
		if(imap == null)
		{
			imap = new HashMap<Integer, Console>();
			Console[] vals = Console.values();
			for(Console c : vals) imap.put(c.getIntValue(), c);
		}
		
		return imap.get(icode);
	}
	
}
