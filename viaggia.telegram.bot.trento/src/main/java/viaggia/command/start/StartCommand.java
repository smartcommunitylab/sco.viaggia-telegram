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
import bot.timed.SendBundleAnswerCallbackQuery;
import bot.timed.TimedAbsSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import viaggia.Users;
import viaggia.command.start.query.StartQuery;
import viaggia.command.start.query.StartQueryBuilder;
import viaggia.command.start.query.StartQueryParser;
import viaggia.extended.CommandRegistryUtils;
import viaggia.utils.MessageBundleBuilder;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Luca Mosetti on 2017
 * <p>
 * Shows a list of all registered commands
 */
public class StartCommand extends UseCaseCommand implements HandleCallbackQuery, HandleInlineQuery {

    private static final Logger logger = LoggerFactory.getLogger(StartCommand.class);
    private static final Command COMMAND_ID = new Command("start", "startdescrition");
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
    public void init() {
    }

    @Override
    public void respondCommand(TimedAbsSender absSender, User user, Chat chat) {
        mBB.setUser(user);
        execute(absSender, chat);
    }

    @Override
    public void respondMessage(TimedAbsSender absSender, User user, Chat chat, String arguments) {
        mBB.setUser(user);
        try {
            switch (arguments) {
                case SWITCH_HELPER:
                    absSender.execute(new SendMessage()
                            .setChatId(chat.getId())
                            .setParseMode(ParseMode.MARKDOWN)
                            .setText(mBB.getMessage("inlinehelp"))
                            .setReplyMarkup(useCaseCommandsInlineKeyboard()));
                    break;

                default:
                    execute(absSender, chat);
                    break;
            }
        } catch (EmptyKeyboardException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void respondCallbackQuery(TimedAbsSender absSender, CallbackQuery cbq, Query q) {
        mBB.setUser(cbq.getFrom());
        StartQuery query = startQueryParser.parse(q);

        try {
            SendMessage sendMessage = new SendMessage()
                    .setChatId(cbq.getMessage().getChatId())
                    .setText(mBB.getMessage("tapinlinebtn"))
                    .setReplyMarkup(useCaseCommandInlineKeyboard(query.getUseCase()));

            AnswerCallbackQuery answer = new AnswerCallbackQuery()
                    .setCallbackQueryId(cbq.getId());

            absSender.execute(new SendBundleAnswerCallbackQuery<>(sendMessage, answer));

        } catch (EmptyKeyboardException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void respondInlineQuery(TimedAbsSender absSender, User user, String id, String arguments) {
        mBB.setUser(user);
        absSender.execute(new AnswerInlineQuery()
                .setInlineQueryId(id)
                .setResults(new ArrayList<>())
                .setSwitchPmParameter(SWITCH_HELPER)
                .setSwitchPmText(switchHelper()));
    }

    private void execute(TimedAbsSender absSender, Chat chat) {
        StringBuilder helpMessageBuilder = new StringBuilder();
        helpMessageBuilder
                .append(mBB.getMessage("introduction"))
                .append("\n\n")
                .append(mBB.getMessage("commandsintro"))
                .append("\n")
                .append(commandRegistry.getHelpMessage(mBB))
                .append(mBB.getMessage("people", Long.toString(Users.count())))
                .append("\n\n")
                .append(mBB.getMessage("credits"));

        try {
            absSender.execute(new SendMessage()
                    .setChatId(chat.getId())
                    .setParseMode(ParseMode.MARKDOWN)
                    .setText(helpMessageBuilder.toString())
                    .disableWebPagePreview()
                    .setReplyMarkup(new ReplyKeyboardRemove()));

            absSender.execute(new SendMessage()
                    .setChatId(chat.getId())
                    .setParseMode(ParseMode.MARKDOWN)
                    .setText(mBB.getMessage("contribute"))
                    .setReplyMarkup(inlineKeyboardMarkupBuilder
                            .addFullRowUrlInlineButton(
                                    mBB.getMessage("donates"),
                                    "paypal.me/LucaMosetti"
                            )
                            .addFullRowUrlInlineButton(
                                    mBB.getMessage("report"),
                                    "trello.com/b/jaVW8Tr5"
                            )
                            .addFullRowUrlInlineButton(
                                    mBB.getMessage("translate"),
                                    "crowdin.com/project/viaggiatrentobot"
                            )
                            .addFullRowSwitchInlineButton(
                                    mBB.getMessage("share"),
                                    ""
                            )
                            .build()));

            Chats.setCommand(chat.getId(), COMMAND_ID);
        } catch (EmptyKeyboardException e) {
            logger.error(e.getMessage());
        }
    }

    private String switchHelper() {
        StringBuilder helpMessageBuilder = new StringBuilder(mBB.getMessage("type"));

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
                        mBB.getMessage("type") + useCase,
                        useCase
                ).build();
    }

    private InlineKeyboardMarkup useCaseCommandsInlineKeyboard() throws EmptyKeyboardException {
        List<Map.Entry<String, String>> buttons = new ArrayList<>();

        for (UseCaseCommand useCase : commandRegistry.getRegisteredCommands()) {
            if (useCase instanceof HandleCallbackQuery && !useCase.getCommand().equals(getCommand()))
                buttons.add(new AbstractMap.SimpleEntry<>(useCase.getCommand().getCommandIdentifier(), startQueryBuilder
                        .setCommand(getCommand())
                        .setUseCase(useCase.getCommand().getCommandIdentifier())
                        .build(true)));
        }

        return inlineKeyboardMarkupBuilder.setColumns(2)
                .addSeparateRowsKeyboardButtons(buttons)
                .build();
    }

    // endregion bot.keyboard

}
