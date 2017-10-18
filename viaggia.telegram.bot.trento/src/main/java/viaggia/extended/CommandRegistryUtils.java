package viaggia.extended;

import bot.CommandRegistry;
import bot.model.UseCaseCommand;
import viaggia.utils.MessageBundleBuilder;

import java.util.Collection;

public class CommandRegistryUtils {

    private final CommandRegistry commandRegistry;

    public CommandRegistryUtils(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    public String getHelpMessage(MessageBundleBuilder mBB) {
        StringBuilder messageBuilder = new StringBuilder();

        for (UseCaseCommand command : commandRegistry.getRegisteredCommands()) {
            messageBuilder
                    .append('/')
                    .append(command.getCommand().getCommandIdentifier())
                    .append("\n")
                    .append(mBB.getMessage(command.getCommand().getDescription()))
                    .append("\n\n");
        }

        return messageBuilder.toString();
    }

    public Collection<UseCaseCommand> getRegisteredCommands() {
        return commandRegistry.getRegisteredCommands();
    }
}