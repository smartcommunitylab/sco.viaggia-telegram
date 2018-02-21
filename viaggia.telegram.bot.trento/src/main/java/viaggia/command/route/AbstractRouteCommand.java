package viaggia.command.route;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import gekoramy.telegram.bot.keyboard.InlineKeyboardMarkupBuilder;
import gekoramy.telegram.bot.model.Command;
import gekoramy.telegram.bot.model.query.Query;
import gekoramy.telegram.bot.responder.CallbackQueryResponder;
import gekoramy.telegram.bot.responder.InlineCallbackQueryResponder;
import gekoramy.telegram.bot.responder.InlineQueryResponder;
import gekoramy.telegram.bot.responder.MessageResponder;
import gekoramy.telegram.bot.responder.type.CallbackQueryEditor;
import it.sayservice.platform.smartplanner.data.message.alerts.CreatorType;
import mobilityservice.model.ComparableId;
import mobilityservice.model.ComparableRoute;
import mobilityservice.model.ComparableRoutes;
import mobilityservice.model.MapTimeTable;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import viaggia.command.route.general.query.RouteQuery;
import viaggia.command.route.general.query.RouteQueryBuilder;
import viaggia.command.route.general.query.RouteQueryParser;
import viaggia.command.route.general.utils.InlineKeyboardRowNavRouteBuilder;
import viaggia.command.route.general.utils.Mode;
import viaggia.exception.NotHandledException;
import viaggia.extended.DistinguishedUseCaseCommand;

