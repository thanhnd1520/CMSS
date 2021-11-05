package model;

import java.nio.ByteBuffer;

public class FileInfor {
	
	private String fileName;
	private byte[] content;
	
	public FileInfor() {
		super();
	}

	public FileInfor(String fileName) {
		this.fileName = fileName;
	}
	
	public FileInfor(String fileName, byte[] content) {
		super();
		this.fileName = fileName;
		this.content = content;
	}
	
	// tạo FileInfor khi truyền value của TLVMessage
	public  FileInfor(byte[] data) {
		int point =  0;
		byte[] tmp = new byte[4];
		
		System.arraycopy(data, point, tmp, 0, 4);
		int fileNameLength = ByteBuffer.wrap(tmp).getInt();	point+=4;
		byte[] fileNameByte = new byte[fileNameLength];
		System.arraycopy(data, point, fileNameByte, 0, fileNameLength);
		this.fileName = new String(fileNameByte);
		point+=fileNameLength;
		
		System.arraycopy(data, point, tmp, 0, 4);
		int contentLength = ByteBuffer.wrap(tmp).getInt();	point+=4;
		byte[] contentByte = new byte[contentLength];
		System.arraycopy(data, point, contentByte, 0, contentLength);
		this.content = contentByte;
	}

	public byte[] getData() {
		int point = 0;
		
		byte[] fileNameByte = this.fileName.getBytes();
		int length = 4 + fileNameByte.length + 4 + content.length;
		byte[] data = new byte[length];
		
		System.arraycopy(ByteBuffer.allocate(4).putInt(fileNameByte.length).array(), 0, data, point, 4);
		point+=4;
		System.arraycopy(fileNameByte, 0, data, point, fileNameByte.length);
		point+=fileNameByte.length;
		
		System.arraycopy(ByteBuffer.allocate(4).putInt(this.content.length).array(), 0, data, point, 4);
		point+=4;
		System.arraycopy(this.content, 0, data, point, this.content.length);
		
		return data;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}
}
