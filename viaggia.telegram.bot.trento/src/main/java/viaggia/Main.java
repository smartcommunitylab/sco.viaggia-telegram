package viaggia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

/**
 * Created by Luca Mosetti on 2017
 */
public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
//        String botName = "asdfjhgasdfjkhgasdfjhgasdfbot";
        String botName = "SArchonBot";
//        String botToken = "262858092:AAFUJEvBA3-IetGi7upGZtgpfeGAsi2_g68";
        String botToken = "398109514:AAE8rW02WkXK_62V8KwTx9T5s9YL-6yjaSk";

        if (args != null && args.length == 2) {
            botName = args[0].trim();
            botToken = args[1].trim();
        }

        // register the bot
        ApiContextInitializer.init();

        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(new ViaggiaTrentoBot(botName, botToken));
        } catch (TelegramApiRequestException e) {
            logger.error(e.getMessage());
        }
    }
}
