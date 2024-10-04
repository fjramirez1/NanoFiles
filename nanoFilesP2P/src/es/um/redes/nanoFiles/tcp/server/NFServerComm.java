package es.um.redes.nanoFiles.tcp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFServerComm {

	public static void serveFilesToClient(Socket socket) {
		/*
		 * TODO: Crear dis/dos a partir del socket
		 */
		/*
		 * TODO: Mientras el cliente esté conectado, leer mensajes de socket,
		 * convertirlo a un objeto PeerMessage y luego actuar en función del tipo de
		 * mensaje recibido, enviando los correspondientes mensajes de respuesta.
		 */
		/*
		 * TODO: Para servir un fichero, hay que localizarlo a partir de su hash (o
		 * subcadena) en nuestra base de datos de ficheros compartidos. Los ficheros
		 * compartidos se pueden obtener con NanoFiles.db.getFiles(). El método
		 * FileInfo.lookupHashSubstring es útil para buscar coincidencias de una
		 * subcadena del hash. El método NanoFiles.db.lookupFilePath(targethash)
		 * devuelve la ruta al fichero a partir de su hash completo.
		 */
		try {
			PeerMessage response = null;
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			PeerMessage msgFromClient = PeerMessage.readMessageFromInputStream(dis);
			byte opcode = msgFromClient.getOpcode();
			switch (opcode) {
			case (PeerMessageOps.OPCODE_DOWNLOAD):
				byte[] targethash = msgFromClient.getHash();
				FileInfo[] files = NanoFiles.db.getFiles();
				FileInfo[] filesWithHash = FileInfo.lookupHashSubstring(files, new String(targethash));
				if (filesWithHash.length == 0) {
					response = new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_FOUND);
					System.out.println("File with hash \"" + new String(targethash) + "\" not found");
					response.writeMessageToOutputStream(dos);
				} else if (filesWithHash.length > 1) {
					response = new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_FOUND);
					System.out.println("Hash \"" + new String(targethash) + "\" ambiguous");
					response.writeMessageToOutputStream(dos);
				} else if (filesWithHash.length == 1) {
					targethash = filesWithHash[0].fileHash.getBytes();
					String filePath = NanoFiles.db.lookupFilePath(new String(targethash));
					sendFileInChunks(filePath, dos, targethash);
				}
				break;
			default:
				System.err
						.println("Unexpected message operation: \"" + PeerMessageOps.opcodeToOperation(opcode) + "\"");
			}
		} catch (IOException e) {
			System.out.println("Server exception: " + e.getMessage());
			e.printStackTrace();
		}

	}

	private static void sendFileInChunks(String filePath, DataOutputStream dos, byte[] targethash) {
	    final int CHUNK_SIZE = 32; // tamaño del campo fijo
	    try {
	        RandomAccessFile file = new RandomAccessFile(filePath, "r");
	        long fileLength = file.length();
	        long offset = 0;

	        while (offset < fileLength) {
	            int bytesToRead = (int) Math.min(CHUNK_SIZE, fileLength - offset);
	            byte[] chunk = new byte[bytesToRead];
	            file.seek(offset);
	            file.readFully(chunk, 0, bytesToRead);

	            PeerMessage chunkMessage = new PeerMessage(PeerMessageOps.OPCODE_FILE, chunk);
	            chunkMessage.writeMessageToOutputStream(dos);

	            offset += bytesToRead;
	        }

	        file.close();
	        PeerMessage endOfFileMessage = new PeerMessage(PeerMessageOps.OPCODE_END_OF_FILE, targethash);
	        endOfFileMessage.writeMessageToOutputStream(dos);
	    } catch (IOException e) {
	        System.err.println("Error sending file: " + e.getMessage());
	        e.printStackTrace();
	    }
	}
}
