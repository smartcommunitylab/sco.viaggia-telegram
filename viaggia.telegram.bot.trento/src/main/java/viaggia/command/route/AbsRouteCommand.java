package viaggia.command.route;

import com.google.common.primitives.Ints;
import gekoramy.telegram.bot.keyboard.InlineKeyboardMarkupBuilder;
import gekoramy.telegram.bot.keyboard.ReplyKeyboardMarkupBuilder;
import gekoramy.telegram.bot.model.Command;
import gekoramy.telegram.bot.model.query.Query;
import gekoramy.telegram.bot.responder.CallbackQueryResponder;
import gekoramy.telegram.bot.responder.InlineCallbackQueryResponder;
import gekoramy.telegram.bot.responder.InlineQueryResponder;
import gekoramy.telegram.bot.responder.MessageResponder;
import mobilityservice.model.*;
import mobilityservice.utils.AlphanumComparator;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendVenue;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Location;
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

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * General route info
 *
 * @author Luca Mosetti
 * @since 2017
 */
public abstract class AbsRouteCommand extends DistinguishedUseCaseCommand {

    protected static final String HOURS = "hours";
    protected static final String FILTER = "flt";
    protected static final String NOW = "now";

    private static final Comparator<? super Map.Entry<String, String>> buttonsComparator = new AlphanumComparator<Map.Entry<String, String>>() {
        @Override
        public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
            return o1.getValue().compareTo(o2.getValue());
        }
    };

    private final Mode mode;

    protected AbsRouteCommand(Command command, Mode mode) {
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
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void respondText(MessageResponder absSender, Chat chat, User user, String arguments) {
        super.respondText(absSender, chat, user, arguments);
        try {
            ComparableRoute route = getRoute(arguments);
            MapTimeTable routeTT = getRouteTimeTable(route.getId());

            String text;
            InlineKeyboardMarkup key;

            if (routeTT.getTimes().isEmpty()) {
                text = notTransitTextResponseBuilder(route, user.getId(), false);
                key = notTransitInlineKeyboard(route.getId(), user.getId(), false).build();
            } else {
                text = textResponseBuilder(route, routeTT, nowIndex(routeTT), false, user.getId());
                key = tripsInlineKeyboard(route, routeTT, nowIndex(routeTT), null, user.getId()).build();
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
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    protected void respondLocation(MessageResponder absSender, Chat chat, User user, Location location) {
        super.respondLocation(absSender, chat, user, location);
        try {
            Map<ComparableStop, List<MapTimeTable>> toSend = new LinkedHashMap<>();

            for (ComparableStop stop : getSortedStops(location)) {
                toSend.put(stop, getRouteTimeTables().stream().filter(mapTimeTable -> mapTimeTable.getStops().contains(stop)).collect(Collectors.toList()));
            }

            for (Map.Entry<ComparableStop, List<MapTimeTable>> entry : toSend.entrySet()) {
                List<Map.Entry<String, String>> buttons = new ArrayList<>();

                for (MapTimeTable time : entry.getValue()) {
                    buttons.add(
                            new AbstractMap.SimpleEntry<>(
                                    (mode == Mode.LONG_NAME) ? getRoute(time.getRouteId()).getRouteLongName() : getRoute(time.getRouteId()).getRouteShortName(),
                                    new RouteQueryBuilder()
                                            .setCommand(getCommand())
                                            .setId(time.getRouteId())
                                            .setValue(NOW)
                                            .setStopId(entry.getKey().getId())
                                            .newMessage()
                                            .build()
                            )
                    );
                }

                buttons.sort(buttonsComparator);

                absSender.send(
                        new SendVenue()
                                .setTitle(entry.getKey().getName())
                                .setAddress(entry.getKey().getName())
                                .setLatitude((float) entry.getKey().getLatitude())
                                .setLongitude((float) entry.getKey().getLongitude())
                                .setReplyMarkup(
                                        new InlineKeyboardMarkupBuilder()
                                                .addSeparateRowsKeyboardButtons((mode == Mode.LONG_NAME) ? 1 : 5, buttons)
                                                .build()
                                )
                );
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void respondCallbackQuery(CallbackQueryResponder absSender, Query query, User user, Message message) {
        try {
            RouteQuery q = RouteQueryParser.parse(query);
            Map.Entry<String, InlineKeyboardMarkup> msg = getMessage(q, user.getId());

            if (q.isNewMessage()) {
                absSender.send(new SendMessage()
                        .setParseMode(ParseMode.MARKDOWN)
                        .setText(msg.getKey())
                        .setReplyMarkup(msg.getValue()));
            } else {
                if ((q.getValue().equals(HOURS) || q.getValue().equals(FILTER)) && absSender.equalsFormattedTexts(msg.getKey(), message.getText(), ParseMode.MARKDOWN))
                    absSender.send(new EditMessageReplyMarkup()
                            .setReplyMarkup(msg.getValue()));
                else
                    absSender.send(new EditMessageText()
                            .setParseMode(ParseMode.MARKDOWN)
                            .setText(msg.getKey())
                            .setReplyMarkup(msg.getValue()));
            }

            absSender.answer(new AnswerCallbackQuery().setText(q.getId().getId()));
        } catch (NotHandledException e) {
            absSender.setHandled(false);
            logger.error(e.getMessage(), e);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void respondCallbackQuery(InlineCallbackQueryResponder absSender, Query query, User user) {
        try {
            RouteQuery q = RouteQueryParser.parse(query);
            Map.Entry<String, InlineKeyboardMarkup> msg = getMessage(q, user.getId());

            absSender.send(new EditMessageText()
                    .setParseMode(ParseMode.MARKDOWN)
                    .setText(msg.getKey())
                    .setReplyMarkup(msg.getValue()));

            absSender.answer(new AnswerCallbackQuery().setText(q.getId().getId()));
        } catch (NotHandledException e) {
            absSender.setHandled(false);
            logger.error(getClass().toString(), e);
        } catch (Throwable e) {
            logger.error(getClass().toString(), e);
        }
    }

    @Override
    public void respondInlineQuery(InlineQueryResponder absSender, User user, String arguments, Location location) {
        try {
            List<Map.Entry<ComparableRoute, MapTimeTable>> timeTables = new ArrayList<>();
            for (ComparableRoute route : getRoutes(arguments)) {
                timeTables.add(new AbstractMap.SimpleEntry<>(route, getRouteTimeTable(route.getId())));
            }

            absSender.answer(new AnswerInlineQuery()
                    .setResults(results(user.getId(), timeTables)));
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

    private Map.Entry<String, InlineKeyboardMarkup> getMessage(RouteQuery q, int userId) throws ExecutionException, NotHandledException {
        ComparableRoute route = getRoute(q.getId());
        MapTimeTable routeTT = getRouteTimeTable(route.getId(), q.getStopId());

        String stopId = q.getStopId();
        String value = q.getValue();

        boolean filtered = stopId != null && !stopId.isEmpty() && !stopId.equals("null");
        String text;
        InlineKeyboardMarkup markup;

        if (routeTT.getTimes().isEmpty()) {
            // not transit route

            text = notTransitTextResponseBuilder(route, userId, filtered);
            markup = notTransitInlineKeyboard(route.getId(), userId, filtered).build();

        } else switch (value) {
            case FILTER:
                // filter request

                text = header(route) + mBB.getMessage(userId, "choose_stop");
                markup = stopsInlineKeyboard(route.getId(), new ArrayList<>(routeTT.getStops()), stopId, userId).build();
                break;

            case HOURS:
                // hours request

                text = header(route) + mBB.getMessage(userId, "choose_hour");
                markup = hoursInlineKeyboard(route.getId(), routeTT, stopId, userId).build();
                break;

            default:
                // default request

                Integer val = Ints.tryParse(value);

                if (val == null || val < 0 || val > routeTT.getTimes().size())
                    val = nowIndex(routeTT);

                text = textResponseBuilder(route, routeTT, val, filtered, userId);
                markup = tripsInlineKeyboard(route, routeTT, val, stopId, userId).build();
                break;
        }

        return new AbstractMap.SimpleEntry<>(text, markup);
    }

    // region getters

    protected abstract List<MapTimeTable> getRouteTimeTables();

    protected abstract List<ComparableStop> getSortedStops(Location location);

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

    protected abstract MapTimeTable getRouteTimeTable(ComparableId routeId) throws ExecutionException, NotHandledException;

    private MapTimeTable getRouteTimeTable(ComparableId routeId, String stopId) throws ExecutionException, NotHandledException {
        if (stopId == null || stopId.isEmpty() || stopId.equals("null"))
            return getRouteTimeTable(routeId);

        return getRouteTimeTable(routeId).subMapTimeTable(stopId);
    }

    // endregion getters

    // region utils

    private int timeIndex(MapTimeTable timeTable, LocalTime time) {
        LocalTime tmp;

        for (Trip trip : timeTable.getTrips()) {
            if ((tmp = getLast(timeTable.getTimes().get(trip.getTripId()))) != null) {
                if (tmp.isAfter(time.minus(trip.getDelay())))
                    return timeTable.getTrips().indexOf(trip);
            }
        }

        return timeTable.getTrips().size() - 1;
    }

    private int nowIndex(MapTimeTable timeTable) {
        return timeIndex(timeTable, LocalTime.now());
    }

    private LocalTime getLast(List<Map.Entry<String, LocalTime>> map) {
        if (map.isEmpty())
            return null;

        return map.get(map.size() - 1).getValue();
    }

    private List<InlineQueryResult> results(int userId, List<Map.Entry<ComparableRoute, MapTimeTable>> timeTables) throws ExecutionException, NotHandledException {
        List<InlineQueryResult> results = new ArrayList<>();

        for (Map.Entry<ComparableRoute, MapTimeTable> e : timeTables) {
            ComparableRoute route = e.getKey();
            MapTimeTable timeTable = e.getValue();

            String title;
            StringBuilder thumbUrl = new StringBuilder();
            String description = null;
            InlineKeyboardMarkup key;
            String text;

            switch (mode) {
                case LONG_NAME:
                    title = e.getKey().getRouteLongName();
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
                key = notTransitInlineKeyboard(route.getId(), userId, false).build();
                text = notTransitTextResponseBuilder(route, userId, false);
            } else {
                thumbUrl.insert(0, "https://fakeimg.pl/100x100/168dfe/fff/?&font_size=70&retina=1&text=");
                key = tripsInlineKeyboard(route, timeTable, 0, null, userId).build();
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

    private String notTransitTextResponseBuilder(ComparableRoute route, int userId, boolean filtered) {
        return header(route) + mBB.getMessage(userId, filtered ? "no_transit_stop" : "no_transit");
    }

    private String textResponseBuilder(ComparableRoute route, MapTimeTable timeTable, int chosen, boolean filtered, int userId) {
        return header(route) + timeTableToString(timeTable, chosen, filtered, userId);
    }

    private String timeTableToString(MapTimeTable timeTable, int chosen, boolean filtered, int userId) {
        StringBuilder text = new StringBuilder();
        List<Map.Entry<String, LocalTime>> times = timeTable.getTimes().get(timeTable.getTrips().get(chosen).getTripId());
        LocalTime now = LocalTime.now();
        Duration delay = Duration.from(timeTable.getTrips().get(chosen).getDelay());

        boolean before;

        if (delay.getSeconds() > 0) {
            text.append("`")
                    .append(LocalTime.ofSecondOfDay(delay.getSeconds()).format(MapTimeTable.TIME_FORMATTER))
                    .append("` ")
                    .append(mBB.getMessage(userId, "delay"))
                    .append("\n");
            now = now.minusSeconds(delay.getSeconds());
        }

        for (Map.Entry<String, LocalTime> stopTime : times) {
            LocalTime time = stopTime.getValue();
            before = time.isBefore(now) ^ time.getHour() == 0;
            text.append("`").append(time.format(MapTimeTable.TIME_FORMATTER)).append("` ");

            String stopName = timeTable.getStop(stopTime.getKey()).getName();
            // filtered ? 1st result in bold
            if (filtered) {
                /*⇣×*/
                if (before) text.append("\u21E3 ");
                text.append("*").append(stopName).append("*");
                filtered = false;
            } else {
                if (before) text.append("\u21E3 _").append(stopName).append("_");
                else text.append(stopName);
            }
            text.append("\n");
        }

        return text.toString();
    }

    // endregion text

    // region keyboard

    private ReplyKeyboard linesKeyboard() throws ExecutionException {
        switch (mode) {
            case LONG_NAME:
                return new ReplyKeyboardMarkupBuilder()
                        .setResizeKeyboard(true)
                        .setOneTimeKeyboard(true)
                        .addKeyboardButtons(1, getRoutes().getLongNames())
                        .addRequestLocationButton()
                        .build();

            default:
            case SHORT_NAME:
                return new ReplyKeyboardMarkupBuilder()
                        .setResizeKeyboard(true)
                        .setOneTimeKeyboard(true)
                        .addKeyboardButtons(6, getRoutes().getShortNames())
                        .addRequestLocationButton()
                        .build();
        }
    }

    private InlineKeyboardMarkupBuilder notTransitInlineKeyboard(ComparableId routeId, int userId, boolean filtered) {
        return new InlineKeyboardMarkupBuilder()
                .addFullRowInlineButton(mBB.getMessage(userId, filtered ? "full" : "refresh"), new RouteQueryBuilder()
                        .setCommand(getCommand())
                        .setId(routeId)
                        .setValue(NOW)
                        .build());
    }

    protected InlineKeyboardMarkupBuilder stopsInlineKeyboard(ComparableId routeId, List<ComparableStop> stops, String stopId, int userId) throws ExecutionException, NotHandledException {
        List<Map.Entry<String, String>> entryButtons = new ArrayList<>();

        for (ComparableStop stop : stops) {
            entryButtons.add(
                    new AbstractMap.SimpleEntry<>(
                            stop.getId().equals(stopId) ? "· " + stop.getName() + " ·" : stop.getName(),
                            new RouteQueryBuilder()
                                    .setCommand(getCommand())
                                    .setId(routeId)
                                    .setStopId(stop.getId())
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
                        .build());
    }

    protected InlineKeyboardMarkupBuilder hoursInlineKeyboard(ComparableId routeId, MapTimeTable timeTable, String stopId, int userId) throws ExecutionException, NotHandledException {
        List<Map.Entry<String, String>> hours = new ArrayList<>();

        LocalTime firstTime = getLast(timeTable.getTimes().get(timeTable.getTrips().get(0).getTripId()));
        LocalTime lastTime = getLast(timeTable.getTimes().get(timeTable.getTrips().get(timeTable.getTrips().size() - 1).getTripId()));

        if (firstTime == null || lastTime == null)
            return notTransitInlineKeyboard(routeId, userId, false);

        int fistHour = firstTime.getHour();
        int lastHour = lastTime.getHour() == 0 ? 23 : lastTime.getHour();

        RouteQueryBuilder builder = new RouteQueryBuilder()
                .setCommand(getCommand())
                .setId(routeId)
                .setStopId(stopId);

        for (LocalTime i = LocalTime.of(fistHour, 0), last = LocalTime.of(lastHour, 0); i.isBefore(last); i = i.plusMinutes(30)) {
            hours.add(
                    new AbstractMap.SimpleImmutableEntry<>(i.format(MapTimeTable.TIME_FORMATTER),
                            builder.setValue(Integer.toString(timeIndex(timeTable, i))).build())
            );
        }

        return new InlineKeyboardMarkupBuilder()
                .addSeparateRowsKeyboardButtons(4, hours)
                .addFullRowInlineButton(mBB.getMessage(userId, "now"), builder.setValue(NOW).build());
    }

    protected InlineKeyboardMarkupBuilder tripsInlineKeyboard(ComparableRoute route, MapTimeTable timeTable, int chosen, String stopId, int userId) throws ExecutionException, NotHandledException {
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

    // endregion keyboard
}
