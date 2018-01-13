package viaggia.command.language;

import bot.exception.EmptyKeyboardException;
import bot.keyboard.InlineKeyboardMarkupBuilder;
import bot.model.Command;
import bot.model.handling.HandleCallbackQuery;
import bot.model.query.Query;
import bot.timed.TimedAbsSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import viaggia.utils.MessageBundleBuilder;

import java.util.*;

/**
 * Created by Luca Mosetti in 2017
 */
public class LanguageCommand extends DistinguishedUseCaseCommand implements HandleCallbackQuery {

    private final static Logger logger = LoggerFactory.getLogger(LanguageCommand.class);
    private final static Command COMMAND_ID = new Command("language", "language_description");
    private final static Locale[] languages = {Locale.ITALY, Locale.US, Locale.FRANCE, Locale.GERMANY};

    private final MessageBundleBuilder mBB = new MessageBundleBuilder();
    private final InlineKeyboardMarkupBuilder inlineKeyboardMarkupBuilder = new InlineKeyboardMarkupBuilder();
    private final LanguageQueryBuilder languageQueryBuilder = new LanguageQueryBuilder();
    private final LanguageQueryParser languageQueryParser = new LanguageQueryParser();

    public LanguageCommand() {
        super(COMMAND_ID);
    }

    @Override
    public void respondCommand(TimedAbsSender absSender, User user, Chat chat) {
        super.respondCommand(absSender, user, chat);
        mBB.setUser(user);
        languageMessage(absSender, chat);
    }

    @Override
    public void respondText(TimedAbsSender absSender, User user, Chat chat, String arguments) {
        super.respondText(absSender, user, chat, arguments);
        mBB.setUser(user);
        languageMessage(absSender, chat);
    }

    @Override
    public void respondCallbackQuery(TimedAbsSender absSender, String callbackQueryId, Query query, User user, Message message) {
        mBB.setUser(user);
        LanguageQuery q = languageQueryParser.parse(query);
        Users.setLocale(user.getId(), Locale.forLanguageTag(q.getLanguage()));

        try {
            EditMessageText editMessageText = new EditMessageText()
                    .setChatId(message.getChatId())
                    .setMessageId(message.getMessageId())
                    .setParseMode(ParseMode.MARKDOWN)
                    .setText(mBB.getMessage("languages"))
                    .setReplyMarkup(languagesInlineKeyboard());

            AnswerCallbackQuery answer = new AnswerCallbackQuery()
                    .setCallbackQueryId(callbackQueryId)
                    .setText(Locale.forLanguageTag(q.getLanguage()).getDisplayLanguage());

            if (!equalsFormattedTexts(editMessageText.getText(), message.getText(), ParseMode.MARKDOWN)) {
                absSender.requestExecute(message.getChatId(), editMessageText);
            }

            absSender.requestExecute(message.getChatId(), answer);

        } catch (EmptyKeyboardException e) {
            logger.error(e.getMessage());
        }    }

    @Override
    public void respondCallbackQuery(TimedAbsSender absSender, String callbackQueryId, Query query, User user, String inlineMessageId) {
        // WON'T HAPPEN
    }

    private void languageMessage(TimedAbsSender absSender, Chat chat) {
        try {
            absSender.requestExecute(chat.getId(), new SendMessage()
                    .setChatId(chat.getId())
                    .setParseMode(ParseMode.MARKDOWN)
                    .setText(mBB.getMessage("languages"))
                    .setReplyMarkup(languagesInlineKeyboard()));
        } catch (EmptyKeyboardException e) {
            logger.error(e.getMessage());
        }
    }

    private InlineKeyboardMarkup languagesInlineKeyboard() throws EmptyKeyboardException {
        List<Map.Entry<String, String>> buttons = new ArrayList<>();

        for (Locale locale : languages) {
            buttons.add(new AbstractMap.SimpleEntry<>(locale.getDisplayLanguage(), languageQueryBuilder
                    .setCommand(getCommand())
                    .setLanguage(locale.toLanguageTag())
                    .build(true)));
        }

        return inlineKeyboardMarkupBuilder
                .addSeparateRowsKeyboardButtons(2, buttons)
                .build(true);
    }
}