package viaggia.command.route.bus;

import gekoramy.telegram.bot.keyboard.InlineKeyboardMarkupBuilder;
import gekoramy.telegram.bot.keyboard.ReplyKeyboardMarkupBuilder;
import gekoramy.telegram.bot.model.Command;
import mobilityservice.model.*;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import viaggia.command.route.AbstractRouteCommand;
import viaggia.command.route.general.query.RouteQueryBuilder;
import viaggia.command.route.general.utils.Mode;
import viaggia.exception.NotHandledException;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class BusCommand extends AbstractRouteCommand {
    private static final Command COMMAND_ID = new Command("bus", "bus_description");

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
     * @throws ExecutionException  cannot download
     * @throws NotHandledException no route found
     */
    @Override
    protected ComparableRoute getRoute(String arguments) throws ExecutionException, NotHandledException {
        Bus bus = BusDataManagement.getBusRoutes().getWithShortName(arguments);
        if (bus == null) throw new NotHandledException();

        return bus.getDirect();
    }

    @Override
    protected ComparableRoute getRoute(ComparableId id) throws ExecutionException, NotHandledException {
        ComparableRoute route = BusDataManagement.getBusRoutes().getRouteWithId(id);
        if (route == null) throw new NotHandledException();

        return route;
    }

    @Override
    protected MapTimeTable getRouteTimeTable(ComparableRoute route) throws ExecutionException, NotHandledException {
        MapTimeTable timeTable = BusDataManagement.getBusTimeTable(route.getId());
        if (timeTable == null) throw new NotHandledException();

        return timeTable;
    }

    @Override
    protected ComparableRoutes getRoutes() throws ExecutionException {
        return BusDataManagement.getBusRoutes().getDirects();
    }

    @Override
    protected ReplyKeyboard linesKeyboard() throws ExecutionException {
        List<String> buses = new ComparableRoutes(getRoutes()).getShortNames();
        return new ReplyKeyboardMarkupBuilder()
                .setResizeKeyboard(true)
                .setOneTimeKeyboard(true)
                .addKeyboardButtons(6, buses)
                .build();
    }

    @Override
    protected InlineKeyboardMarkup routesInlineKeyboard(ComparableRoute route, MapTimeTable timeTable, int chosen, String stopId, int userId) throws NotHandledException, ExecutionException {
        InlineKeyboardMarkupBuilder keyboardBuilder = inlineKeyboardMarkupBuilder(route, timeTable, chosen, stopId, userId);
        RouteQueryBuilder queryBuilder = new RouteQueryBuilder()
                .setCommand(getCommand())
                .setValue(NOW);

        Bus bus = getBus(route.getRouteShortName());

        if (bus.isDirect(route) && bus.hasReturn())
            keyboardBuilder
                    .addFullRowInlineButton(mBB.getMessage(userId, "return"), queryBuilder
                            .setId(bus.getReturn().getId())
                            .build());

        if (bus.isReturn(route))
            keyboardBuilder
                    .addFullRowInlineButton(mBB.getMessage(userId, "direct"), queryBuilder
                            .setId(bus.getDirect().getId())
                            .build());

        return keyboardBuilder.build();
    }

    private Bus getBus(String busShortName) throws ExecutionException, NotHandledException {
        Bus bus = BusDataManagement.getBusRoutes().getWithShortName(busShortName);
        if (bus == null) throw new NotHandledException();

        return bus;
    }
}
