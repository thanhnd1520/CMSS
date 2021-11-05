package server;

import java.util.AbstractMap.SimpleEntry;

import org.apache.commons.io.FileUtils;

import config.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import model.FileInfor;
import model.QueueMessage;
import model.TLVMessage;
import model.TLVParser;
import model.User;

public class ServerConsumer implements Runnable {

	private final BlockingQueue<QueueMessage> queue;
	private Server server;
	private static final String folder = Configuration.getProperties("server.folderDirectory");

	public ServerConsumer(BlockingQueue<QueueMessage> queue, Server server) {
		
		this.queue = queue;
		this.server = server;
	}
    //
	public void run() {
		while (true) {
			try {
				QueueMessage message = queue.take();
				System.out.println("serverConsumer: lấy 1 element in queue, even: " + message.getEven()) ;
				// logic
				if(message.getEven().equals("c")) {
					sendFileToAllClient(message.getFileName(), message.getUserName());
				}else if(message.getEven().equals("d")) {
					deleteFileToAllClient(message.getFileName(), message.getUserName());
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void sendFileToAllClient(String fileName, String userName) {
		try {
//			server.checkUserLive();
			Path pathFile = Paths.get(folder + "/" + fileName);
			File file = new File(pathFile.toUri());
			FileInfor fileInfor = new FileInfor(fileName, Files.readAllBytes(pathFile));
			TLVMessage tlvResponse = new TLVMessage("c", fileInfor.getData());
			this.server.clientsOutputStream.forEach((k,v) ->{
				if(!k.equals(userName)) {
					try {
						System.out.println("SERVER send file to " + k);
						System.out.println("SERVER: length tlv" + TLVParser.convertTLVToByte(tlvResponse).length);
						v.getOutputStream().write(TLVParser.convertTLVToByte(tlvResponse));
						System.out.println("SERVER: send thành công");
					} catch (IOException e) {
						server.removeClient(userName);
						e.printStackTrace();
					}
				}
			});
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	private void deleteFileToAllClient(String fileName, String userName) {
//		server.checkUserLive();
		TLVMessage tlvResponse = new TLVMessage("d", fileName);
		this.server.clientsOutputStream.forEach((k,v) ->{
			if(!k.equals(userName)) {
				try {
					System.out.println("SERVER send file to " + k);
					System.out.println("SERVER: length tlv" + TLVParser.convertTLVToByte(tlvResponse).length);
					v.getOutputStream().write(TLVParser.convertTLVToByte(tlvResponse));
					System.out.println("SERVER: send thành công");
				} catch (IOException e) {
					server.removeClient(userName);
					e.printStackTrace();
				}
			}
		});
	}
	
}
