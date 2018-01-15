package viaggia.command.contribute;

import bot.exception.EmptyKeyboardException;
import bot.keyboard.InlineKeyboardMarkupBuilder;
import bot.model.Command;
import bot.timed.TimedAbsSender;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import viaggia.extended.DistinguishedUseCaseCommand;
import viaggia.utils.MessageBundleBuilder;

/**
 * Created by Luca Mosetti in 2017
 * <p>
 * Info for people who wants to contribute to @ViaggiaTrentoBot
 */
public class ContributeCommand extends DistinguishedUseCaseCommand {

    private static final Command COMMAND_ID = new Command("contribute", "contribute_description");

    private final MessageBundleBuilder mBB = new MessageBundleBuilder();
    private final InlineKeyboardMarkupBuilder inlineKeyboardMarkupBuilder = new InlineKeyboardMarkupBuilder();

    public ContributeCommand() {
        super(COMMAND_ID);
    }

    @Override
    public void respondCommand(TimedAbsSender absSender, User user, Chat chat) {
        super.respondCommand(absSender, user, chat);
        mBB.setUser(user);
        contributeCommand(absSender, chat);
    }

    @Override
    public void respondText(TimedAbsSender absSender, User user, Chat chat, String arguments) {
        super.respondText(absSender, user, chat, arguments);
        mBB.setUser(user);
        contributeCommand(absSender, chat);
    }

    private void contributeCommand(TimedAbsSender absSender, Chat chat) {
        try {
            absSender.requestExecute(chat.getId(), new SendMessage()
                    .setText(mBB.getMessage("contribute"))
                    .setChatId(chat.getId())
                    .setParseMode(ParseMode.MARKDOWN)
                    .setReplyMarkup(
                            inlineKeyboardMarkupBuilder
                                    .addFullRowUrlInlineButton(
                                            mBB.getMessage("donates"),
                                            "paypal.me/LucaMosetti"
                                    )
                                    .addFullRowUrlInlineButton(
                                            mBB.getMessage("report"),
                                            "t.me/VTNSupportBot"
                                    )
                                    .addFullRowUrlInlineButton(
                                            mBB.getMessage("translate"),
                                            "crowdin.com/project/viaggiatrentobot"
                                    )
                                    .addFullRowSwitchInlineButton(
                                            mBB.getMessage("share"),
                                            ""
                                    )
                                    .build(true)
                    ));
        } catch (EmptyKeyboardException e) {
            e.printStackTrace();
        }
    }
}
