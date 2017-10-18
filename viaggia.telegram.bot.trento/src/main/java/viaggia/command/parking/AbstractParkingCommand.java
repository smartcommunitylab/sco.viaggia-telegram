package viaggia.command.parking;

import bot.exception.EmptyKeyboardException;
import bot.keyboard.InlineKeyboardMarkupBuilder;
import bot.keyboard.ReplyKeyboardMarkupBuilder;
import bot.model.Command;
import bot.model.UseCaseCommand;
import bot.model.handling.HandleCallbackQuery;
import bot.model.handling.HandleInlineQuery;
import bot.model.handling.HandleLocation;
import bot.model.query.Query;
import bot.timed.Chats;
import bot.timed.SendBundleAnswerCallbackQuery;
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
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Location;
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
import viaggia.utils.MessageBundleBuilder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Created by Luca Mosetti on 2017
 */
public abstract class AbstractParkingCommand extends UseCaseCommand implements HandleLocation, HandleCallbackQuery, HandleInlineQuery {
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
        mBB.setUser(user);
        parkingStartCommand(absSender, chat);
    }

    @Override
    public void respondMessage(TimedAbsSender absSender, User user, Chat chat, String arguments) {
        mBB.setUser(user);
        try {
            Parking parking = getParking(arguments);

            absSender.execute(new SendVenue()
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

            absSender.execute(new SendMessage()
                    .setChatId(chat.getId())
                    .setParseMode(ParseMode.MARKDOWN)
                    .setText(textBuilderDistance(bikeDistance))
                    .setReplyMarkup(keyboardMarkup(chat)));

        } catch (ExecutionException | EmptyKeyboardException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void respondCallbackQuery(TimedAbsSender absSender, CallbackQuery cbq, Query query) {
        mBB.setUser(cbq.getFrom());
        try {
            ParkingQuery q = parkingQueryParser.parse(query);
            Parking parking = getSimilarParking(q.getName());

            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup()
                    .setReplyMarkup(inlineKeyboardMarkup(parking));

            if (cbq.getMessage() != null)
                editMessageReplyMarkup
                        .setChatId(cbq.getMessage().getChatId())
                        .setMessageId(cbq.getMessage().getMessageId());
            else
                editMessageReplyMarkup
                        .setInlineMessageId(cbq.getInlineMessageId());

            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCacheTime(30);
            answer.setCallbackQueryId(cbq.getId()).setText(parking.getName());

            if (Integer.parseInt(q.getValue()) != (available(parking))) {
                absSender.execute(new SendBundleAnswerCallbackQuery<>(editMessageReplyMarkup, answer));
            } else {
                absSender.execute(answer);
            }

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
        } catch (EmptyKeyboardException | ExecutionException e) {
            logger.error(e.getMessage());
        }
    }

    private void parkingStartCommand(TimedAbsSender absSender, Chat chat) {
        try {
            absSender.execute(new SendMessage()
                    .setChatId(chat.getId())
                    .setText(mBB.getMessage(getCommand().getDescription()))
                    .setReplyMarkup(keyboardMarkup(chat)));

            Chats.setCommand(chat.getId(), getCommand());
        } catch (EmptyKeyboardException | ExecutionException e) {
            logger.error(e.getMessage());
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
                .setColumns(2)
                .setKeyboardButtons(parkings);

        if (chat.isUserChat())
            replyKeyboardMarkupBuilder
                    .addRequestLocationButton();

        return replyKeyboardMarkupBuilder.build();
    }

    private InlineKeyboardMarkup inlineKeyboardMarkup(Parking parking) throws EmptyKeyboardException {
        return inlineKeyboardMarkupBuilder
                .addFullRowInlineButton(slotsToString(parking), parkingQueryBuilder
                        .setCommand(getCommand())
                        .setName(parking.getName())
                        .setAvailable(available(parking))
                        .build(true))
                .build();
    }

    // endregion keyboard

}
