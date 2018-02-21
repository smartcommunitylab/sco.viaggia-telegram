package viaggia.command.language;

import gekoramy.telegram.bot.keyboard.InlineKeyboardMarkupBuilder;
import gekoramy.telegram.bot.model.Command;
import gekoramy.telegram.bot.model.query.Query;
import gekoramy.telegram.bot.responder.CallbackQueryResponder;
import gekoramy.telegram.bot.responder.MessageResponder;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import viaggia.Users;
import viaggia.command.language.query.LanguageQuery;
import viaggia.command.language.query.LanguageQueryBuilder;
import viaggia.command.language.query.LanguageQueryParser;
import viaggia.extended.DistinguishedUseCaseCommand;

import java.util.*;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class LanguageCommand extends DistinguishedUseCaseCommand {
    private final static Command COMMAND_ID = new Command("language", "language_description");
    private final static Locale[] languages = {Locale.ITALY, Locale.US, Locale.FRANCE};

    public LanguageCommand() {
        super(COMMAND_ID);
    }

    @Override
    public void respondCommand(MessageResponder absSender, Chat chat, User user) {
        super.respondCommand(absSender, chat, user);
        try {
            absSender.send(languageMessage(user.getId()));
        } catch (Throwable e) {
            logger.error(getClass().toString(), e);
        }
    }

    @Override
    public void respondText(MessageResponder absSender, Chat chat, User user, String arguments) {
        super.respondCommand(absSender, chat, user, arguments);
        try {
            absSender.send(languageMessage(user.getId()));
        } catch (Throwable e) {
            logger.error(getClass().toString(), e);
        }
    }

    @Override
    public void respondCallbackQuery(CallbackQueryResponder absSender, Query query, User user, Message message) {
        try {
            LanguageQuery q = new LanguageQueryParser().parse(query);
            Users.setLocale(user.getId(), Locale.forLanguageTag(q.getLanguage()));

            absSender.send(new EditMessageText()
                    .setParseMode(ParseMode.MARKDOWN)
                    .setText(mBB.getMessage(user.getId(), "languages"))
                    .setReplyMarkup(languagesInlineKeyboard()));

            absSender.answer(new AnswerCallbackQuery()
                    .setText(Locale.forLanguageTag(q.getLanguage()).getDisplayLanguage()));
        } catch (Throwable e) {
            logger.error(getClass().toString(), e);
        }
    }

    private SendMessage languageMessage(int userId) {
        return new SendMessage()
                .setParseMode(ParseMode.MARKDOWN)
                .setText(mBB.getMessage(userId, "languages"))
                .setReplyMarkup(languagesInlineKeyboard());
    }

    private InlineKeyboardMarkup languagesInlineKeyboard() {
        List<Map.Entry<String, String>> buttons = new ArrayList<>();

        for (Locale locale : languages) {
            buttons.add(new AbstractMap.SimpleEntry<>(locale.getDisplayLanguage(), new LanguageQueryBuilder()
                    .setCommand(getCommand())
                    .setLanguage(locale.toLanguageTag())
                    .build()));
        }

        return new InlineKeyboardMarkupBuilder()
                .addSeparateRowsKeyboardButtons(3, buttons)
                .build();
    }
}
