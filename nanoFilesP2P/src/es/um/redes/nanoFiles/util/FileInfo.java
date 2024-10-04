package es.um.redes.nanoFiles.util;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import es.um.redes.nanoFiles.shell.NFShell;

public class FileInfo {
	public String fileHash;
	public String fileName;
	public String filePath;
	public long fileSize = -1;

	public FileInfo(String hash, String name, long size, String path) {
		fileHash = hash;
		fileName = name;
		fileSize = size;
		filePath = path;
	}

	public FileInfo() {
	}

	public String toString() {
		StringBuffer strBuf = new StringBuffer();

		strBuf.append(String.format("%1$-30s", fileName));
		strBuf.append(String.format("%1$10s", fileSize));
		strBuf.append(String.format(" %1$-45s", fileHash));
		return strBuf.toString();
	}

	public static void printToSysout(FileInfo[] files) {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(String.format("%1$-30s", "Name"));
		strBuf.append(String.format("%1$10s", "Size"));
		strBuf.append(String.format(" %1$-45s", "Hash"));
		System.out.println(strBuf);
		for (FileInfo file : files) {
			System.out.println(file);
		}
	}

	/**
	 * Scans the given directory and returns an array of FileInfo objects, one for
	 * each file recursively found in the given folder and its subdirectories.
	 * 
	 * @param sharedFolderPath The folder to be scanned
	 * @return An array of file metadata (FileInfo) of all the files found
	 */
	public static FileInfo[] loadFilesFromFolder(String sharedFolderPath) {
		File folder = new File(sharedFolderPath);

		Map<String, FileInfo> files = loadFileMapFromFolder(folder);

		FileInfo[] fileinfoarray = new FileInfo[files.size()];
		Iterator<FileInfo> itr = files.values().iterator();
		int numFiles = 0;
		while (itr.hasNext()) {
			fileinfoarray[numFiles++] = itr.next();
		}
		return fileinfoarray;
	}

	/**
	 * Scans the given directory and returns a map of <filehash,FileInfo> pairs.
	 * 
	 * @param folder The folder to be scanned
	 * @return A map of the metadata (FileInfo) of all the files recursively found
	 *         in the given folder and its subdirectories.
	 */
	protected static Map<String, FileInfo> loadFileMapFromFolder(final File folder) {
		Map<String, FileInfo> files = new HashMap<String, FileInfo>();
		scanFolderRecursive(folder, files);
		return files;
	}

	private static void scanFolderRecursive(final File folder, Map<String, FileInfo> files) {
		if (folder.exists() == false) {
			System.err.println("scanFolder cannot find folder " + folder.getPath());
			return;
		}
		if (folder.canRead() == false) {
			System.err.println("scanFolder cannot access folder " + folder.getPath());
			return;
		}

		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				scanFolderRecursive(fileEntry, files);
			} else {
				String fileName = fileEntry.getName();
				String filePath = fileEntry.getPath();
				String fileHash = FileDigest.computeFileChecksumString(filePath);
				long fileSize = fileEntry.length();
				if (fileSize > 0) {
					files.put(fileHash, new FileInfo(fileHash, fileName, fileSize, filePath));
				} else {
					if (fileName.equals(NFShell.FILENAME_TEST_SHELL)) {
						NFShell.enableVerboseShell();
						System.out.println("[Enabling verbose shell]");
					} else {
						System.out.println("Ignoring empty file found in shared folder: " + filePath);
					}
				}
			}
		}
	}

	public static FileInfo[] lookupHashSubstring(FileInfo[] files, String hashSubstr) {
		String needle = hashSubstr.toLowerCase();
		Vector<FileInfo> matchingFiles = new Vector<FileInfo>();
		for (int i = 0; i < files.length; i++) {
			if (files[i].fileHash.toLowerCase().contains(needle)) {
				matchingFiles.add(files[i]);
			}
		}
		FileInfo[] result = new FileInfo[matchingFiles.size()];
		matchingFiles.toArray(result);
		return result;
	}
}
