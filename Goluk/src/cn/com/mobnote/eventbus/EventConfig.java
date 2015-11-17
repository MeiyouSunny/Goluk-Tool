package cn.com.mobnote.eventbus;

/* public for event bus operation code */
public class EventConfig {

    /* EventBus opcode for MainActivity and CarRecorderActivity */
    public final static int CAR_RECORDER_BIND_SUCESS = 400;
    public final static int LIVE_MAP_QUERY = 99;
    public final static int WIFI_STATE = 3;
    public final static int LOCATION_FINISH = 1000;

    /* Event opcode for refresh user info view */
    public final static int REFRESH_USER_INFO = 100;

    /* Event opcode for IPC update ununited */
    public final static int UPDATE_IPC_UNUNITED = 18;

	/* Event opcode for IPC update file not exist*/
	public static final int UPDATE_FILE_NOT_EXISTS = 10;
	/* Event opcode for IPC update prepare file */
	public static final int UPDATE_PREPARE_FILE = 11;
}
