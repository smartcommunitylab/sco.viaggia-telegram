package viaggia.command.parking.general.utils;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
import org.telegram.telegrambots.api.objects.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Edited by Luca Mosetti
 */
public class DistanceCalculator {

    private double distance(double lat1, double lon1, double lat2, double lon2, Unit unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * unit.getValue();

        return (dist);
    }

    /**
     * This function converts decimal degrees to radians
     *
     * @param deg decimal degrees
     * @return decimal degrees to radians
     */
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /**
     * This function converts radians to decimal degrees
     *
     * @param rad radians
     * @return radians to decimal degrees
     */
    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public Map<String, Double> calculate(List<Parking> parkings, Location location, Unit unit) {
        Map<String, Double> parkingsDistances = new HashMap<>();

        for (Parking p : parkings) {
            parkingsDistances.put(p.getName(),
                    distance(
                            location.getLatitude(),
                            location.getLongitude(),
                            p.getPosition()[0],
                            p.getPosition()[1],
                            unit
                    ));
        }

        return ImmutableSortedMap.copyOf(parkingsDistances, Ordering.natural().onResultOf(Functions.forMap(parkingsDistances)));
    }
}