import java.time.LocalTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public abstract class AbstractRouteCommand extends DistinguishedUseCaseCommand {

    private static final String HOURS = "hours";
    private static final String FILTER = "flt";
    protected static final String NOW = "now";

    private final Mode mode;

    public AbstractRouteCommand(Command command, Mode mode) {
        super(command);
        this.mode = mode;
    }

    @Override
    public void respondCommand(MessageResponder absSender, Chat chat, User user) {
        super.respondCommand(absSender, chat, user);
        try {
            absSender.send(routeStartCommand(user.getId()))
                    .toComplete();
        } catch (Throwable e) {
            logger.error(getClass().toString(), e);
        }
    }

    @Override
    public void respondText(MessageResponder absSender, Chat chat, User user, String arguments) {
        super.respondText(absSender, chat, user, arguments);
        try {
            ComparableRoute route = getRoute(arguments);
            MapTimeTable routeTT = getRouteTimeTable(route);

            String text;
            InlineKeyboardMarkup key;

            if (routeTT.getTimes().isEmpty()) {
                text = notTransitTextResponseBuilder(route, user.getId());
                key = notTransitInlineKeyboard(route, user.getId());
            } else {
                text = textResponseBuilder(route, routeTT, nowIndex(routeTT), false, user.getId());
                key = routesInlineKeyboard(route, routeTT, nowIndex(routeTT), null, user.getId());
            }

            absSender.send(new SendMessage()
                    .setParseMode(ParseMode.MARKDOWN)
                    .setText(text)
                    .setReplyMarkup(key));

        } catch (NotHandledException e) {
            absSender.send(routeStartCommand(user.getId()));
            absSender.toComplete();
            absSender.setHandled(false);
        } catch (Throwable e) {
            logger.error(getClass().toString(), e);
        }
    }

    @Override
    public void respondCallbackQuery(CallbackQueryResponder absSender, Query query, User user, Message message) {
        routeCallbackQueryHandling(absSender, query, user.getId());
    }

    @Override
    public void respondCallbackQuery(InlineCallbackQueryResponder absSender, Query query, User user) {
        routeCallbackQueryHandling(absSender, query, user.getId());
    }

    @Override
    public void respondInlineQuery(InlineQueryResponder absSender, User user, String arguments) {
        try {
            absSender.answer(new AnswerInlineQuery()
                    .setResults(results(arguments, user.getId())));
        } catch (Throwable e) {
            logger.error(getClass().toString(), e);
        }
    }

    private void routeCallbackQueryHandling(CallbackQueryEditor absSender, Query query, int userId) {
        try {
            RouteQuery q = RouteQueryParser.parse(query);
            ComparableRoute route = getRoute(q.getId());

            absSender.send(editRoute(userId, route, q.getStopId(), q.getValue()));
            absSender.answer(new AnswerCallbackQuery().setText(route.getRouteShortName()));
        } catch (NotHandledException e) {
            absSender.setHandled(false);
            logger.error(getClass().toString(), e);
        } catch (Throwable e) {
            logger.error(getClass().toString(), e);
        }
    }

    private SendMessage routeStartCommand(int userId) {
        try {
            return new SendMessage()
                    .setText(mBB.getMessage(userId, getCommand().getDescription()))
                    .setReplyMarkup(linesKeyboard());
        } catch (Throwable e) {
            return new SendMessage()
                    .setText("Something went wrong...");
        }
    }

    private EditMessageText editRoute(int userId, ComparableRoute route, String stopId, String value) {
        try {
            MapTimeTable routeTT = getRouteTimeTable(route, stopId);

            String text;
            InlineKeyboardMarkup markup;

            if (routeTT.getTimes().isEmpty()) {
                // not transit route

                text = notTransitTextResponseBuilder(route, userId);
                markup = notTransitInlineKeyboard(route, userId);

            } else switch (value) {
                case FILTER:
                    // filter request

                    text = header(route) + mBB.getMessage(userId, "choose_stop");
                    markup = stopsInlineKeyboard(route.getId(), routeTT.getStops(), routeTT.getStopsId(), stopId, userId);
                    break;

                case HOURS:
                    // hours request

                    text = header(route) + mBB.getMessage(userId, "choose_hour");
                    markup = hoursInlineKeyboard(route, routeTT, stopId, userId);
                    break;

                default:
                    // default request

                    Integer val = Ints.tryParse(value);

                    if (val == null || val < 0 || val > routeTT.getTimes().size())
                        val = nowIndex(routeTT);

                    text = textResponseBuilder(route, routeTT, val, stopId != null && !stopId.isEmpty() && !stopId.equals("null"), userId);
                    markup = routesInlineKeyboard(route, routeTT, val, stopId, userId);
                    break;
            }

            return new EditMessageText()
                    .setParseMode(ParseMode.MARKDOWN)
                    .setText(text)
                    .setReplyMarkup(markup);
        } catch (ExecutionException | NotHandledException e) {
            return new EditMessageText()
                    .setText("Something went wrong...")
                    .setReplyMarkup(notTransitInlineKeyboard(route, userId));
        }
    }

    // region getters

    protected abstract ComparableRoute getRoute(String arguments) throws ExecutionException, NotHandledException;

    protected abstract ComparableRoute getRoute(ComparableId id) throws ExecutionException, NotHandledException;

    protected abstract ComparableRoutes getRoutes() throws ExecutionException;

    private ComparableRoutes getRoutes(String filter) throws ExecutionException, NotHandledException {
        if (filter == null || filter.isEmpty()) return getRoutes();

        ComparableRoutes subRoutes;

        switch (mode) {
            case LONG_NAME:
                subRoutes = getRoutes().subRoutesLongName(filter);
                break;

            case SHORT_NAME:
            default:
                subRoutes = getRoutes().subRoutesShortName(filter);
                break;
        }

        if (subRoutes.isEmpty()) throw new NotHandledException();

        return subRoutes;
    }

    protected abstract MapTimeTable getRouteTimeTable(ComparableRoute route) throws ExecutionException, NotHandledException;

    private MapTimeTable getRouteTimeTable(ComparableRoute route, String stopId) throws ExecutionException, NotHandledException {
        if (stopId == null || stopId.isEmpty() || stopId.equals("null")) return getRouteTimeTable(route);

        MapTimeTable subMapTimeTable = getRouteTimeTable(route).subMapTimeTable(stopId);

        if (subMapTimeTable == null || subMapTimeTable.getTimes().isEmpty())
            throw new NotHandledException();

        return subMapTimeTable;
    }

    // endregion getters

    // region utils

    private int timeIndex(MapTimeTable timeTable, LocalTime time) {
        LocalTime tmp;

        for (int i = 0, numTimes = timeTable.getTimes().size(); i < numTimes; i++) {
            Integer delay;
            List<LocalTime> times = timeTable.getTimes().get(i);

            if (timeTable.getDelays() != null && timeTable.getDelays().get(i) != null && timeTable.getDelays().get(i).getValues().get(CreatorType.SERVICE) != null && (delay = Ints.tryParse(timeTable.getDelays().get(i).getValues().get(CreatorType.SERVICE))) != null) {
                if ((tmp = getLastNotNull(times)) != null && tmp.isAfter(time.minusMinutes(delay)))
                    return i;
            } else {
                if ((tmp = getLastNotNull(times)) != null && tmp.isAfter(time))
                    return i;
            }
        }

        return timeTable.getTimes().size() - 1;
    }

    private int nowIndex(MapTimeTable timeTable) {
        return timeIndex(timeTable, LocalTime.now());
    }

    private LocalTime getLastNotNull(List<LocalTime> times) {
        for (LocalTime time : Lists.reverse(times))
            if (time != null) return time;

        return null;
    }

    private List<InlineQueryResult> results(String filter, int userId) throws ExecutionException {
        List<InlineQueryResult> results = new ArrayList<>();

        try {
            for (ComparableRoute route : getRoutes(filter)) {
                MapTimeTable timeTable = getRouteTimeTable(route);

                String title;
                StringBuilder thumbUrl = new StringBuilder();
                String description = null;
                InlineKeyboardMarkup key;
                String text;

                switch (mode) {
                    case LONG_NAME:
                        title = route.getRouteLongName();
                        thumbUrl.append(route.getRouteLongName().substring(0, 2));
                        break;
                    case SHORT_NAME:
                    default:
                        title = route.getRouteShortName();
                        description = route.getRouteLongName();
                        thumbUrl.append(route.getRouteShortName());
                        break;
                }

                if (timeTable.getTimes().isEmpty()) {
                    thumbUrl.insert(0, "https://fakeimg.pl/100x100/bababa/fff/?&font_size=70&retina=1&text=");
                    key = notTransitInlineKeyboard(route, userId);
                    text = notTransitTextResponseBuilder(route, userId);
                } else {
                    thumbUrl.insert(0, "https://fakeimg.pl/100x100/168dfe/fff/?&font_size=70&retina=1&text=");
                    key = routesInlineKeyboard(route, timeTable, 0, null, userId);
                    text = mBB.getMessage(userId, "browse");
                }

                results.add(new InlineQueryResultArticle()
                        .setId(route.getId().getId())
                        .setThumbUrl(thumbUrl.toString())
                        .setTitle(title)
                        .setDescription(description)
                        .setReplyMarkup(key)
                        .setThumbHeight(100)
                        .setThumbHeight(100)
                        .setInputMessageContent(new InputTextMessageContent()
                                .setMessageText(text)
                                .setParseMode(ParseMode.MARKDOWN)));
            }
        } catch (NotHandledException e) {
            logger.error(e.getMessage());
        }

        return results;
    }

    // endregion utils

    // region text

    private String header(ComparableRoute route) {
        switch (mode) {
            case LONG_NAME:
                return "*" + getCommand().getCommandIdentifier().toUpperCase() + ' ' + route.getRouteLongName() + "*\n";

            case SHORT_NAME:
            default:
                return "*" + getCommand().getCommandIdentifier().toUpperCase() + ' ' + route.getRouteShortName() + "*\n";
        }
    }

    private String notTransitTextResponseBuilder(ComparableRoute route, int userId) {
        return header(route) + mBB.getMessage(userId, "no_transit");
    }

    private String textResponseBuilder(ComparableRoute route, MapTimeTable timeTable, int chosen, boolean filtered, int userId) {
        return header(route) + timeTableToString(timeTable, chosen, filtered, userId);
    }

    private String timeTableToString(MapTimeTable timeTable, int chosen, boolean filtered, int userId) {
        StringBuilder text = new StringBuilder();
        List<String> stops = timeTable.getStops();
        List<LocalTime> times = timeTable.getTimes().get(chosen);
        LocalTime now = LocalTime.now();
        Integer delay = 0;

        if (timeTable.getDelays() != null && timeTable.getDelays().get(chosen) != null && timeTable.getDelays().get(chosen).getValues().get(CreatorType.SERVICE) != null) {
            delay = Ints.tryParse(timeTable.getDelays().get(chosen).getValues().get(CreatorType.SERVICE));
        }

        boolean before;

        if (delay != null && delay > 0) {
            text.append("`")
                    .append(LocalTime.ofSecondOfDay(delay * 60).format(MapTimeTable.TIME_FORMATTER))
                    .append("` ")
                    .append(mBB.getMessage(userId, "delay"))
                    .append("\n");
            now = now.minusMinutes(delay);
        }

        for (int i = 0, timesSize = times.size(); i < timesSize; i++) {
            LocalTime time = times.get(i);
            if (time != null) {
                before = time.isBefore(now) ^ time.getHour() == 0;
                text.append("`").append(time.format(MapTimeTable.TIME_FORMATTER)).append("` ");

                if (filtered) {
                    /*⇣*/
                    if (before) text.append("× ");
                    text.append("*").append(stops.get(i)).append("*");
                    filtered = false;
                } else {
                    if (before) text.append("× _").append(stops.get(i)).append("_");
                    else text.append(stops.get(i));
                }
                text.append("\n");
            }
        }
        return text.toString();
    }

    // endregion text

    // region keyboard

    private InlineKeyboardMarkup notTransitInlineKeyboard(ComparableRoute route, int userId) {
        return new InlineKeyboardMarkupBuilder()
                .addFullRowInlineButton(mBB.getMessage(userId, "refresh"), new RouteQueryBuilder()
                        .setCommand(getCommand())
                        .setId(route.getId())
                        .setValue("now")
                        .build())
                .build();
    }

    private InlineKeyboardMarkup stopsInlineKeyboard(ComparableId routeId, List<String> stops, List<String> stopsId, String stopId, int userId) {
        int textButtonsSize;
        List<Map.Entry<String, String>> entryButtons = new ArrayList<>();

        if ((textButtonsSize = stops.size()) != stopsId.size())
            throw new IllegalArgumentException();

        for (int i = 0; i < textButtonsSize; i++) {
            entryButtons.add(
                    new AbstractMap.SimpleEntry<>(
                            stopsId.get(i).equals(stopId) ? "· " + stops.get(i) + " ·" : stops.get(i),
                            new RouteQueryBuilder()
                                    .setCommand(getCommand())
                                    .setId(routeId)
                                    .setStopId(stopsId.get(i))
                                    .setValue("now")
                                    .build()
                    )
            );
        }

        return new InlineKeyboardMarkupBuilder()
                .addSeparateRowsKeyboardButtons(1, entryButtons)
                .addFullRowInlineButton(mBB.getMessage(userId, "full"), new RouteQueryBuilder()
                        .setCommand(getCommand())
                        .setId(routeId)
                        .setValue("now")
                        .build())
                .build();
    }

    protected InlineKeyboardMarkupBuilder inlineKeyboardMarkupBuilder(ComparableRoute route, MapTimeTable timeTable, int chosen, String stopId, int userId) {
        if (chosen < 0 && chosen > timeTable.getTimes().size() - 1)
            chosen = nowIndex(timeTable);

        List<Map.Entry<String, String>> routes = new InlineKeyboardRowNavRouteBuilder().build(timeTable.getTimes().size(), chosen, 5, getCommand(), route.getId(), stopId);
        List<Map.Entry<String, String>> buttons = new ArrayList<>();

        RouteQueryBuilder builder = new RouteQueryBuilder()
                .setCommand(getCommand())
                .setId(route.getId())
                .setStopId(stopId);

        buttons.add(new AbstractMap.SimpleEntry<>(mBB.getMessage(userId, "hours"), builder.setValue(HOURS).build()));

        buttons.add(new AbstractMap.SimpleEntry<>(mBB.getMessage(userId, "now"), builder.setValue(NOW).build()));

        buttons.add(new AbstractMap.SimpleEntry<>(mBB.getMessage(userId, "filter"), builder.setValue(FILTER).build()));

        return new InlineKeyboardMarkupBuilder()
                .addSeparateRowsKeyboardButtons(5, routes)
                .addSeparateRowsKeyboardButtons(5, buttons);

    }

    protected InlineKeyboardMarkup routesInlineKeyboard(ComparableRoute route, MapTimeTable timeTable, int chosen, String stopId, int userId) throws NotHandledException, ExecutionException {
        return inlineKeyboardMarkupBuilder(route, timeTable, chosen, stopId, userId).build();
    }

    protected abstract ReplyKeyboard linesKeyboard() throws ExecutionException;

    private InlineKeyboardMarkup hoursInlineKeyboard(ComparableRoute route, MapTimeTable timeTable, String stopId, int userId) {
        List<Map.Entry<String, String>> hours = new ArrayList<>();

        LocalTime firstTime = getLastNotNull(timeTable.getTimes().get(0));
        LocalTime lastTime = getLastNotNull(timeTable.getTimes().get(timeTable.getTimes().size() - 1));

        if (firstTime == null || lastTime == null)
            return notTransitInlineKeyboard(route, userId);

        int fistHour = firstTime.getHour();
        int lastHour = lastTime.getHour();

        RouteQueryBuilder builder = new RouteQueryBuilder()
                .setCommand(getCommand())
                .setId(route.getId())
                .setStopId(stopId);

        for (LocalTime i = LocalTime.of(fistHour, 0), last = LocalTime.of(lastHour, 0); i.isBefore(last); i = i.plusMinutes(30)) {
            hours.add(
                    new AbstractMap.SimpleImmutableEntry<>(i.format(MapTimeTable.TIME_FORMATTER),
                            builder.setValue(Integer.toString(timeIndex(timeTable, i))).build())
            );
        }

        return new InlineKeyboardMarkupBuilder()
                .addSeparateRowsKeyboardButtons(4, hours)
                .addFullRowInlineButton(mBB.getMessage(userId, "now"), builder.setValue(NOW).build())
                .build();
    }

    // endregion keyboard
}
