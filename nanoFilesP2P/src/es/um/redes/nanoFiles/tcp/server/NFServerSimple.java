package es.um.redes.nanoFiles.tcp.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class NFServerSimple {

	private static final int SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS = 1000;
	//private static final String STOP_SERVER_COMMAND = "fgstop";
	private static final int INITIAL_PORT = 10000;
	private static final int MAX_PORT_TRIES = 100; // Número máximo de puertos a intentar
	private int port;
	private ServerSocket serverSocket = null;
	private boolean stop;

	public NFServerSimple() throws IOException {
		/*
		 * TODO: Crear una direción de socket a partir del puerto especificado
		 */
		port = INITIAL_PORT;
		/*
		 * TODO: Crear un socket servidor y ligarlo a la dirección de socket anterior
		 */
		boolean isBound = false;
		for (int i = 0; i < MAX_PORT_TRIES; i++) {
			try {
				InetSocketAddress socketAddress = new InetSocketAddress(port);
				serverSocket = new ServerSocket();
				serverSocket.bind(socketAddress);
				isBound = true;
				break;
			} catch (IOException e) {
				port++;
			}
		}

		if (!isBound) {
			throw new IOException("Unable to bind server to any port in the range");
		}
		serverSocket.setSoTimeout(SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS);
		stop = false;
	}

	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación a menos que se implemente la funcionalidad de
	 * detectar el comando STOP_SERVER_COMMAND (opcional)
	 * 
	 */
	public void run() {
		/*
		 * TODO: Comprobar que el socket servidor está creado y ligado
		 */
		if (serverSocket == null || !serverSocket.isBound()) {
			System.err.println("* Server socket is not created or not bound.");
			return;
		}

		System.out.println("* Server is running on port " + port);

		/*
		 * TODO: Usar el socket servidor para esperar conexiones de otros peers que
		 * soliciten descargar ficheros
		 */
		/*
		 * TODO: Al establecerse la conexión con un peer, la comunicación con dicho
		 * cliente se hace en el método NFServerComm.serveFilesToClient(socket), al cual
		 * hay que pasarle el socket devuelto por accept
		 */

		try {
			while (!stop) {
				try {
					Socket clientSocket = serverSocket.accept();
					System.out.println("New client connected: " + clientSocket.getInetAddress().toString() + ":"
							+ clientSocket.getPort());
					NFServerComm.serveFilesToClient(clientSocket);
					System.out.println("Disconnected client: " + clientSocket.getInetAddress().toString() + ":"
							+ clientSocket.getPort());
				} catch (SocketTimeoutException e) {
					// Ignorar el tiempo de espera y continuar esperando nuevas conexiones
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (!serverSocket.isClosed() && serverSocket != null) {
					serverSocket.close();
				}
			} catch (IOException e) {
				System.err.println("* Error while closing server socket: " + e.getMessage());
				e.printStackTrace();
			}
		}

		System.out.println("NFServerSimple stopped. Returning to the nanoFiles shell...");
	}

	public void stopServer() {
		stop = true;
	}
}
