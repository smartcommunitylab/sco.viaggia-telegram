package org.telegram;

import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.updateshandlers.GestioneHandlers;

/**
 * Created by gekoramy
 */
public class Main {

    public static void main(String[] args) {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new GestioneHandlers());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
