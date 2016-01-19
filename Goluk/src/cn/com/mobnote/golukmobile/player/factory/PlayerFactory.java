package cn.com.mobnote.golukmobile.player.factory;

public class PlayerFactory {

	public static final int DEFAULT_PLAYER = 0;
	public static final int EXO_PLAYER = 1;
	public static GolukPlayer createPlayer(int type) {
		GolukPlayer player = null;
		switch (type) {
		case DEFAULT_PLAYER:
			player = new GolukMediaPlayer();
			break;
		case EXO_PLAYER:
			player = new GolukExoPlayer();
			break;
		}
		return player;
	}
}
