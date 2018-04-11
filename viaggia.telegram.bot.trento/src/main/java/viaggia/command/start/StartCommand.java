package viaggia.command.start;

import gekoramy.telegram.bot.CommandRegistry;
import gekoramy.telegram.bot.keyboard.InlineKeyboardMarkupBuilder;
import gekoramy.telegram.bot.model.Command;
import gekoramy.telegram.bot.model.UseCaseCommand;
import gekoramy.telegram.bot.model.query.Query;
import gekoramy.telegram.bot.responder.CallbackQueryResponder;
import gekoramy.telegram.bot.responder.InlineQueryResponder;
import gekoramy.telegram.bot.responder.MessageResponder;
import org.telegram.telegrambots.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Location;
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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Presentation message
 * Also inline helper
 *
 * @author Luca Mosetti
 * @since 2017
 */
public class StartCommand extends DistinguishedUseCaseCommand {

    private static final Command COMMAND_ID = new Command("start", "start_description");
    private static final String SWITCH_HELPER = "SWITCH_HELPER";

    private final CommandRegistryUtils commandRegistry;

    public StartCommand(CommandRegistry commandRegistry) {
        super(COMMAND_ID);
        this.commandRegistry = new CommandRegistryUtils(commandRegistry);
    }

    @Override
    protected void respondCommand(MessageResponder absSender, Chat chat, User user) {
        super.respondCommand(absSender, chat, user);
        try {
            startMessage(absSender, user.getId());
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    protected void respondText(MessageResponder absSender, Chat chat, User user, String text) {
        super.respondText(absSender, chat, user, text);
        try {
            switch (text) {
                case SWITCH_HELPER:
                    absSender.send(new SendMessage()
                            .setParseMode(ParseMode.MARKDOWN)
                            .setText(mBB.getMessage(user.getId(), "inline_help"))
                            .setReplyMarkup(useCaseCommandsInlineKeyboard()));
                    break;

                default:
                    startMessage(absSender, user.getId());
                    break;
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void respondCallbackQuery(CallbackQueryResponder absSender, Query query, User user, Message message) {
        try {
            StartQuery q = StartQueryParser.parse(query);

            absSender.send(new SendMessage()
                    .setText(mBB.getMessage(user.getId(), "tap_inline"))
                    .setReplyMarkup(useCaseCommandInlineKeyboard(q.getUseCase(), user.getId())));

        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void respondInlineQuery(InlineQueryResponder absSender, User user, String arguments, Location location) {
        absSender.answer(new AnswerInlineQuery()
                .setResults(new ArrayList<>())
                .setSwitchPmParameter(SWITCH_HELPER)
                .setSwitchPmText(switchHelper(user.getId())));
    }

    private void startMessage(MessageResponder absSender, int userId) {
        String helpMessageBuilder = mBB.getMessage(userId, "introduction") + "\n\n" +
                mBB.getMessage(userId, "commands_intro") + "\n\n" +
                mBB.getMessage(userId, "people", Long.toString(Users.count())) + "\n\n" +
                mBB.getMessage(userId, "credits");


        absSender.send(new SendMessage()
                .setParseMode(ParseMode.MARKDOWN)
                .setText(helpMessageBuilder)
                .disableWebPagePreview()
                .setReplyMarkup(new ReplyKeyboardRemove()))
                .send(newsMessage(userId))
                .toComplete();
    }

    private SendMessage newsMessage(int userId) {
        return new SendMessage()
                .setParseMode(ParseMode.MARKDOWN)
                .setText(mBB.getMessage(userId, "news"))
                .setReplyMarkup(new InlineKeyboardMarkupBuilder()
                        .addFullRowUrlInlineButton(
                                mBB.getMessage(userId, "update"),
                                "t.me/ViaggiaTrentoChannel"
                        )
                        .addFullRowSwitchInlineButton(
                                mBB.getMessage(userId, "share"),
                                ""
                        )
                        .build());
    }

    private String switchHelper(int userId) {
        StringBuilder helpMessageBuilder = new StringBuilder(mBB.getMessage(userId, "type")).append(" ");

        for (UseCaseCommand useCase : commandRegistry.getRegisteredCommands()) {
            if (useCase.isHandlingInline() && !useCase.getCommand().equals(getCommand()))
                helpMessageBuilder
                        .append(useCase.getCommand().getCommandIdentifier())
                        .append(' ');
        }

        return helpMessageBuilder.toString();
    }

    // region bot.keyboard

    private InlineKeyboardMarkup useCaseCommandInlineKeyboard(String useCase, int userId) {
        return new InlineKeyboardMarkupBuilder()
                .addFullRowSwitchInlineButton(
                        mBB.getMessage(userId, "type") + " " + useCase,
                        useCase
                ).build();
    }

    private InlineKeyboardMarkup useCaseCommandsInlineKeyboard() {
        List<Map.Entry<String, String>> buttons = new ArrayList<>();

        for (UseCaseCommand useCase : commandRegistry.getRegisteredCommands()) {
            if (useCase.isHandlingInline() && !useCase.getCommand().equals(getCommand()))
                buttons.add(new AbstractMap.SimpleEntry<>(useCase.getCommand().getCommandIdentifier(), new StartQueryBuilder()
                        .setCommand(getCommand())
                        .setUseCase(useCase.getCommand().getCommandIdentifier())
                        .build()));
        }

        return new InlineKeyboardMarkupBuilder()
                .addSeparateRowsKeyboardButtons(2, buttons)
                .build();
    }

    // endregion bot.keyboard

}
