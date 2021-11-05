package model;

import java.io.Serializable;
import java.util.Date;

public class FileDate implements Serializable{
	private static final long serialVerionUID = 5L;
	private String fileName;
	private Long updateDate;
	
	public FileDate(String fileName, Long updateDate) {
		super();
		this.fileName = fileName;
		this.updateDate = updateDate;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public Long getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}
}
