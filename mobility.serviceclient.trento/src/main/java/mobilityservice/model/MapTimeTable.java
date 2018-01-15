package mobilityservice.model;

import eu.trentorise.smartcampus.mobilityservice.model.Delay;
import eu.trentorise.smartcampus.mobilityservice.model.TimeTable;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Luca Mosetti in 2017
 */
public class MapTimeTable {

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final int MAX_STOPS_FILTERED = 5;

    private final List<String> tripIds;
    private final List<String> stops;
    private final List<String> stopsId;
    private final List<List<LocalTime>> times;
    private final List<Delay> delays;

    public MapTimeTable(TimeTable timeTable) {
        this.tripIds = timeTable.getTripIds();
        this.stops = timeTable.getStops();
        this.stopsId = timeTable.getStopsId();
        this.times = new ArrayList<>(timeTable.getTimes().size());
        this.delays = timeTable.getDelays();

        for (List<String> strings : timeTable.getTimes()) {
            LocalTime[] tmp = new LocalTime[stops.size()];

            if (fillArray(tmp, strings))
                this.times.add(Arrays.asList(tmp));
        }
    }

    private boolean fillArray(LocalTime[] times, List<String> strings) {
        int notNull = 0;

        LocalTime pre = LocalTime.MIN;

        for (int i = 0, length = times.length < strings.size() ? times.length : strings.size(); i < length; i++) {
            String string = strings.get(i);
            if (string.isEmpty()) {
                times[i] = null;
            } else {
                times[i] = LocalTime.parse(string, TIME_FORMATTER);
                if (pre.isAfter(times[i])) times[i] = pre;

                pre = times[i];
                notNull++;
            }
        }

        return notNull >= 2;
    }

    private MapTimeTable(MapTimeTable mapTimeTable) {
        this.tripIds = mapTimeTable.tripIds;
        this.stops = mapTimeTable.stops;
        this.stopsId = mapTimeTable.stopsId;
        this.times = new ArrayList<>();
        this.delays = mapTimeTable.delays;
    }

    public List<String> getTripIds() {
        return tripIds;
    }

    public List<String> getStops() {
        return stops;
    }

    public List<String> getStopsId() {
        return stopsId;
    }

    public List<List<LocalTime>> getTimes() {
        return times;
    }

    public List<Delay> getDelays() {
        return delays;
    }

    public MapTimeTable subMapTimeTable(String stopId) {
        MapTimeTable clone = new MapTimeTable(this);

        int stopIndex = clone.getStopsId().indexOf(stopId);

        if (stopIndex < 0 || stopIndex > stops.size())
            return this;

        for (List<LocalTime> time : this.times) {
            if (time.get(stopIndex) != null) {
                clone.getTimes().add(subListFrom(time, stopIndex, MAX_STOPS_FILTERED));
            }
        }

        return clone;
    }

    private List<LocalTime> subListFrom(List<LocalTime> list, int begin, int max) {
        int tmp = 0;
        List<LocalTime> result = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            result.add(null);
        }

        for (int i = begin; i < list.size() && tmp < max; i++) {
            if (list.get(i) != null) {
                result.set(i, list.get(i));
                tmp++;
            }
        }

        return result;
    }
}
