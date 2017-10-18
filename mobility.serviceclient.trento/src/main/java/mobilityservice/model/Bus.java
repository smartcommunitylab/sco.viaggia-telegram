package mobilityservice.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Luca Mosetti on 2017
 */
public class Bus {

    // Map.Entry<String, Map<String, ComparableRoute>

    private final static String DIRECT = "DIRECT";
    private final static String RETURN = "RETURN";

    private String routeShortName;
    private Map<String, ComparableRoute> routes = new HashMap<>(2);

    public boolean addRoute(ComparableRoute route) {
        if (routeShortName == null)
            this.routeShortName = route.getRouteShortName();

        if (!route.getRouteShortName().equals(routeShortName))
            return false;

        String id = route.getId().getId();
        switch (id.charAt(id.length() - 1)) {
            case 'c':
            case 'C':
            case 'a':
            case 'A':
                setDirect(route);
                break;

            case 'r':
            case 'R':
                setReturn(route);
                break;

            default:
                throw new IllegalArgumentException(id);
        }

        return true;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public ComparableRoute getDirect() {
        return this.routes.get(DIRECT);
    }

    private void setDirect(ComparableRoute comparableRoute) {
        this.routes.put(DIRECT, comparableRoute);
    }

    public ComparableRoute getReturn() {
        return this.routes.get(RETURN);
    }

    private void setReturn(ComparableRoute comparableRoute) {
        this.routes.put(RETURN, comparableRoute);
    }

    public ComparableRoute getWithId(ComparableId id) {
        for (Map.Entry<String, ComparableRoute> entry : routes.entrySet()) {
            if (entry.getValue().getId().equals(id))
                return entry.getValue();
        }

        return null;
    }

    private boolean hasDirect() {
        return this.routes.containsKey(DIRECT);
    }

    public boolean hasReturn() {
        return this.routes.containsKey(RETURN);
    }

    public boolean isDirect(ComparableRoute route) {
        return hasDirect() && getDirect().equals(route);
    }

    public boolean isReturn(ComparableRoute route) {
        return hasReturn() && getReturn().equals(route);
    }
}
