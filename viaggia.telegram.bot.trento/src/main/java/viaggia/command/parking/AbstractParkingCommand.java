package viaggia.command.parking;

import bot.exception.EmptyKeyboardException;
import bot.keyboard.InlineKeyboardMarkupBuilder;
import bot.keyboard.ReplyKeyboardMarkupBuilder;
import bot.model.Command;
import bot.model.handling.HandleCallbackQuery;
import bot.model.handling.HandleInlineQuery;
import bot.model.query.Query;
import bot.timed.Chats;
import bot.timed.TimedAbsSender;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
import mobilityservice.model.Parkings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendVenue;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Location;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResultVenue;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import viaggia.command.parking.general.query.ParkingQuery;
import viaggia.command.parking.general.query.ParkingQueryBuilder;
import viaggia.command.parking.general.query.ParkingQueryParser;
import viaggia.command.parking.general.utils.DistanceCalculator;
import viaggia.command.parking.general.utils.Unit;
import viaggia.exception.IncorrectValueException;
import viaggia.extended.DistinguishedUseCaseCommand;
import viaggia.utils.MessageBundleBuilder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Created by Luca Mosetti in 2017
 */
public abstract class AbstractParkingCommand extends DistinguishedUseCaseCommand implements HandleCallbackQuery, HandleInlineQuery {
    private static final Logger logger = LoggerFactory.getLogger(AbstractParkingCommand.class);

    protected final MessageBundleBuilder mBB = new MessageBundleBuilder();

    private final ReplyKeyboardMarkupBuilder replyKeyboardMarkupBuilder = new ReplyKeyboardMarkupBuilder();
    private final InlineKeyboardMarkupBuilder inlineKeyboardMarkupBuilder = new InlineKeyboardMarkupBuilder();
    private final DistanceCalculator distanceCalculator = new DistanceCalculator();
    private final ParkingQueryParser parkingQueryParser = new ParkingQueryParser();
    private final ParkingQueryBuilder parkingQueryBuilder = new ParkingQueryBuilder();
    private final Unit unit;
    private final int maxDistance;

    public AbstractParkingCommand(Command command, int maxDistance, Unit unit) {
        super(command);
        this.maxDistance = maxDistance;
        this.unit = unit;
    }

    @Override
    public void respondCommand(TimedAbsSender absSender, User user, Chat chat) {
        super.respondCommand(absSender, user, chat);
        mBB.setUser(user);
        parkingStartCommand(absSender, chat);
    }

