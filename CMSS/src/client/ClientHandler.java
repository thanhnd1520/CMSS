package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;

import config.Configuration;
import model.TLVMessage;
import model.TLVParser;
import model.User;
import tcp.TCPClientConection;

public class ClientHandler {
	private static final String clientChangeFile = "clientChange.txt";
	private static final String clientDateFile = "clientDate.txt";
	private static final String path = Configuration.getProperties("client.folderDirectory");
	private Queue<TLVMessage> clientQueue;
	private ConcurrentHashMap<String, String> logChange; 
	private ConcurrentHashMap<String, String> logDate;
	private TCPClientConection conn;
	private File changeFile ;
	private File dateFile;
	
	public ClientHandler(TCPClientConection conn) {
		this.conn = conn;
		logChange = new ConcurrentHashMap<String, String>();
		logDate = new ConcurrentHashMap<String, String>();
		changeFile = new File(clientChangeFile);
		dateFile = new File(clientDateFile);
		readFromFile(changeFile, logChange);
		readFromFile(dateFile, logDate);
		compareChange();
		System.out.println("Start ClientConsumer");
		print(logChange);
	}

	public void submit() {
		try {
			synchronized (logChange) {
				Map.Entry<String,String> entry = logChange.entrySet().iterator().next();
				String fileName = entry.getKey();
				TLVMessage tlvRequest = new TLVMessage("m", fileName);
				try {
					this.conn.getSocket().getOutputStream().write(TLVParser.convertTLVToByte(tlvRequest));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}catch(NoSuchElementException e) {
			return;
		}
	}
	
	public void addFileDelete(String fileName) {
		try {
			System.out.println("delete file: " + fileName);
			if(logChange.size() < 1) {
				System.out.println("put delete");
				logChange.put(fileName, "d");
			}else if(logChange.get(fileName) == null) {
				logChange.put(fileName, "d");
			}else if(logChange.get(fileName) != null && logChange.get(fileName).equals("c")){
				System.out.println("file đã dc tạo");
				logChange.remove(fileName);
			}
			else if(logChange.get(fileName) != null && logChange.get(fileName).equals("u")){
				logChange.put(fileName, "d");
			}
			else {
				System.out.println("nhận từ server, xóa");
				logChange.remove(fileName);
			}
			writeToFile(changeFile,logChange);
//			print(logChange);
			getFileInFolder(logDate); 
			writeToFile(dateFile, logDate);
		}catch(Throwable e) {
			e.printStackTrace();
		}
		
	}
	public void addFileCreate(String fileName) {
		System.out.println("create file: " + fileName);
		if(logChange.containsKey(fileName) && logChange.get(fileName).equals("s")) {
			if(fileName.endsWith(".txt")) {
				logChange.remove(fileName);
			}
			logChange.put(fileName, "s1");
		}else {
			logChange.put(fileName, "c");
		}
		writeToFile(changeFile,logChange);
//		print(logChange);
		getFileInFolder(logDate); 
		writeToFile(dateFile, logDate);
	}
	public void addFileRename(String oldName, String newName) {
		if(logChange.containsKey(oldName) && logChange.get(oldName).equals("c")) { // mới tạo và đổi tên luôn
			logChange.remove(oldName);
			logChange.put(newName, "c");
		}else {
			logChange.put(oldName, "d");
			logChange.put(newName, "c");
		}
		writeToFile(changeFile,logChange);
//		print(logChange);
		getFileInFolder(logDate); 
		writeToFile(dateFile, logDate);
	}
	public void addFileUpdate(String fileName) {
		System.out.println("ClientHandler: update file: " + fileName);
		if(logChange.get(fileName) == null) {
			logChange.put(fileName, "u");
			writeToFile(changeFile,logChange);
			print(logChange);
			getFileInFolder(logDate); 
			writeToFile(dateFile, logDate);
		}
		else if(logChange.get(fileName).equals("s1")) { 
			logChange.remove(fileName);
			getFileInFolder(logDate); 
//			print(logChange);
			writeToFile(dateFile, logDate);
		}
		else if(logChange.containsKey(fileName)) {
			// nothing
		}else {
			logChange.put(fileName, "u");
			writeToFile(changeFile,logChange);
			print(logChange);
			getFileInFolder(logDate); 
			writeToFile(dateFile, logDate);
		}
	}
	public void addServerEvenChange(String fileName) {
		logChange.remove(fileName);
		System.out.println("ADD CHANGE form server: " + fileName);
		logChange.put(fileName, "s");
		writeToFile(changeFile,logChange);
		print(logChange);
		getFileInFolder(logDate); 
		writeToFile(dateFile, logDate);
	}
	public void deleteServerEvenChange(String fileName) {
		logChange.remove(fileName);
	}
	public void sendLogin(User user) {
		TLVMessage tlv = new TLVMessage(user);
		conn.sendData(tlv);
		System.out.println("ClientConsumer: send login");
	}
	// xóa even log của client khi dã update file lên server
	public void deleteEvenLogChange(String fileName) {
		synchronized (logChange) {
			System.out.println("consumer: delete file name: " + fileName);
			logChange.remove(fileName);
		}
		writeToFile(changeFile,logChange);
		print(logChange);
		getFileInFolder(logDate); 
		writeToFile(dateFile, logDate);
	}
	public String getEven(String fileName) {
		printLogChange(fileName);
		return logChange.get(fileName);
	}
	private void printLogChange(String fileName) {
		System.out.println("PRINT LOGCHANGE for " + fileName);
		logChange.forEach((k,v) ->{
			System.out.println(k + " -- " + v);
		});
		System.out.println("");
	}
	private void readFromFile(File file, ConcurrentHashMap<String, String> list) {
		try {
			if(!file.exists()) {
				file.createNewFile();
			}else if(file.length() != 0){
				FileInputStream fileIn = new FileInputStream(file);
				ObjectInputStream in = new ObjectInputStream(fileIn);
				list = (ConcurrentHashMap<String, String>) in.readObject();
				in.close();
				fileIn.close();
				if(file.getName().equals(clientChangeFile)) {
					this.logChange = list;
				}
				else {
					this.logDate = list;
				}
				System.out.println("read file success");
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void writeToFile(File file, ConcurrentHashMap<String, String> list) {
		System.out.println("\n write to log file: " + file.getName());
		try {
			FileOutputStream fileOut = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(list);
			out.close();
			fileOut.close();
		}catch(FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void getFileInFolder(ConcurrentHashMap<String, String> list) {
		list.clear();
		File folder = new File(path);
		if (!folder.exists()){
			folder.mkdirs();
		}
		for ( File fileEntry : folder.listFiles()) {
	        String fileName = fileEntry.getName();
	        String time ="" + fileEntry.lastModified();
	        list.put(fileName, time);
	    }
	}
	private void compareChange() {
		ConcurrentHashMap<String, String> currentLogDate = new ConcurrentHashMap<String, String>();
		getFileInFolder(currentLogDate); // lấy cây thư mục hiên tại
		writeToFile(dateFile, currentLogDate); // cập nhật cây thư mục hiện giờ vào logDate
		if(currentLogDate != null && currentLogDate.size() > 0) {
			currentLogDate.forEach((k,v) ->{ /// so sánh với cây thư mục trc lúc cmss-client tắt đi
				if(logDate.containsKey(k)) {
					if(logDate.get(k).equals(v)) {
						
					}else {
						if(logChange.get(k) != null && !logChange.get(k).equals("c")) {
							logChange.put(k, "u");
						}
					}
					System.out.println("LOGDATE constans CurrentLogDate");
					logDate.remove(k);
				}else {
					System.out.println("LOGDATE not constans CurrentLogDate");
					logChange.put(k, "c");
				}
			});
		}
		try {
			if(logDate.size() > 0) {
				List<String> list = new LinkedList<String>();
				logDate.forEach((k,v) ->{
					list.add(k);
				});
				if(list.size() > 0) {
					for(String fileName : list) {
						if(logChange.get(fileName) != null && logChange.get(fileName).equals("c")) {
							System.out.println(fileName);
							logChange.remove(fileName);
						}else {
							logChange.put(fileName, "d");
						}
					}
				}
			}
			writeToFile(changeFile, logChange);
		}catch(Throwable e) {
			e.printStackTrace();
		}
		
//		System.out.println("log change sau: ");
//		print(logChange);
	}
	private void print(ConcurrentHashMap<String, String> list) {
		list.forEach((k,v) ->{
			System.out.println(k + " -- " + v);
		});
	}

	public void synchronize() {
		// xoa hết file
		File folder = new File(path);
		try {
			FileUtils.cleanDirectory(folder);
			while(folder.listFiles().length > 0) {
				Thread.sleep(10);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Log change sau khi xóa file");
		this.logChange = new ConcurrentHashMap<String, String>();
		this.logChange.forEach((k,v) -> {
			System.out.println(k+ " -- " + "v");
		});
		// yêu cầu tải file
		TLVMessage tlvMessage = new TLVMessage("g", " ");
		try {
			conn.getSocket().getOutputStream().write(TLVParser.convertTLVToByte(tlvMessage));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}	
