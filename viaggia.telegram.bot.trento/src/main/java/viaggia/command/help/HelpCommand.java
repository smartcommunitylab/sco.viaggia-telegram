package viaggia.command.help;

import gekoramy.telegram.bot.CommandRegistry;
import gekoramy.telegram.bot.model.Command;
import gekoramy.telegram.bot.responder.MessageResponder;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import viaggia.extended.CommandRegistryUtils;
import viaggia.extended.DistinguishedUseCaseCommand;

/**
 * Helper message responder
 * it shows a list of all registered commands
 *
 * @author Luca Mosetti
 * @since 2017
 */
public class HelpCommand extends DistinguishedUseCaseCommand {
    private static final Command COMMAND_ID = new Command("help", "help_description");

    private final CommandRegistryUtils commandRegistry;

    public HelpCommand(CommandRegistry commandRegistry) {
        super(COMMAND_ID);
        this.commandRegistry = new CommandRegistryUtils(commandRegistry);
    }

    @Override
    public void respondCommand(MessageResponder absSender, Chat chat, User user) {
        super.respondCommand(absSender, chat, user);
        absSender.send(helpMessage(user.getId()));
    }

    @Override
    public void respondText(MessageResponder absSender, Chat chat, User user, String arguments) {
        super.respondText(absSender, chat, user, arguments);
        absSender.send(helpMessage(user.getId()));
    }

    // region SendMessage

    private SendMessage helpMessage(int userId) {
        return new SendMessage()
                .setParseMode(ParseMode.MARKDOWN)
                .setText(mBB.getMessage(userId, "help") + "\n\n" + commandRegistry.getHelpMessage(mBB, userId))
                .setReplyMarkup(new ReplyKeyboardRemove());
    }

    // endregion SendMessage
}