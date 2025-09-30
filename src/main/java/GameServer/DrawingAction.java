package GameServer;

import java.io.Serializable;

/**
 * A serializable class to represent a single drawing action.
 * This object will be sent from the server to clients.
 */
public class DrawingAction implements Serializable {
    // A unique version ID for serialization
    private static final long serialVersionUID = 1L;

    public enum ActionType {
        DRAW,
        ERASE,
        CLEAR
    }

    private final GameClient.DrawingAction.ActionType type;
    private final double x;
    private final double y;
    private final double size;
    private final GameClient.DrawingAction.SerializableColor color;

    // Constructor for DRAW and ERASE actions
    public DrawingAction(GameClient.DrawingAction.ActionType type, double x, double y, double size, GameClient.DrawingAction.SerializableColor color) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.size = size;
        this.color = color;
    }

    // Constructor for CLEAR action
    public DrawingAction() {
        this.type = GameClient.DrawingAction.ActionType.CLEAR;
        this.x = 0;
        this.y = 0;
        this.size = 0;
        this.color = null;
    }

    // Getters for all fields
    public GameClient.DrawingAction.ActionType getType() { return type; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getSize() { return size; }
    public GameClient.DrawingAction.SerializableColor getColor() { return color; }

    /**
     * A helper class to make JavaFX's Color serializable.
     */
    public static class SerializableColor implements Serializable {
        private static final long serialVersionUID = 1L;
        private final double red;
        private final double green;
        private final double blue;
        private final double opacity;

        public SerializableColor(double red, double green, double blue, double opacity) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.opacity = opacity;
        }

        public double getRed() { return red; }
        public double getGreen() { return green; }
        public double getBlue() { return blue; }
        public double getOpacity() { return opacity; }
    }
}