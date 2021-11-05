package tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;

import client.ClientHandler;
import client.ClientListener;
import client.ClientView;
import config.Configuration;
import model.TLVMessage;


public class TCPClientConection {

	private TCPClientConectionState state;
	private Socket socket;
	private int portNumber;
	private int reconectRetry;
	private String serverAddress;
	private CheckTask checkTask;
	private ClientListener clientHandler;
	private Thread clientHandlerThread = null;
	private ClientHandler clientConsumer;

	ClientView view;
	
	public TCPClientConection() {
		this.portNumber = Integer.parseInt( Configuration.getProperties("server.portNumber"));
		serverAddress = Configuration.getProperties("server.address");
		this.state = new DisconnectedState(this);
		System.out.println(this.portNumber + this.serverAddress);
		this.connect(serverAddress);
		checkTask = new CheckTask(this);
	}
	
	// method function

	
	public void startListen() {
		stopListen();
		System.out.println("start listen");
		this.clientHandler = new ClientListener(this, this.clientConsumer);
		this.clientHandlerThread = new Thread(this.clientHandler);
		this.clientHandler.setClientConsumer(clientConsumer);
		this.clientHandlerThread.start();
	}
	public void setClientConsumer(ClientHandler clientConsumer) {
		this.clientConsumer = clientConsumer;
		if(this.clientHandler != null) {
			this.clientHandler.setClientConsumer(clientConsumer);
		}
	}

	public void stopListen() {
		this.clientHandlerThread.stop();
		this.clientHandlerThread = null;
	}
	public void changeState(TCPClientConectionState state) {
		synchronized (this.state) {
			if(state instanceof ConnectedState && !(this.state instanceof ConnectedState)) {
				this.state = state;
				startListen();
			}else {
				this.state = state;
				System.out.println("change 3");
			}
		}
	}
	public void checkConnection() {
		synchronized (this.state) {
			String connectString = state.checkConnection();
			view.setConnectStringView(connectString);
			//System.out.println(connectString);
		}
	}
	public int connect(String serverAddress) {
		return state.connect(serverAddress);
	}
	public int disconnect() {
		return state.disconnect();
	}
	public int sendData(TLVMessage tlv){
		try {
			return state.sendData(tlv);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("send data error");
		}
		return -1;
	}
	public void setCheckLoginView(boolean check) {
		this.view.setCheckLogin(check);
	}
	// run checkTask cho socket<kiem tra connect giua client va server>
	// chạy sau khi login thành công
	public void runCheckTask() {
		Timer timer = new Timer();
		timer.schedule(checkTask, 0, 1000*5);
	}
	
	// seter and geter property
	public void setView(ClientView view) {
		this.view = view;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public ClientListener getClientHandler() {
		return clientHandler;
	}

	public void setClientHandler(ClientListener clientHandler) {
		this.clientHandler = clientHandler;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public int getReconectRetry() {
		return reconectRetry;
	}
	public void setReconectRetry(int reconectRetry) {
		this.reconectRetry = reconectRetry;
	}
	public TCPClientConectionState getState() {
		return state;
	}
	public void setState(TCPClientConectionState state) {
		this.state = state;
	}
	public int getPortNumber() {
		return portNumber;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
//		setInputOutput();
	}
	public Socket getSocket() {
		return socket;
	}
	

}
