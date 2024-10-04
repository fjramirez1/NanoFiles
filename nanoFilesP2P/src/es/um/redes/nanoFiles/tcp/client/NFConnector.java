package es.um.redes.nanoFiles.tcp.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileDigest;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor
public class NFConnector {
	private Socket socket;
	private InetSocketAddress serverAddr;
	private DataInputStream dis;
	private DataOutputStream dos;

	public NFConnector(InetSocketAddress fserverAddr) throws UnknownHostException, IOException {
		serverAddr = fserverAddr;
		/*
		 * TODO Se crea el socket a partir de la dirección del servidor (IP, puerto). La
		 * creación exitosa del socket significa que la conexión TCP ha sido
		 * establecida.
		 */

		socket = new Socket(fserverAddr.getAddress(), fserverAddr.getPort());

		/*
		 * TODO Se crean los DataInputStream/DataOutputStream a partir de los streams de
		 * entrada/salida del socket creado. Se usarán para enviar (dos) y recibir (dis)
		 * datos del servidor.
		 */

		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());

	}

	/**
	 * Método para descargar un fichero a través del socket mediante el que estamos
	 * conectados con un peer servidor.
	 * 
	 * @param targetFileHashSubstr Subcadena del hash del fichero a descargar
	 * @param file                 El objeto File que referencia el nuevo fichero
	 *                             creado en el cual se escribirán los datos
	 *                             descargados del servidor
	 * @return Verdadero si la descarga se completa con éxito, falso en caso
	 *         contrario.
	 * @throws IOException Si se produce algún error al leer/escribir del socket.
	 */
	public boolean downloadFile(String targetFileHashSubstr, File file) throws IOException {
		boolean downloaded = false;
		/*
		 * TODO: Construir objetos PeerMessage que modelen mensajes con los valores
		 * adecuados en sus campos (atributos), según el protocolo diseñado, y enviarlos
		 * al servidor a través del "dos" del socket mediante el método
		 * writeMessageToOutputStream.
		 */
		/*
		 * TODO: Recibir mensajes del servidor a través del "dis" del socket usando
		 * PeerMessage.readMessageFromInputStream, y actuar en función del tipo de
		 * mensaje recibido, extrayendo los valores necesarios de los atributos del
		 * objeto (valores de los campos del mensaje).
		 */
		/*
		 * TODO: Para escribir datos de un fichero recibidos en un mensaje, se puede
		 * crear un FileOutputStream a partir del parámetro "file" para escribir cada
		 * fragmento recibido (array de bytes) en el fichero mediante el método "write".
		 * Cerrar el FileOutputStream una vez se han escrito todos los fragmentos.
		 */
		/*
		 * NOTA: Hay que tener en cuenta que puede que la subcadena del hash pasada como
		 * parámetro no identifique unívocamente ningún fichero disponible en el
		 * servidor (porque no concuerde o porque haya más de un fichero coincidente con
		 * dicha subcadena)
		 */
		/*
		 * TODO: Finalmente, comprobar la integridad del fichero creado para comprobar
		 * que es idéntico al original, calculando el hash a partir de su contenido con
		 * FileDigest.computeFileChecksumString y comparándolo con el hash completo del
		 * fichero solicitado. Para ello, es necesario obtener del servidor el hash
		 * completo del fichero descargado, ya que quizás únicamente obtuvimos una
		 * subcadena del mismo como parámetro.
		 */

		PeerMessage request = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD, targetFileHashSubstr.getBytes(),
				file.getName().getBytes());
		request.writeMessageToOutputStream(dos);

		FileOutputStream fos = new FileOutputStream(file);

		boolean endOfFile = false;
		while (!endOfFile) {
			PeerMessage response = PeerMessage.readMessageFromInputStream(dis);
			switch (response.getOpcode()) {
			case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
				System.err.println("* File not found in server.");
				endOfFile = true;
				break;
			case PeerMessageOps.OPCODE_FILE:
				fos.write(response.getFile_data(), 0, response.getFile_data().length);
				break;
			case PeerMessageOps.OPCODE_END_OF_FILE:
				endOfFile = true;
				downloaded = true;

				String downloadedFileHash = FileDigest.computeFileChecksumString(file.getPath());
				String hash = new String(response.getHash());
				if (downloadedFileHash.equals(hash)) {
					downloaded = true;
				} else {
					System.err.println("* Failed while cheking file integrity.");
				}
				break;
			default:
				System.err.println("* Unexpected response opcode: " + response.getOpcode());
				endOfFile = true;
				break;
			}
		}

		fos.close();

		return downloaded;
	}

	public InetSocketAddress getServerAddr() {
		return serverAddr;
	}

}
