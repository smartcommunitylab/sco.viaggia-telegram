package viaggia.command.start;

import bot.CommandRegistry;
import bot.exception.EmptyKeyboardException;
import bot.keyboard.InlineKeyboardMarkupBuilder;
import bot.model.Command;
import bot.model.UseCaseCommand;
import bot.model.handling.HandleCallbackQuery;
import bot.model.handling.HandleInlineQuery;
import bot.model.query.Query;
import bot.timed.Chats;
import bot.timed.TimedAbsSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import viaggia.Users;
import viaggia.command.start.query.StartQuery;
import viaggia.command.start.query.StartQueryBuilder;
import viaggia.command.start.query.StartQueryParser;
import viaggia.extended.CommandRegistryUtils;
import viaggia.extended.DistinguishedUseCaseCommand;
import viaggia.utils.MessageBundleBuilder;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Luca Mosetti in 2017
 * <p>
 * Shows a list of all registered commands
 */
public class StartCommand extends DistinguishedUseCaseCommand implements HandleCallbackQuery, HandleInlineQuery {

    private static final Logger logger = LoggerFactory.getLogger(StartCommand.class);
    private static final Command COMMAND_ID = new Command("start", "start_description");
    private static final String SWITCH_HELPER = "SWITCH_HELPER";

    private final MessageBundleBuilder mBB = new MessageBundleBuilder();
    private final StartQueryBuilder startQueryBuilder = new StartQueryBuilder();
    private final StartQueryParser startQueryParser = new StartQueryParser();
    private final InlineKeyboardMarkupBuilder inlineKeyboardMarkupBuilder = new InlineKeyboardMarkupBuilder();
    private final CommandRegistryUtils commandRegistry;

    public StartCommand(CommandRegistry commandRegistry) {
        super(COMMAND_ID);
        this.commandRegistry = new CommandRegistryUtils(commandRegistry);
    }

    @Override
    public void respondCommand(TimedAbsSender absSender, User user, Chat chat) {
        super.respondCommand(absSender, user, chat);
        mBB.setUser(user);
        startMessage(absSender, chat);
    }

    @Override
    protected void respondText(TimedAbsSender absSender, User user, Chat chat, String text) {
        super.respondText(absSender, user, chat, text);
        mBB.setUser(user);
        try {
            switch (text) {
                case SWITCH_HELPER:
                    absSender.requestExecute(chat.getId(), new SendMessage()
                            .setChatId(chat.getId())
                            .setParseMode(ParseMode.MARKDOWN)
                            .setText(mBB.getMessage("inline_help"))
                            .setReplyMarkup(useCaseCommandsInlineKeyboard()));
                    break;

                default:
                    startMessage(absSender, chat);
                    break;
            }
        } catch (EmptyKeyboardException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void respondCallbackQuery(TimedAbsSender absSender, String callbackQueryId, Query query, User user, Message message) {
        mBB.setUser(user);
        StartQuery q = startQueryParser.parse(query);

        try {
            SendMessage sendMessage = new SendMessage()
                    .setChatId(message.getChatId())
                    .setText(mBB.getMessage("tap_inline"))
                    .setReplyMarkup(useCaseCommandInlineKeyboard(q.getUseCase()));

            AnswerCallbackQuery answer = new AnswerCallbackQuery()
                    .setCallbackQueryId(callbackQueryId);

            absSender.requestExecute(message.getChatId(), sendMessage);
            absSender.requestExecute(message.getChatId(), answer);

        } catch (EmptyKeyboardException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void respondCallbackQuery(TimedAbsSender absSender, String callbackQueryId, Query query, User user, String inlineMessageId) {
        // WON'T HAPPEN
    }

    @Override
    public void respondInlineQuery(TimedAbsSender absSender, User user, String id, String arguments) {
        mBB.setUser(user);
        absSender.requestExecute(null, new AnswerInlineQuery()
                .setInlineQueryId(id)
                .setResults(new ArrayList<>())
                .setSwitchPmParameter(SWITCH_HELPER)
                .setSwitchPmText(switchHelper()));
    }

    private void startMessage(TimedAbsSender absSender, Chat chat) {
        String helpMessageBuilder = mBB.getMessage("introduction") + "\n\n" +
                mBB.getMessage("commands_intro") + "\n\n" +
                mBB.getMessage("people", Long.toString(Users.count())) + "\n\n" +
                mBB.getMessage("credits");

        absSender.requestExecute(chat.getId(), new SendMessage()
                .setChatId(chat.getId())
                .setParseMode(ParseMode.MARKDOWN)
                .setText(helpMessageBuilder)
                .disableWebPagePreview()
                .setReplyMarkup(new ReplyKeyboardRemove()));

        newsMessage(absSender, chat.getId());

        Chats.setCommand(chat.getId(), COMMAND_ID);
    }

    private void newsMessage(TimedAbsSender absSender, long chatId) {
        try {
            absSender.requestExecute(chatId, new SendMessage()
                    .setChatId(chatId)
                    .setParseMode(ParseMode.MARKDOWN)
                    .setText(mBB.getMessage("news"))
                    .setReplyMarkup(inlineKeyboardMarkupBuilder
                            .addFullRowUrlInlineButton(
                                    mBB.getMessage("update"),
                                    "t.me/ViaggiaTrentoChannel"
                            )
                            .addFullRowSwitchInlineButton(
                                    mBB.getMessage("share"),
                                    ""
                            )
                            .build(true)));
        } catch (EmptyKeyboardException e) {
            e.printStackTrace();
        }
    }

    private String switchHelper() {
        StringBuilder helpMessageBuilder = new StringBuilder(mBB.getMessage("type")).append(" ");

        for (UseCaseCommand useCase : commandRegistry.getRegisteredCommands()) {
            if (useCase instanceof HandleInlineQuery && !useCase.getCommand().equals(getCommand()))
                helpMessageBuilder
                        .append(useCase.getCommand().getCommandIdentifier())
                        .append(' ');
        }

        return helpMessageBuilder.toString();
    }

    // region bot.keyboard

    private InlineKeyboardMarkup useCaseCommandInlineKeyboard(String useCase) throws EmptyKeyboardException {
        return inlineKeyboardMarkupBuilder
                .addFullRowSwitchInlineButton(
                        mBB.getMessage("type") + " " + useCase,
                        useCase
                ).build(true);
    }

    private InlineKeyboardMarkup useCaseCommandsInlineKeyboard() throws EmptyKeyboardException {
        List<Map.Entry<String, String>> buttons = new ArrayList<>();

        for (UseCaseCommand useCase : commandRegistry.getRegisteredCommands()) {
            if (useCase instanceof HandleInlineQuery && !useCase.getCommand().equals(getCommand()))
                buttons.add(new AbstractMap.SimpleEntry<>(useCase.getCommand().getCommandIdentifier(), startQueryBuilder
                        .setCommand(getCommand())
                        .setUseCase(useCase.getCommand().getCommandIdentifier())
                        .build(true)));
        }

        return inlineKeyboardMarkupBuilder
                .addSeparateRowsKeyboardButtons(2, buttons)
                .build(true);
    }

    // endregion bot.keyboard

}
