package viaggia.command.start.query;

import bot.model.Command;
import bot.model.query.QueryBuilder;

/**
 * Created by Luca Mosetti on 2017
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
