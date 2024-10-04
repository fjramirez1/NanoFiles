package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class PeerMessageTest {

	public static void main(String[] args) throws IOException {
		String nombreArchivo = "peermsg.bin";

		byte[] hash = "12345678901234567890123456789012".getBytes();
		byte[] name = "example.txt".getBytes();
		//byte[] data = "hola".getBytes();

		DataOutputStream fos = new DataOutputStream(new FileOutputStream(nombreArchivo));

		/*
		 * TODO: Probar a crear diferentes tipos de mensajes (con los opcodes válidos
		 * definidos en PeerMessageOps), estableciendo los atributos adecuados a cada
		 * tipo de mensaje. Luego, escribir el mensaje a un fichero con
		 * writeMessageToOutputStream para comprobar que readMessageFromInputStream
		 * construye un mensaje idéntico al original.
		 */

		//PeerMessage msgOut = new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_FOUND);
		PeerMessage msgOut = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD, hash, name);
		//PeerMessage msgOut = new PeerMessage(PeerMessageOps.OPCODE_FILE, data);
		msgOut.writeMessageToOutputStream(fos);

		DataInputStream fis = new DataInputStream(new FileInputStream(nombreArchivo));
		PeerMessage msgIn = PeerMessage.readMessageFromInputStream((DataInputStream) fis);
		/*
		 * TODO: Comprobar que coinciden los valores de los atributos relevantes al tipo
		 * de mensaje en ambos mensajes (msgOut y msgIn), empezando por el opcode.
		 */

		switch (msgOut.getOpcode()) {
		case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
			System.out.println("out code: " + msgOut.getOpcode());
			System.out.println("in code: " + msgIn.getOpcode());

			if (msgOut.getOpcode() != msgIn.getOpcode()) {
				System.err.println("Opcode does not match!");
			} else {
				System.out.println("Messages match!");
			}
			break;

		case PeerMessageOps.OPCODE_DOWNLOAD:
			System.out.println("out code: " + msgOut.getOpcode());
			System.out.println("in code: " + msgIn.getOpcode());
			System.out.println("out hash: " + bytesToHex(msgOut.getHash()));
			System.out.println("in hash: " + bytesToHex(msgIn.getHash()));
			System.out.println("out file name: " + new String(msgOut.getFile_name()));
			System.out.println("in file name: " + new String(msgIn.getFile_name()));

			if (msgOut.getOpcode() != msgIn.getOpcode()) {
				System.err.println("Opcode does not match!");
			} else if (!Arrays.equals(msgOut.getHash(), msgIn.getHash())) {
				System.err.println("Hash does not match!");
			} else if (!Arrays.equals(msgOut.getFile_name(), msgIn.getFile_name())) {
				System.err.println("File name does not match!");
			} else {
				System.out.println("Messages match!");
			}
			break;
		case PeerMessageOps.OPCODE_FILE:
			System.out.println("out code: " + msgOut.getOpcode());
			System.out.println("in code: " + msgIn.getOpcode());
			System.out.println("out data: " + new String(msgOut.getFile_data()));
			System.out.println("in data: " + new String(msgOut.getFile_data()));

			if (msgOut.getOpcode() != msgIn.getOpcode()) {
				System.err.println("Opcode does not match!");
			} else if (!Arrays.equals(msgOut.getHash(), msgIn.getHash())) {
				System.err.println("Data does not match!");
			} else {
				System.out.println("Messages match!");
			}
			break;

		default:
			throw new IllegalArgumentException("Unexpected value: " + msgOut.getOpcode());
		}
	}

	public static String bytesToHex(byte[] bytes) {
		StringBuilder string = new StringBuilder();
		for (byte b : bytes) {
			string.append(String.format("%02x", b));
		}
		return string.toString();
	}

}
