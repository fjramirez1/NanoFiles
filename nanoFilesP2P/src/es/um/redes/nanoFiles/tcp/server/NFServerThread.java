package es.um.redes.nanoFiles.tcp.server;

import java.net.Socket;
import java.io.IOException;

public class NFServerThread extends Thread {
	/*
	 * TODO: Esta clase modela los hilos que son creados desde NFServer y cada uno
	 * de los cuales simplemente se encarga de invocar a
	 * NFServerComm.serveFilesToClient con el socket retornado por el método accept
	 * (un socket distinto para "conversar" con un cliente)
	 */

	private Socket clientSocket;

	public NFServerThread(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void run() {
		
		NFServerComm.serveFilesToClient(clientSocket);

		try {
			if (!clientSocket.isClosed())
				clientSocket.close();
		} catch (IOException e) {
			System.out.println("* Error while closing server socket: " + e.getMessage());
			e.printStackTrace();
		}

	}

}
