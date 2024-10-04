package es.um.redes.nanoFiles.udp.message;

public class DirMessageOps {

	/*
	 * TODO: Añadir aquí todas las constantes que definen los diferentes tipos de
	 * mensajes del protocolo de comunicación con el directorio.
	 */
	public static final String OPERATION_INVALID = "invalid_operation";
	public static final String OPERATION_LOGIN = "login";
	public static final String OPERATION_LOGIN_OK = "login_ok";
	public static final String OPERATION_LOGIN_FAILED = "login_failed";
	public static final String OPERATION_USERLIST = "userlist";
	public static final String OPERATION_USERLIST_RESPONSE = "userlist_response";
	public static final String OPERATION_REGISTER_SERVER = "register_server";
	public static final String OPERATION_REGISTER_SERVER_OK = "register_server_ok";
	public static final String OPERATION_REGISTER_SERVER_FAILED = "register_server_failed";
	public static final String OPERATION_LOGOUT = "logout";
	public static final String OPERATION_LOGOUT_OK = "logout_ok";
	public static final String OPERATION_LOGOUT_FAILED = "logout_failed";
	public static final String OPERATION_LOOKUP_USERNAME = "lookup_username";
	public static final String OPERATION_LOOKUP_RESPONSE = "lookup_response";
	public static final String OPERATION_PUBLISH_FILES = "publish_files";
	public static final String OPERATION_PUBLISH_FILES_OK = "publish_files_ok";
	public static final String OPERATION_PUBLISH_FILES_FAILED = "publish_files_failed";
	public static final String OPERATION_FILELIST = "filelist";
	public static final String OPERATION_FILELIST_RESPONSE = "filelist_response";
	public static final String OPERATION_SEARCH = "search";
	public static final String OPERATION_SEARCH_RESPONSE = "search_response";
}
