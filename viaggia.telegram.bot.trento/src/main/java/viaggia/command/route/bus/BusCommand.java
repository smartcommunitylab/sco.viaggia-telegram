package viaggia.command.route.bus;

import gekoramy.telegram.bot.keyboard.InlineKeyboardMarkupBuilder;
import gekoramy.telegram.bot.model.Command;
import mobilityservice.model.*;
import org.telegram.telegrambots.api.objects.Location;
import viaggia.command.route.AbsRouteCommand;
import viaggia.command.route.general.query.RouteQueryBuilder;
import viaggia.command.route.general.utils.Mode;
import viaggia.exception.NotHandledException;
import viaggia.utils.Distance;
import viaggia.utils.DistanceCalculator;
import viaggia.utils.Unit;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class BusCommand extends AbsRouteCommand {
    private static final Command COMMAND_ID = new Command("bus", "bus_description");
    private final BusDataManagement data = new BusDataManagement();

    public BusCommand() {
        super(COMMAND_ID, Mode.SHORT_NAME);
    }

    @Override
    public void init() {
    }

    private Bus getBus(ComparableId routeId) throws ExecutionException, NotHandledException {
        Bus bus = data.getBusRoutes().getWithRouteId(routeId);
        if (bus == null) throw new NotHandledException();

        return bus;
    }

    /**
     * @param shortName route.getShortName
     * @return bus.getDirects()
     * @throws ExecutionException  cannot download
     * @throws NotHandledException no route found
     */
    @Override
    protected ComparableRoute getRoute(String shortName) throws ExecutionException, NotHandledException {
        Bus bus = data.getBusRoutes().getWithShortName(shortName);
        if (bus == null) throw new NotHandledException();

        return bus.getDirect();
    }

    @Override
    protected ComparableRoute getRoute(ComparableId id) throws ExecutionException, NotHandledException {
        ComparableRoute route = data.getBusRoutes().getRouteWithId(id);
        if (route == null) throw new NotHandledException();

        return route;
    }

    @Override
    protected ComparableRoutes getRoutes() throws ExecutionException {
        return data.getBusRoutes().getDirects();
    }

    @Override
    protected MapTimeTable getRouteTimeTable(ComparableId routeId) throws ExecutionException, NotHandledException {
        MapTimeTable timeTable = data.getTimeTable(routeId);
        if (timeTable == null) throw new NotHandledException();

        return timeTable;
    }

    @Override
    protected List<MapTimeTable> getRouteTimeTables() {
        return data.getTimeTables();
    }

    @Override
    protected List<ComparableStop> getSortedStops(Location location) {
        Deque<Distance<ComparableStop>> stops = new LinkedList<>(DistanceCalculator.stopDistance(Unit.METER, location, data.getStops()));
        List<ComparableStop> closestStops = new ArrayList<>();

        if (!stops.isEmpty()) {
            do closestStops.add(stops.poll().getValue());
            while (stops.peek() != null && stops.peek().getDistance() < 200);
        }

        return closestStops;
    }

    @Override
    protected InlineKeyboardMarkupBuilder tripsInlineKeyboard(ComparableRoute route, MapTimeTable timeTable, int chosen, String stopId, int userId) throws ExecutionException, NotHandledException {
        InlineKeyboardMarkupBuilder keyboardBuilder = super.tripsInlineKeyboard(route, timeTable, chosen, stopId, userId);
        RouteQueryBuilder queryBuilder = new RouteQueryBuilder()
                .setCommand(getCommand())
                .setValue(NOW);

        Bus bus = getBus(route.getId());

        if (bus.isDirect(route.getId()) && bus.hasReturn())
            keyboardBuilder
                    .addFullRowInlineButton(mBB.getMessage(userId, "return"), queryBuilder
                            .setId(bus.getReturn().getId())
                            .build());

        if (bus.isReturn(route.getId()))
            keyboardBuilder
                    .addFullRowInlineButton(mBB.getMessage(userId, "direct"), queryBuilder
                            .setId(bus.getDirect().getId())
                            .build());

        return keyboardBuilder;
    }

    @Override
    protected InlineKeyboardMarkupBuilder stopsInlineKeyboard(ComparableId routeId, List<ComparableStop> stops, String stopId, int userId) throws ExecutionException, NotHandledException {
        Bus bus = getBus(routeId);

        RouteQueryBuilder queryBuilder = new RouteQueryBuilder()
                .setCommand(getCommand())
                .setValue(FILTER);

        if (bus.isDirect(routeId) && bus.hasReturn())
            return super.stopsInlineKeyboard(routeId, stops, stopId, userId)
                    .addFullRowInlineButton(mBB.getMessage(userId, "return"), queryBuilder
                            .setId(bus.getReturn().getId())
                            .build());

        if (bus.isReturn(routeId))
            return super.stopsInlineKeyboard(routeId, stops, stopId, userId)
                    .addFullRowInlineButton(mBB.getMessage(userId, "direct"), queryBuilder
                            .setId(bus.getDirect().getId())
                            .build());

        return super.stopsInlineKeyboard(routeId, stops, stopId, userId);
    }

    @Override
    protected InlineKeyboardMarkupBuilder hoursInlineKeyboard(ComparableId routeId, MapTimeTable timeTable, String stopId, int userId) throws ExecutionException, NotHandledException {
        Bus bus = getBus(routeId);

        RouteQueryBuilder queryBuilder = new RouteQueryBuilder()
                .setCommand(getCommand())
                .setValue(HOURS);

        if (bus.isDirect(routeId) && bus.hasReturn())
            return super.hoursInlineKeyboard(routeId, timeTable, stopId, userId)
                    .addFullRowInlineButton(mBB.getMessage(userId, "return"), queryBuilder
                            .setId(bus.getReturn().getId())
                            .build());

        if (bus.isReturn(routeId))
            return super.hoursInlineKeyboard(routeId, timeTable, stopId, userId)
                    .addFullRowInlineButton(mBB.getMessage(userId, "direct"), queryBuilder
                            .setId(bus.getDirect().getId())
                            .build());

        return super.hoursInlineKeyboard(routeId, timeTable, stopId, userId);
    }
}
