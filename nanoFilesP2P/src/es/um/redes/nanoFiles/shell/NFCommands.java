package es.um.redes.nanoFiles.shell;

public class NFCommands {
	/**
	 * Códigos para todos los comandos soportados por el shell
	 */
	public static final byte COM_INVALID = 0;
	public static final byte COM_QUIT = 1;
	public static final byte COM_LOGIN = 2;
	public static final byte COM_USERLIST = 3;
	public static final byte COM_FILELIST = 4;
	public static final byte COM_MYFILES = 6;
	public static final byte COM_FGSERVE = 10;
	public static final byte COM_BGSERVE = 11;
	public static final byte COM_PUBLISH = 12;
	public static final byte COM_STOP_SERVER = 13;
	public static final byte COM_DOWNLOADFROM = 23;
	public static final byte COM_SEARCH = 24;
	public static final byte COM_DOWNLOAD = 25;
	public static final byte COM_LOGOUT = 30;
	public static final byte COM_SLEEP = 49;
	public static final byte COM_HELP = 50;
	public static final byte COM_SOCKET_IN = 100;


	
	/**
	 * Códigos de los comandos válidos que puede
	 * introducir el usuario del shell. El orden
	 * es importante para relacionarlos con la cadena
	 * que debe introducir el usuario y con la ayuda
	 */
	private static final Byte[] _valid_user_commands = { 
		COM_QUIT,
		COM_LOGIN,
		COM_USERLIST,
		COM_FILELIST,
		COM_MYFILES,
		COM_FGSERVE,
		COM_BGSERVE,
		COM_PUBLISH,
		COM_STOP_SERVER,
		COM_DOWNLOADFROM,
		COM_SEARCH,
		COM_DOWNLOAD,
		COM_LOGOUT,
		COM_SLEEP,
		COM_HELP,
		COM_SOCKET_IN
		};

	/**
	 * cadena exacta de cada orden
	 */
	private static final String[] _valid_user_commands_str = {
			"quit",
			"login",
			"userlist",	
			"filelist",
			"myfiles",
			"fgserve",
			"bgserve",
			"publish",
			"stopserver",
			"downloadfrom",
			"search",
			"download",
			"logout",
			"sleep",
			"help"
		};

	/**
	 * Mensaje de ayuda para cada orden
	 */
	private static final String[] _valid_user_commands_help = {
			"quit the application",
			"log into <directory> using <nickname>",
			"show list of users logged into the directory",
			"show list of files tracked by the directory",
			"show contents of local folder (files that may be served)",
			"run file server in foreground (blocking)",
			"run file server in background (non-blocking)",
			"publish list of served files to the directory",
			"stop file server running in background",
			"download from server given by <nickname> the file identified by <hash>",
			"show list of servers sharing the file identified by <hash>",
			"download the file identified by <hash> from all available server(s)",
			"log out from the current directory",
			"sleep during <num> seconds",
			"shows this information"
			};

	/**
	 * Transforma una cadena introducida en el código de comando correspondiente
	 */
	public static byte stringToCommand(String comStr) {
		//Busca entre los comandos si es válido y devuelve su código
		for (int i = 0;
		i < _valid_user_commands_str.length; i++) {
			if (_valid_user_commands_str[i].equalsIgnoreCase(comStr)) {
				return _valid_user_commands[i];
			}
		}
		//Si no se corresponde con ninguna cadena entonces devuelve el código de comando no válido
		return COM_INVALID;
	}

	public static String commandToString(byte command) {
		for (int i = 0;
		i < _valid_user_commands.length; i++) {
			if (_valid_user_commands[i] == command) {
				return _valid_user_commands_str[i];
			}
		}
		return null;
	}

	/**
	 * Imprime la lista de comandos y la ayuda de cada uno
	 */
	public static void printCommandsHelp() {
		System.out.println("List of commands:");
		for (int i = 0; i < _valid_user_commands_str.length; i++) {
			System.out.println(String.format("%1$15s", _valid_user_commands_str[i]) + " -- "
					+ _valid_user_commands_help[i]);
		}		
	}
}	

