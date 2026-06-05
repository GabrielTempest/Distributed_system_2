package distsys26.local_eoc.enums;

/**
 * Enumerations for alert levels (color code) in the early warning system.
 */
public enum AlertLevel {
    GREEN,
    YELLOW,
    ORANGE,
    RED;

    public static AlertLevel fromInt(int level) {
        return switch (level) {
            case 0 -> GREEN;
            case 1 -> YELLOW;
            case 2 -> ORANGE;
            case 3 -> RED;
            default -> throw new IllegalArgumentException("Invalid alert level: " + level);
        };
    }

    public int toInt() {
        return switch (this) {
            case GREEN -> 0;
            case YELLOW -> 1;
            case ORANGE -> 2;
            case RED -> 3;
        };
    }
}
