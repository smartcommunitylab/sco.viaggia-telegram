package viaggia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

/**
 * @author Luca Mosetti
 * @since 2017
 */
class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String botName = "";
        String botToken = "";
        String chatbaseToken = "";

        if (args != null && args.length == 3) {
            botName = args[0].trim();
            botToken = args[1].trim();
            chatbaseToken = args[2].trim();
        }

        // register the bot
        ApiContextInitializer.init();

        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(new ViaggiaTrentoBot(botName, botToken, chatbaseToken));
        } catch (TelegramApiRequestException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
