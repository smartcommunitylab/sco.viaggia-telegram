package viaggia.extended;

import gekoramy.telegram.bot.CommandRegistry;
import gekoramy.telegram.bot.model.Command;
import gekoramy.telegram.bot.model.UseCaseCommand;
import viaggia.utils.MessageBundleBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class CommandRegistryUtils {

    private final CommandRegistry commandRegistry;

    public CommandRegistryUtils(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    public String getHelpMessage(MessageBundleBuilder mBB, int userId) {
        List<Command> commands = new ArrayList<>();
        List<Command> tmp = new ArrayList<>();

        commands.add(new Command("start", "start_description"));
        commands.add(new Command("help", "help_description"));
        commands.add(new Command("language", "language_description"));

        for (UseCaseCommand cmd : commandRegistry.getRegisteredCommands()) {
            if (!commands.contains(cmd.getCommand()))
                tmp.add(cmd.getCommand());
        }

        tmp.sort(Comparator.comparing(Command::getCommandIdentifier));

        commands.addAll(tmp);

        StringBuilder messageBuilder = new StringBuilder();

        for (Command cmd : commands) {
            messageBuilder
                    .append('/')
                    .append(cmd.getCommandIdentifier())
                    .append("\n")
                    .append(mBB.getMessage(userId, cmd.getDescription()))
                    .append("\n\n");
        }

        return messageBuilder.toString().trim();
    }

    public Collection<UseCaseCommand> getRegisteredCommands() {
        return commandRegistry.getRegisteredCommands();
    }
}