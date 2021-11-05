package server_handler;

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
import model.QueueMessage;
import model.TLVMessage;
import model.TLVParser;
import model.User;
import server.Server;

//ClientConsumer vừa là product vừa là client
// product của ServerConsumer
// consumer của ServerHandler
public class ServerThreadConsumer implements Runnable { 

	private final BlockingQueue<TLVMessage> clientQueue;
	private final BlockingQueue<QueueMessage> serverQueue;
	private Server server;
	private ServerHandler serverHandler;
	private OutputStream output;
	private static final String folder = Configuration.getProperties("server.folderDirectory");
	
	public ServerThreadConsumer(ServerHandler serverHandler) {
		this.clientQueue = serverHandler.getClientQueue();
		this.server = serverHandler.getServer();
		this.serverQueue = this.server.queue;
		this.serverHandler = serverHandler;
	}
    //
	public void run() {
		try {
			this.output = serverHandler.getSocket().getOutputStream();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (true) {
			try {
				TLVMessage tlv = clientQueue.take();
				String tagName = tlv.getTag();
				try {
					switch (tagName) {
					case "l":
						server.checkUserLive();
						User user = TLVParser.getUserFromTLV(tlv);
						System.out.println("LOGIN:");
						System.out.println("username: " + user.getUsername());
						System.out.println("password: " + user.getPassword());
						int t = server.checkUser(user);
						if(t == 200) {
							this.server.addClient(serverHandler.getSocket()	, user.getUsername()); // thêm client vào hasttable của server
							serverHandler.setUsername(user.getUsername());	// set username cho serverHander
						}
						TLVMessage tlvResponse = new TLVMessage("r", ""+t);
						this.output.write(TLVParser.convertTLVToByte(tlvResponse));// trả về result đăng nhập cho client
						break;
					case "m":
						tlvResponse = new TLVMessage("m", new String(tlv.getValue()));
						serverHandler.getSocket().getOutputStream().write(TLVParser.convertTLVToByte(tlvResponse));
						System.out.println("server thread: send again file name: " + TLVParser.getMessageFromTLV(tlvResponse));
						break;
					case "u":
						modifyFile(tlv);
						break;
					case "c":
						createFile(tlv);
						break;
					case "d":
						deleteFile(tlv);
						break;
					case "g":
						System.out.println("receive g tag");
						File folder = new File(this.folder);
						System.out.println(folder);
						System.out.println(folder.list().length);
						for ( File fileEntry : folder.listFiles()) {
							try {
								System.out.println("send file: " + fileEntry.getName());
								File file = new File(folder+"/"+fileEntry.getName());
								FileInfor fileInfor = new FileInfor(fileEntry.getName(), Files.readAllBytes(Paths.get(file.toURI())));
								tlvResponse = new TLVMessage("c", fileInfor.getData());
								this.output.write(TLVParser.convertTLVToByte(tlvResponse));
								System.out.println("");
							}catch(Throwable e) {
								e.printStackTrace();
							}
					    }
						break;
					case "p":
						
						break;
					case "e":
						tlvResponse = new TLVMessage("e", " ");
						this.output.write(TLVParser.convertTLVToByte(tlvResponse));
						break;
					}
				}catch(IOException ex) {
					///
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private synchronized void createFile(TLVMessage tlv) {
		String tmp="";
		try {
			FileInfor fileInforReceive = TLVParser.getFileFromTLV(tlv);
			String fileName = fileInforReceive.getFileName();
			java.io.File file = new java.io.File(this.folder + "/" + fileName);
			tmp =  fileName;
			if(!file.exists()) {
				file.createNewFile();
				System.out.println("create file: " +  file.getPath());
			}
			Files.write(Paths.get(file.toURI()), fileInforReceive.getContent(), StandardOpenOption.WRITE);
			
			serverQueue.put(new QueueMessage(serverHandler.getUsername(), "c", fileName));
			TLVMessage tlvResponse = new TLVMessage("s", fileName);
			this.output.write(TLVParser.convertTLVToByte(tlvResponse));
		} catch (IOException | InterruptedException e) {
			System.out.println("SERVER THREAD: error create file:" + tmp);
			e.printStackTrace();
		}
	}
	private synchronized void modifyFile(TLVMessage tlv) {
		try {
			FileInfor fileInforReceive = TLVParser.getFileFromTLV(tlv);
			String fileName = fileInforReceive.getFileName();
			java.io.File file = new java.io.File(this.folder + "/" + fileName);
			Files.write(Paths.get(file.toURI()), fileInforReceive.getContent(), StandardOpenOption.WRITE);
			
			serverQueue.put(new QueueMessage(serverHandler.getUsername(), "u", fileName));
			TLVMessage tlvResponse = new TLVMessage("s", fileName);
			this.output.write(TLVParser.convertTLVToByte(tlvResponse));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private synchronized void deleteFile(TLVMessage tlv) {
		try {
			String fileName = TLVParser.getMessageFromTLV(tlv);
			System.out.println("delte file: " + fileName);
			java.io.File file = new java.io.File(this.folder + "/" + fileName);
			file.delete();
			serverQueue.put(new QueueMessage(serverHandler.getUsername(), "d", fileName));
			TLVMessage tlvResponse = new TLVMessage("s", fileName);
			this.output.write(TLVParser.convertTLVToByte(tlvResponse));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
