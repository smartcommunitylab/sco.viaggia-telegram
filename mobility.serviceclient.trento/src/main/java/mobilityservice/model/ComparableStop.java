package mobilityservice.model;

import it.sayservice.platform.smartplanner.data.message.otpbeans.Stop;

/**
 * @author Luca Mosetti
 * @since 02/2018
 */
public class ComparableStop implements Comparable<ComparableStop> {
    private final int wheelChairBoarding;
    private final String id;
    private final String name;
    private final double latitude;
    private final double longitude;

    public ComparableStop(Stop stop) {
        this.wheelChairBoarding = stop.getWheelChairBoarding();
        this.id = stop.getId();
        this.name = stop.getName();
        this.latitude = stop.getLatitude();
        this.longitude = stop.getLongitude();
    }

    ComparableStop(String stopId) {
        this.wheelChairBoarding = 0;
        this.id = stopId;
        this.name = stopId;
        this.latitude = 0;
        this.longitude = 0;
    }

    public String toString() {
        return this.getName().length() > 0 ? this.getName() : super.toString();
    }

    public int getWheelChairBoarding() {
        return wheelChairBoarding;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj != null && getClass() == obj.getClass() && (getId().equals(((ComparableStop) obj).getId()));
    }

    @Override
    public int compareTo(ComparableStop obj) {
        return getId().compareTo(obj.getId());
    }
}
