package viaggia.command.route;

import bot.exception.EmptyKeyboardException;
import bot.keyboard.InlineKeyboardMarkupBuilder;
import bot.model.Command;
import bot.model.UseCaseCommand;
import bot.model.handling.HandleCallbackQuery;
import bot.model.handling.HandleInlineQuery;
import bot.model.query.Query;
import bot.timed.Chats;
import bot.timed.SendBundleAnswerCallbackQuery;
import bot.timed.TimedAbsSender;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
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
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Chat;
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
import viaggia.utils.MessageBundleBuilder;

import java.time.LocalTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by Luca Mosetti on 2017
 */
public abstract class AbstractRouteCommand extends UseCaseCommand implements HandleCallbackQuery, HandleInlineQuery {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRouteCommand.class);

    protected final InlineKeyboardMarkupBuilder inlineKeyboardMarkupBuilder = new InlineKeyboardMarkupBuilder();
    protected final RouteQueryBuilder routeQueryBuilder = new RouteQueryBuilder();
    protected final MessageBundleBuilder mBB = new MessageBundleBuilder();

    private final InlineKeyboardRowNavRouteBuilder inlineKeyboardRowNavRouteBuilder = new InlineKeyboardRowNavRouteBuilder();
    private final RouteQueryParser routeQueryParser = new RouteQueryParser();
    private final Mode mode;

    public AbstractRouteCommand(Command command, Mode mode) {
        super(command);
        this.mode = mode;
    }

    @Override
    public void respondCommand(TimedAbsSender absSender, User user, Chat chat) {
        mBB.setUser(user);
        routeStartCommand(absSender, chat);
    }

    @Override
    public void respondMessage(TimedAbsSender absSender, User user, Chat chat, String arguments) {
        mBB.setUser(user);

        try {
            SendMessage sendMessage = new SendMessage()
                    .setChatId(chat.getId())
                    .setParseMode(ParseMode.MARKDOWN);

            ComparableRoute route = getRoute(arguments);
            MapTimeTable routeTT = getRouteTimeTable(route);

            if (routeTT.getTimes().isEmpty())
                sendMessage
                        .setText(notTransitTextResponseBuilder(route))
                        .setReplyMarkup(notTransitInlineKeyboard(route));
            else
                sendMessage
                        .setText(textResponseBuilder(route, routeTT, nowIndex(routeTT), false))
                        .setReplyMarkup(routesInlineKeyboard(route, routeTT, nowIndex(routeTT), null));

            absSender.execute(sendMessage);

        } catch (IncorrectValueException e) {
            routeStartCommand(absSender, chat);
        } catch (ExecutionException | EmptyKeyboardException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void respondCallbackQuery(TimedAbsSender absSender, CallbackQuery cbq, Query query) {
        mBB.setUser(cbq.getFrom());
        try {
            RouteQuery q = routeQueryParser.parse(query);
            ComparableRoute route = getRoute(q.getId());
            MapTimeTable routeTT = getRouteTimeTable(route, q.getStopId());

            AnswerCallbackQuery answer = new AnswerCallbackQuery()
                    .setCallbackQueryId(cbq.getId())
                    .setText(route.getRouteShortName());

            EditMessageText editMessageText = new EditMessageText()
                    .setParseMode(ParseMode.MARKDOWN);

            // which message I'm editing
            if (cbq.getMessage() != null)
                editMessageText
                        .setChatId(cbq.getMessage().getChatId())
                        .setMessageId(cbq.getMessage().getMessageId());
            else
                editMessageText
                        .setInlineMessageId(cbq.getInlineMessageId());

            if (routeTT.getTimes().isEmpty()) {
                editMessageText
                        .setText(notTransitTextResponseBuilder(route))
                        .setReplyMarkup(notTransitInlineKeyboard(route));
            } else if ((q.getStopId() == null || q.getStopId().isEmpty()) && (q.getValue().equals("flt")))
                // filter request
                editMessageText
                        .setText(mBB.getMessage("chooseStop"))
                        .setReplyMarkup(stopsInlineKeyboard(route.getId(), routeTT.getStops(), routeTT.getStopsId()));
            else {
                Integer val = Ints.tryParse(q.getValue());

                if (val == null || val < 0 || val > routeTT.getTimes().size())
                    val = nowIndex(routeTT);

                editMessageText
                        .setText(textResponseBuilder(route, routeTT, val, q.getStopId() != null && !q.getStopId().isEmpty() && !q.getStopId().equals("null")))
                        .setReplyMarkup(routesInlineKeyboard(route, routeTT, val, q.getStopId()));
            }

            if (cbq.getMessage() == null || !equalsFormattedTexts(editMessageText.getText(), cbq.getMessage().getText(), ParseMode.MARKDOWN))
                absSender.execute(new SendBundleAnswerCallbackQuery<>(editMessageText, answer));
            else
                absSender.execute(answer);


        } catch (ExecutionException | EmptyKeyboardException e) {
            logger.error(e.getMessage());
        } catch (IncorrectValueException e) {
            /* DO NOTHING */
        }
    }

    @Override
    public void respondInlineQuery(TimedAbsSender absSender, User user, String id, String arguments) {
        mBB.setUser(user);
        try {
            absSender.execute(new AnswerInlineQuery()
                    .setInlineQueryId(id)
                    .setResults(results(arguments)));
        } catch (ExecutionException | EmptyKeyboardException e) {
            logger.error(e.getMessage());
        }
    }

    private void routeStartCommand(TimedAbsSender absSender, Chat chat) {
        try {
            absSender.execute(new SendMessage()
                    .setChatId(chat.getId())
                    .setText(mBB.getMessage(getCommand().getDescription()))
                    .setReplyMarkup(linesKeyboard()));

            Chats.setCommand(chat.getId(), getCommand());
        } catch (EmptyKeyboardException | ExecutionException e) {
            logger.error(e.getMessage());
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

    // region mobilityservice.utils

    private int nowIndex(MapTimeTable timeTable) {
        LocalTime tmp;

        for (List<LocalTime> times : timeTable.getTimes()) {
            if ((tmp = getLastNotNull(times)) != null && tmp.isAfter(LocalTime.now()))
                return timeTable.getTimes().indexOf(times);
        }

        return timeTable.getTimes().size() - 1;
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
                InlineQueryResultArticle result = new InlineQueryResultArticle()
                        .setId(route.getId().getId());
                String thumb;

                switch (mode) {
                    case LONG_NAME:
                        result.setTitle(route.getRouteLongName());
                        thumb = route.getRouteLongName().substring(0, 2);
                        break;
                    case SHORT_NAME:
                    default:
                        result.setTitle(route.getRouteShortName())
                                .setDescription(route.getRouteLongName());
                        thumb = route.getRouteShortName();
                        break;
                }

                if (timeTable.getTimes().isEmpty())
                    result.setThumbUrl("https://fakeimg.pl/100x100/00CED1/384F47/?&font_size=70&retina=1&text=" + thumb)
                            .setReplyMarkup(notTransitInlineKeyboard(route))
                            .setInputMessageContent(new InputTextMessageContent()
                                    .setParseMode(ParseMode.MARKDOWN)
                                    .setMessageText(notTransitTextResponseBuilder(route)));
                else
                    result.setThumbUrl("https://fakeimg.pl/100x100/F3AC61/845422/?&font_size=70&retina=1&text=" + thumb)
                            .setReplyMarkup(routesInlineKeyboard(route, timeTable, 0, null))
                            .setInputMessageContent(new InputTextMessageContent()
                                    .setParseMode(ParseMode.MARKDOWN)
                                    .setMessageText(mBB.getMessage("browse")));


                results.add(result);
            }
        } catch (IncorrectValueException e) {
            logger.error(e.getMessage());
        }

        return results;
    }

    // endregion mobilityservice.utils

    // region text

    private String notTransitTextResponseBuilder(ComparableRoute route) {
        switch (mode) {
            case LONG_NAME:
                return "*" + getCommand().getCommandIdentifier().toUpperCase() + ' ' + route.getRouteLongName() + "*\n"
                        + mBB.getMessage("notransit");
            case SHORT_NAME:
            default:
                return "*" + getCommand().getCommandIdentifier().toUpperCase() + ' ' + route.getRouteShortName() + "*\n"
                        + mBB.getMessage("notransit");
        }

    }

    private String textResponseBuilder(ComparableRoute route, MapTimeTable timeTable, int chosen, boolean filtred) {
        switch (mode) {
            case LONG_NAME:
                return "*" + getCommand().getCommandIdentifier().toUpperCase() + ' ' + route.getRouteLongName() + "*"
                        + timeTableToString(timeTable, chosen, filtred);

            case SHORT_NAME:
            default:
                return "*" + getCommand().getCommandIdentifier().toUpperCase() + ' ' + route.getRouteShortName() + "*"
                        + timeTableToString(timeTable, chosen, filtred);
        }
    }

    private String timeTableToString(MapTimeTable timeTable, int chosen, boolean filtred) {
        StringBuilder text = new StringBuilder();
        List<String> stops = timeTable.getStops();
        List<LocalTime> times = timeTable.getTimes().get(chosen);
        boolean before;

        for (int i = 0, timesSize = times.size(); i < timesSize; i++) {
            LocalTime time = times.get(i);
            if (time != null) {
                before = time.isBefore(LocalTime.now()) ^ time.getHour() == 0;
                text.append("\n`").append(time.format(MapTimeTable.TIME_FORMATTER)).append("` ");

                if (filtred) {
                    /*⇣*/
                    if (before) text.append("× ");
                    text.append("*").append(stops.get(i)).append("*");
                    filtred = false;
                } else {
                    if (before) text.append("× _").append(stops.get(i)).append("_");
                    else text.append(stops.get(i));
                }
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
                .build();
    }

    private InlineKeyboardMarkup stopsInlineKeyboard(ComparableId routeId, List<String> stops, List<String> stopsId) throws EmptyKeyboardException {
        int textButtonsSize;
        List<Map.Entry<String, String>> entryButtons = new ArrayList<>();

        if ((textButtonsSize = stops.size()) != stopsId.size())
            throw new IllegalArgumentException();

        for (int i = 0; i < textButtonsSize; i++) {
            entryButtons.add(
                    new AbstractMap.SimpleEntry<>(
                            stops.get(i),
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
                .setColumns(1)
                .addSeparateRowsKeyboardButtons(entryButtons)
                .addFullRowInlineButton(mBB.getMessage("full"), routeQueryBuilder
                        .setCommand(getCommand())
                        .setId(routeId)
                        .setValue("now")
                        .build(true))
                .build();
    }

    protected void inlineKeyboardMarkupBuilder(ComparableRoute route, MapTimeTable timeTable, int chosen, String stopId) {
        if (chosen < 0 && chosen > timeTable.getTimes().size() - 1)
            chosen = nowIndex(timeTable);

        List<Map.Entry<String, String>> buttons = new ArrayList<>();

        buttons.add(new AbstractMap.SimpleEntry<>(mBB.getMessage("now"), routeQueryBuilder
                .setCommand(getCommand())
                .setId(route.getId())
                .setValue("now")
                .setStopId(stopId)
                .build(true)));

        buttons.add(new AbstractMap.SimpleEntry<>(mBB.getMessage("filter"), routeQueryBuilder
                .setCommand(getCommand())
                .setId(route.getId())
                .setValue("flt")
                .build(true)));

        inlineKeyboardMarkupBuilder
                .setColumns(5)
                .addSeparateRowsKeyboardButtons(
                        inlineKeyboardRowNavRouteBuilder.build(timeTable.getTimes().size(), chosen, 5, getCommand(), route.getId(), stopId)
                )
                .addSeparateRowsKeyboardButtons(buttons);
    }

    protected InlineKeyboardMarkup routesInlineKeyboard(ComparableRoute route, MapTimeTable timeTable, int chosen, String stopId) throws EmptyKeyboardException, IncorrectValueException, ExecutionException {
        inlineKeyboardMarkupBuilder(route, timeTable, chosen, stopId);
        return inlineKeyboardMarkupBuilder.build();
    }

    protected abstract ReplyKeyboard linesKeyboard() throws EmptyKeyboardException, ExecutionException;

    // endregion keyboard
}
