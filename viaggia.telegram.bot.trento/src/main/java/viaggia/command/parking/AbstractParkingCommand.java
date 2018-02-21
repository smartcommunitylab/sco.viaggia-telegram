package viaggia.command.parking;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;
import gekoramy.telegram.bot.keyboard.InlineKeyboardMarkupBuilder;
import gekoramy.telegram.bot.keyboard.ReplyKeyboardMarkupBuilder;
import gekoramy.telegram.bot.model.Command;
import gekoramy.telegram.bot.model.query.Query;
import gekoramy.telegram.bot.responder.CallbackQueryResponder;
import gekoramy.telegram.bot.responder.InlineCallbackQueryResponder;
import gekoramy.telegram.bot.responder.InlineQueryResponder;
import gekoramy.telegram.bot.responder.MessageResponder;
import gekoramy.telegram.bot.responder.type.CallbackQueryEditor;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
import mobilityservice.model.Parkings;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendVenue;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
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
import viaggia.exception.NotHandledException;
import viaggia.extended.DistinguishedUseCaseCommand;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public abstract class AbstractParkingCommand extends DistinguishedUseCaseCommand {

    private final Unit unit;
    private final int maxDistance;

    public AbstractParkingCommand(Command command, int maxDistance, Unit unit) {
        super(command);
        this.maxDistance = maxDistance;
        this.unit = unit;
    }

    @Override
    public void respondCommand(MessageResponder absSender, Chat chat, User user) {
        super.respondCommand(absSender, chat, user);
        try {
            absSender.send(parkingMessage(chat, user.getId()))
                    .toComplete();
        } catch (Throwable e) {
            logger.error(getClass().toString(), e);
        }
    }

    @Override
    public void respondText(MessageResponder absSender, Chat chat, User user, String arguments) {
        super.respondText(absSender, chat, user, arguments);
        try {
            Parking parking = getParking(arguments);

            absSender.send(
                    new SendVenue()
                            .setTitle(parking.getName())
                            .setLatitude((float) parking.getPosition()[0])
                            .setLongitude((float) parking.getPosition()[1])
                            .setAddress(parking.getDescription())
                            .setReplyMarkup(inlineKeyboardMarkup(parking, user.getId()))
            );

        } catch (NotHandledException e) {
            absSender.send(parkingMessage(chat, user.getId()));
            absSender.toComplete();
            absSender.setHandled(false);
        } catch (Throwable e) {
            logger.error(getClass().toString(), e);
        }
    }

    @Override
    public void respondLocation(MessageResponder absSender, Chat chat, User user, Location location) {
        try {
            Map<String, Double> bikeDistance = getParkings(location);

            absSender.send(new SendMessage()
                    .setParseMode(ParseMode.MARKDOWN)
                    .setText(textBuilderDistance(bikeDistance, user.getId()))
                    .setReplyMarkup(keyboardMarkup(chat)));

        } catch (Throwable e) {
            logger.error(getClass().toString(), e);
        }
    }

    public void respondCallbackQuery(CallbackQueryResponder absSender, Query query, User user, Message message) {
        parkingCallbackQueryHandling(absSender, query, user.getId());

    }

    public void respondCallbackQuery(InlineCallbackQueryResponder absSender, Query query, User user) {
        parkingCallbackQueryHandling(absSender, query, user.getId());
    }

    public void respondInlineQuery(InlineQueryResponder absSender, User user, String arguments) {
        try {
            absSender.answer(new AnswerInlineQuery()
                    .setResults(results(arguments, user.getId())));
        } catch (Throwable e) {
            logger.error(getClass().toString(), e);
        }
    }

    private void parkingCallbackQueryHandling(CallbackQueryEditor absSender, Query query, Integer userId) {
        try {
            ParkingQuery q = ParkingQueryParser.parse(query);
            Parking parking = getSimilarParking(q.getName());

            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setText(parking.getName());
            answer.setCacheTime(30);

            EditMessageText edit = new EditMessageText();
            edit.setReplyMarkup(inlineKeyboardMarkup(parking, userId));

            if (Integer.parseInt(q.getValue()) != available(parking)) {
                absSender.send(edit);
            }

            absSender.answer(answer);

        } catch (NotHandledException e) {
            absSender.setHandled(false);
            logger.error(getClass().toString(), e);
        } catch (Throwable e) {
            logger.error(getClass().toString(), e);
        }
    }

    // region SendMessage

    private SendMessage parkingMessage(Chat chat, int userId) {
        try {
            return new SendMessage()
                    .setText(mBB.getMessage(userId, getCommand().getDescription()))
                    .setReplyMarkup(keyboardMarkup(chat));
        } catch (ExecutionException e) {
            return new SendMessage()
                    .setText("Something went wrong...");
        }
    }

    // endregion SendMessage

    // region getters

    protected abstract List<Parking> getParkings() throws ExecutionException;

    private Parking getSimilarParking(String name) throws ExecutionException, NotHandledException {
        Parkings parkings = new Parkings();
        parkings.putAll(getParkings());
        Parking parking = parkings.getSimilar(name);

        if (parking == null) throw new NotHandledException();

        return parking;
    }

    private Parking getParking(String arguments) throws ExecutionException, NotHandledException {
        Parkings parkings = new Parkings();
        parkings.putAll(getParkings());
        Parking parking = parkings.get(arguments);

        if (parking == null) throw new NotHandledException();

        return parking;
    }

    private List<Parking> getParkings(String filter) throws ExecutionException {
        Parkings p = new Parkings();
        p.putAll(getParkings());

        return filter == null || filter.isEmpty() ? p : p.subParkings(filter);
    }

    private Map<String, Double> getParkings(Location location) throws ExecutionException {
        Map<String, Double> map = DistanceCalculator.calculate(getParkings(), location, unit).entrySet().stream().filter(entry -> entry.getValue() < maxDistance).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b));

        return ImmutableSortedMap.copyOf(map, Ordering.natural().onResultOf(Functions.forMap(map)));
    }

    // endregion getters

    // region utils

    private List<InlineQueryResult> results(String filter, int userId) throws ExecutionException {
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
                            .setReplyMarkup(inlineKeyboardMarkup(p, userId)));
        }

        return results;
    }

    // endregion utils

    // region text

    private String textBuilderDistance(Map<String, Double> parkingDistance, int userId) {
        return mBB.getMessage(userId, "neighbours") + "\n" + distanceToString(parkingDistance);
    }

    protected abstract int available(Parking parking);

    protected abstract String slotsToString(Parking parking, int userId);

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

    private ReplyKeyboard keyboardMarkup(Chat chat) throws ExecutionException {
        List<String> parkings = ((Parkings) getParkings()).getNames();
        ReplyKeyboardMarkupBuilder builder = new ReplyKeyboardMarkupBuilder()
                .setResizeKeyboard(true)
                .setOneTimeKeyboard(true)
                .addKeyboardButtons(2, parkings);

        if (chat.isUserChat())
            builder
                    .addRequestLocationButton();

        return builder.build();
    }

    private InlineKeyboardMarkup inlineKeyboardMarkup(Parking parking, int userId) {
        return new InlineKeyboardMarkupBuilder()
                .addFullRowInlineButton(slotsToString(parking, userId), new ParkingQueryBuilder()
                        .setCommand(getCommand())
                        .setName(parking.getName())
                        .setAvailable(available(parking))
                        .build())
                .build();
    }

    // endregion keyboard

}
