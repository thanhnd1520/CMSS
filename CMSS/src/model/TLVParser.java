package model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

import config.Configuration;

public class TLVParser {
	private static final int TLV_TAG_SIZE = Integer.parseInt(Configuration.getProperties("tlv.tag"));
	private static final int TLV_VALUE_SIZE = Integer.parseInt(Configuration.getProperties("tlv.length"));
	private static final int TLV_MIN_SIZE = TLV_TAG_SIZE + TLV_VALUE_SIZE;
	
	public static TLVMessage receiveData(byte[] bytes) {
		//System.out.println("length tlv: " + bytes.length);
		TLVMessage tlv = null;
		if(bytes == null) {
			return new TLVMessage("e",  "Invalid TLV data: can not be null");
		}
		else if(bytes.length < TLV_MIN_SIZE) {
			return new TLVMessage("e", "Invalid TLV data: must be at least 7 characters");
		}
		else {
			int i = 0;
			while(true) {
				byte[] tagByte = new byte[TLV_TAG_SIZE];
				byte[] lengthByte  = new byte[TLV_VALUE_SIZE];
				System.arraycopy(bytes, 0, tagByte, 0, TLV_TAG_SIZE);
				System.arraycopy(bytes, TLV_TAG_SIZE, lengthByte, 0, TLV_VALUE_SIZE);
				
				// get tag into tagByte
				String tag = new String(tagByte);
				
				// get length into lengthByte
				int length = ByteBuffer.wrap(lengthByte).getInt();
				
				//
				byte[] valueByte = new byte[length];
				System.arraycopy(bytes, tagByte.length + lengthByte.length, valueByte, 0, length);
				if(tag.equals("r") || tag.equals("l") || tag.equals("f") ||  // tag truyen len dung
						tag.equals("m") || tag.equals("p")) {
					return new TLVMessage(tag, valueByte);
				}
				else { // tag truyen len bi sai
					return new TLVMessage("e", "Invalid TLV tag");
				}
			}
		}
	}
	
	public static User getUserFromTLV(TLVMessage tlv) {
		ByteArrayInputStream bis = new ByteArrayInputStream(tlv.getValue());
		ObjectInput in = null;
		User user = null;
		try {
		  in = new ObjectInputStream(bis);
		  user = (User) in.readObject(); 
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		  try {
		    if (in != null) {
		      in.close();
		    }
		  } catch (IOException ex) {
		    // ignore close exception
		  }
		}
		return user;
	}
	
	public static FileInfor getFileFromTLV(TLVMessage tlv) {
		return new FileInfor(tlv.getValue());
	}
	
	public static String getMessageFromTLV(TLVMessage tlv) {
		return new String(tlv.getValue());
	}
	
	public static byte[] convertTLVToByte(TLVMessage tlv) {
		int length = tlv.getValue().length;
		byte[] bytes1 = new byte[1];
		//get byte1
		bytes1 = tlv.getTag().getBytes();
		//get bytes2
		byte[] bytes2 = ByteBuffer.allocate(4).putInt(length).array();
		return mapToBytes(bytes1, bytes2, tlv.getValue());
	}
	
	public static byte[] mapToBytes(byte[] bytes1, byte[] bytes2, byte[] bytes3) {
		byte[] bytes = new byte[bytes1.length + bytes2.length + bytes3.length];
		System.arraycopy(bytes1, 0, bytes, 0, bytes1.length);
		System.arraycopy(bytes2, 0, bytes, bytes1.length, bytes2.length);
		System.arraycopy(bytes3, 0, bytes, bytes1.length + bytes2.length, bytes3.length);
		return bytes;
	}
}
