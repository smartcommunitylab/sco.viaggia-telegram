package viaggia.extended;

import bot.CommandRegistry;
import bot.model.Command;
import bot.model.UseCaseCommand;
import viaggia.utils.MessageBundleBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Luca Mosetti
 */
public class CommandRegistryUtils {

    private final CommandRegistry commandRegistry;
    private final List<Command> commands = new ArrayList<>();
    private final List<Command> tmp = new ArrayList<>();

    public CommandRegistryUtils(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;

        List<Command> tmp = new ArrayList<>();
    }

    public String getHelpMessage(MessageBundleBuilder mBB) {

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
                    .append(mBB.getMessage(cmd.getDescription()))
                    .append("\n\n");
        }

        return messageBuilder.toString().trim();
    }

    public Collection<UseCaseCommand> getRegisteredCommands() {
        return commandRegistry.getRegisteredCommands();
    }
}