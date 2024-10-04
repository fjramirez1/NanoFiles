package es.um.redes.nanoFiles.shell;

import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

import es.um.redes.nanoFiles.application.NanoFiles;

public class NFShell {
	/**
	 * Scanner para leer comandos de usuario de la entrada estándar
	 */
	private Scanner reader;

	byte command = NFCommands.COM_INVALID;
	String[] commandArgs = new String[0];

	boolean enableComSocketIn = false;
	private boolean skipValidateArgs;

	/*
	 * Testing-related: print command to stdout (when reading commands from stdin)
	 */
	public static final String FILENAME_TEST_SHELL = ".nanofiles-test-shell";
	public static boolean enableVerboseShell = false;

	public NFShell() {
		reader = new Scanner(System.in);

		System.out.println("NanoFiles shell");
		System.out.println("For help, type 'help'");
	}

	// devuelve el comando introducido por el usuario
	public byte getCommand() {
		return command;
	}

	// Devuelve los parámetros proporcionados por el usuario para el comando actual
	public String[] getCommandArguments() {
		return commandArgs;
	}

	// Espera hasta obtener un comando válido entre los comandos existentes
	public void readGeneralCommand() {
		boolean validArgs;
		do {
			commandArgs = readGeneralCommandFromStdIn();
			// si el comando tiene parámetros hay que validarlos
			validArgs = validateCommandArguments(commandArgs);
		} while (!validArgs);
	}

	// Usa la entrada estándar para leer comandos y procesarlos
	private String[] readGeneralCommandFromStdIn() {
		String[] args = new String[0];
		Vector<String> vargs = new Vector<String>();
		while (true) {
			System.out.print("(nanoFiles@" + NanoFiles.sharedDirname + ") ");
			// obtenemos la línea tecleada por el usuario
			String input = reader.nextLine();
			StringTokenizer st = new StringTokenizer(input);
			// si no hay ni comando entonces volvemos a empezar
			if (st.hasMoreTokens() == false) {
				continue;
			}
			// traducimos la cadena del usuario en el código de comando correspondiente
			command = NFCommands.stringToCommand(st.nextToken());
			if (enableVerboseShell) {
				if (command != NFCommands.COM_SLEEP) {
					System.out.println(input);
				} else {
					System.out.println();
				}
			}
			skipValidateArgs = false;
			// Dependiendo del comando...
			switch (command) {
			case NFCommands.COM_INVALID:
				// El comando no es válido
				System.out.println("Invalid command");
				continue;
			case NFCommands.COM_HELP:
				// Mostramos la ayuda
				NFCommands.printCommandsHelp();
				continue;
			case NFCommands.COM_SLEEP:
				// Requiere un parámetro
				if (st.hasMoreTokens()) {
					int seconds = Integer.parseInt(st.nextToken());
					try {
						Thread.sleep(seconds * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				continue;
			case NFCommands.COM_QUIT:
			case NFCommands.COM_USERLIST:
			case NFCommands.COM_LOGOUT:
			case NFCommands.COM_FILELIST:
			case NFCommands.COM_PUBLISH:
			case NFCommands.COM_STOP_SERVER:
			case NFCommands.COM_MYFILES:
			case NFCommands.COM_FGSERVE:
			case NFCommands.COM_BGSERVE:
				// Estos comandos son válidos sin parámetros
				break;
			case NFCommands.COM_DOWNLOADFROM:
			case NFCommands.COM_SEARCH:
			case NFCommands.COM_DOWNLOAD:
			case NFCommands.COM_LOGIN:
				// Estos requieren un parámetro
				while (st.hasMoreTokens()) {
					vargs.add(st.nextToken());
				}
				break;
			default:
				skipValidateArgs = true;
				System.out.println("Invalid command");
				;
			}
			break;
		}
		return vargs.toArray(args);
	}

	// Algunos comandos requieren un parámetro
	// Este método comprueba si se proporciona parámetro para los comandos
	private boolean validateCommandArguments(String[] args) {
		if (skipValidateArgs)
			return false;
		switch (this.command) {
		case NFCommands.COM_LOGIN:
			if (args.length != 2) {
				System.out.println(
						"Correct use: " + NFCommands.commandToString(command) + " <directory_server> <nickname>");
				return false;
			}
			break;
		case NFCommands.COM_DOWNLOADFROM:
			if (args.length != 3) {
				System.out.println("Correct use:" + NFCommands.commandToString(command)
						+ " <nickame/IP:port> <file_hash> <local_filename>");
				return false;
			}
			break;
		case NFCommands.COM_SEARCH:
			if (args.length != 1) {
				System.out.println("Correct use: " + NFCommands.commandToString(command) + " <file_hash>");
				return false;
			}
			break;
		case NFCommands.COM_DOWNLOAD:
			if (args.length != 2) {
				System.out.println(
						"Correct use:" + NFCommands.commandToString(command) + " <file_hash> <local_filename>");
				return false;
			}
			break;
		default:
		}
		// El resto no requieren parámetro
		return true;
	}

	public static void enableVerboseShell() {
		enableVerboseShell = true;
	}
}
