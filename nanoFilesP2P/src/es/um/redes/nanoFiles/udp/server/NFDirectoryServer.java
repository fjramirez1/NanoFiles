package es.um.redes.nanoFiles.udp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFDirectoryServer {
	/**
	 * Número de puerto UDP en el que escucha el directorio
	 */
	public static final int DIRECTORY_PORT = 6868;

	/**
	 * Socket de comunicación UDP con el cliente UDP (DirectoryConnector)
	 */
	private DatagramSocket socket = null;
	/**
	 * Estructura para guardar los nicks de usuarios registrados, y clave de sesión
	 * 
	 */
	private HashMap<String, Integer> nicks;
	/**
	 * Estructura para guardar las claves de sesión y sus nicks de usuario asociados
	 * 
	 */
	private HashMap<Integer, String> sessionKeys;
	/*
	 * TODO: Añadir aquí como atributos las estructuras de datos que sean necesarias
	 * para mantener en el directorio cualquier información necesaria para la
	 * funcionalidad del sistema nanoFilesP2P: ficheros publicados, servidores
	 * registrados, etc.
	 */
	private HashMap<String, InetSocketAddress> servers;
	private HashMap<Integer, FileInfo[]> files;
	/**
	 * Generador de claves de sesión aleatorias (sessionKeys)
	 */
	Random random = new Random();
	/**
	 * Probabilidad de descartar un mensaje recibido en el directorio (para simular
	 * enlace no confiable y testear el código de retransmisión)
	 */
	private double messageDiscardProbability;

	public NFDirectoryServer(double corruptionProbability) throws SocketException {
		/*
		 * Guardar la probabilidad de pérdida de datagramas (simular enlace no
		 * confiable)
		 */
		messageDiscardProbability = corruptionProbability;
		/*
		 * TODO: (Boletín UDP) Inicializar el atributo socket: Crear un socket UDP
		 * ligado al puerto especificado por el argumento directoryPort en la máquina
		 * local,
		 */
		InetSocketAddress serverAddress = new InetSocketAddress(DIRECTORY_PORT);
		socket = new DatagramSocket(serverAddress);
		/*
		 * TODO: (Boletín UDP) Inicializar el resto de atributos de esta clase
		 * (estructuras de datos que mantiene el servidor: nicks, sessionKeys, etc.)
		 */
		nicks = new HashMap<String, Integer>();
		sessionKeys = new HashMap<Integer, String>();
		servers = new HashMap<String, InetSocketAddress>();
		files = new HashMap<Integer, FileInfo[]>();

		if (NanoFiles.testMode) {
			if (socket == null || nicks == null || sessionKeys == null) {
				System.err.println("[testMode] NFDirectoryServer: code not yet fully functional.\n"
						+ "Check that all TODOs in its constructor and 'run' methods have been correctly addressed!");
				System.exit(-1);
			}
		}
	}

	public void run() throws IOException {
		byte[] receptionBuffer = null;
		InetSocketAddress clientAddr = null;
		int dataLength = -1;
		/*
		 * TODO: (Boletín UDP) Crear un búfer para recibir datagramas y un datagrama
		 * asociado al búfer
		 */
		receptionBuffer = new byte[DirMessage.PACKET_MAX_SIZE];
		DatagramPacket requestPacket = new DatagramPacket(receptionBuffer, receptionBuffer.length);

		System.out.println("Directory starting...");

		while (true) { // Bucle principal del servidor de directorio

			// TODO: (Boletín UDP) Recibimos a través del socket un datagrama
			socket.receive(requestPacket);
			// TODO: (Boletín UDP) Establecemos dataLength con longitud del datagrama
			// recibido
			dataLength = requestPacket.getLength();
			// TODO: (Boletín UDP) Establecemos 'clientAddr' con la dirección del cliente,
			// obtenida del
			// datagrama recibido
			clientAddr = (InetSocketAddress) requestPacket.getSocketAddress();

			if (NanoFiles.testMode) {
				if (receptionBuffer == null || clientAddr == null || dataLength < 0) {
					System.err.println("NFDirectoryServer.run: code not yet fully functional.\n"
							+ "Check that all TODOs have been correctly addressed!");
					System.exit(-1);
				}
			}
			System.out.println("Directory received datagram from " + clientAddr + " of size " + dataLength + " bytes");

			// Analizamos la solicitud y la procesamos
			if (dataLength > 0) {
				/*
				 * TODO: (Boletín UDP) Construir una cadena a partir de los datos recibidos en
				 * el buffer de recepción
				 */
				String messageFromClient = new String(receptionBuffer, 0, dataLength);

				if (NanoFiles.testMode) { // En modo de prueba (mensajes en "crudo", boletín UDP)
					System.out.println("[testMode] Contents interpreted as " + dataLength + "-byte String: \""
							+ messageFromClient + "\"");
					/*
					 * TODO: (Boletín UDP) Comprobar que se ha recibido un datagrama con la cadena
					 * "login" y en ese caso enviar como respuesta un mensaje al cliente con la
					 * cadena "loginok". Si el mensaje recibido no es "login", se informa del error
					 * y no se envía ninguna respuesta.
					 */
					if (messageFromClient.equals("login")) {
						String messageToClient = new String("login_ok");
						byte[] dataToClient = messageToClient.getBytes();
						DatagramPacket packToClient = new DatagramPacket(dataToClient, dataToClient.length, clientAddr);
						socket.send(packToClient);
					} else {
						System.err.println("* Error no login received.");
					}
				} else { // Servidor funcionando en modo producción (mensajes bien formados)

					// Vemos si el mensaje debe ser ignorado por la probabilidad de descarte
					double rand = Math.random();
					if (rand < messageDiscardProbability) {
						System.err.println("Directory DISCARDED datagram from " + clientAddr);
						continue;
					}
					/*
					 * TODO: Construir String partir de los datos recibidos en el datagrama. A
					 * continuación, imprimir por pantalla dicha cadena a modo de depuración.
					 * Después, usar la cadena para construir un objeto DirMessage que contenga en
					 * sus atributos los valores del mensaje (fromString).
					 */
					messageFromClient = new String(receptionBuffer, 0, dataLength);
					// System.out.println("Content interpreted as " + dataLength + "-byte String:
					// \"" + messageFromClient + "\"");
					DirMessage dirMessageFromClient = DirMessage.fromString(messageFromClient);
					/*
					 * TODO: Llamar a buildResponseFromRequest para construir, a partir del objeto
					 * DirMessage con los valores del mensaje de petición recibido, un nuevo objeto
					 * DirMessage con el mensaje de respuesta a enviar. Los atributos del objeto
					 * DirMessage de respuesta deben haber sido establecidos con los valores
					 * adecuados para los diferentes campos del mensaje (operation, etc.)
					 */
					DirMessage dirMessageToClient = buildResponseFromRequest(dirMessageFromClient, clientAddr);
					/*
					 * TODO: Convertir en string el objeto DirMessage con el mensaje de respuesta a
					 * enviar, extraer los bytes en que se codifica el string (getBytes), y
					 * finalmente enviarlos en un datagrama
					 */
					String messageToClient = dirMessageToClient.toString();
					byte[] dataToClient = messageToClient.getBytes();
					DatagramPacket packToClient = new DatagramPacket(dataToClient, dataToClient.length, clientAddr);
					socket.send(packToClient);
					System.out.println("Sent " + dirMessageFromClient.getOperation() + " response to " + clientAddr);
				}
			} else {
				System.err.println("Directory ignores EMPTY datagram from " + clientAddr);
			}

		}
	}

	private DirMessage buildResponseFromRequest(DirMessage msg, InetSocketAddress clientAddr) {
		/*
		 * TODO: Construir un DirMessage con la respuesta en función del tipo de mensaje
		 * recibido, leyendo/modificando según sea necesario los atributos de esta clase
		 * (el "estado" guardado en el directorio: nicks, sessionKeys, servers,
		 * files...)
		 */
		String operation = msg.getOperation();
		System.out.println("Received " + operation + " request from " + clientAddr);
		DirMessage response = null;
		String username = msg.getNickname();

		switch (operation) {
		case DirMessageOps.OPERATION_LOGIN: {
			/*
			 * TODO: Comprobamos si tenemos dicho usuario registrado (atributo "nicks"). Si
			 * no está, generamos su sessionKey (número aleatorio entre 0 y 1000) y añadimos
			 * el nick y su sessionKey asociada. NOTA: Puedes usar random.nextInt(10000)
			 * para generar la session key
			 */
			/*
			 * TODO: Construimos un mensaje de respuesta que indique el éxito/fracaso del
			 * login y contenga la sessionKey en caso de éxito, y lo devolvemos como
			 * resultado del método.
			 */
			/*
			 * TODO: Imprimimos por pantalla el resultado de procesar la petición recibida
			 * (éxito o fracaso) con los datos relevantes, a modo de depuración en el
			 * servidor
			 */
			if (!nicks.containsKey(username)) {
				int sessionKey = random.nextInt(10000);
				nicks.put(username, sessionKey);
				sessionKeys.put(sessionKey, username);
				response = new DirMessage(DirMessageOps.OPERATION_LOGIN_OK, sessionKey);
				System.out.println("* Client " + clientAddr + " successfully registered username " + username);
			} else {
				response = new DirMessage(DirMessageOps.OPERATION_LOGIN_FAILED);
				System.out.println("* Client " + clientAddr + " failed to register username " + username);
			}
			break;
		}
		case DirMessageOps.OPERATION_USERLIST: {
			response = new DirMessage(DirMessageOps.OPERATION_USERLIST_RESPONSE, nicks.keySet().toArray(new String[0]),
					servers.keySet());
			System.out.println("* Client " + clientAddr + " successfully obtained userlist");
			break;
		}
		case DirMessageOps.OPERATION_LOGOUT: {
			if (nicks.containsKey(username) && nicks.get(username).equals(msg.getSessionKey())) {
				nicks.remove(username);
				sessionKeys.remove(msg.getSessionKey());
				servers.remove(username);
				files.remove(msg.getSessionKey());
				response = new DirMessage(DirMessageOps.OPERATION_LOGOUT_OK);
				System.out.println("* Client " + clientAddr + " successfully deregistered username " + username);
			} else {
				response = new DirMessage(DirMessageOps.OPERATION_LOGOUT_FAILED);
				System.out.println("* Client " + clientAddr + " failed to deregister username " + username);
			}
			break;
		}
		case DirMessageOps.OPERATION_REGISTER_SERVER: {
			username = sessionKeys.get(msg.getSessionKey());
			if (nicks.containsKey(username) && !servers.containsKey(username)) {
				servers.put(username, new InetSocketAddress(clientAddr.getAddress().getHostName(), msg.getPort()));
				response = new DirMessage(DirMessageOps.OPERATION_REGISTER_SERVER_OK);
				System.out.println("* Client " + clientAddr + "(" + username + ") successfully registered as server");
			} else if (nicks.containsKey(username) && servers.containsKey(username)) {
				// Si el servidor de ficheros ya estaba registrado, lo da de baja
				servers.remove(username);
				response = new DirMessage(DirMessageOps.OPERATION_REGISTER_SERVER_OK);
				System.out.println("* Client " + clientAddr + "(" + username + ") was removed as server");
			} else {
				response = new DirMessage(DirMessageOps.OPERATION_REGISTER_SERVER_FAILED);
				System.out.println("* Client " + clientAddr + " failed to register as server");
			}
			break;
		}
		case DirMessageOps.OPERATION_LOOKUP_USERNAME: {
			if (nicks.containsKey(username) && servers.containsKey(username)) {
				response = new DirMessage(DirMessageOps.OPERATION_LOOKUP_RESPONSE, servers.get(username));
				System.out.println("* Client " + clientAddr + " queried about username " + username
						+ ", obtained this info: " + servers.get(username));
			} else {
				InetSocketAddress nula = null;
				response = new DirMessage(DirMessageOps.OPERATION_LOOKUP_RESPONSE, nula);
				System.out.println("* Client " + clientAddr + " queried about username " + username
						+ ", obtained this info: " + servers.get(username));
			}
			break;
		}
		case DirMessageOps.OPERATION_PUBLISH_FILES: {
			if (sessionKeys.containsKey(msg.getSessionKey()) && msg.getFiles() != null) {
				files.put(msg.getSessionKey(), msg.getFiles());
				response = new DirMessage(DirMessageOps.OPERATION_PUBLISH_FILES_OK);
				username = sessionKeys.get(msg.getSessionKey());
				System.out.println("* Client " + clientAddr + " using username " + username + " serving "
						+ msg.getFiles().length + " files on address " + servers.get(username));
			} else {
				response = new DirMessage(DirMessageOps.OPERATION_PUBLISH_FILES_FAILED);
				System.out.println("* Client " + clientAddr + " failed to publish files");
			}
			break;
		}
		case DirMessageOps.OPERATION_FILELIST: {
			int totalSize = 0;
			for (FileInfo[] fileInfos : files.values()) {
				totalSize += fileInfos.length;
			}
			FileInfo[] allFiles = new FileInfo[totalSize];
			int currentIndex = 0;
			for (FileInfo[] fileInfos : files.values()) {
				System.arraycopy(fileInfos, 0, allFiles, currentIndex, fileInfos.length);
				currentIndex += fileInfos.length;
			}
			response = new DirMessage(DirMessageOps.OPERATION_FILELIST_RESPONSE, allFiles);
			break;
		}
		case DirMessageOps.OPERATION_SEARCH: {
			List<String> users = new LinkedList<String>();
			for (Integer sessionKey : sessionKeys.keySet()) {
				FileInfo[] fileInfoArray = files.get(sessionKey);
				if (fileInfoArray != null) {
					if (FileInfo.lookupHashSubstring(fileInfoArray, msg.getHash()).length > 0) {
						String nickname = sessionKeys.get(sessionKey);
						if (!users.contains(nickname))
							users.add(nickname);
					}
				}
			}
			response = new DirMessage(DirMessageOps.OPERATION_SEARCH_RESPONSE, users.toArray(new String[0]));
			break;
		}
		default:
			System.err.println("Unexpected message operation: \"" + operation + "\"");
			break;
		}
		return response;

	}
}
