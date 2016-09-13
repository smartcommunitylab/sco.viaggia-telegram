package it.smartcommunitylab.viaggia.telegram;

import it.smartcommunitylab.viaggia.telegram.updateshandlers.ViaggiaBot;

import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;

/**
 * Created by gekoramy
 */
public class Main {

    public static void main(String[] args) {
    	
    	if (args == null || args.length != 2) {
    		System.err.println("Require 2 parameters: botName botToken");
    		System.exit(1);
    	}
    	
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new ViaggiaBot(args[0].trim(), args[1].trim()));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
