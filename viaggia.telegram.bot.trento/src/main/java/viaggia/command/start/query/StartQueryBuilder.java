package viaggia.command.start.query;

import gekoramy.telegram.bot.model.Command;
import gekoramy.telegram.bot.model.query.QueryBuilder;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class StartQueryBuilder extends QueryBuilder implements StartRegex {

    public StartQueryBuilder setCommand(Command command) {
        put(COMMAND, command.getCommandIdentifier());
        return this;
    }

    public StartQueryBuilder setUseCase(String useCase) {
        put(USECASE, useCase);
        return this;
    }
}
