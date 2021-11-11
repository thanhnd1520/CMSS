package client;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import bussiness.FileProcessReceive;
import bussiness.FileProcessSender;
import config.Configuration;
import model.MessageDefs;
import model.TLVMessage;
import tcp.TCPClientConection;

public class ClientConsumer implements Runnable{
	private BlockingQueue<TLVMessage> queue;
	private TCPClientConection conn;
	private ClientHandler clientHandler;
	private FileProcessReceive fileProcessReceive;
	private FileProcessSender fileProcessSender;
	static Logger log = Logger.getLogger(ClientConsumer.class.getName());
	
	public ClientConsumer(BlockingQueue<TLVMessage> queue, TCPClientConection conn, ClientHandler clientConsumer) {
		this.queue = queue;
		this.conn = conn;
		this.clientHandler = clientHandler;
		fileProcessReceive = new FileProcessReceive();
		fileProcessSender = new FileProcessSender();
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
			log.info("Connect to server");
			OutputStream output = this.conn.getSocket().getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(true) {
			try {
				TLVMessage tlv = queue.take();
				int tag = tlv.getTag();
				String fileName;
				String even;
				switch (tag) {
				case MessageDefs.MessageTypes.MT_SEND_TEXT_RES:
					fileName = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_NAME).getData());
					even = clientHandler.getEven(fileName);
					System.out.println("MT_SEND_TEXT_RES: " + fileName);
					if(even.equals("d")) {
						 TLVMessage tlvRequest = new TLVMessage(MessageDefs.MessageTypes.MT_FILE_DELETE_REQ);
						 tlvRequest.addString(MessageDefs.FieldTypes.FT_FILE_NAME, fileName);
						 try {
							this.conn.getSocket().getOutputStream().write(tlvRequest.flat());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else {
						fileProcessSender.startSendFile(fileName);
//						Thread.sleep(500);
						TLVMessage tlvRequest = fileProcessSender.getFragement();
						sendTlv(tlvRequest);
						System.out.println("ClientConsumer: send thành công");
					}
					break;
				case MessageDefs.MessageTypes.MT_BEGIN_FILE_TRANSFER_RES:
					sendTlv(fileProcessSender.getFragement());
					break;
				case MessageDefs.MessageTypes.MT_FILE_FRAGMENT_RES:
					sendTlv(fileProcessSender.getFragement());
					break;
				case MessageDefs.MessageTypes.MT_END_FILE_TRANSFER_RES:
					fileName = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_NAME).getData());
					System.out.println("server send end file: "  + fileName);
					clientHandler.deleteEvenLogChange(fileName);
					Thread.sleep(1000);
					clientHandler.submit();
					break;
				case MessageDefs.MessageTypes.MT_FILE_DELETE_RES:
					fileName = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_NAME).getData());
					clientHandler.deleteEvenLogChange(fileName);
					Thread.sleep(100);
					clientHandler.submit();
					break;
				case MessageDefs.MessageTypes.MT_LOGIN_RES:
					conn.setCheckLoginView(true);
					break;
					
					/// server tranfer from other client
				case MessageDefs.MessageTypes.MT_BEGIN_FILE_TRANSFER_REQ:
					fileName = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_NAME).getData());
					clientHandler.addServerEvenChange(fileName);
					fileProcessReceive.createFile(tlv);
					break;
				case MessageDefs.MessageTypes.MT_FILE_FRAGMENT_REQ:
					fileProcessReceive.appendFile(tlv);
					break;
				case MessageDefs.MessageTypes.MT_END_FILE_TRANSFER_REQ:
					fileName = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_NAME).getData());
					fileProcessReceive.closeFile(tlv);
					clientHandler.deleteEvenLogChange(fileName);
					System.out.println("server send and file: "  + fileName);
					break;
				case MessageDefs.MessageTypes.MT_FILE_DELETE_REQ:
					fileName = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_NAME).getData());
					System.out.println("ClientConsumer delete file: " + fileName);
					deleteFile(fileName);
					break;
				case MessageDefs.MessageTypes.MT_SYNCHRONIZE_RES:
					
					break;
				default:
					break;
				}
			}catch(InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	
	private void deleteFile(String fileName) {
		WatchingFolder.checkUpdate = false;
		String folder = Configuration.getProperties("client.folderDirectory");
		System.out.println("folder: " + folder);
		System.out.println("fileName: " + fileName);
		java.io.File file = new java.io.File(folder + "\\" + fileName);
		if(file.exists()) {
			file.delete();
		}
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		WatchingFolder.checkUpdate = true;
	}
	
	private void sendTlv(TLVMessage tlv) {
		try {
			conn.getSocket().getOutputStream().write(tlv.flat());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
