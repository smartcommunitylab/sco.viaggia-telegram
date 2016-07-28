package org.telegram.updateshandlers;

import eu.trentorise.smartcampus.mobilityservice.MobilityServiceException;
import eu.trentorise.smartcampus.mobilityservice.model.TimeTable;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Stop;
import org.telegram.BotConfig;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendVenue;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.updateshandlers.GestioneMessaggi.*;

import java.util.List;

import static org.telegram.updateshandlers.GestioneMessaggi.Commands.*;
import static org.telegram.updateshandlers.GestioneMessaggi.Keyboards.*;
import static org.telegram.updateshandlers.GestioneMessaggi.Texts.*;

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
            if (update.hasMessage()) {
                Message message = update.getMessage();
                if (message.hasText())
                    handleIncomingTextMessage(message);
                else if (message.hasLocation())
                    handleIncomingPositionMessage(message);
            } else if (update.getCallbackQuery() != null) {
                CallbackQuery cbq = update.getCallbackQuery();
                EditMessageText prova = new EditMessageText();
                prova.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                prova.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
                prova.setReplyMarkup(getInlineKeyboard());
                editMessageText(prova);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleIncomingTextMessage(Message message) throws TelegramApiException, MobilityServiceException {
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
                            case ESPAÑOL:
                                Current.setLanguage(chatId, Language.ESPAÑOL);
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
                                sendMessageDefault(message, keyboardStart(chatId), textStartTaxi(Database.getTaxiInfo()));
                                break;
                            case AUTOBUSCOMMAND:
                                sendMessageDefault(message, keyboardAutobus(chatId, Database.getAutbusRoute()), textStartAutobus(Current.getLanguage(chatId)));
                                break;
                            case TRAINSCOMMAND:
                                sendMessageDefault(message, keyboardTrains(chatId, Database.getTrainsRoute()), textStartTrains(Current.getLanguage(chatId)));
                                break;
                            case PARKINGSCOMMAND:
                                sendMessageDefault(message, keyboardParkings(chatId, Database.getParkings()), textStartParkings(Current.getLanguage(chatId)));
                                break;
                            case BIKESHARINGSCOMMAND:
                                sendMessageDefault(message, keyboardBikeSharings(chatId, Database.getBikeSharing()), textStartBikeSharings(Current.getLanguage(chatId)));
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

    private void handleIncomingPositionMessage(Message message) throws TelegramApiException, MobilityServiceException {
        switch (Current.getMenu(message.getChatId())) {
            case START:
                // TODO
                option(message);
                break;
            case AUTOBUS:
                // TODO
                option(message);
                break;
            case TRAINS:
                // TODO
                option(message);
                break;
            case PARKINGS:
                sendMessageDefault(message, Keyboards.keyboardParkings(message.getChatId(), Database.getParkings()), textParkingsNear(Database.getNear(Database.getParkings(), message.getLocation())));
                break;
            case BIKESHARINGS:
                sendMessageDefault(message, Keyboards.keyboardBikeSharings(message.getChatId(), Database.getBikeSharing()), textBikeSharingsNear(Database.getNear(Database.getBikeSharing(), message.getLocation())));
                break;
        }
    }

    // region TODO voids

    private void autobus(Message message) throws TelegramApiException, MobilityServiceException {
        TimeTable timeTable = Database.getAutobusTimetable(message.getText(), true);
        if (timeTable == null)
            option(message);
        else
            sendMessageDefault(message, getInlineKeyboard(timeTable.getTimes().size(), "RITORNO"), textAutobus(message.getText(), timeTable, 0));
    }

    private void trains(Message message) throws TelegramApiException, MobilityServiceException {
        List<Stop> bibbula = Database.getStopTrain(message.getText());
        if (bibbula == null) option(message);
        else
            sendMessageDefault(message, keyboardTrains(message.getChatId(), Database.getTrainsRoute()), textTrain(bibbula));
    }

    // endregion TODO voids

    // region utilities

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

    private void zone(Message message, Menu menu) throws TelegramApiException, MobilityServiceException {
        boolean flag = false;
        List<Parking> parkings = menu == Menu.PARKINGS ? Database.getParkings() : Database.getBikeSharing();

        String text = message.getText().startsWith("/") ? message.getText().substring(1) : message.getText();

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

        if (!flag) option(message);
    }

    private void option(Message message) throws TelegramApiException {
        sendMessageDefaultWithReply(message, null, textOption(Current.getLanguage(message.getChatId())));
    }

    // endregion utilities

}