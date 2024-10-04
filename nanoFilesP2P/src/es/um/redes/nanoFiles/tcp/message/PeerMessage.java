package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PeerMessage {

	private byte opcode;

	/*
	 * TODO: Añadir atributos y crear otros constructores específicos para crear
	 * mensajes con otros campos (tipos de datos)
	 * 
	 */
	private byte[] hash;
	private byte[] file_name;
	private byte[] file_data;

	public PeerMessage() {
		opcode = PeerMessageOps.OPCODE_INVALID_CODE;
	}

	public PeerMessage(byte op) {
		opcode = op;
	}

	public PeerMessage(byte op, byte[] hash, byte[] name) {
		opcode = op;
		this.hash = hash;
		file_name = name;
	}

	public PeerMessage(byte op, byte[] hashOrData) {
		opcode = op;
		if (op == PeerMessageOps.OPCODE_FILE)
			file_data = hashOrData;
		else
			hash = hashOrData;
	}

	/*
	 * TODO: Crear métodos getter y setter para obtener valores de nuevos atributos,
	 * comprobando previamente que dichos atributos han sido establecidos por el
	 * constructor (sanity checks)
	 */
	public byte getOpcode() {
		return opcode;
	}

	public byte[] getHash() {
		return hash;
	}

	public byte[] getFile_name() {
		return file_name;
	}

	public byte[] getFile_data() {
		return file_data;
	}

	public void setOpcode(byte opcode) {
		this.opcode = opcode;
	}

	public void setHash(byte[] hash) {
		this.hash = hash;
	}

	public void setFile_name(byte[] file_name) {
		this.file_name = file_name;
	}

	public void setFile_data(byte[] file_data) {
		this.file_data = file_data;
	}

	/**
	 * Método de clase para parsear los campos de un mensaje y construir el objeto
	 * DirMessage que contiene los datos del mensaje recibido
	 * 
	 * @param data El array de bytes recibido
	 * @return Un objeto de esta clase cuyos atributos contienen los datos del
	 *         mensaje recibido.
	 * @throws IOException
	 */
	public static PeerMessage readMessageFromInputStream(DataInputStream dis) throws IOException {
		/*
		 * TODO: En función del tipo de mensaje, leer del socket a través del "dis" el
		 * resto de campos para ir extrayendo con los valores y establecer los atributos
		 * del un objeto DirMessage que contendrá toda la información del mensaje, y que
		 * será devuelto como resultado. NOTA: Usar dis.readFully para leer un array de
		 * bytes, dis.readInt para leer un entero, etc.
		 */

		PeerMessage message = new PeerMessage();
		byte opcode = dis.readByte();
		message.setOpcode(opcode);
		int hashLength;
		byte[] hashBuffer;
		switch (opcode) {
		case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
			break;
		case PeerMessageOps.OPCODE_END_OF_FILE:
			hashLength = dis.readInt();
			hashBuffer = new byte[hashLength];
			dis.readFully(hashBuffer);
			message.setHash(hashBuffer);
			break;
		case PeerMessageOps.OPCODE_DOWNLOAD:
			hashLength = dis.readInt();
			hashBuffer = new byte[hashLength];
			dis.readFully(hashBuffer);
			int nameLength = dis.readInt();
			byte[] nameBuffer = new byte[nameLength];
			dis.readFully(nameBuffer);
			message.setHash(hashBuffer);
			message.setFile_name(nameBuffer);
			break;
		case PeerMessageOps.OPCODE_FILE:
			int dataLength = dis.readInt();
			byte[] dataBuffer = new byte[dataLength];
			dis.readFully(dataBuffer);
			message.setFile_data(dataBuffer);
			break;
		default:
			System.err.println("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: "
					+ PeerMessageOps.opcodeToOperation(opcode));
			System.exit(-1);
		}
		return message;
	}

	public void writeMessageToOutputStream(DataOutputStream dos) throws IOException {
		/*
		 * TODO: Escribir los bytes en los que se codifica el mensaje en el socket a
		 * través del "dos", teniendo en cuenta opcode del mensaje del que se trata y
		 * los campos relevantes en cada caso. NOTA: Usar dos.write para leer un array
		 * de bytes, dos.writeInt para escribir un entero, etc.
		 */

		dos.writeByte(opcode);
		switch (opcode) {
		case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
			break;
		case PeerMessageOps.OPCODE_END_OF_FILE:
			dos.writeInt(hash.length);
			dos.write(hash);
			break;
		case PeerMessageOps.OPCODE_DOWNLOAD:
			dos.writeInt(hash.length);
			dos.write(hash);
			dos.writeInt(file_name.length);
			dos.write(file_name);
			break;
		case PeerMessageOps.OPCODE_FILE:
			dos.writeInt(file_data.length);
			dos.write(file_data);
			break;
		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + " "
					+ "(" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}
}
