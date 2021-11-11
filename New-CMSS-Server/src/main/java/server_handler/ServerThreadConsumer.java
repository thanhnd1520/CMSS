package server_handler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import config.Configuration;
import model.MessageDefs;
import model.QueueMessage;
import model.TLVMessage;
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
	FileProcess fileProcess;
	static Logger log = Logger.getLogger(ServerThreadConsumer.class.getName());
	
	public ServerThreadConsumer(ServerHandler serverHandler) {
		this.clientQueue = serverHandler.getClientQueue();
		this.server = serverHandler.getServer();
		this.serverQueue = this.server.queue;
		this.serverHandler = serverHandler;
		fileProcess = new FileProcess(this);
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
				int tag = (int)tlv.getTag();
				TLVMessage tlvResponse = new TLVMessage();
				int resultCode;
				String fileName;
				
				switch (tag) {
				case MessageDefs.MessageTypes.MT_LOGIN_REQ:
					server.checkUserLive();
					String username = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_USERID).getData());
					String password = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_USERPASS).getData());
					this.serverHandler.setUsername(username);
					server.addClient(serverHandler.getSocket(), username);
					System.out.println(username + " -- " + password);
					tlvResponse.setTag(MessageDefs.MessageTypes.MT_LOGIN_RES);
					tlvResponse.addInt(MessageDefs.FieldTypes.FT_RESULT_CODE, 200);
					log.info("Client " + username + " login to server");
					send(tlvResponse);
					break;
				case MessageDefs.MessageTypes.MT_SEND_TEXT_REQ:
					fileName = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_NAME).getData());
					tlvResponse.setTag(MessageDefs.MessageTypes.MT_SEND_TEXT_RES);
					tlvResponse.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileName);
					System.out.println("send test RES");
					send(tlvResponse);
					break;
				case MessageDefs.MessageTypes.MT_BEGIN_FILE_TRANSFER_REQ:
					resultCode = fileProcess.createFile(tlv);
					tlvResponse.setTag(MessageDefs.MessageTypes.MT_BEGIN_FILE_TRANSFER_RES);
					send(tlvResponse);
					addToQueue(tlv);
					log.info("Client " + this.serverHandler.getUsername() + " send file " + fileProcess.getFileName());
					break;
				case MessageDefs.MessageTypes.MT_FILE_FRAGMENT_REQ:
					resultCode = fileProcess.appendFile(tlv);
					tlvResponse.setTag(MessageDefs.MessageTypes.MT_FILE_FRAGMENT_RES);
					send(tlvResponse);
					addToQueue(tlv);
					break;
				case MessageDefs.MessageTypes.MT_END_FILE_TRANSFER_REQ:
					fileName = fileProcess.getFileName();
					System.out.println("create file: "  + fileName + " success");
					resultCode = fileProcess.closeFile(tlv);
					tlvResponse.setTag(MessageDefs.MessageTypes.MT_END_FILE_TRANSFER_RES);
					tlvResponse.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileName);
					send(tlvResponse);
					log.info("Client " + this.serverHandler.getUsername() + " send success file " + fileName);
					if(resultCode == 1) {
						addToQueue(tlv);
					}
					break;
				case MessageDefs.MessageTypes.MT_FILE_DELETE_REQ:
					fileName = new String(tlv.getString(MessageDefs.FieldTypes.FT_FILE_NAME));
					System.out.println("ServerThread receive delete file request: " + fileName);
					fileProcess.deleteFile(tlv);
					tlvResponse.setTag(MessageDefs.MessageTypes.MT_FILE_DELETE_RES);
					tlvResponse.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileName);
					send(tlvResponse);
					log.info("Client " + this.serverHandler.getUsername() + " delete file " + fileName);
					addToQueue(tlv);
					break;
				case MessageDefs.MessageTypes.MT_SYNCHRONIZE_REQ:
					fileProcess.synchronize(output);
					log.info("Client " + this.serverHandler.getUsername() + " request synchronize ");
					break;
				case MessageDefs.MessageTypes.MT_HEAD_BEAT:
					
					break;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void send(TLVMessage tlvResponse) {
		try {
			Thread.sleep(20);
			this.output.write(tlvResponse.flat());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void addToQueue(TLVMessage tlv) {
		String userName = this.serverHandler.getUsername();
		QueueMessage message = new QueueMessage();
		message.setUserName(userName);
		message.setTlv(tlv);
		try {
			serverQueue.put(message);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
