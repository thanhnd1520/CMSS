package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import database.JDBCQuery;
import entity.ServerFileInfor;
import entity.User;
import model.QueueMessage;
import model.TLVMessage;
import model.TLVParser;
import server_handler.ServerHandler;

public class Server {
	private ServerSocket serverSocket;
	private Socket socket;
	public Hashtable<String, Socket> clientsOutputStream; 
	public final BlockingQueue<QueueMessage> queue = new LinkedBlockingQueue<>();
	private ServerConsumer consumer; // xử lý tất cả các message gửi lên từ client
//	private JDBCQuery databse;
	
	public Server(int port)  {
		try {
			clientsOutputStream = new Hashtable<String, Socket>();
			serverSocket = new ServerSocket(port);
			consumer = new ServerConsumer(this.queue, this);
			Thread serverConsumerThread = new Thread(consumer);
			serverConsumerThread.start();
			System.out.println(serverSocket.getLocalPort());
//			databse = new JDBCQuery();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void waitingForClient() {
		while(true){
			try {
				socket = serverSocket.accept();
				ServerHandler client =  new ServerHandler(this, socket);
				System.out.println(1);
				Thread clientThread = new Thread(client);
				clientThread.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	//send file to all client
	public void newFileUpdate(String fileName, String clientName) {
		
	}
	// add client to hash table
	public void addClient(Socket socket, String username) {
		synchronized (clientsOutputStream){
			clientsOutputStream.put(username, socket);
			System.out.println("List client");
			clientsOutputStream.forEach((k,v) ->{
				System.out.println(k);
			});
		}
	}
	
	//Removing the client from the client hash table
	public void removeClient(String username) {
		synchronized (clientsOutputStream){
			try {
				clientsOutputStream.remove(username);
			}catch(NullPointerException ex) {
				
			}
		}
	}
	public void checkUserLive() {
		List<String> list = new LinkedList<String>();
		synchronized (clientsOutputStream){
			clientsOutputStream.forEach((k,v) ->{
				TLVMessage tlvPing = new TLVMessage("p", "server ping to client");
				try {
					v.getOutputStream().write(TLVParser.convertTLVToByte(tlvPing));
				} catch (IOException e) {
					list.add(k);
				}
			});
			for(String user : list) {
				removeClient(user);
			}
		}
	}
	public void saveFileFromDatabase(ServerFileInfor file) {
		
	}
	public void deleteFileFromDatabase(ServerFileInfor file) {
		
	}
	public void getFileFromDatabase(String fileName) {
		
	}
	public void insertUser(User user) {
		
	}
	public java.util.List<ServerFileInfor> getAllFileFromDatabase(){
		return null;
	}
	public int checkUser(model.User userReceive) { // return true if done
		String username = userReceive.getUsername();
		String password = userReceive.getPassword();
//		User userEntity = databse.getUser(username);
//		if(userEntity == null) {
//			return 404;
//		}else if(!password.equals(userEntity.getPassword())) {
//			return 400;
//		}else {
//			return 200;
//		}
		return 200;
	}
}
