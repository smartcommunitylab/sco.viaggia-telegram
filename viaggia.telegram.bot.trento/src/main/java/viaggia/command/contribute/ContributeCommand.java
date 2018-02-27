package viaggia.command.contribute;

import gekoramy.telegram.bot.keyboard.InlineKeyboardMarkupBuilder;
import gekoramy.telegram.bot.model.Command;
import gekoramy.telegram.bot.responder.MessageResponder;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import viaggia.extended.DistinguishedUseCaseCommand;

/**
 * Info for people who wants to contribute to @ViaggiaTrentoBot
 *
 * @author Luca Mosetti
 * @since 2017
 */
public class ContributeCommand extends DistinguishedUseCaseCommand {
    private static final Command COMMAND_ID = new Command("contribute", "contribute_description");

    public ContributeCommand() {
        super(COMMAND_ID);
    }

    @Override
    public void respondCommand(MessageResponder absSender, Chat chat, User user) {
        super.respondCommand(absSender, chat, user);
        try {
            absSender.send(contributeMessage(user.getId()));
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void respondText(MessageResponder absSender, Chat chat, User user, String arguments) {
        super.respondText(absSender, chat, user, arguments);
        try {
            absSender.send(contributeMessage(user.getId()));
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    // region SendMessage

    private SendMessage contributeMessage(int userId) {
        return new SendMessage()
                .setText(mBB.getMessage(userId, "contribute"))
                .setParseMode(ParseMode.MARKDOWN)
                .setReplyMarkup(
                        new InlineKeyboardMarkupBuilder()
                                .addFullRowUrlInlineButton(
                                        mBB.getMessage(userId, "donates"),
                                        "paypal.me/LucaMosetti"
                                )
                                .addFullRowUrlInlineButton(
                                        mBB.getMessage(userId, "report"),
                                        "t.me/VTNSupportBot"
                                )
                                .addFullRowUrlInlineButton(
                                        mBB.getMessage(userId, "translate"),
                                        "crowdin.com/project/viaggiatrentobot"
                                )
                                .addFullRowSwitchInlineButton(
                                        mBB.getMessage(userId, "share"),
                                        ""
                                )
                                .build()
                );
    }

    // endregion SendMessage
}
