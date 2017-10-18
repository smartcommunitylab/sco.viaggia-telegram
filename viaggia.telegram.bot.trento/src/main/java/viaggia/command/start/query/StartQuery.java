package viaggia.command.start.query;

import bot.model.query.Query;

import java.util.Map;

/**
 * Created by Luca Mosetti on 2017
 */
public class StartQuery extends Query implements StartRegex {

    /*package*/ StartQuery(Map map) {
        super(map);
    }

    public String getUseCase() {
        return super.get(USECASE);
    }
}