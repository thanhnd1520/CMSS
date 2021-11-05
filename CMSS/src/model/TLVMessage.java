package model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class TLVMessage {
	// tag == "d" -- delete file()
	// tag == "c" -- create file có update
	//--------
	// tag == "m" -- message 
	// tag == "l" -- login
	// tag == "r" -- login result
	// tag == "e" -- error
	// tag == "p" -- client ping to server
	
	private String tag; // 1 byte
	private int length; // 4 byte
	private byte[] value;
	
	public TLVMessage( User user) {
		super();
		this.tag = "l";
		this.value = convertValueToByteArray(user);
	}
	
	public TLVMessage(String tag, String message) {
		super();
		this.tag = tag;
		this.value = convertValueToByteArray(message);
	}
	public TLVMessage(String tag, byte[] value) {
		this.tag = tag;
		this.length = value.length;
		this.value = value;
	}
	
	public String getTag() {
		return tag;
	}
	public int getLength() {
		return length;
	}
	public byte[] getValue() {
		return value;
	}
	
	private byte[] convertValueToByteArray(FileInfor file) {
		return file.getData();
	}
	private byte[] convertValueToByteArray(User user) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = null;
		try {
		  out = new ObjectOutputStream(bos);   
		  out.writeObject(user);
		  out.flush();
		  byte[] yourBytes = bos.toByteArray();
		  return yourBytes;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		  try {
		    bos.close();
		  } catch (IOException ex) {
		    // ignore close exception
		  }
		}
		return null;
	}
	private byte[] convertValueToByteArray(String message) {
		byte[] tmp = new byte[message.getBytes().length];
		tmp = message.getBytes();
		this.length = tmp.length;
		return tmp;
	}
}
