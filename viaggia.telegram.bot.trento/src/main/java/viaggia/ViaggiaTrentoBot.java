package viaggia;

import bot.BotHandler;
import bot.CommandRegistry;
import bot.exception.TwoCommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.inlinequery.InlineQuery;
import viaggia.command.help.HelpCommand;
import viaggia.command.language.LanguageCommand;
import viaggia.command.parking.bike.BikeCommand;
import viaggia.command.parking.parking.ParkingCommand;
import viaggia.command.route.bus.BusCommand;
import viaggia.command.route.train.TrainCommand;
import viaggia.command.start.StartCommand;

import java.io.InvalidClassException;

/**
 * Created by Luca Mosetti on 2017
 */
public class ViaggiaTrentoBot extends BotHandler {

    private static Logger logger = LoggerFactory.getLogger(ViaggiaTrentoBot.class);

    /**
     * Here should be registered all the UseCaseCommand
     *
     * @param botName
     * @param botToken
     */
    public ViaggiaTrentoBot(String botName, String botToken) {
        super(botName, botToken);

        try {
            CommandRegistry cr = getCommandRegistry();

            HelpCommand help = new HelpCommand(cr);
            StartCommand start = new StartCommand(cr);

            cr.register(start);
            cr.register(help);
            cr.register(new BikeCommand());
            cr.register(new BusCommand());
            cr.register(new ParkingCommand());
            cr.register(new TrainCommand());
            cr.register(new LanguageCommand());
            cr.setDefaultCommand(help);
            cr.setDefaultInlineCommand(start);

        } catch (TwoCommandException | InvalidClassException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    protected void onMessageUpdate(Message message) {
        Users.putUser(message.getFrom().getId());
        super.onMessageUpdate(message);
    }

    @Override
    protected void onCallbackQueryUpdate(CallbackQuery callbackQuery) {
        Users.putUser(callbackQuery.getFrom().getId());
        super.onCallbackQueryUpdate(callbackQuery);
    }

    @Override
    protected void onInlineQueryUpdate(InlineQuery inlineQuery) {
        Users.putUser(inlineQuery.getFrom().getId());
        super.onInlineQueryUpdate(inlineQuery);
    }

    @Override
    protected void onFailure(Exception e) {
        logger.error(e.getMessage());
    }
}
