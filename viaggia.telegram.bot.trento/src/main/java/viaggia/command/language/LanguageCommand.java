package viaggia.command.language;

import bot.exception.EmptyKeyboardException;
import bot.keyboard.InlineKeyboardMarkupBuilder;
import bot.model.Command;
import bot.model.UseCaseCommand;
import bot.model.handling.HandleCallbackQuery;
import bot.model.query.Query;
import bot.timed.SendBundleAnswerCallbackQuery;
import bot.timed.TimedAbsSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import viaggia.Users;
import viaggia.command.language.query.LanguageQuery;
import viaggia.command.language.query.LanguageQueryBuilder;
import viaggia.command.language.query.LanguageQueryParser;
import viaggia.utils.MessageBundleBuilder;

import java.util.*;

/**
 * Created by Luca Mosetti on 2017
 */
public class LanguageCommand extends UseCaseCommand implements HandleCallbackQuery {

    private final static Logger logger = LoggerFactory.getLogger(LanguageCommand.class);
    private final static Command COMMAND_ID = new Command("language", "languagedescription");
    private final static Locale[] languages = {Locale.ITALY, Locale.US};

    private final MessageBundleBuilder mBB = new MessageBundleBuilder();
    private final InlineKeyboardMarkupBuilder inlineKeyboardMarkupBuilder = new InlineKeyboardMarkupBuilder();
    private final LanguageQueryBuilder languageQueryBuilder = new LanguageQueryBuilder();
    private final LanguageQueryParser languageQueryParser = new LanguageQueryParser();

    public LanguageCommand() {
        super(COMMAND_ID);
    }

    @Override
    public void respondCommand(TimedAbsSender absSender, User user, Chat chat) {
        mBB.setUser(user);
        execute(absSender, chat);
    }

    @Override
    public void respondMessage(TimedAbsSender absSender, User user, Chat chat, String arguments) {
        mBB.setUser(user);
        execute(absSender, chat);
    }

    @Override
    public void respondCallbackQuery(TimedAbsSender absSender, CallbackQuery cbq, Query query) {
        mBB.setUser(cbq.getFrom());
        LanguageQuery q = languageQueryParser.parse(query);
        Users.setLocale(cbq.getFrom().getId(), Locale.forLanguageTag(q.getLanguage()));

        try {
            EditMessageText editMessageText = new EditMessageText()
                    .setChatId(cbq.getMessage().getChatId())
                    .setMessageId(cbq.getMessage().getMessageId())
                    .setParseMode(ParseMode.MARKDOWN)
                    .setText(mBB.getMessage("languages"))
                    .setReplyMarkup(languagesInlineKeyboard());

            AnswerCallbackQuery answer = new AnswerCallbackQuery()
                    .setCallbackQueryId(cbq.getId())
                    .setText(Locale.forLanguageTag(q.getLanguage()).getDisplayLanguage());

            if (cbq.getMessage() == null || !equalsFormattedTexts(editMessageText.getText(), cbq.getMessage().getText(), ParseMode.MARKDOWN))
                absSender.execute(new SendBundleAnswerCallbackQuery<>(editMessageText, answer));
            else
                absSender.execute(answer);

        } catch (EmptyKeyboardException e) {
            logger.error(e.getMessage());
        }
    }

    private void execute(TimedAbsSender absSender, Chat chat) {
        try {
            absSender.execute(new SendMessage()
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

        return inlineKeyboardMarkupBuilder.setColumns(2)
                .addSeparateRowsKeyboardButtons(buttons)
                .build();
    }
}
