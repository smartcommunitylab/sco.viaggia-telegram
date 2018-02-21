package mobilityservice.model;

import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class Buses extends ArrayList<Bus> {

    public void putAll(List<Route> routes) {
        Bus bus;

        for (ComparableRoute route : routes.stream().map(ComparableRoute::new).collect(Collectors.toList())) {
            bus = this.getWithShortName(route.getRouteShortName());
            if (bus == null) {
                bus = new Bus();
                this.add(bus);
            }
            bus.addRoute(route);
        }
    }

    public ComparableRoute getRouteWithId(ComparableId id) {
        for (Bus bus : this) {
            ComparableRoute route = bus.getWithId(id);
            if (route != null)
                return route;
        }

        return null;
    }

    public Bus getWithShortName(String shortName) {
        for (Bus bus : this) {
            if (bus.getRouteShortName().equals(shortName))
                return bus;
        }

        return null;
    }

    public List<String> getShortNames() {
        return this.stream().map(Bus::getRouteShortName).collect(Collectors.toList());
    }

    public Buses subBuses(String filter) {
        Buses subBuses = new Buses();

        for (Bus bus : this) {
            if (bus.getDirect().getRouteShortName().toLowerCase().contains(filter.toLowerCase()))
                subBuses.add(bus);
        }

        return subBuses;
    }

    public ComparableRoutes getDirects() {
        return this.stream().map(Bus::getDirect).collect(Collectors.toCollection(ComparableRoutes::new));
    }
}

