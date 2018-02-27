package viaggia.command.route.train;

import gekoramy.telegram.bot.model.Command;
import mobilityservice.model.*;
import org.telegram.telegrambots.api.objects.Location;
import viaggia.command.route.AbsRouteCommand;
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
public class TrainCommand extends AbsRouteCommand {
    private static final Command COMMAND_ID = new Command("train", "train_description");
    private final TrainDataManagement data = new TrainDataManagement();

    public TrainCommand() {
        super(COMMAND_ID, Mode.LONG_NAME);
    }

    @Override
    public void init() {
    }

    @Override
    protected ComparableRoute getRoute(String arguments) throws ExecutionException, NotHandledException {
        ComparableRoute route = data.getTrainsComparableRoutes().getWithLongName(arguments);
        if (route == null) throw new NotHandledException();

        return route;
    }

    @Override
    protected ComparableRoute getRoute(ComparableId id) throws ExecutionException, NotHandledException {
        ComparableRoute route = data.getTrainsComparableRoutes().getWithId(id);
        if (route == null) throw new NotHandledException();

        return route;
    }

    @Override
    protected ComparableRoutes getRoutes() throws ExecutionException {
        return data.getTrainsComparableRoutes();
    }

    @Override
    protected MapTimeTable getRouteTimeTable(ComparableId routeId) throws ExecutionException, NotHandledException {
        MapTimeTable routeTT = data.getTimeTable(routeId);
        if (routeTT == null) throw new NotHandledException();

        return routeTT;
    }

    @Override
    protected List<MapTimeTable> getRouteTimeTables() {
        return data.getTimeTables();
    }

    @Override
    protected List<ComparableStop> getSortedStops(Location location) {
        Deque<Distance<ComparableStop>> stops = new LinkedList<>(DistanceCalculator.stopDistance(Unit.KILOMETER, location, data.getStops()));
        List<ComparableStop> closestStops = new ArrayList<>();

        if (!stops.isEmpty()) {
            do closestStops.add(stops.poll().getValue());
            while (stops.peek() != null && stops.peek().getDistance() < 1);
        }

        return closestStops;
    }
}
