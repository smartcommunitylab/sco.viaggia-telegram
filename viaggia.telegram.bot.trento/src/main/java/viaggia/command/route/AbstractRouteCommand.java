package viaggia.command.route;

import bot.exception.EmptyKeyboardException;
import bot.keyboard.InlineKeyboardMarkupBuilder;
import bot.model.Command;
import bot.model.handling.HandleCallbackQuery;
import bot.model.handling.HandleInlineQuery;
import bot.model.query.Query;
import bot.timed.Chats;
import bot.timed.TimedAbsSender;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import it.sayservice.platform.smartplanner.data.message.alerts.CreatorType;
import mobilityservice.model.ComparableId;
import mobilityservice.model.ComparableRoute;
import mobilityservice.model.ComparableRoutes;
import mobilityservice.model.MapTimeTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import viaggia.exception.IncorrectValueException;
import viaggia.extended.DistinguishedUseCaseCommand;
import viaggia.utils.MessageBundleBuilder;

import java.time.LocalTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by Luca Mosetti in 2017
 */
public abstract class AbstractRouteCommand extends DistinguishedUseCaseCommand implements HandleCallbackQuery, HandleInlineQuery {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRouteCommand.class);

    protected final InlineKeyboardMarkupBuilder inlineKeyboardMarkupBuilder = new InlineKeyboardMarkupBuilder();
    protected final RouteQueryBuilder routeQueryBuilder = new RouteQueryBuilder();
    protected final MessageBundleBuilder mBB = new MessageBundleBuilder();

    private final InlineKeyboardRowNavRouteBuilder inlineKeyboardRowNavRouteBuilder = new InlineKeyboardRowNavRouteBuilder();
    private final RouteQueryParser routeQueryParser = new RouteQueryParser();
    private final Mode mode;

    private static final String HOURS = "hours";
    protected static final String NOW = "now";
    private static final String FILTER = "flt";

    public AbstractRouteCommand(Command command, Mode mode) {
        super(command);
        this.mode = mode;
    }

    @Override
    public void respondCommand(TimedAbsSender absSender, User user, Chat chat) {
        super.respondCommand(absSender, user, chat);
        mBB.setUser(user);
        routeStartCommand(absSender, chat);
    }

    @Override
    public void respondText(TimedAbsSender absSender, User user, Chat chat, String arguments) {
        super.respondText(absSender, user, chat, arguments);
        mBB.setUser(user);

        try {
            ComparableRoute route = getRoute(arguments);
            MapTimeTable routeTT = getRouteTimeTable(route);

            String text;
            InlineKeyboardMarkup key;

            if (routeTT.getTimes().isEmpty()) {
                text = notTransitTextResponseBuilder(route);
                key = notTransitInlineKeyboard(route);
            } else {
                text = textResponseBuilder(route, routeTT, nowIndex(routeTT), false);
                key = routesInlineKeyboard(route, routeTT, nowIndex(routeTT), null);
            }

            absSender.requestExecute(chat.getId(), new SendMessage()
                    .setChatId(chat.getId())
                    .setParseMode(ParseMode.MARKDOWN)
                    .setText(text)
                    .setReplyMarkup(key));

        } catch (IncorrectValueException e) {
            routeStartCommand(absSender, chat);
        } catch (ExecutionException | EmptyKeyboardException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void respondCallbackQuery(TimedAbsSender absSender, String callbackQueryId, Query query, User user, Message message) {
        routeCallbackQueryHandling(absSender, callbackQueryId, query, user, message.getChatId(), message.getMessageId(), message.getText(), null);

    }

    @Override
    public void respondCallbackQuery(TimedAbsSender absSender, String callbackQueryId, Query query, User user, String inlineMessageId) {
        routeCallbackQueryHandling(absSender, callbackQueryId, query, user, null, null, null, inlineMessageId);
    }

    @Override
    public void respondInlineQuery(TimedAbsSender absSender, User user, String id, String arguments) {
        mBB.setUser(user);
        try {
            absSender.requestExecute(null, new AnswerInlineQuery()
                    .setInlineQueryId(id)
                    .setResults(results(arguments)));
        } catch (ExecutionException | EmptyKeyboardException e) {
            logger.error(e.getMessage());
        }
    }

    private void routeStartCommand(TimedAbsSender absSender, Chat chat) {
        try {
            absSender.requestExecute(chat.getId(), new SendMessage()
                    .setChatId(chat.getId())
                    .setText(mBB.getMessage(getCommand().getDescription()))
                    .setReplyMarkup(linesKeyboard()));

            Chats.setCommand(chat.getId(), getCommand());
        } catch (EmptyKeyboardException | ExecutionException e) {
            logger.error(e.getMessage());
        }
    }

    private void routeCallbackQueryHandling(TimedAbsSender absSender, String callbackQueryId, Query query, User user, Long chatId, Integer messageId, String messageText, String inlineMessageId) {
        mBB.setUser(user);

        try {
            RouteQuery q = routeQueryParser.parse(query);
            ComparableRoute route = getRoute(q.getId());
            MapTimeTable routeTT = getRouteTimeTable(route, q.getStopId());

            String text;
            InlineKeyboardMarkup markup;

            if (routeTT.getTimes().isEmpty()) {
                // not transit route

                text = notTransitTextResponseBuilder(route);
                markup = notTransitInlineKeyboard(route);

            } else switch (q.getValue()) {
                case FILTER:
                    // filter request

                    text = header(route) + mBB.getMessage("choose_stop");
                    markup = stopsInlineKeyboard(route.getId(), routeTT.getStops(), routeTT.getStopsId(), q.getStopId());
                    break;

                case HOURS:
                    // hours request

                    text = header(route) + mBB.getMessage("choose_hour");
                    markup = hoursInlineKeyboard(route, routeTT, q.getStopId());
                    break;

                default:
                    // default request

                    Integer val = Ints.tryParse(q.getValue());

                    if (val == null || val < 0 || val > routeTT.getTimes().size())
                        val = nowIndex(routeTT);

                    text = textResponseBuilder(route, routeTT, val, q.getStopId() != null && !q.getStopId().isEmpty() && !q.getStopId().equals("null"));
                    markup = routesInlineKeyboard(route, routeTT, val, q.getStopId());
                    break;
            }

            EditMessageText edit = chatId != null ? new EditMessageText()
                    .setChatId(chatId)
                    .setMessageId(messageId)
                    .setParseMode(ParseMode.MARKDOWN)
                    .setText(text)
                    .setReplyMarkup(markup)
                    : new EditMessageText()
                    .setInlineMessageId(inlineMessageId)
                    .setParseMode(ParseMode.MARKDOWN)
                    .setText(text)
                    .setReplyMarkup(markup);

            AnswerCallbackQuery answer = new AnswerCallbackQuery()
                    .setCallbackQueryId(callbackQueryId)
                    .setText(route.getRouteShortName());

            if (chatId == null)
                chatId = (long) user.hashCode();

            if (!equalsFormattedTexts(edit.getText().trim(), messageText, ParseMode.MARKDOWN)) {
                absSender.requestExecute(chatId, edit);
            }

            absSender.requestExecute(chatId, answer);

        } catch (ExecutionException | EmptyKeyboardException e) {
            logger.error(e.getMessage());
        } catch (IncorrectValueException e) {
            /* DO NOTHING */
        }
    }

    // region getters
    protected abstract ComparableRoute getRoute(String arguments) throws ExecutionException, IncorrectValueException;

    protected abstract ComparableRoute getRoute(ComparableId id) throws ExecutionException, IncorrectValueException;

    protected abstract ComparableRoutes getRoutes() throws ExecutionException;

    private ComparableRoutes getRoutes(String filter) throws ExecutionException, IncorrectValueException {
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

        if (subRoutes.isEmpty()) throw new IncorrectValueException();

        return subRoutes;
    }

    protected abstract MapTimeTable getRouteTimeTable(ComparableRoute route) throws ExecutionException, IncorrectValueException;

    private MapTimeTable getRouteTimeTable(ComparableRoute route, String stopId) throws ExecutionException, IncorrectValueException {
        if (stopId == null || stopId.isEmpty() || stopId.equals("null")) return getRouteTimeTable(route);

        MapTimeTable subMapTimeTable = getRouteTimeTable(route).subMapTimeTable(stopId);

        if (subMapTimeTable == null || subMapTimeTable.getTimes().isEmpty())
            throw new IncorrectValueException();

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

    private List<InlineQueryResult> results(String filter) throws ExecutionException, EmptyKeyboardException {
        List<InlineQueryResult> results = new ArrayList<>();

        try {
            for (ComparableRoute route : getRoutes(filter)) {
                MapTimeTable timeTable = getRouteTimeTable(route);

                String title;
                StringBuilder thumbUrl = new StringBuilder();
                String description = null;
                InlineKeyboardMarkup markup;
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
                    markup = notTransitInlineKeyboard(route);
                    text = notTransitTextResponseBuilder(route);
                } else {
                    thumbUrl.insert(0, "https://fakeimg.pl/100x100/168dfe/fff/?&font_size=70&retina=1&text=");
                    markup = routesInlineKeyboard(route, timeTable, 0, null);
                    text = mBB.getMessage("browse");
                }

                results.add(new InlineQueryResultArticle()
                        .setId(route.getId().getId())
                        .setThumbUrl(thumbUrl.toString())
                        .setTitle(title)
                        .setDescription(description)
                        .setReplyMarkup(markup)
                        .setThumbHeight(100)
                        .setThumbHeight(100)
                        .setInputMessageContent(new InputTextMessageContent()
                                .setMessageText(text)
                                .setParseMode(ParseMode.MARKDOWN)));
            }
        } catch (IncorrectValueException e) {
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

    private String notTransitTextResponseBuilder(ComparableRoute route) {
        return header(route) + mBB.getMessage("no_transit");
    }

    private String textResponseBuilder(ComparableRoute route, MapTimeTable timeTable, int chosen, boolean filtered) {
        return header(route) + timeTableToString(timeTable, chosen, filtered);
    }

    private String timeTableToString(MapTimeTable timeTable, int chosen, boolean filtered) {
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
                    .append(mBB.getMessage("delay"))
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

    private InlineKeyboardMarkup notTransitInlineKeyboard(ComparableRoute route) throws EmptyKeyboardException {
        return inlineKeyboardMarkupBuilder
                .addFullRowInlineButton(mBB.getMessage("refresh"), routeQueryBuilder
                        .setCommand(getCommand())
                        .setId(route.getId())
                        .setValue("now")
                        .build(true))
                .build(true);
    }

    private InlineKeyboardMarkup stopsInlineKeyboard(ComparableId routeId, List<String> stops, List<String> stopsId, String stopId) throws EmptyKeyboardException {
        int textButtonsSize;
        List<Map.Entry<String, String>> entryButtons = new ArrayList<>();

        if ((textButtonsSize = stops.size()) != stopsId.size())
            throw new IllegalArgumentException();

        for (int i = 0; i < textButtonsSize; i++) {
            entryButtons.add(
                    new AbstractMap.SimpleEntry<>(
                            stopsId.get(i).equals(stopId) ? "· " + stops.get(i) + " ·" : stops.get(i),
                            routeQueryBuilder
                                    .setCommand(getCommand())
                                    .setId(routeId)
                                    .setStopId(stopsId.get(i))
                                    .setValue("now")
                                    .build(true)
                    )
            );
        }

        return inlineKeyboardMarkupBuilder
                .addSeparateRowsKeyboardButtons(1, entryButtons)
                .addFullRowInlineButton(mBB.getMessage("full"), routeQueryBuilder
                        .setCommand(getCommand())
                        .setId(routeId)
                        .setValue("now")
                        .build(true))
                .build(true);
    }

    protected void inlineKeyboardMarkupBuilder(ComparableRoute route, MapTimeTable timeTable, int chosen, String stopId) {
        if (chosen < 0 && chosen > timeTable.getTimes().size() - 1)
            chosen = nowIndex(timeTable);

        List<Map.Entry<String, String>> routes = inlineKeyboardRowNavRouteBuilder.build(timeTable.getTimes().size(), chosen, 5, getCommand(), route.getId(), stopId);
        List<Map.Entry<String, String>> buttons = new ArrayList<>();

        routeQueryBuilder
                .setCommand(getCommand())
                .setId(route.getId())
                .setStopId(stopId);

        buttons.add(new AbstractMap.SimpleEntry<>(mBB.getMessage("hours"), routeQueryBuilder.setValue(HOURS).build(false)));

        buttons.add(new AbstractMap.SimpleEntry<>(mBB.getMessage("now"), routeQueryBuilder.setValue(NOW).build(false)));

        buttons.add(new AbstractMap.SimpleEntry<>(mBB.getMessage("filter"), routeQueryBuilder.setValue(FILTER).build(true)));

        inlineKeyboardMarkupBuilder
                .addSeparateRowsKeyboardButtons(5, routes)
                .addSeparateRowsKeyboardButtons(5, buttons);

    }

    protected InlineKeyboardMarkup routesInlineKeyboard(ComparableRoute route, MapTimeTable timeTable, int chosen, String stopId) throws EmptyKeyboardException, IncorrectValueException, ExecutionException {
        inlineKeyboardMarkupBuilder(route, timeTable, chosen, stopId);

        return inlineKeyboardMarkupBuilder.build(true);
    }

    protected abstract ReplyKeyboard linesKeyboard() throws EmptyKeyboardException, ExecutionException;

    private InlineKeyboardMarkup hoursInlineKeyboard(ComparableRoute route, MapTimeTable timeTable, String stopId) throws EmptyKeyboardException {
        List<Map.Entry<String, String>> hours = new ArrayList<>();

        LocalTime firstTime = getLastNotNull(timeTable.getTimes().get(0));
        LocalTime lastTime = getLastNotNull(timeTable.getTimes().get(timeTable.getTimes().size() - 1));

        if (firstTime == null || lastTime == null)
            return notTransitInlineKeyboard(route);

        int fistHour = firstTime.getHour();
        int lastHour = lastTime.getHour();

        routeQueryBuilder
                .setCommand(getCommand())
                .setId(route.getId())
                .setStopId(stopId);

        for (LocalTime i = LocalTime.of(fistHour, 0), last = LocalTime.of(lastHour, 0); i.isBefore(last); i = i.plusMinutes(30)) {
            hours.add(
                    new AbstractMap.SimpleImmutableEntry<>(i.format(MapTimeTable.TIME_FORMATTER),
                            routeQueryBuilder.setValue(Integer.toString(timeIndex(timeTable, i))).build(false))
            );
        }

        return inlineKeyboardMarkupBuilder
                .addSeparateRowsKeyboardButtons(4, hours)
                .addFullRowInlineButton(mBB.getMessage("now"), routeQueryBuilder.setValue(NOW).build(true))
                .build(true);
    }

    // endregion keyboard
}
