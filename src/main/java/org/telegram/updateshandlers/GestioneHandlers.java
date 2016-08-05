package org.telegram.updateshandlers;

import eu.trentorise.smartcampus.mobilityservice.MobilityServiceException;
import eu.trentorise.smartcampus.mobilityservice.model.TimeTable;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;
import org.telegram.BotConfig;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendVenue;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.updateshandlers.GestioneMessaggi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.telegram.updateshandlers.GestioneMessaggi.Commands.*;
import static org.telegram.updateshandlers.GestioneMessaggi.Keyboards.*;
import static org.telegram.updateshandlers.GestioneMessaggi.Texts.*;

/**
 * Created by gekoramy
 */
public class GestioneHandlers extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return BotConfig.USERNAMEMYPROJECT;
    }

    @Override
    public String getBotToken() {
        return BotConfig.TOKENMYPROJECT;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasCallbackQuery())
                handleIncomingCallbackQuery(update.getCallbackQuery());

            if (update.hasMessage()) {
                Message message = update.getMessage();
                if (message.hasText())
                    handleIncomingTextMessage(message);
                if (message.hasLocation())
                    handleIncomingPositionMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // region handlers

    private void handleIncomingTextMessage(Message message) throws TelegramApiException, MobilityServiceException, ExecutionException {
        Long chatId = message.getChatId();

        switch (message.getText()) {
            // region commands
            case LANGUAGECOMMAND:
                sendMessageDefault(message, keyboardLanguage(chatId), textLanguage(Current.getLanguage(chatId)));
                break;

            case STARTCOMMAND:
                sendMessageDefault(message, keyboardStart(chatId), textStart(Current.getLanguage(chatId)));
                break;

            case BACKCOMMAND:
                sendMessageDefault(message, keyboardStart(chatId), textStartMain(Current.getLanguage(chatId)));
                break;
            // endregion commands
            default:
                switch (Current.getMenu(chatId)) {
                    case LANGUAGE:
                        // region menu.LANGUAGE
                        switch (message.getText()) {
                            case ITALIANO:
                                Current.setLanguage(chatId, Language.ITALIANO);
                                sendMessageDefault(message, keyboardLanguage(chatId), textLanguageChange(Current.getLanguage(chatId)));
                                break;
                            case ENGLISH:
                                Current.setLanguage(chatId, Language.ENGLISH);
                                sendMessageDefault(message, keyboardLanguage(chatId), textLanguageChange(Current.getLanguage(chatId)));
                                break;
                            case ESPANOL:
                                Current.setLanguage(chatId, Language.ESPANOL);
                                sendMessageDefault(message, keyboardLanguage(chatId), textLanguageChange(Current.getLanguage(chatId)));
                                break;
                        }
                        // endregion menu.LANGUAGE
                        break;
                    case START:
                        // region menu.START
                        switch (message.getText()) {
                            case HELPCOMMAND:
                                sendMessageDefault(message, keyboardStart(chatId), textStartHelp(Current.getLanguage(chatId)));
                                break;
                            case TAXICOMMAND:
                                sendMessageDefault(message, keyboardStart(chatId), textStartTaxi(Database.getTaxiContacts()));
                                break;
                            case AUTOBUSCOMMAND:
                                sendMessageDefault(message, keyboardAutobus(chatId, Database.getAutobusRoutes()), textStartAutobus(Current.getLanguage(chatId)));
                                break;
                            case TRAINSCOMMAND:
                                sendMessageDefault(message, keyboardTrains(chatId, Database.getTrainsRoutes()), textStartTrains(Current.getLanguage(chatId)));
                                break;
                            case PARKINGSCOMMAND:
                                sendMessageDefault(message, keyboardParkings(chatId, Database.getParkings()), textStartParkings(Current.getLanguage(chatId)));
                                break;
                            case BIKESHARINGSCOMMAND:
                                sendMessageDefault(message, keyboardBikeSharings(chatId, Database.getBikeSharings()), textStartBikeSharings(Current.getLanguage(chatId)));
                                break;
                            default:
                                sendMessageDefaultWithReply(message, keyboardStart(chatId), textError(Current.getLanguage(chatId)));
                                break;
                        }
                        // endregion start menu
                        break;
                    case AUTOBUS:
                        // region menu.AUTOBUS
                        switch (message.getText()) {
                            case HELPCOMMAND:
                                sendMessageDefault(message, textAutobusHelp(Current.getLanguage(chatId)));
                                break;
                            default:
                                autobus(message);
                                break;
                        }
                        // endregion menu.AUTOBUS
                        break;
                    case TRAINS:
                        // region menu.TRAINS
                        switch (message.getText()) {
                            case HELPCOMMAND:
                                sendMessageDefault(message, textTrainHelp(Current.getLanguage(chatId)));
                                break;
                            default:
                                trains(message);
                                break;
                        }
                        // endregion menu.TRAINS
                        break;
                    case PARKINGS:
                        // region menu.PARKINGS
                        switch (message.getText()) {
                            case HELPCOMMAND:
                                sendMessageDefault(message, textParkingsHelp(Current.getLanguage(chatId)));
                                break;
                            default:
                                zone(message, Menu.PARKINGS);
                                break;
                        }
                        // endregion menu.PARKINGS
                        break;
                    case BIKESHARINGS:
                        // region menu.BIKESHARINGS
                        switch (message.getText()) {
                            case HELPCOMMAND:
                                sendMessageDefault(message, textBikeSharingsHelp(Current.getLanguage(chatId)));
                                break;
                            default:
                                zone(message, Menu.BIKESHARINGS);
                                break;
                        }
                        // endregion menu.BIKESHARINGS
                        break;
                }
        }
    }

    private void handleIncomingPositionMessage(Message message) throws TelegramApiException, MobilityServiceException, ExecutionException {
        switch (Current.getMenu(message.getChatId())) {
            case START:
                error(message);
                break;
            case AUTOBUS:
                error(message);
                break;
            case TRAINS:
                error(message);
                break;
            case PARKINGS:
                sendMessageDefault(message, Keyboards.keyboardParkings(message.getChatId(), Database.getParkings()), textParkingsNear(Database.findNear(Database.getParkings(), message.getLocation())));
                break;
            case BIKESHARINGS:
                sendMessageDefault(message, Keyboards.keyboardBikeSharings(message.getChatId(), Database.getBikeSharings()), textBikeSharingsNear(Database.findNear(Database.getBikeSharings(), message.getLocation())));
                break;
        }
    }

    private void handleIncomingCallbackQuery(CallbackQuery cbq) throws TelegramApiException, MobilityServiceException, ExecutionException {
        Message message = cbq.getMessage();

        if (message.getText().startsWith(AUTOBUSCOMMAND))
            autobusEdit(cbq);
        else if (message.getText().startsWith(TRAINSCOMMAND)) {
            trainsEdit(cbq);
        }
    }

    // endregion handlers

    // region voids

    private void autobus(Message message) throws TelegramApiException, MobilityServiceException, ExecutionException {
        String routeId = Database.findAutobusAndataRouteId(message.getText());

        if (routeId == null)
            error(message);
        else {
            TimeTable timeTable = Database.getAutobusTimetable(routeId);
            int index = Database.findCurrentIndex(timeTable);
            sendMessageDefault(message, inlineKeyboardAutobus(routeId, index, timeTable.getTimes().size() - 1), textAutobus(message.getText(), timeTable, index));
        }
    }

    private void trains(Message message) throws TelegramApiException, MobilityServiceException, ExecutionException {
        String routeId = Database.findTrainRouteId(message.getText());

        if (routeId == null) {
            error(message);
        } else {
            TimeTable timeTable = Database.getTrainTimetable(routeId);
            int index = Database.findCurrentIndex(timeTable);
            sendMessageDefault(message, inlineKeyboardTrain(routeId, index, timeTable.getTimes().size() - 1), textTrain(message.getText(), timeTable, index));
        }
    }

    private void autobusEdit(CallbackQuery cbq) throws MobilityServiceException, TelegramApiException, ExecutionException {

        String routeId = cbq.getData().substring(0, cbq.getData().indexOf('~'));
        String option = cbq.getData().substring(cbq.getData().indexOf('~') + 1, cbq.getData().lastIndexOf('~'));
        int chosen = Integer.parseInt(cbq.getData().substring(cbq.getData().lastIndexOf('~') + 1));


        TimeTable timeTable;

        switch (option) {
            case CURRENT:
                // DO NOTHING
                break;

            case INDEX:
                timeTable = Database.getAutobusTimetable(routeId);

                autobusSendEdit(routeId, chosen, timeTable, cbq);
                break;

            case RETURN:
                routeId = routeId.replace('A', 'R');
                timeTable = Database.getAutobusTimetable(routeId);

                autobusSendEdit(routeId, chosen, timeTable, cbq);
                break;

            case ANDATA:
                routeId = routeId.replace('R', 'A');
                timeTable = Database.getAutobusTimetable(routeId);

                autobusSendEdit(routeId, chosen, timeTable, cbq);
                break;

            case NOW:
                timeTable = Database.getAutobusTimetable(routeId);
                int now = Database.findCurrentIndex(timeTable);

                if (now != chosen) autobusSendEdit(routeId, now, timeTable, cbq);
                break;
        }


        answerCallbackQuery(cbq, AUTOBUSCOMMAND);
    }

    private void trainsEdit(CallbackQuery cbq) throws MobilityServiceException, TelegramApiException, ExecutionException {

        String routeId = cbq.getData().substring(0, cbq.getData().indexOf('~'));
        String option = cbq.getData().substring(cbq.getData().indexOf('~') + 1, cbq.getData().lastIndexOf('~'));
        int chosen = Integer.parseInt(cbq.getData().substring(cbq.getData().lastIndexOf('~') + 1));


        TimeTable timeTable;

        switch (option) {
            case CURRENT:
                // DO NOTHING
                break;

            case INDEX:
                timeTable = Database.getTrainTimetable(routeId);

                trainSendEdit(routeId, chosen, timeTable, cbq);
                break;

            case NOW:
                timeTable = Database.getTrainTimetable(routeId);
                int now = Database.findCurrentIndex(timeTable);

                if (now != chosen) trainSendEdit(routeId, now, timeTable, cbq);
                break;
        }


        answerCallbackQuery(cbq, TRAINSCOMMAND);
    }

    private void autobusSendEdit(String routeId, int chosen, TimeTable timeTable, CallbackQuery cbq) throws MobilityServiceException, TelegramApiException, ExecutionException {
        String nameAutobus = "";

        for (Route route : Database.getAutobusRoutes())
            if (route.getId().getId().equals(routeId))
                nameAutobus = route.getRouteShortName();

        editMessageDefault(cbq.getMessage(), inlineKeyboardAutobus(routeId, chosen, timeTable.getTimes().size() - 1), textAutobus(nameAutobus, timeTable, chosen));
    }

    private void trainSendEdit(String routeId, int chosen, TimeTable timeTable, CallbackQuery cbq) throws MobilityServiceException, TelegramApiException, ExecutionException {
        String nameTrain = "";

        for (Route route : Database.getTrainsRoutes())
            if (route.getId().getId().equals(routeId))
                nameTrain = route.getRouteLongName();

        editMessageDefault(cbq.getMessage(), inlineKeyboardTrain(routeId, chosen, timeTable.getTimes().size() - 1), textTrain(nameTrain, timeTable, chosen));
    }

    private void zone(Message message, Menu menu) throws TelegramApiException, MobilityServiceException, ExecutionException {
        boolean flag = false;
        List<Parking> parkings = menu == Menu.PARKINGS ? Database.getParkings() : menu == Menu.BIKESHARINGS ? Database.getBikeSharings() : new ArrayList<>();

        String text = message.getText();

        for (Parking p : parkings)
            if (p.getName().equals(text)) {
                switch (menu) {
                    case PARKINGS:
                        sendMessageDefault(message, keyboardParkings(message.getChatId(), parkings), textParking(p));
                        break;
                    case BIKESHARINGS:
                        sendMessageDefault(message, keyboardBikeSharings(message.getChatId(), parkings), textBikeSharings(p));
                        break;
                }
                sendVenueDefault(message, (float) p.getPosition()[0], (float) p.getPosition()[1]);
                flag = true;
            }

        if (!flag) error(message);
    }

    private void error(Message message) throws TelegramApiException {
        sendMessageDefaultWithReply(message, null, Texts.textError(Current.getLanguage(message.getChatId())));
    }

    // endregion voids

    // region utilities

    private void answerCallbackQuery(CallbackQuery cbq, String aCbqText) throws TelegramApiException {
        AnswerCallbackQuery aCbq = new AnswerCallbackQuery();
        aCbq.setCallbackQueryId(cbq.getId());
        aCbq.setText(aCbqText);
        answerCallbackQuery(aCbq);
    }

    private void editMessageDefault(Message message, InlineKeyboardMarkup keyboard, String messageText) throws TelegramApiException {
        EditMessageText edit = new EditMessageText();
        edit.enableMarkdown(true);
        edit.setMessageId(message.getMessageId());
        edit.setChatId(message.getChatId().toString());
        edit.setText(messageText);
        edit.setReplyMarkup(keyboard);
        editMessageText(edit);
    }

    private void sendMessageDefaultWithReply(Message message, ReplyKeyboard keyboard, String text) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage().setChatId(message.getChatId().toString()).enableMarkdown(true);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(keyboard);
        sendMessage.setReplyToMessageId(message.getMessageId());

        sendMessage(sendMessage);
    }

    private void sendMessageDefault(Message message, ReplyKeyboard keyboard, String text) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage().setChatId(message.getChatId().toString()).enableMarkdown(true);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(keyboard);

        sendMessage(sendMessage);
    }

    private void sendMessageDefault(Message message, String text) throws TelegramApiException {
        sendMessageDefault(message, null, text);
    }

    private void sendVenueDefault(Message message, Float latitude, Float longitude) throws TelegramApiException {
        SendVenue sendVenue = new SendVenue().setChatId(message.getChatId().toString());
        sendVenue.setLatitude(latitude);
        sendVenue.setLongitude(longitude);

        sendVenue(sendVenue);
    }

    // endregion utilities

}