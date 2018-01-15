package viaggia.command.route.bus;

import bot.exception.EmptyKeyboardException;
import bot.keyboard.ReplyKeyboardMarkupBuilder;
import bot.model.Command;
import mobilityservice.model.*;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import viaggia.command.route.AbstractRouteCommand;
import viaggia.command.route.general.utils.Mode;
import viaggia.exception.IncorrectValueException;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Luca Mosetti in 2017
 */
public class BusCommand extends AbstractRouteCommand {

    private static final Command COMMAND_ID = new Command("bus", "bus_description");

    private final ReplyKeyboardMarkupBuilder replyKeyboardMarkupBuilder = new ReplyKeyboardMarkupBuilder();

    public BusCommand() {
        super(COMMAND_ID, Mode.SHORT_NAME);
    }

    @Override
    public void init() {
        BusDataManagement.scheduleUpdate();
    }

    /**
     * @param arguments route.getShortName
     * @return bus.getDirects()
     * @throws ExecutionException      cannot download
     * @throws IncorrectValueException no route found
     */
    @Override
    protected ComparableRoute getRoute(String arguments) throws ExecutionException, IncorrectValueException {
        Bus bus = BusDataManagement.getBusRoutes().getWithShortName(arguments);
        if (bus == null) throw new IncorrectValueException();

        return bus.getDirect();
    }

    @Override
    protected ComparableRoute getRoute(ComparableId id) throws ExecutionException, IncorrectValueException {
        ComparableRoute route = BusDataManagement.getBusRoutes().getRouteWithId(id);
        if (route == null) throw new IncorrectValueException();

        return route;
    }

    @Override
    protected MapTimeTable getRouteTimeTable(ComparableRoute route) throws ExecutionException, IncorrectValueException {
        MapTimeTable timeTable = BusDataManagement.getBusTimeTable(route.getId());
        if (timeTable == null) throw new IncorrectValueException();

        return timeTable;
    }

    @Override
    protected ComparableRoutes getRoutes() throws ExecutionException {
        return BusDataManagement.getBusRoutes().getDirects();
    }

    @Override
    protected ReplyKeyboard linesKeyboard() throws EmptyKeyboardException, ExecutionException {
        List<String> buses = new ComparableRoutes(getRoutes()).getShortNames();
        return replyKeyboardMarkupBuilder
                .setResizeKeyboard(true)
                .setOneTimeKeyboard(true)
                .addKeyboardButtons(6, buses)
                .build(true);
    }

    @Override
    protected InlineKeyboardMarkup routesInlineKeyboard(ComparableRoute route, MapTimeTable timeTable, int chosen, String stopId) throws EmptyKeyboardException, IncorrectValueException, ExecutionException {
        inlineKeyboardMarkupBuilder(route, timeTable, chosen, stopId);

        Bus bus = getBus(route.getRouteShortName());

        if (bus.isDirect(route) && bus.hasReturn())
            inlineKeyboardMarkupBuilder
                    .addFullRowInlineButton(mBB.getMessage("return"), routeQueryBuilder
                            .setCommand(getCommand())
                            .setId(bus.getReturn().getId())
                            .setValue(NOW)
                            .build(true));

        if (bus.isReturn(route))
            inlineKeyboardMarkupBuilder
                    .addFullRowInlineButton(mBB.getMessage("direct"), routeQueryBuilder
                            .setCommand(getCommand())
                            .setId(bus.getDirect().getId())
                            .setValue(NOW)
                            .build(true));

        return inlineKeyboardMarkupBuilder.build(true);
    }

    private Bus getBus(String busShortName) throws ExecutionException, IncorrectValueException {
        Bus bus = BusDataManagement.getBusRoutes().getWithShortName(busShortName);
        if (bus == null) throw new IncorrectValueException();

        return bus;
    }
}
