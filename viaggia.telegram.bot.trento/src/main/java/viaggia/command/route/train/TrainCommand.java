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

    public TrainCommand() {
        super(COMMAND_ID, Mode.LONG_NAME);
    }

    @Override
    public void init() {
        TrainDataManagement.scheduleUpdate();
    }

    @Override
    protected ComparableRoute getRoute(String arguments) throws ExecutionException, NotHandledException {
        ComparableRoute route = TrainDataManagement.getTrainsComparableRoutes().getWithLongName(arguments);
        if (route == null) throw new NotHandledException();

        return route;
    }

    @Override
    protected ComparableRoute getRoute(ComparableId id) throws ExecutionException, NotHandledException {
        ComparableRoute route = TrainDataManagement.getTrainsComparableRoutes().getWithId(id);
        if (route == null) throw new NotHandledException();

        return route;
    }

    @Override
    protected ComparableRoutes getRoutes() throws ExecutionException {
        return TrainDataManagement.getTrainsComparableRoutes();
    }

    @Override
    protected MapTimeTable getRouteTimeTable(ComparableId routeId) throws ExecutionException, NotHandledException {
        MapTimeTable routeTT = TrainDataManagement.getTrainTimetable(routeId);
        if (routeTT == null) throw new NotHandledException();

        return routeTT;
    }

    @Override
    protected List<MapTimeTable> getRouteTimeTables() {
        return TrainDataManagement.getBusTimeTables();
    }

    @Override
    protected List<ComparableStop> getSortedStops(Location location) {
        Deque<Distance<ComparableStop>> stops = new LinkedList<>(DistanceCalculator.stopDistance(Unit.METER, location, TrainDataManagement.getStops()));
        List<ComparableStop> closestStops = new ArrayList<>();

        do {
            closestStops.add(stops.poll().getValue());
        } while (stops.peek().getDistance() > 500);

        return closestStops;
    }
}
