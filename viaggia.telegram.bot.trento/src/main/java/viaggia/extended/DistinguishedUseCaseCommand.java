package viaggia.extended;

import bot.model.Command;
import bot.model.UseCaseCommand;
import bot.timed.TimedAbsSender;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Location;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.User;

/**
 * Created by Luca Mosetti in 2018
 * <p>
 * Distinguishes:
 * - command messages
 * - text messages
 * - location messages
 */
public class DistinguishedUseCaseCommand extends UseCaseCommand {

    public DistinguishedUseCaseCommand(Command command) {
        super(command);
    }

    @Override
    public void respondCommand(TimedAbsSender absSender, User user, Chat chat, String arguments) {
        if (arguments != null && !arguments.trim().isEmpty())
            respondText(absSender, user, chat, arguments);
        else
            respondCommand(absSender, user, chat);
    }

    @Override
    public void respondMessage(TimedAbsSender absSender, Message message) {
        if (message.getText() != null)
            respondText(absSender, message.getFrom(), message.getChat(), message.getText());

        if (message.getLocation() != null)
            respondLocation(absSender, message.getFrom(), message.getChat(), message.getLocation());
    }

    protected void respondCommand(TimedAbsSender absSender, User user, Chat chat) {}

    protected void respondText(TimedAbsSender absSender, User user, Chat chat, String text) {}

    protected void respondLocation(TimedAbsSender absSender, User user, Chat chat, Location location) {}
}