    @Override
    public void respondText(TimedAbsSender absSender, User user, Chat chat, String arguments) {
        super.respondText(absSender, user, chat, arguments);
        mBB.setUser(user);
        try {
            Parking parking = getParking(arguments);

            absSender.requestExecute(chat.getId(), new SendVenue()
                    .setChatId(chat.getId())
                    .setTitle(parking.getName())
                    .setLatitude((float) parking.getPosition()[0])
                    .setLongitude((float) parking.getPosition()[1])
                    .setAddress(parking.getDescription())
                    .setReplyMarkup(inlineKeyboardMarkup(parking)));

        } catch (IncorrectValueException e) {
            parkingStartCommand(absSender, chat);
        } catch (ExecutionException | EmptyKeyboardException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void respondLocation(TimedAbsSender absSender, User user, Chat chat, Location location) {
        mBB.setUser(user);
        try {
            Map<String, Double> bikeDistance = getParkings(location);

            absSender.requestExecute(chat.getId(), new SendMessage()
                    .setChatId(chat.getId())
                    .setParseMode(ParseMode.MARKDOWN)
                    .setText(textBuilderDistance(bikeDistance))
                    .setReplyMarkup(keyboardMarkup(chat)));

        } catch (ExecutionException | EmptyKeyboardException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void respondCallbackQuery(TimedAbsSender absSender, String callbackQueryId, Query query, User user, Message message) {
        parkingCallbackQueryHandling(absSender, callbackQueryId, query, user, message.getChatId(), message.getMessageId(), null);
    }

    @Override
    public void respondCallbackQuery(TimedAbsSender absSender, String callbackQueryId, Query query, User user, String inlineMessageId) {
        parkingCallbackQueryHandling(absSender, callbackQueryId, query, user, null, null, inlineMessageId);
    }

    @Override
    public void respondInlineQuery(TimedAbsSender absSender, User user, String id, String arguments) {
        mBB.setUser(user);
        try {
            absSender.requestExecute(null, new AnswerInlineQuery()
                    .setInlineQueryId(id)
                    .setResults(results(arguments)));
        } catch (EmptyKeyboardException | ExecutionException e) {
            logger.error(e.getMessage());
        }
    }

    private void parkingStartCommand(TimedAbsSender absSender, Chat chat) {
        try {
            absSender.requestExecute(chat.getId(), new SendMessage()
                    .setChatId(chat.getId())
                    .setText(mBB.getMessage(getCommand().getDescription()))
                    .setReplyMarkup(keyboardMarkup(chat)));

            Chats.setCommand(chat.getId(), getCommand());
        } catch (EmptyKeyboardException | ExecutionException e) {
            logger.error(e.getMessage());
        }
    }

    private void parkingCallbackQueryHandling(TimedAbsSender absSender, String callbackQueryId, Query query, User user, Long chatId, Integer messageId, String inlineMessageId) {
        mBB.setUser(user);
        try {
            ParkingQuery q = parkingQueryParser.parse(query);
            Parking parking = getSimilarParking(q.getName());

            EditMessageReplyMarkup edit = chatId != null ? new EditMessageReplyMarkup()
                    .setReplyMarkup(inlineKeyboardMarkup(parking))
                    .setChatId(chatId)
                    .setMessageId(messageId)
                    : new EditMessageReplyMarkup()
                    .setReplyMarkup(inlineKeyboardMarkup(parking))
                    .setInlineMessageId(inlineMessageId);

            AnswerCallbackQuery answer = new AnswerCallbackQuery()
                    .setCallbackQueryId(callbackQueryId)
                    .setText(parking.getName());

            answer.setCacheTime(30);

            if (chatId == null)
                chatId = (long) user.hashCode();

            if (Integer.parseInt(q.getValue()) != (available(parking))) {
                absSender.requestExecute(chatId, edit);
                absSender.requestExecute(chatId, answer);
            } else {
                absSender.requestExecute(chatId, answer);
            }

        } catch (ExecutionException | EmptyKeyboardException e) {
            logger.error(e.getMessage());
        } catch (IncorrectValueException e) {
            /* DO NOTHING */
        }
    }

    // region getters

    protected abstract List<Parking> getParkings() throws ExecutionException;

    private Parking getSimilarParking(String name) throws ExecutionException, IncorrectValueException {
        Parkings parkings = new Parkings();
        parkings.putAll(getParkings());
        Parking parking = parkings.getSimilar(name);

        if (parking == null) throw new IncorrectValueException();

        return parking;
    }

    private Parking getParking(String arguments) throws ExecutionException, IncorrectValueException {
        Parkings parkings = new Parkings();
        parkings.putAll(getParkings());
        Parking parking = parkings.get(arguments);

        if (parking == null) throw new IncorrectValueException();

        return parking;
    }

    private List<Parking> getParkings(String filter) throws ExecutionException {
        Parkings p = new Parkings();
        p.putAll(getParkings());

        return filter == null || filter.isEmpty() ? p : p.subParkings(filter);
    }

    private Map<String, Double> getParkings(Location location) throws ExecutionException {
        Map<String, Double> map = distanceCalculator.calculate(getParkings(), location, unit).entrySet().stream().filter(entry -> entry.getValue() < maxDistance).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b));

        return ImmutableSortedMap.copyOf(map, Ordering.natural().onResultOf(Functions.forMap(map)));
    }

    // endregion getters

    // region utils

    private List<InlineQueryResult> results(String filter) throws EmptyKeyboardException, ExecutionException {
        List<Parking> subParkings = getParkings(filter);
        List<InlineQueryResult> results = new ArrayList<>();

        for (Parking p : subParkings) {
            results.add(
                    new InlineQueryResultVenue()
                            .setId(p.getName())
                            .setTitle(p.getName())
                            .setLatitude((float) p.getPosition()[0])
                            .setLongitude((float) p.getPosition()[1])
                            .setAddress(p.getDescription())
                            .setReplyMarkup(inlineKeyboardMarkup(p)));
        }

        return results;
    }

    // endregion utils

    // region text

    private String textBuilderDistance(Map<String, Double> parkingDistance) {
        return mBB.getMessage("neighbours") + "\n" + distanceToString(parkingDistance);
    }

    protected abstract int available(Parking parking);

    protected abstract String slotsToString(Parking parking);

    private String distanceToString(Map<String, Double> parkingDistance) {
        StringBuilder text = new StringBuilder();
        DecimalFormat df = new DecimalFormat("0.0");

        for (Map.Entry<String, Double> entry : parkingDistance.entrySet()) {
            text.append("`");

            switch (unit) {
                case KILOMETER:
                    text.append(String.format("%3s", df.format(entry.getValue())));
                    break;
                case METER:
                    text.append(String.format("%3s", entry.getValue().intValue()));
                    break;
                case NAUTICAL_MILES:
                    text.append(String.format("%3s", df.format(entry.getValue())));
                    break;
            }

            text.append(unit.getAbbreviation()).append("` - ").append(entry.getKey()).append("\n");
        }

        return text.toString();
    }

    // endregion text

    // region keyboard

    private ReplyKeyboard keyboardMarkup(Chat chat) throws EmptyKeyboardException, ExecutionException {
        List<String> parkings = ((Parkings) getParkings()).getNames();

        replyKeyboardMarkupBuilder
                .setResizeKeyboard(true)
                .setOneTimeKeyboard(true)
                .addKeyboardButtons(2, parkings);

        if (chat.isUserChat())
            replyKeyboardMarkupBuilder
                    .addRequestLocationButton();

        return replyKeyboardMarkupBuilder.build(true);
    }

    private InlineKeyboardMarkup inlineKeyboardMarkup(Parking parking) throws EmptyKeyboardException {
        return inlineKeyboardMarkupBuilder
                .addFullRowInlineButton(slotsToString(parking), parkingQueryBuilder
                        .setCommand(getCommand())
                        .setName(parking.getName())
                        .setAvailable(available(parking))
                        .build(true))
                .build(true);
    }

    // endregion keyboard

}
