package GameClient;

public class GameContext {

    private static String loggedInUsername;
    private static String gameMode;
    private static int myScore = 0;

    public static String getLoggedInUsername() {
        return loggedInUsername;
    }

    public static void setLoggedInUsername(String username) {
        // FIX: Assign the value to the variable, don't call the method again.
        GameContext.loggedInUsername = username;
    }

    public static String getGameMode() {
        return gameMode;
    }

    public static void setGameMode(String mode) {
        GameContext.gameMode = mode;
    }

    public static int getMyScore() {
        return myScore;
    }

    public static void setMyScore(int score) {
        GameContext.myScore = score;
    }
}