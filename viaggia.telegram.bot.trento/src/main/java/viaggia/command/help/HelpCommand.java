package viaggia.command.help;

import bot.CommandRegistry;
import bot.model.Command;
import bot.model.UseCaseCommand;
import bot.timed.Chats;
import bot.timed.TimedAbsSender;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import viaggia.extended.CommandRegistryUtils;
import viaggia.utils.MessageBundleBuilder;

/**
 * Created by Luca Mosetti in 2017
 * <p>
 * Helper message responder
 * it shows a list of all registered commands
 */
public class HelpCommand extends UseCaseCommand {

    private static final Command COMMAND_ID = new Command("help", "help_description");

    private final MessageBundleBuilder mBB = new MessageBundleBuilder();
    private final CommandRegistryUtils commandRegistry;

    public HelpCommand(CommandRegistry commandRegistry) {
        super(COMMAND_ID);
        this.commandRegistry = new CommandRegistryUtils(commandRegistry);
    }

    @Override
    public void respondCommand(TimedAbsSender absSender, User user, Chat chat) {
        mBB.setUser(user);
        helpMessage(absSender, chat);
    }

    @Override
    public void respondMessage(TimedAbsSender absSender, User user, Chat chat, String arguments) {
        mBB.setUser(user);
        helpMessage(absSender, chat);
    }

    private void helpMessage(TimedAbsSender absSender, Chat chat) {
        absSender.execute(new SendMessage()
                .setChatId(chat.getId())
                .setParseMode(ParseMode.MARKDOWN)
                .setText(mBB.getMessage("help") + "\n\n" + commandRegistry.getHelpMessage(mBB))
                .setReplyMarkup(new ReplyKeyboardRemove()));

        Chats.setCommand(chat.getId(), COMMAND_ID);
    }
}

