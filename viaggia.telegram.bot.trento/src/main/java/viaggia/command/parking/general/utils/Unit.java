package viaggia.command.parking.general.utils;

/**
 * Created by Luca Mosetti on 2017
 */
public enum Unit {

    KILOMETER(1.609344, "km"),
    METER(1.609344 * 100, "m"),
    NAUTICAL_MILES(0.8684, "nmi");

    private double value;
    private String abbreviation;

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
