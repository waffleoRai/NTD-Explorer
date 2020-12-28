package waffleoRai_NTDExCore.seq;

import javax.sound.midi.MidiMessage;

import waffleoRai_SeqSound.MIDI;

public class MIDIText {

	public static String eventToText(MidiMessage msg){
		if(msg == null) return null;
		byte[] mbytes = msg.getMessage();
		
		int stat = Byte.toUnsignedInt(mbytes[0]);
		int fam = stat & 0xf0;
		fam = fam >>> 4;
		int ch = stat & 0xf;
		
		int n = 0;
		double d = 0.0;
		switch(fam){
		case 0x8: //Note off
			//Get note
			n = (int)mbytes[1]; //Will always be <128
			//Get velocity
			return "[" + ch + "]NOTE_OFF " + MIDI.getNoteName(n) + " v" + mbytes[2];
		case 0x9: //Note on
			n = (int)mbytes[1];
			return "[" + ch + "]NOTE_ON " + MIDI.getNoteName(n) + " v" + mbytes[2];
		case 0xa: //Key pressure
			n = (int)mbytes[1];
			return "[" + ch + "]POLYPHON_KEY " + MIDI.getNoteName(n) + " v" + mbytes[2];
		case 0xb: //Controller
			int ctrl = Byte.toUnsignedInt(mbytes[1]);
			switch(ctrl){
			case 0x0:
				return "[" + ch + "]BANK_SELECT " + mbytes[2];
			case 0x20:
				return "[" + ch + "]BANK_SELECT_LO " + mbytes[2];
			case 0x1:
				return "[" + ch + "]MOD_WHEEL " + String.format("0x%02x", mbytes[2]);
			case 0x21:
				return "[" + ch + "]MOD_WHEEL_LO " + String.format("0x%02x", mbytes[2]);
			case 0x6:
				return "[" + ch + "]DATA_ENTRY " + String.format("0x%02x", mbytes[2]);
			case 0x26:
				return "[" + ch + "]DATA_ENTRY_LO " + String.format("0x%02x", mbytes[2]);
			case 0x7:
				return "[" + ch + "]VOLUME " + mbytes[2];
			case 0x27:
				return "[" + ch + "]VOLUME_LO " + mbytes[2];
			case 0xa:
				d = (double)mbytes[2] - 0x40;
				d = d/0x40 * 100.0;
				if(d < 0) return "[" + ch + "]PAN " + String.format("%.2f", (d * -1.0)) + "% L";
				if(d == 0) return "[" + ch + "]PAN Center";
				return "[" + ch + "]PAN " + String.format("%.2f", d) + "% R";
			case 0x2a:
				return "[" + ch + "]PAN_LO " + String.format("0x%02x", mbytes[2]);
			case 0xb:
				return "[" + ch + "]EXPRESSION " + mbytes[2];
			case 0x2b:
				return "[" + ch + "]EXPRESSION_LO " + mbytes[2];
			case 0x40:
				return "[" + ch + "]DAMPER_PDL " + mbytes[2];
			case 0x63:
				return "[" + ch + "]NRPN " + String.format("0x%02x", mbytes[2]);
			default: return "[" + ch + "]CTRLR" + ctrl + ": " + String.format("0x%02x", mbytes[2]);
			}
		case 0xc: //Change program
			return "[" + ch + "]CHANGE_PROG " + mbytes[1];
		case 0xd: //Channel pressure
			return "[" + ch + "]CHANNEL_PRESSURE v" + mbytes[1];
		case 0xe: //Pitch bend
			n = (int)mbytes[2] << 7 | (int)mbytes[1];
			n -= 0x2000;
			d = (double)n/(double)0x3FFF;
			return "[" + ch + "]PITCH_WHEEL " + String.format("%.1f", d) + "%";
		case 0xf: //Meta
			if(ch == 0xf){
				n = Byte.toUnsignedInt(mbytes[1]);
				if(n == 0x51){
					int t = (Byte.toUnsignedInt(mbytes[3]) << 16) | (Byte.toUnsignedInt(mbytes[4]) << 8) | Byte.toUnsignedInt(mbytes[5]);
					t = MIDI.uspqn2bpm(t, 48);
					return "[" + ch + "]SET_TEMPO " + t + "bpm";
				}
				if(n == 0x2f){
					return "END_TRACK";
				}
				else{
					StringBuilder sb = new StringBuilder(128);
					for(int k = 0; k < mbytes.length; k++){
						sb.append(String.format("%02x ", mbytes[k]));
					}
					return "UNDEF_META " + sb.toString();
				}
			}
			else{
				StringBuilder sb = new StringBuilder(128);
				for(int k = 0; k < mbytes.length; k++){
					sb.append(String.format("%02x ", mbytes[k]));
				}
				return "UNDEF_META " + sb.toString();
			}
		default: return "<INVALID STATUS>";
		}
	}
	
	
}
