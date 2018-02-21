package viaggia.command.start.query;

import gekoramy.telegram.bot.model.query.Query;

import java.util.Map;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class StartQuery extends Query implements StartRegex {

    StartQuery(Map<String, String> map) {
        super(map);
    }

    public String getUseCase() {
        return super.get(USECASE);
    }
}
