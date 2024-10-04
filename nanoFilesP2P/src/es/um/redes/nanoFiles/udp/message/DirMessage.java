package es.um.redes.nanoFiles.udp.message;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Clase que modela los mensajes del protocolo de comunicación entre pares para
 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
 * mensajes son intercambiados entre las clases DirectoryServer y
 * DirectoryConnector, y se codifican como texto en formato "campo:valor".
 * 
 * @author rtitos
 *
 */
public class DirMessage {
	public static final int PACKET_MAX_SIZE = 65507; // 65535 - 8 (UDP header) - 20 (IP header)

	private static final char DELIMITER = ':'; // Define el delimitador
	private static final char END_LINE = '\n'; // Define el carácter de fin de línea

	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	private static final String FIELDNAME_OPERATION = "operation";
	/*
	 * TODO: Definir de manera simbólica los nombres de todos los campos que pueden
	 * aparecer en los mensajes de este protocolo (formato campo:valor)
	 */
	private static final String FIELDNAME_NICKNAME = "nickname";
	private static final String FIELDNAME_SESSIONKEY = "sessionkey";
	private static final String FIELDNAME_USERS = "users";
	private static final String FIELDNAME_PORT = "port";
	private static final String FIELDNAME_ADDR = "addr";
	private static final String FIELDNAME_FILE = "file";
	private static final String FIELDNAME_HASH = "hash";

	/**
	 * Tipo del mensaje, de entre los tipos definidos en DirMessageOps.
	 */
	private String operation = DirMessageOps.OPERATION_INVALID;
	/*
	 * TODO: Crear un atributo correspondiente a cada uno de los campos de los
	 * diferentes mensajes de este protocolo.
	 */
	private String nickname;
	private int sessionKey;
	private List<String> users;
	private int port;
	private InetSocketAddress address;
	private List<FileInfo> files;
	private String hash;

	public DirMessage(String op) {
		operation = op;
	}

	/*
	 * TODO: Crear diferentes constructores adecuados para construir mensajes de
	 * diferentes tipos con sus correspondientes argumentos (campos del mensaje)
	 */
	public DirMessage(String op, String nicknameOrHash) {
		operation = op;
		if (op == DirMessageOps.OPERATION_SEARCH)
			this.hash = nicknameOrHash;
		else
			this.nickname = nicknameOrHash;
	}

	public DirMessage(String op, int sessionKey) {
		operation = op;
		this.sessionKey = sessionKey;
	}

	public DirMessage(String op, String nickname, int sessionKey) {
		operation = op;
		this.nickname = nickname;
		this.sessionKey = sessionKey;
	}

	public DirMessage(String op, String[] users, Set<String> servers) {
		operation = op;
		this.users = new LinkedList<>();
		for (String user : users) {
			if (servers.contains(user)) {
				this.users.add(user + " [server]");
			} else {
				this.users.add(user);
			}
		}
	}

	public DirMessage(String op, String[] users) {
		operation = op;
		this.users = new LinkedList<String>(Arrays.asList(users));
	}

	public DirMessage(String op, InetSocketAddress address) {
		operation = op;
		this.address = address;
	}

	public DirMessage(String op, int sessionKey, int port) {
		operation = op;
		this.sessionKey = sessionKey;
		this.port = port;
	}

	public DirMessage(String op, int sessionKey, FileInfo[] files) {
		operation = op;
		this.sessionKey = sessionKey;
		this.files = new LinkedList<FileInfo>(Arrays.asList(files));
	}

	public DirMessage(String op, FileInfo[] files) {
		operation = op;
		this.files = new LinkedList<FileInfo>(Arrays.asList(files));
	}

	public String getOperation() {
		return operation;
	}

	public void setNickname(String nick) {
		nickname = nick;
	}

	public String getNickname() {
		return nickname;
	}

	public void setSessionKey(int sessionKey) {
		this.sessionKey = sessionKey;
	}

