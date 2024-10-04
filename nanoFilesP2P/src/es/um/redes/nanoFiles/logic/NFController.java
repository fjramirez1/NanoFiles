package es.um.redes.nanoFiles.logic;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.shell.NFCommands;
import es.um.redes.nanoFiles.shell.NFShell;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFController {
	/**
	 * Diferentes estados del cliente de acuerdo con el autómata
	 */
	private static final byte LOGGED_OUT = 0;
	private static final byte LOGGED_IN = 1;
	private static final byte BGSERVE = 2;
	/*
	 * TODO: Añadir más constantes que representen los estados del autómata del
	 * cliente de directorio.
	 */

	/**
	 * Shell para leer comandos de usuario de la entrada estándar
	 */
	private NFShell shell;
	/**
	 * Último comando proporcionado por el usuario
	 */
	private byte currentCommand;

	/**
	 * Objeto controlador encargado de la comunicación con el directorio
	 */
	private NFControllerLogicDir controllerDir;
	/**
	 * Objeto controlador encargado de la comunicación con otros peers (como
	 * servidor o cliente)
	 */
	private NFControllerLogicP2P controllerPeer;

	/**
	 * El estado en que se encuentra este peer (según el autómata). El estado debe
	 * actualizarse cuando se produce un evento (comando) que supone un cambio en el
	 * autómata.
	 */
	private byte currentState;
	/**
	 * Atributos donde se establecen los argumentos pasados a los distintos comandos
	 * del shell. Estos atributos se establecen automáticamente según la orden y se
	 * deben usar para pasar los valores de los parámetros a las funciones invocadas
	 * desde este controlador.
	 */
	private String nickname; // Nick del usuario (register)
	private String directory; // Nombre/IP del host donde está el directorio (login)
	private String downloadTargetFileHash; // Hash del fichero a descargar (download)
	private String downloadLocalFileName; // Nombre con el que se guardará el fichero descargado
	private String downloadTargetServer; // nombre o IP:puerto del sevidor del que se descargará el fichero

	// Constructor
	public NFController() {
		shell = new NFShell();
		controllerDir = new NFControllerLogicDir();
		controllerPeer = new NFControllerLogicP2P();
		// Estado inicial del autómata
		currentState = LOGGED_OUT;
	}

	/**
	 * Método que procesa los comandos introducidos por un usuario. Se encarga
	 * principalmente de invocar los métodos adecuados de NFControllerLogicDir y
	 * NFControllerLogicP2P según el comando.
	 */
	public void processCommand() {

		if (!canProcessCommandInCurrentState()) {
			return;
		}
		/*
		 * En función del comando, invocar los métodos adecuados de NFControllerLogicDir
		 * y NFControllerLogicP2P, ya que son estas dos clases las que implementan
		 * realmente la lógica de cada comando y procesan la información recibida
		 * mediante la comunicación con el directorio u otros pares de NanoFiles
		 * (imprimir por pantalla el resultado de la acción y los datos recibidos,
		 * etc.).
		 */
		boolean commandSucceeded = false;
		switch (currentCommand) {
		case NFCommands.COM_MYFILES:
			showMyLocalFiles(); // Muestra los ficheros en el directorio local compartido
			break;
		case NFCommands.COM_LOGIN:
			if (NanoFiles.testMode) {
				try {
					controllerDir.testCommunicationWithDirectory(directory);
					return;
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("[testMode] An error occurred, failed to communicate with directory");
					System.exit(-1);
				}
			}

			/*
			 * Pedir al controllerDir que "inicie sesión" en el directorio, para comprobar
			 * que está activo y disponible, y registrar un nombre de usuario.
			 */
			commandSucceeded = controllerDir.doLogin(directory, nickname);
			break;
		case NFCommands.COM_LOGOUT:
			/*
			 * Pedir al controllerDir que "cierre sesión" en el directorio para dar de baja
			 * el nombre de usuario registrado (método doLogout).
			 */
			commandSucceeded = controllerDir.doLogout();
			break;
		case NFCommands.COM_USERLIST:
			/*
			 * Pedir al controllerDir que obtenga del directorio la lista de nicknames
			 * registrados, y la muestre por pantalla (método getAndPrintUserList)
			 */
			commandSucceeded = controllerDir.getAndPrintUserList();
			break;
		case NFCommands.COM_FILELIST:
			/*
			 * Pedir al controllerDir que obtenga del directorio la lista de ficheros que
			 * hay publicados (los ficheros que otros peers están sirviendo), y la imprima
			 * por pantalla (método getAndPrintFileList)
			 */
			commandSucceeded = controllerDir.getAndPrintFileList();
			break;
		case NFCommands.COM_FGSERVE:
			/*
			 * Pedir al controllerPeer que lance un servidor de ficheros en primer plano
			 * (método foregroundServeFiles). Este método no retorna...
			 */
			controllerPeer.foregroundServeFiles();
			break;
		case NFCommands.COM_PUBLISH:
			/*
			 * Pedir al controllerDir que publique en el directorio nuestra lista de
			 * ficheros disponibles (NanoFiles.db) para ser descargados desde otros peers
			 * (método publishLocalFiles)
			 */
			commandSucceeded = controllerDir.publishLocalFiles();
			break;
		case NFCommands.COM_BGSERVE:
			/*
			 * Pedir al controllerPeer que lance un servidor en segundo plano. A
			 * continuación (método backgroundServeFiles). Si el servidor se ha podido
			 * iniciar correctamente, pedir al controllerDir darnos de alta como servidor de
			 * ficheros en el directorio, indicando el puerto en el que nuestro servidor
			 * escucha conexiones de otros peers (método registerFileServer).
			 */
			boolean serverRunning = controllerPeer.backgroundServeFiles();
			if (serverRunning) {
				commandSucceeded = controllerDir.registerFileServer(controllerPeer.getServerPort());
			}
			break;
		case NFCommands.COM_STOP_SERVER:
			/*
			 * Pedir al controllerPeer que pare el servidor en segundo plano (método método
			 * stopBackgroundFileServer). A continuación, pedir al controllerDir que
			 * solicite al directorio darnos de baja como servidor de ficheros (método
			 * unregisterFileServer).
			 */
			controllerPeer.stopBackgroundFileServer();
			commandSucceeded = controllerDir.unregisterFileServer();
			break;
		case NFCommands.COM_DOWNLOADFROM:
			/*
			 * Pedir al controllerDir que obtenga del directorio la dirección de socket (IP
			 * y puerto) del servidor cuyo nickname indica el atributo downloadTargetServer
			 * (1er argumento pasado al comando en el shell). Si se ha obtenido del
			 * directorio la dirección del servidor de ficheros asociada al nick
			 * exitosamente, pedir al controllerPeer que descargue del servidor en dicha
			 * dirección el fichero indicado en downloadTargetFileHash (2º argumento pasado
			 * al comando) y lo guarde con el nombre indicado en downloadLocalFileName (3er
			 * argumento)
			 */
			InetSocketAddress serverAddr = controllerDir.getServerAddress(downloadTargetServer);
			commandSucceeded = controllerPeer.downloadFileFromSingleServer(serverAddr, downloadTargetFileHash,
					downloadLocalFileName);
			break;
		case NFCommands.COM_SEARCH:
			/*
			 * Pedir al controllerDir que obtenga del directorio y muestre los servidores
			 * que tienen disponible el fichero identificado por dicho hash (puede ser una
			 * subcadena del hash o el hash completo)
			 */
			commandSucceeded = controllerDir.getAndPrintServersNicknamesSharingThisFile(downloadTargetFileHash);
			break;
		case NFCommands.COM_DOWNLOAD:
			/*
			 * Pedir al controllerDir que obtenga del directorio la lista de nicknames de
			 * servidores que comparten el fichero indicado en downloadTargetFileHash (1er
			 * argumento pasado al comando). Si existen servidores que comparten dicho
			 * fichero, pedir al controllerPeer que descargue el fichero indicado de los
			 * servidores obtenidos, y lo guarde con el nombre indicado en
			 * downloadLocalFileName (2º argumento)
			 */
			LinkedList<InetSocketAddress> serverAddressList = controllerDir
					.getServerAddressesSharingThisFile(downloadTargetFileHash);
			commandSucceeded = controllerPeer.downloadFileFromMultipleServers(serverAddressList, downloadTargetFileHash,
					downloadLocalFileName);
			break;
		case NFCommands.COM_QUIT:
		default:
		}

		updateCurrentState(commandSucceeded);
	}

	/**
	 * Método que comprueba si se puede procesar un comando introducidos por un
	 * usuario, en función del estado del autómata en el que nos encontramos.
	 */
	public boolean canProcessCommandInCurrentState() {
		/*
		 * TODO: Para cada comando tecleado en el shell (currentCommand), comprobar
		 * "currentState" para ver si dicho comando es válido según el estado actual del
		 * autómata, ya que no todos los comandos serán válidos en cualquier estado.
		 * Este método NO debe modificar clientStatus.
		 */
		boolean commandAllowed = true;
		switch (currentCommand) {
		case NFCommands.COM_MYFILES: {
			commandAllowed = true;
			break;
		}
		case NFCommands.COM_LOGIN:
			if (currentState != LOGGED_OUT) {
				commandAllowed = false;
				System.err.println("* You cannot login because you are not logged out from the directory");
			}
			break;
		case NFCommands.COM_LOGOUT:
			if (currentState == LOGGED_OUT) {
				commandAllowed = false;
				System.err.println("* You cannot logout because you are not logged into the directory");
			} else if (currentState == BGSERVE) {
				commandAllowed = false;
				System.err.println("* You cannot logout because server is running");
			}
			break;
		case NFCommands.COM_QUIT:
			if (currentState != LOGGED_OUT) {
				commandAllowed = false;
				System.err.println("* You cannot quit because you are not logged out from the directory");
			}
			break;
		case NFCommands.COM_USERLIST:
			if (currentState == LOGGED_OUT) {
				commandAllowed = false;
				System.err.println("* You cannot get the list of users because you are not logged into the directory");
			}
			break;
		case NFCommands.COM_DOWNLOADFROM:
			if (currentState == LOGGED_OUT) {
				commandAllowed = false;
				System.err.println("* You cannot downloadfrom because you are not logged into the directory");
			}
			break;
		case NFCommands.COM_BGSERVE:
			if (currentState == LOGGED_OUT) {
				commandAllowed = false;
				System.err.println("* You cannot run the server because you are not logged into the directory");
			} else if (currentState == BGSERVE) {
				commandAllowed = false;
				System.err.println("* You cannot run the server because it is already running");
			}
			break;
		case NFCommands.COM_STOP_SERVER: {
			if (currentState != BGSERVE) {
				commandAllowed = false;
				System.err.println("* You cannot stop the server because it is not running");
			}
			break;
		}
		case NFCommands.COM_PUBLISH:
			if (currentState == LOGGED_OUT) {
				commandAllowed = false;
				System.err.println("* You cannot publish your files because you are not logged into the directory");
			} else if (currentState == LOGGED_IN) {
				commandAllowed = false;
				System.err.println("* You cannot publish your files because server is not running");
			}
			break;
		case NFCommands.COM_SEARCH:
			if (currentState == LOGGED_OUT) {
				commandAllowed = false;
				System.err.println("* You cannot search the file because you are not logged into the directory");
			}
			break;
		default:
			// System.err.println("ERROR: undefined behaviour for " + currentCommand + "
			// command!");
		}
		return commandAllowed;
	}

	private void updateCurrentState(boolean success) {
		/*
		 * TODO: Si el comando ha sido procesado con éxito, debemos actualizar
		 * currentState de acuerdo con el autómata diseñado para pasar al siguiente
		 * estado y así permitir unos u otros comandos en cada caso.
		 */
		if (!success) {
			return;
		}
		switch (currentCommand) {
		case NFCommands.COM_LOGIN: {
			currentState = LOGGED_IN;
			break;
		}
		case NFCommands.COM_LOGOUT: {
			currentState = LOGGED_OUT;
			break;
		}
		case NFCommands.COM_BGSERVE: {
			currentState = BGSERVE;
			break;
		}
		case NFCommands.COM_STOP_SERVER: {
			currentState = LOGGED_IN;
			break;
		}
		default:
		}

	}

	private void showMyLocalFiles() {
		System.out.println("List of files in local folder:");
		FileInfo.printToSysout(NanoFiles.db.getFiles());
	}

	/**
	 * Método que comprueba si el usuario ha introducido el comando para salir de la
	 * aplicación
	 */
	public boolean shouldQuit() {
		return currentCommand == NFCommands.COM_QUIT && currentState == LOGGED_OUT;
	}

	/**
	 * Establece el comando actual
	 * 
	 * @param command el comando tecleado en el shell
	 */
	private void setCurrentCommand(byte command) {
		currentCommand = command;
	}

	/**
	 * Registra en atributos internos los posibles parámetros del comando tecleado
	 * por el usuario.
	 */
	private void setCurrentCommandArguments(String[] args) {
		switch (currentCommand) {
		case NFCommands.COM_LOGIN:
			directory = args[0];
			nickname = args[1];
			break;
		case NFCommands.COM_SEARCH:
			downloadTargetFileHash = args[0];
			break;
		case NFCommands.COM_DOWNLOADFROM:
			downloadTargetServer = args[0];
			downloadTargetFileHash = args[1];
			downloadLocalFileName = args[2];
			break;
		case NFCommands.COM_DOWNLOAD:
			downloadTargetFileHash = args[0];
			downloadLocalFileName = args[1];
			break;
		default:
		}
	}

	/**
	 * Método para leer un comando general
	 */
	public void readGeneralCommandFromShell() {
		// Pedimos el comando al shell
		shell.readGeneralCommand();
		// Establecemos que el comando actual es el que ha obtenido el shell
		setCurrentCommand(shell.getCommand());
		// Analizamos los posibles parámetros asociados al comando
		setCurrentCommandArguments(shell.getCommandArguments());
	}

}
