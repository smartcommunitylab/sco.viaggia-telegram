package viaggia.utils;

import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
import mobilityservice.model.ComparableStop;
import org.telegram.telegrambots.api.objects.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author (edited) Luca Mosetti
 * @since 2017
 */
public class DistanceCalculator {

    /**
     * @param lat1 Latitude of point 1 (in decimal degrees)
     * @param lon1 Longitude of point 1 (in decimal degrees)
     * @param lat2 Latitude of point 2 (in decimal degrees)
     * @param lon2 Longitude of point 2 (in decimal degrees)
     * @param unit meters unit
     * @return distance between point 1 and point 2 express in unit
     */
    private static double distance(double lat1, double lon1, double lat2, double lon2, Unit unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * unit.getValue();

        return dist;
    }

    /**
     * Decimal degrees to radians
     *
     * @param deg decimal degrees
     * @return decimal degrees to radians
     */
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /**
     * Radians to decimal degrees
     *
     * @param rad radians
     * @return radians to decimal degrees
     */
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public static List<Distance<ComparableStop>> calculateStops(Unit unit, final Location user, final Set<ComparableStop> stops) {
        final List<Distance<ComparableStop>> distances = new ArrayList<>();

        for (final ComparableStop stop : stops) {
            distances.add(
                    new Distance<>(
                            stop,
                            distance(user.getLatitude(), user.getLongitude(),
                                    stop.getLatitude(), stop.getLongitude(),
                                    unit))
            );
        }

        Collections.sort(distances);
        return distances;
    }

    public static List<Distance<Parking>> calculateParkings(Unit unit, final Location user, final Set<Parking> parkings) {
        final List<Distance<Parking>> distances = new ArrayList<>();

        for (final Parking p : parkings) {
            distances.add(
                    new Distance<>(
                            p,
                            distance(user.getLatitude(), user.getLongitude(),
                                    p.getPosition()[0], p.getPosition()[1],
                                    unit))
            );
        }

        Collections.sort(distances);
        return distances;
    }
}