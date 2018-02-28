package viaggia;

import gekoramy.chatbase.ChatbaseHandler;
import gekoramy.telegram.bot.CommandRegistry;
import gekoramy.telegram.bot.UseCaseBot;
import gekoramy.telegram.bot.exception.TwoCommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.inlinequery.InlineQuery;
import viaggia.command.contribute.ContributeCommand;
import viaggia.command.help.HelpCommand;
import viaggia.command.language.LanguageCommand;
import viaggia.command.parking.bike.BikeCommand;
import viaggia.command.parking.parking.ParkingCommand;
import viaggia.command.route.bus.BusCommand;
import viaggia.command.route.train.TrainCommand;
import viaggia.command.start.StartCommand;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

/**
 * @author Luca Mosetti
 * @since 2017
 */
class ViaggiaTrentoBot extends UseCaseBot implements ChatbaseHandler {

    private static final Logger logger = LoggerFactory.getLogger(ViaggiaTrentoBot.class);
    private static final ChatbaseMonitor monitor = new ChatbaseMonitor("2.3");

    /**
     * Here should be registered all the UseCaseCommand
     *
     * @param botName  botName
     * @param botToken botToken
     */
    public ViaggiaTrentoBot(String botName, String botToken, String chatbaseToken) {
        super(botName, botToken, 15, monitor);

        monitor.setChatbaseHandler(this);
        monitor.setApiKey(chatbaseToken);

        try {
            CommandRegistry cr = getCommandRegistry();

            HelpCommand help = new HelpCommand(cr);
            StartCommand start = new StartCommand(cr);

            cr.register(start);
            cr.register(help);
            cr.register(new ContributeCommand());
            cr.register(new BikeCommand());
            cr.register(new BusCommand());
            cr.register(new ParkingCommand());
            cr.register(new TrainCommand());
            cr.register(new LanguageCommand());
            cr.setDefaultCmd(help.getCommand());
            cr.setDefaultInlineCmd(start.getCommand());

        } catch (TwoCommandException e) {
            logger.error(getClass().toString(), e);
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
        logger.error(getClass().toString(), e);
    }

    @Override
    public void onSucceed(Entity entity) {
        logger.info("Chatbase sent : " + entity.getEntity().toString());
    }

    @Override
    public void onFailure(Response response) {
        logger.error(response.readEntity(String.class));
    }

    @Override
    public void onException(Throwable throwable) {
        logger.error("Chatbase", throwable);
    }
}
