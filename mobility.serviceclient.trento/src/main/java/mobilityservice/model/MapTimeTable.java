package mobilityservice.model;

import eu.trentorise.smartcampus.mobilityservice.model.Delay;
import eu.trentorise.smartcampus.mobilityservice.model.TimeTable;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Luca Mosetti on 2017
 */
public class MapTimeTable {

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final int MAX_STOPS_FILTRED = 4;

    private final List<String> tripIds;
    private final List<String> routeIds;
    private final List<String> stops;
    private final List<String> stopsId;
    private final List<List<LocalTime>> times;
    private final List<Delay> delays;

    public MapTimeTable(TimeTable timeTable) {
        this.tripIds = timeTable.getTripIds();
        this.routeIds = timeTable.getRouteIds();
        this.stops = timeTable.getStops();
        this.stopsId = timeTable.getStopsId();
        this.times = new ArrayList<>(timeTable.getTimes().size());
        this.delays = timeTable.getDelays();

        for (List<String> strings : timeTable.getTimes()) {
            this.times.add(correctTimes(stringsToTime(strings.subList(0, stops.size() <= strings.size() ? stops.size() : strings.size()))));
        }

        clear();
    }

    private MapTimeTable(MapTimeTable mapTimeTable) {
        this.tripIds = mapTimeTable.tripIds;
        this.routeIds = mapTimeTable.routeIds;
        this.stops = mapTimeTable.stops;
        this.stopsId = mapTimeTable.stopsId;
        this.times = new ArrayList<>();
        this.delays = mapTimeTable.delays;
    }

    private List<LocalTime> stringsToTime(List<String> strings) {
        List<LocalTime> tmp = new ArrayList<>(strings.size());
        for (String string : strings) {
            tmp.add(string.isEmpty() ? null : LocalTime.parse(string, TIME_FORMATTER));
        }

        return tmp;
    }

    private List<LocalTime> correctTimes(List<LocalTime> localTimes) {
        LocalTime pre = LocalTime.MIN;
        LocalTime tmp;

        for (int i = 1; i < localTimes.size(); i++) {
            tmp = localTimes.get(i);
            if (tmp != null) {
                if (pre.isAfter(tmp)) localTimes.set(i, pre);
                pre = tmp;
            }
        }

        return localTimes;
    }

    /**
     * remove those trips which:
     * - are empty trips
     * - are one stop trips
     * - have nearby duplicated trips
     */
    private void clear() {
        List<LocalTime> tmp = null;

        for (int i = 0; i < this.times.size(); i++) {
            List<LocalTime> toCheck = this.times.get(i);
            if (isEmpty(toCheck) || toCheck.equals(tmp))
                this.times.remove(i);
            tmp = toCheck;
        }
    }

    private boolean isEmpty(List<LocalTime> times) {
        int tmp = 0;

        for (LocalTime time : times) {
            if (time != null) {
                tmp++;
                if (tmp == 1 && stops.size() == 1) return false;
                if (tmp == 2) return false;
            }
        }

        return true;
    }

    public List<String> getTripIds() {
        return tripIds;
    }

    public List<String> getRouteIds() {
        return routeIds;
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
                clone.getTimes().add(subListFrom(time, stopIndex, MAX_STOPS_FILTRED));
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
