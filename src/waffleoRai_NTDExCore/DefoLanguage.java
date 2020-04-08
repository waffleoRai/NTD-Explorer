package waffleoRai_NTDExCore;

import java.util.HashMap;
import java.util.Map;

public enum DefoLanguage {

	UNKNOWN('?'),
	
	ENGLISH('E'),
	SPANISH('S'),
	FRENCH('F'),
	GERMAN('D'),
	JAPANESE('J'),
	KOREAN('K'),
	ITALIAN('I'),
	ENGLISH_US_AUS('T'),
	ANY('A'),
	MULTI('O'),
	ANY_PAL('P');
	
	private char code_letter;
	
	private DefoLanguage(char code1)
	{
		code_letter = code1;
	}
	
	public char getCharCode()
	{
		return code_letter;
	}
	
	//----------------------------
	
	private static Map<Character, DefoLanguage> lmap;
	
	public static DefoLanguage getLanReg(char code)
	{
		if(lmap == null)
		{
			lmap = new HashMap<Character, DefoLanguage>();
			for(DefoLanguage l : DefoLanguage.values()) lmap.put(l.getCharCode(), l);
		}
		
		return lmap.get(code);
	}
	
}