	public int getSessionKey() {
		return sessionKey;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

	public List<String> getUsers() {
		return users;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public void setAddress(InetSocketAddress address) {
		this.address = address;
	}

	public InetSocketAddress getAddress() {
		return address;
	}

	public void setFiles(List<FileInfo> files) {
		this.files = files;
	}

	public void addFiles(FileInfo file) {
		this.files.add(file);
	}

	public FileInfo[] getFiles() {
		if (files != null)
			return files.toArray(new FileInfo[0]);
		return null;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getHash() {
		return hash;
	}

	public boolean isFilesNull() {
		if (this.files == null)
			return true;
		return false;
	}

	/**
	 * Método que convierte un mensaje codificado como una cadena de caracteres, a
	 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
	 * han sido establecidos con el valor de los campos del mensaje.
	 * 
	 * @param message El mensaje recibido por el socket, como cadena de caracteres
	 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
	 *         etc.)
	 */
	public static DirMessage fromString(String message) {
		/*
		 * TODO: Usar un bucle para parsear el mensaje línea a línea, extrayendo para
		 * cada línea el nombre del campo y el valor, usando el delimitador DELIMITER, y
		 * guardarlo en variables locales.
		 */

		// System.out.println("DirMessage read from socket:");
		// System.out.println(message);
		String[] lines = message.split(END_LINE + "");
		// Local variables to save data during parsing
		DirMessage m = null;

		for (String line : lines) {
			int idx = line.indexOf(DELIMITER); // Posición del delimitador
			String fieldName = line.substring(0, idx).toLowerCase(); // minúsculas
			String value = line.substring(idx + 1).trim();

			switch (fieldName) {
			case FIELDNAME_OPERATION: {
				assert (m == null);
				m = new DirMessage(value);
				break;
			}
			case FIELDNAME_NICKNAME: {
				m.setNickname(value);
				break;
			}
			case FIELDNAME_SESSIONKEY: {
				m.setSessionKey(Integer.parseInt(value));
				break;
			}
			case FIELDNAME_USERS: {
				m.setUsers(new LinkedList<String>(Arrays.asList(value.split(","))));
				break;
			}
			case FIELDNAME_PORT: {
				m.setPort(Integer.parseInt(value));
				break;
			}
			case FIELDNAME_ADDR: {
				String[] parts = value.split(",");
				assert (parts.length == 2);
				m.setAddress(new InetSocketAddress(parts[0], Integer.parseInt(parts[1])));
				break;
			}
			case FIELDNAME_FILE: {
				if (m.isFilesNull()) {
					m.setFiles(new LinkedList<FileInfo>());
				}
				String[] parts = value.split(",");
				assert (parts.length == 3);
				FileInfo file = new FileInfo(parts[0], parts[1], Long.parseLong(parts[2]), "");
				m.addFiles(file);
				break;
			}
			case FIELDNAME_HASH: {
				m.setHash(value);
				break;
			}
			default:
				System.err.println("PANIC: DirMessage.fromString - message with unknown field name " + fieldName + ".");
				System.err.println("Message was:\n" + message);
				System.exit(-1);
			}
		}

		return m;
	}

	/**
	 * Método que devuelve una cadena de caracteres con la codificación del mensaje
	 * según el formato campo:valor, a partir del tipo y los valores almacenados en
	 * los atributos.
	 * 
	 * @return La cadena de caracteres con el mensaje a enviar por el socket.
	 */
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append(FIELDNAME_OPERATION + DELIMITER + operation + END_LINE); // Construimos el campo
		/*
		 * TODO: En función del tipo de mensaje, crear una cadena con el tipo y
		 * concatenar el resto de campos necesarios usando los valores de los atributos
		 * del objeto.
		 */
		switch (operation) {
		case DirMessageOps.OPERATION_LOGIN: {
			sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_LOGIN_OK: {
			sb.append(FIELDNAME_SESSIONKEY + DELIMITER + sessionKey + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_USERLIST_RESPONSE: {
			sb.append(FIELDNAME_USERS + DELIMITER + String.join(",", users) + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_LOGOUT: {
			sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE);
			sb.append(FIELDNAME_SESSIONKEY + DELIMITER + sessionKey + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_LOOKUP_USERNAME: {
			sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_LOOKUP_RESPONSE: {
			String aux1 = null;
			int aux2 = -1;
			if (address != null) {
				aux1 = address.getAddress().getHostAddress();
				aux2 = address.getPort();
			}
			sb.append(FIELDNAME_ADDR + DELIMITER + aux1 + "," + aux2 + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_REGISTER_SERVER: {
			sb.append(FIELDNAME_SESSIONKEY + DELIMITER + sessionKey + END_LINE);
			sb.append(FIELDNAME_PORT + DELIMITER + port + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_PUBLISH_FILES: {
			sb.append(FIELDNAME_SESSIONKEY + DELIMITER + sessionKey + END_LINE);
			for (FileInfo f : this.files) {
				sb.append(FIELDNAME_FILE + DELIMITER + f.fileHash + "," + f.fileName + "," + f.fileSize + END_LINE);
			}
			break;
		}
		case DirMessageOps.OPERATION_FILELIST_RESPONSE: {
			for (FileInfo f : this.files) {
				sb.append(FIELDNAME_FILE + DELIMITER + f.fileHash + "," + f.fileName + "," + f.fileSize + END_LINE);
			}
			break;
		}
		case DirMessageOps.OPERATION_SEARCH: {
			sb.append(FIELDNAME_HASH + DELIMITER + hash + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_SEARCH_RESPONSE: {
			sb.append(FIELDNAME_USERS + DELIMITER + String.join(",", users) + END_LINE);
			break;
		}
		}
		sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString();
	}
}
