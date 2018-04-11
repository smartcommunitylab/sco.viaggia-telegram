package mobilityservice.model;

import eu.trentorise.smartcampus.mobilityservice.model.Delay;
import eu.trentorise.smartcampus.mobilityservice.model.TimeTable;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.IntStream;

/**
 * @author Luca Mosetti
 * @since 02/2018
 */
public class MapTimeTable {

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final int MAX_STOPS_FILTERED = 5;

    private final ComparableId routeId;
    private final List<Trip> trips;
    private final LinkedHashMap<String, ComparableStop> stopsMap;
    private final Map<String, List<Map.Entry<String, LocalTime>>> times;

    private MapTimeTable(MapTimeTable m) {
        this.routeId = m.routeId;
        this.trips = new ArrayList<>();
        this.stopsMap = m.stopsMap;
        this.times = new HashMap<>();
    }

    public MapTimeTable(ComparableId routeId, List<ComparableStop> stops, TimeTable timeTable) {
        this.routeId = routeId;
        this.stopsMap = new LinkedHashMap<>();
        stops.forEach(stop -> this.stopsMap.put(stop.getId(), stop));
        this.trips = new ArrayList<>();
        this.times = new HashMap<>();

        List<String> actualStops = new ArrayList<>();

        if (timeTable != null) {
            for (int i = 0, size = timeTable.getTripIds().size(); i < size; i++) {
                trips.add(i, new Trip(timeTable.getTripIds().get(i), timeTable.getDelays().get(i)));
            }

            for (int t = 0; t < trips.size(); t++) {
                String tripId = trips.get(t).getTripId();
                times.put(tripId, new ArrayList<>());

                for (int i = 0, size = timeTable.getStopsId().size(); i < size; i++) {
                    String stopId = timeTable.getStopsId().get(i);
                    LocalTime tmp = tryParse(timeTable.getTimes().get(t), i);

                    if (tmp != null) {
                        if (this.stopsMap.containsKey(stopId)) actualStops.add(stopId);
                        else stopId = timeTable.getStops().get(i);
                        times.get(tripId).add(new AbstractMap.SimpleEntry<>(stopId, tmp));
                    }
                }

                if (times.get(tripId).size() < 2) {
                    times.remove(trips.get(t).getTripId());
                    trips.remove(t);
                }
            }
        }

        for (String stopID : actualStops) {
            if (!this.stopsMap.containsKey(stopID))
                System.out.println(routeId.getId() + " : " + stopID);
        }

        this.stopsMap.keySet().retainAll(actualStops);
    }

    private LocalTime tryParse(List<String> strings, int i) {
        try {
            return LocalTime.parse(strings.get(i), TIME_FORMATTER);
        } catch (Throwable e) {
            return null;
        }
    }

    private List<Map.Entry<String, LocalTime>> filtered(List<Map.Entry<String, LocalTime>> entries, int fromIndex) {
        return entries.subList(fromIndex, fromIndex + MAX_STOPS_FILTERED > entries.size() ? entries.size() : fromIndex + MAX_STOPS_FILTERED);
    }

    public ComparableId getRouteId() {
        return routeId;
    }

    public List<Trip> getTrips() {
        return trips;
    }

    public Collection<ComparableStop> getStops() {
        return stopsMap.values();
    }

    public ComparableStop getStop(String stopId) {
        if (!stopsMap.containsKey(stopId)) return new ComparableStop(stopId);
        return stopsMap.get(stopId);
    }

    public Map<String, List<Map.Entry<String, LocalTime>>> getTimes() {
        return times;
    }

    public MapTimeTable subMapTimeTable(String stopId) {
        MapTimeTable clone = new MapTimeTable(this);
        int stopIndex;

        for (Trip trip : this.trips) {
            List<Map.Entry<String, LocalTime>> times = this.getTimes().get(trip.getTripId());

            if ((stopIndex = IntStream.range(0, times.size()).filter(i -> times.get(i).getKey().equals(stopId)).findFirst().orElse(-1)) != -1) {
                clone.getTrips().add(trip);
                clone.getTimes().put(trip.getTripId(), filtered(times, stopIndex));
            }
        }

        return clone;
    }

    public void updateDelays(List<Delay> delays) {
        int size = this.getTrips().size() < delays.size() ? this.getTrips().size() : delays.size();
        for (int i = 0; i < size; i++) {
            trips.get(i).setDelay(delays.get(i));
        }
    }
}
