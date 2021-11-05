package entity;

import java.util.Date;
import java.io.IOException;
import java.nio.file.Files;

public class ServerFileInfor {
	
	private String fileName;
	private String url;
	private Date uploadTime;
	
	public ServerFileInfor( String fileName, Date uploadTime) {
		super();
		this.fileName = fileName;
		this.uploadTime = uploadTime;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Date getUploadTime() {
		return uploadTime;
	}
	public void setUploadTime(Date uploadTime) {
		this.uploadTime = uploadTime;
	}
	
}
