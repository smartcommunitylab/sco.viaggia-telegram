package viaggia.command.route.train;

import bot.exception.EmptyKeyboardException;
import bot.keyboard.ReplyKeyboardMarkupBuilder;
import bot.model.Command;
import mobilityservice.model.ComparableId;
import mobilityservice.model.ComparableRoute;
import mobilityservice.model.ComparableRoutes;
import mobilityservice.model.MapTimeTable;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import viaggia.command.route.AbstractRouteCommand;
import viaggia.command.route.general.utils.Mode;
import viaggia.exception.IncorrectValueException;

import java.util.concurrent.ExecutionException;

/**
 * Created by Luca Mosetti in 2017
 */
public class TrainCommand extends AbstractRouteCommand {

    private static final Command COMMAND_ID = new Command("train", "train_description");

    private final ReplyKeyboardMarkupBuilder replyKeyboardMarkupBuilder = new ReplyKeyboardMarkupBuilder();

    public TrainCommand() {
        super(COMMAND_ID, Mode.LONG_NAME);
    }

    @Override
    public void init() {
        TrainDataManagement.scheduleUpdate();
    }

    /**
     * @param arguments route.getLongName
     * @return
     * @throws ExecutionException
     * @throws IncorrectValueException
     */
    @Override
    protected ComparableRoute getRoute(String arguments) throws ExecutionException, IncorrectValueException {
        ComparableRoute route = TrainDataManagement.getTrainsComparableRoutes().getWithLongName(arguments);
        if (route == null) throw new IncorrectValueException();

        return route;
    }

    @Override
    protected ComparableRoute getRoute(ComparableId id) throws ExecutionException, IncorrectValueException {
        ComparableRoute route = TrainDataManagement.getTrainsComparableRoutes().getWithId(id);
        if (route == null) throw new IncorrectValueException();

        return route;
    }

    @Override
    protected MapTimeTable getRouteTimeTable(ComparableRoute route) throws ExecutionException, IncorrectValueException {
        MapTimeTable routeTT = TrainDataManagement.getTrainTimetable(route.getId());
        if (routeTT == null) throw new IncorrectValueException();

        return routeTT;
    }

    @Override
    protected ComparableRoutes getRoutes() throws ExecutionException {
        return TrainDataManagement.getTrainsComparableRoutes();
    }

    @Override
    protected ReplyKeyboard linesKeyboard() throws EmptyKeyboardException, ExecutionException {
        return replyKeyboardMarkupBuilder
                .setResizeKeyboard(true)
                .setOneTimeKeyboard(true)
                .addKeyboardButtons(1, (getRoutes()).getLongNames())
                .build(true);
    }
}
