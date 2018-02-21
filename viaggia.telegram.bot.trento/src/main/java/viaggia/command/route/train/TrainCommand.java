package viaggia.command.route.train;

import gekoramy.telegram.bot.keyboard.ReplyKeyboardMarkupBuilder;
import gekoramy.telegram.bot.model.Command;
import mobilityservice.model.ComparableId;
import mobilityservice.model.ComparableRoute;
import mobilityservice.model.ComparableRoutes;
import mobilityservice.model.MapTimeTable;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import viaggia.command.route.AbstractRouteCommand;
import viaggia.command.route.general.utils.Mode;
import viaggia.exception.NotHandledException;

import java.util.concurrent.ExecutionException;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class TrainCommand extends AbstractRouteCommand {
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
    protected MapTimeTable getRouteTimeTable(ComparableRoute route) throws ExecutionException, NotHandledException {
        MapTimeTable routeTT = TrainDataManagement.getTrainTimetable(route.getId());
        if (routeTT == null) throw new NotHandledException();

        return routeTT;
    }

    @Override
    protected ComparableRoutes getRoutes() throws ExecutionException {
        return TrainDataManagement.getTrainsComparableRoutes();
    }

    @Override
    protected ReplyKeyboard linesKeyboard() throws ExecutionException {
        return new ReplyKeyboardMarkupBuilder()
                .setResizeKeyboard(true)
                .setOneTimeKeyboard(true)
                .addKeyboardButtons(1, (getRoutes()).getLongNames())
                .build();
    }
}
