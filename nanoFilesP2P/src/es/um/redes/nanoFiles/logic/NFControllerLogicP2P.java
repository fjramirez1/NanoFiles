package es.um.redes.nanoFiles.logic;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;

import es.um.redes.nanoFiles.tcp.client.NFConnector;
import es.um.redes.nanoFiles.tcp.server.NFServer;
import es.um.redes.nanoFiles.tcp.server.NFServerSimple;

public class NFControllerLogicP2P{
	/*
	 * TODO: Para bgserve, se necesita un atributo NFServer que actuará como
	 * servidor de ficheros en segundo plano de este peer
	 */
	private NFServer NFServer;

	protected NFControllerLogicP2P() {
	}

	/**
	 * Método para arrancar un servidor de ficheros en primer plano.
	 * 
	 */
	protected void foregroundServeFiles() {
		/*
		 * TODO: Crear objeto servidor NFServerSimple y ejecutarlo en primer plano.
		 */

		/*
		 * TODO: Las excepciones que puedan lanzarse deben ser capturadas y tratadas en
		 * este método. Si se produce una excepción de entrada/salida (error del que no
		 * es posible recuperarse), se debe informar sin abortar el programa
		 */
		
		try {
			NFServerSimple serverSimple = new NFServerSimple();
			serverSimple.run();
		} catch (IOException e) {
			 System.err.println("* Error while running server in foreground: " + e.getMessage());
		}

	}

	/**
	 * Método para ejecutar un servidor de ficheros en segundo plano. Debe arrancar
	 * el servidor en un nuevo hilo creado a tal efecto.
	 * 
	 * @return Verdadero si se ha arrancado en un nuevo hilo con el servidor de
	 *         ficheros, y está a la escucha en un puerto, falso en caso contrario.
	 * 
	 */
	protected boolean backgroundServeFiles() {
		/*
		 * TODO: Comprobar que no existe ya un objeto NFServer previamente creado, en
		 * cuyo caso el servidor ya está en marcha. Si no lo está, crear objeto servidor
		 * NFServer y arrancarlo en segundo plano creando un nuevo hilo. Finalmente,
		 * comprobar que el servidor está escuchando en un puerto válido (>0) e imprimir
		 * mensaje informando sobre el puerto, y devolver verdadero.
		 */
		/*
		 * TODO: Las excepciones que puedan lanzarse deben ser capturadas y tratadas en
		 * este método. Si se produce una excepción de entrada/salida (error del que no
		 * es posible recuperarse), se debe informar sin abortar el programa
		 */

		if (NFServer != null && NFServer.isRunning()) {
			System.err.println("* Server is already running in the background.");
			return false;
		}

		try {
			NFServer = new NFServer();
			Thread serverThread = new Thread(NFServer);
			serverThread.start();

			if (NFServer.getPort() > 0) {
				System.out.println("* Server is listening on port " + NFServer.getPort());
				return true;
			} else {
				System.err.println("* Server failed to start.");
				NFServer = null;
				return false;
			}
		} catch (IOException e) {
			System.err.println("* Error while starting server in background: " + e.getMessage());
			e.printStackTrace();
		}
		
		return false;
	}

	/**
	 * Método para descargar un fichero del peer servidor de ficheros
	 * 
	 * @param fserverAddr    La dirección del servidor al que se conectará
	 * @param targetFileHash El hash del fichero a descargar
	 * @param localFileName  El nombre con el que se guardará el fichero descargado
	 */
	protected boolean downloadFileFromSingleServer(InetSocketAddress fserverAddr, String targetFileHash,
			String localFileName) {
		boolean result = false;
		if (fserverAddr == null) {
			System.err.println("* Cannot start download - No server address provided");
			return false;
		}
		/*
		 * TODO: Crear un objeto NFConnector para establecer la conexión con el peer
		 * servidor de ficheros, y usarlo para descargar el fichero mediante su método
		 * "downloadFile". Se debe comprobar previamente si ya existe un fichero con el
		 * mismo nombre en esta máquina, en cuyo caso se informa y no se realiza la
		 * descarga. Si todo va bien, imprimir mensaje informando de que se ha
		 * completado la descarga.
		 */
		
		/*
		 * TODO: Las excepciones que puedan lanzarse deben ser capturadas y tratadas en
		 * este método. Si se produce una excepción de entrada/salida (error del que no
		 * es posible recuperarse), se debe informar sin abortar el programa
		 */
		
		File file = new File(localFileName);
	    if (file.exists()) {
	        System.err.println("* This file already exists: " + localFileName);
	        return false;
	    }
		
		try {
	        NFConnector NFConnector = new NFConnector(fserverAddr);
	        result = NFConnector.downloadFile(targetFileHash, file);

	        if (result) {
	            System.out.println("* Download completed successfully: " + localFileName);
	        } else {
	            System.err.println("* Download failed");
	        }
	    } catch (IOException e) {
	        System.err.println("* Error during file download: " + e.getMessage());
	    }

		return result;
	}

	/**
	 * Método para descargar un fichero del peer servidor de ficheros
	 * 
	 * @param serverAddressList La lista de direcciones de los servidores a los que
	 *                          se conectará
	 * @param targetFileHash    Hash completo del fichero a descargar
	 * @param localFileName     Nombre con el que se guardará el fichero descargado
	 */
	public boolean downloadFileFromMultipleServers(LinkedList<InetSocketAddress> serverAddressList,
			String targetFileHash, String localFileName) {
		boolean downloaded = false;

		if (serverAddressList == null) {
			System.err.println("* Cannot start download - No list of server addresses provided");
			return false;
		}
		/*
		 * TODO: Crear un objeto NFConnector para establecer la conexión con cada
		 * servidor de ficheros, y usarlo para descargar un trozo (chunk) del fichero
		 * mediante su método "downloadFileChunk". Se debe comprobar previamente si ya
		 * existe un fichero con el mismo nombre en esta máquina, en cuyo caso se
		 * informa y no se realiza la descarga. Si todo va bien, imprimir mensaje
		 * informando de que se ha completado la descarga.
		 */
		/*
		 * TODO: Las excepciones que puedan lanzarse deben ser capturadas y tratadas en
		 * este método. Si se produce una excepción de entrada/salida (error del que no
		 * es posible recuperarse), se debe informar sin abortar el programa
		 */

		return downloaded;
	}

	/**
	 * Método para obtener el puerto de escucha de nuestro servidor de ficheros en
	 * segundo plano
	 * 
	 * @return El puerto en el que escucha el servidor, o 0 en caso de error.
	 */
	public int getServerPort() {
		/*
		 * TODO: Devolver el puerto de escucha de nuestro servidor de ficheros en
		 * segundo plano
		 */
		int port = NFServer.getPort();
		return port;
	}

	/**
	 * Método para detener nuestro servidor de ficheros en segundo plano
	 * 
	 */
	public void stopBackgroundFileServer() {
		/*
		 * TODO: Enviar señal para detener nuestro servidor de ficheros en segundo plano
		 */
		NFServer.stopServer();
	}

}
