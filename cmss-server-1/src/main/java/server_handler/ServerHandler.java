package server_handler;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import config.Configuration;
import model.TLVMessage;
import server.Server;

public class ServerHandler implements Runnable {

	private Server server;
	private Socket socket;
	private InputStream input;
	private String username;
	private BlockingQueue<TLVMessage> clientQueue;
	private ServerThreadConsumer clientConsumer;

//	private static final int QUEUE_MAX_SIZE = Integer.parseInt(Configuration.getProperties("server.queueSize"));

	public ServerHandler(Server server, Socket socket) {
		this.server = server;
		this.socket = socket;
		clientQueue = new LinkedBlockingQueue<>();
		clientConsumer = new ServerThreadConsumer(this);
		Thread clientComsumerThread = new Thread(clientConsumer);
		clientComsumerThread.start();
	}
	public void run() {
		try {
			//this.socket.set
			InputStream input = this.socket.getInputStream();
			byte[] buffer = null;
			ByteBuffer byteBuffer = ByteBuffer.allocate(0);
			while (true) {
//				if (input.available() <= 0 && byteBuffer.capacity() < 5) {
//					try {
//						Thread.sleep(5);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					continue;
//				}
//				byte[] b = new byte[input.available()];
//				input.read(b);
//				byteBuffer = ByteBuffer.allocate(byteBuffer.capacity() + b.length)
//									   .put(byteBuffer.array())
//									   .put(b);
//				if (byteBuffer.capacity() < 5) {
//					try {
//						Thread.sleep(10);
//						continue;
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//				String tag = new String(byteBuffer.array(),0,1);
//				
//				int length = ByteBuffer.wrap(byteBuffer.array(),1,4).getInt();
//				int totalLength = 5 + length;
//				if (byteBuffer.capacity() < totalLength) {
//					continue;
//				}
//				byteBuffer.position(5);
//				byte[] valueByte = new byte[length];
//				byteBuffer. get(valueByte,0,length);
//				TLVMessage tlv = new TLVMessage(tag, valueByte);
//				if(length <= 0 || !(tag.equals("m") || tag.equals("d") || tag.equals("u") || tag.equals("l")
//						   || tag.equals("c") || tag.equals("g") || tag.equals("s") || tag.equals("p"))) {
//				try {
//					Thread.sleep(3000);
//				} catch (InterruptedException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				b = new byte[input.available()];
//				byteBuffer = ByteBuffer.allocate(0);
//				System.out.println("Error: tag receive: " + tag);
//				System.out.println("Error: length receive: " + length);
//				TLVMessage tvlError = new TLVMessage("e", " ");
//				try {
//					clientQueue.put(tvlError);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				continue;
//			}
//				try {
//					System.out.println("SERVER HANDLER: receive tag: " + tag);
//					clientQueue.put(tlv);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				byteBuffer = ByteBuffer.allocate(byteBuffer.capacity()-totalLength)
//									   .put(byteBuffer.array(), totalLength, byteBuffer.capacity()-totalLength);
				if (input.available() <= 0 ) {
					if(buffer != null && buffer.length > 5) {
						// chuyển xuống dưới
					}
					else {
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						continue;
					}
				}
				byte[] b = new byte[input.available()];
				input.read(b);
				
				if(buffer == null) {
					buffer = b;
				} else {
					byte[] nArr = new byte[b.length + buffer.length];
					System.arraycopy(buffer, 0, nArr, 0, buffer.length);
					System.arraycopy(b, 0, nArr, buffer.length, b.length);
					buffer = nArr;
				}
				if (buffer.length < 5) {
					try {
						Thread.sleep(10);
						continue;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				byte[] tagByte = new byte[1];
				tagByte[0] = buffer[0];
				String tag = new String(tagByte);
				
				byte[] lengthByte = new byte[4];
				System.arraycopy(buffer, 1, lengthByte, 0, 4);
				int length = ByteBuffer.wrap(lengthByte).getInt();
				
				if(length <= 0 || !(tag.equals("m") || tag.equals("d") || tag.equals("u") || tag.equals("l")
							   || tag.equals("c") || tag.equals("g") || tag.equals("s") || tag.equals("p"))) {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					b = new byte[input.available()];
					buffer = null;
					System.out.println("Error: tag receive: " + tag);
					System.out.println("Error: length receive: " + length);
					TLVMessage tvlError = new TLVMessage("e", " ");
					try {
						clientQueue.put(tvlError);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					continue;
				}
				int totalLength = 5 + length;
				if (buffer.length < totalLength) {
					continue;
				}
				byte[] valueByte = new byte[length];
				
				System.arraycopy(buffer, 5, valueByte, 0, length);
				TLVMessage tlv = new TLVMessage(tag, valueByte);
				try {
					System.out.println("SERVER HANDLER: receive tag: " + tag);
					clientQueue.put(tlv);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(buffer.length > totalLength) {
					byte[] rest = new byte[buffer.length - totalLength];
					System.arraycopy(buffer, totalLength, rest, 0, (buffer.length - totalLength));
					buffer = rest;
				}else if(buffer.length == totalLength) {
					buffer = null;
				}
			}
			
		} catch (EOFException e) { // TH socket đóng
			//
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			server.removeClient(username);
		}
	}
	
	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public BlockingQueue<TLVMessage> getClientQueue() {
		return clientQueue;
	}
}
