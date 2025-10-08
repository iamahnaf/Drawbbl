package GameClient;

import java.io.Serializable;

/**
 * A serializable class to represent a single drawing action.
 * This object will be sent from the server to clients.
 */
public class DrawingAction implements Serializable {
    private static final long serialVersionUID = 2L; // Changed version UID

    public enum ActionType {
        // Renamed DRAW to MOVE, and added BEGIN_PATH for clarity
        BEGIN_PATH, // Action for when the mouse is first pressed
        MOVE,       // Action for when the mouse is dragged
        ERASE,
        CLEAR
    }

    private final ActionType type;
    // 'from' coordinates for the start of a line segment
    private final double fromX;
    private final double fromY;
    // 'to' coordinates for the end of a line segment
    private final double toX;
    private final double toY;
    private final double size;
    private final SerializableColor color;

    // Constructor for MOVE, BEGIN_PATH, and ERASE actions
    public DrawingAction(ActionType type, double fromX, double fromY, double toX, double toY, double size, SerializableColor color) {
        this.type = type;
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.size = size;
        this.color = color;
    }

    // Constructor for CLEAR action
    public DrawingAction() {
        this.type = ActionType.CLEAR;
        this.fromX = 0; this.fromY = 0;
        this.toX = 0; this.toY = 0;
        this.size = 0; this.color = null;
    }

    // Getters for all fields
    public ActionType getType() { return type; }
    public double getFromX() { return fromX; }
    public double getFromY() { return fromY; }
    public double getToX() { return toX; }
    public double getToY() { return toY; }
    public double getSize() { return size; }
    public SerializableColor getColor() { return color; }

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