package client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import config.Configuration;
import model.TLVMessage;
import tcp.TCPClientConection;

public class ClientListener implements Runnable{
	
	private static final String folderDirectory = Configuration.getProperties("client.folderDirectory");
	private static final int bytesPerSplit = Integer.parseInt(Configuration.getProperties("tvl.bytesPerSplit"));
	private TCPClientConection conn;
	private ClientHandler clientConsumer;
	private ClientConsumer consumer;
	private final BlockingQueue<TLVMessage> queue = new LinkedBlockingQueue<>();
	
	public ClientListener(TCPClientConection conn, ClientHandler clientConsumer) {
		this.conn = conn;
		this.clientConsumer = clientConsumer;
		this.consumer = new ClientConsumer(queue, conn, clientConsumer);
		Thread consumerThread = new Thread(consumer);
		consumerThread.start();
	}

	public void setConn(TCPClientConection conn) {
		this.conn = conn;
	}

	public void setClientConsumer(ClientHandler clientConsumer) {
		this.clientConsumer = clientConsumer;
		this.consumer.setClientConsumer(clientConsumer);
	}

	@Override
	public void run() {
		try {
			System.out.println("start listenning");
			InputStream input = this.conn.getSocket().getInputStream();
			byte[] buffer = null;
			ByteBuffer byteBuffer = ByteBuffer.allocate(0);
			while (true) {
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
				
				int totalLength = 5 + length;
				if (buffer.length < totalLength) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					continue;
				}
				byte[] valueByte = new byte[length];
				
				System.arraycopy(buffer, 5, valueByte, 0, length);
				TLVMessage tlv = new TLVMessage(tag, valueByte);
				try {
					System.out.println("SERVER HANDLER: receive tag: " + tag);
					queue.put(tlv);
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
//				System.out.println(byteBuffer.capacity());
//				System.out.println(byteBuffer.position());
//				byteBuffer.position(5);
//				byte[] valueByte = new byte[length];
//				byteBuffer. get(valueByte,0,length);
//				TLVMessage tlv = new TLVMessage(tag, valueByte);
//				try {
//					System.out.println("CLIENT LISTENNER: receive tag: " + tag);
//					queue.put(tlv);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				byteBuffer = ByteBuffer.allocate(byteBuffer.capacity()-totalLength)
//									   .put(byteBuffer.array(), totalLength, byteBuffer.capacity()-totalLength);
//			}
			
		} catch (EOFException e) { // TH socket đóng
			//
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
