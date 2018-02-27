package viaggia.command.parking;

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
import mobilityservice.model.ParkingList;
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
import viaggia.exception.NotHandledException;
import viaggia.extended.DistinguishedUseCaseCommand;
import viaggia.utils.Distance;
import viaggia.utils.DistanceCalculator;
import viaggia.utils.Unit;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * General parking info
 *
 * @author Luca Mosetti
 * @since 2017
 */
public abstract class AbsParkingCommand extends DistinguishedUseCaseCommand {

    private final Unit unit;
    private final int maxDistance;

    protected AbsParkingCommand(Command command, int maxDistance, Unit unit) {
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
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void respondLocation(MessageResponder absSender, Chat chat, User user, Location location) {
        try {
            List<Distance<Parking>> distances = getClosestParking(location);

            absSender.send(new SendMessage()
                    .setParseMode(ParseMode.MARKDOWN)
                    .setText(textBuilderDistance(distances, user.getId()))
                    .setReplyMarkup(keyboardMarkup(chat)));

        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    // region SendMessage

    private SendMessage parkingMessage(Chat chat, int userId) {
        return new SendMessage()
                .setText(mBB.getMessage(userId, getCommand().getDescription()))
                .setReplyMarkup(keyboardMarkup(chat));
    }

    // endregion SendMessage

    // region getters

    protected abstract List<Parking> getParkingList();

    private Parking getSimilarParking(String name) throws NotHandledException {
        ParkingList parkingList = new ParkingList();
        parkingList.putAll(getParkingList());
        Parking parking = parkingList.getSimilar(name);

        if (parking == null) throw new NotHandledException();

        return parking;
    }

    private Parking getParking(String name) throws NotHandledException {
        ParkingList parkingList = new ParkingList();
        parkingList.putAll(getParkingList());
        Parking parking = parkingList.get(name);

        if (parking == null) throw new NotHandledException();

        return parking;
    }

    private List<Parking> getParkingSubList(String filter) {
        ParkingList p = new ParkingList();
        p.putAll(getParkingList());

        return filter == null || filter.isEmpty() ? p : p.parkingSubList(filter);
    }

    private List<Distance<Parking>> getClosestParking(Location location) {
        return DistanceCalculator.parkingDistance(unit, location, new HashSet<>(getParkingList()))
                .stream().filter(distance -> distance.getDistance() < maxDistance).collect(Collectors.toList());
    }

    // endregion getters

    // region utils

    private List<InlineQueryResult> results(String filter, int userId) {
        List<Parking> parkingSubList = getParkingSubList(filter);
        List<InlineQueryResult> results = new ArrayList<>();

        for (Parking p : parkingSubList) {
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

    private String textBuilderDistance(List<Distance<Parking>> parkingDistance, int userId) {
        return mBB.getMessage(userId, "neighbours") + "\n" + distanceToString(parkingDistance);
    }

    protected abstract int available(Parking parking);

    protected abstract String slotsToString(Parking parking, int userId);

    private String distanceToString(List<Distance<Parking>> parkingDistance) {
        StringBuilder text = new StringBuilder();
        DecimalFormat df = new DecimalFormat("0.0");

        for (Distance<Parking> distance : parkingDistance) {
            text.append("`");

            switch (unit) {
                case KILOMETER:
                    text.append(String.format("%3s", df.format(distance.getDistance())));
                    break;
                case METER:
                    text.append(String.format("%3s", distance.getDistance().intValue()));
                    break;
                case NAUTICAL_MILES:
                    text.append(String.format("%3s", df.format(distance.getDistance())));
                    break;
            }

            text.append(unit.getAbbreviation()).append("` - ").append(distance.getValue().getName()).append("\n");
        }

        return text.toString();
    }

    // endregion text

    // region keyboard

    private ReplyKeyboard keyboardMarkup(Chat chat) {
        List<String> parkingNames = ((ParkingList) getParkingList()).getNames();
        ReplyKeyboardMarkupBuilder builder = new ReplyKeyboardMarkupBuilder()
                .setResizeKeyboard(true)
                .setOneTimeKeyboard(true)
                .addKeyboardButtons(2, parkingNames);

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
