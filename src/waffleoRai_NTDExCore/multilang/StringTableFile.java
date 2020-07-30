package waffleoRai_NTDExCore.multilang;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

/*
 * Format
 * 
 * Magic # "strT" [4]
 * Version [4]
 * Decompressed Size [4]
 * 
 * DEFLATE'd Data
 * 
 * Data is just a big text file.
 * Comment lines start with #
 * Newline delimits entries. To include newline in a value string, use escape (\n)
 * Equals sign delimits key and value pairs. To include equals in a value string use %eq
 * 
 */

public class StringTableFile {
	
	public static final String MAGIC = "strT";
	public static final int VERSION = 1;
	
	public static final String ENCODING = "UTF8";

	public static int readStringTable(String path, Map<String, String> target) throws IOException, UnsupportedFileTypeException, DataFormatException{
		if(target == null) return 0;
		
		FileBuffer file = FileBuffer.createBuffer(path, true);
		//Check magic #
		long mpos = file.findString(0, 0x10, MAGIC);
		if(mpos < 0) throw new FileBuffer.UnsupportedFileTypeException("String table magic number could not be found!");
		
		//Skip version for now
		int decomp_size = file.intFromFile(mpos+8);
		
		byte[] compdat = file.getBytes(mpos+12, file.getFileSize());
		Inflater dec = new Inflater();
		dec.setInput(compdat);
		byte[] result = new byte[decomp_size];
		dec.inflate(result);
		dec.end();
		
		//Put the result back through a Reader
		int ecount = 0;
		ByteArrayInputStream is = new ByteArrayInputStream(result);
		BufferedReader br = new BufferedReader(new InputStreamReader(is, ENCODING));
		String line = null;
		while((line = br.readLine()) != null){
			if(line.startsWith("#")) continue;
			String[] fields = line.split("=");
			if(fields.length < 2) continue;
			String key = fields[0];
			String val = fields[1].replace("\\n", "\n").replace("%eq", "=");
			target.put(key, val);
			ecount++;
		}
		br.close();
		
		return ecount;
	}
	
	public static void toStringTable(String src_txt_path, String outpath) throws UnsupportedFileTypeException, IOException{
		long sz = FileBuffer.fileSize(src_txt_path);
		if(sz > 0x40000000){
			throw new FileBuffer.UnsupportedFileTypeException("Single files >1GB not supported");
		}
	
		int decomp_size = (int)sz;
		FileBuffer header = new FileBuffer(12, true);
		header.printASCIIToFile(MAGIC);
		header.addToFile(VERSION);
		header.addToFile(decomp_size);
		
		FileBuffer dat = FileBuffer.createBuffer(src_txt_path, true);
		Deflater comp = new Deflater();
		comp.setInput(dat.getBytes());
		comp.finish();
		byte[] compbytes = new byte[decomp_size + 16];
		int complen = comp.deflate(compbytes);
		comp.end();
		
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outpath));
		header.writeToStream(bos);
		bos.write(compbytes, 0, complen);
		bos.close();
			
	}
	
}
