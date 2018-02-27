package viaggia.utils;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public enum Unit {

    KILOMETER(1.609344, "km"),
    METER(KILOMETER.getValue() * 1000, "m"),
    NAUTICAL_MILES(0.8684, "nmi");

    private final double value;
    private final String abbreviation;

    Unit(double value, String abbreviation) {
        this.value = value;
        this.abbreviation = abbreviation;
    }

    public double getValue() {
        return value;
    }

    public String getAbbreviation() {
        return abbreviation;
    }
}
