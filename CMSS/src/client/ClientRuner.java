package client;

import tcp.TCPClientConection;

public class ClientRuner {
	private ClientView viewer;
	private WatchingFolder watcher;
	private TCPClientConection conn;
	private ClientHandler consumer;
	
	public ClientRuner() {
		this.conn = new TCPClientConection();
		this.consumer = new ClientHandler(conn);
		this.conn.setClientConsumer(consumer);
		this.viewer = new ClientView();
		viewer.setConsumer(consumer);
		conn.setView(viewer);
		this.watcher = new WatchingFolder(consumer);
		conn.runCheckTask();
		Thread watchingFolderThread = new Thread(watcher);
		watchingFolderThread.start();
	}
	
	public static void main(String[] args) {
		ClientRuner client = new ClientRuner();
	}
	
}
