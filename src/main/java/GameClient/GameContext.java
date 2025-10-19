package GameClient;

public class GameContext {

    private static String loggedInUsername;
    private static String gameMode; // Will be "NORMAL" or "CS"

    public static String getLoggedInUsername() {
        return loggedInUsername;
    }

    public static void setLoggedInUsername(String loggedInUsername) {
        GameContext.loggedInUsername = loggedInUsername;
    }

    public static String getGameMode() {
        return gameMode;
    }

    public static void setGameMode(String gameMode) {
        GameContext.gameMode = gameMode;
    }
}