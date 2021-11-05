package model;

public class QueueMessage {
	private String userName;
	private String even;
	private String fileName;
	public QueueMessage(String userName, String even, String fileName) {
		super();
		this.userName = userName;
		this.even = even;
		this.fileName = fileName;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getEven() {
		return even;
	}
	public void setEven(String even) {
		this.even = even;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
}
