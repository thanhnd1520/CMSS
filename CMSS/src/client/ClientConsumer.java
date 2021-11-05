package client;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.io.FileUtils;

import config.Configuration;
import model.FileInfor;
import model.TLVMessage;
import model.TLVParser;
import tcp.TCPClientConection;

public class ClientConsumer implements Runnable{
	private BlockingQueue<TLVMessage> queue;
	private TCPClientConection conn;
	private ClientHandler clientHandler;
	
	public ClientConsumer(BlockingQueue<TLVMessage> queue, TCPClientConection conn, ClientHandler clientConsumer) {
		this.queue = queue;
		this.conn = conn;
		this.clientHandler = clientHandler;
	}
	
	private void setQueue(BlockingQueue<TLVMessage> queue) {
		this.queue = queue;
	}
	public void setClientConsumer(ClientHandler clientHandler) {
		this.clientHandler = clientHandler;
	}

	@Override
	public void run() {
		try {
			OutputStream output = this.conn.getSocket().getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(true) {
			try {
				TLVMessage tlv = queue.take();
				String tag = tlv.getTag();
				System.out.println("ClientConsumer: get element in queue" );
				if(tag.equals("m")) {
					try {
						String fileName = TLVParser.getMessageFromTLV(tlv);
						String even = clientHandler.getEven(fileName);
						System.out.println("RECEIVE FROM SERVER: File name: " + fileName +"----" + "even: " + even);
						if(even.equals("d")) {
							TLVMessage tlvResponse = new TLVMessage("d", fileName);
							Thread.sleep(500);
							this.conn.getSocket().getOutputStream().write(TLVParser.convertTLVToByte(tlvResponse));
							System.out.println("gửi thành công data:" + fileName + "-even:" + even);
							
						}else if(even.equals("c") || even.equals("u")) {
							System.out.println("ClientHandler: receive message: " + fileName);
							Path pathFile = Paths.get(Configuration.getProperties("client.folderDirectory") + "\\" + fileName);
							File file = new File(pathFile.toUri());
							FileInfor fileInfor = new FileInfor(fileName, Files.readAllBytes(pathFile));
							TLVMessage tlvResponse = new TLVMessage(even, fileInfor.getData());
							Thread.sleep(500);
							this.conn.getSocket().getOutputStream().write(TLVParser.convertTLVToByte(tlvResponse));
							System.out.println("gửi thành công data:" + fileName + "-even:" + even);
						}
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}catch(Throwable e) {
						e.printStackTrace();
					}
				}
				else if(tag.equals("s")) {
					String fileName = TLVParser.getMessageFromTLV(tlv);
					this.clientHandler.deleteEvenLogChange(fileName);
					this.clientHandler.submit();
				}
				else if(tag.equals("d")) {
					String fileName = new String(tlv.getValue());
					deleteFile(fileName);
				}
				else if(tag.equals("u")) { // modify
					createFile(tlv);
				}
				else if(tag.equals("c")) {
					createFile(tlv);
				}
				else if(tag.equals("r")) {
					if(TLVParser.getMessageFromTLV(tlv).equals("200")) {
						conn.setCheckLoginView(true);
					}
					else {
						conn.setCheckLoginView(false);
					}
				}
				else if(tag.equals("p")) {
					// nothing : server ping để ktra client còn connect không trc khi gửi
				}
				else if(tag.equals("e")) {
					clientHandler.submit();
				}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
	}
	
	
	private String createFile(TLVMessage tlv) {
		try {
			String folder = Configuration.getProperties("client.folderDirectory");
			System.out.println("folder: " + folder);
			FileInfor fileInforReceive = TLVParser.getFileFromTLV(tlv);
			String fileName = fileInforReceive.getFileName();
			clientHandler.addServerEvenChange(fileName);
			System.out.println("Client Consumer: fileName: " + fileName);
			java.io.File file = new java.io.File(folder + "\\" + fileName);
			if(!file.exists()) {
				file.createNewFile();
			}
			Files.write(Paths.get(file.toURI()), fileInforReceive.getContent(), StandardOpenOption.WRITE);
			System.out.println("update file to " + file.getAbsolutePath());
			return fileName;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	private void modifyFile(TLVMessage tlv) {
		try {
			String folder = Configuration.getProperties("client.folderDirectory");
			System.out.println("folder: " + folder);
			FileInfor fileInforReceive = TLVParser.getFileFromTLV(tlv);
			String fileName = fileInforReceive.getFileName();
			clientHandler.addServerEvenChange(fileName);
			System.out.println("fileName: " + fileName);
			java.io.File file = new java.io.File(folder + "\\" + fileName);
			Files.write(Paths.get(file.toURI()), fileInforReceive.getContent(), StandardOpenOption.WRITE);
			System.out.println("update file to " + file.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void deleteFile(String fileName) {
		String folder = Configuration.getProperties("client.folderDirectory");
		System.out.println("folder: " + folder);
		System.out.println("fileName: " + fileName);
		java.io.File file = new java.io.File(folder + "\\" + fileName);
		if(file.exists()) {
			clientHandler.addServerEvenChange(fileName);
			file.delete();
		}
	}
}
