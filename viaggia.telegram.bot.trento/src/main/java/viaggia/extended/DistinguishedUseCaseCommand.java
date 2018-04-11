package viaggia.extended;

import gekoramy.telegram.bot.model.Command;
import gekoramy.telegram.bot.model.UseCaseCommand;
import gekoramy.telegram.bot.responder.MessageResponder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Location;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.User;
import viaggia.utils.MessageBundleBuilder;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class DistinguishedUseCaseCommand extends UseCaseCommand {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final MessageBundleBuilder mBB = new MessageBundleBuilder();


    protected DistinguishedUseCaseCommand(Command command) {
        super(command);
    }

    @Override
    public void respondCommand(MessageResponder absSender, Chat chat, User user, String arguments) {
        if (arguments != null && !arguments.trim().isEmpty())
            respondText(absSender, chat, user, arguments);
        else
            respondCommand(absSender, chat, user);
    }

    @Override
    public void respondMessage(MessageResponder absSender, Message message) {
        if (message.getText() != null)
            respondText(absSender, message.getChat(), message.getFrom(), message.getText());

        if (message.getLocation() != null)
            respondLocation(absSender, message.getChat(), message.getFrom(), message.getLocation());
    }

    protected void respondCommand(MessageResponder absSender, Chat chat, User user) {
    }

    protected void respondText(MessageResponder absSender, Chat chat, User user, String text) {
    }

    protected void respondLocation(MessageResponder absSender, Chat chat, User user, Location location) {
    }
}
