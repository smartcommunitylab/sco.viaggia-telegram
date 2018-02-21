package viaggia.command.route.general.utils;

import gekoramy.telegram.bot.model.Command;
import mobilityservice.model.ComparableId;
import viaggia.command.route.general.query.RouteQueryBuilder;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class InlineKeyboardRowNavRouteBuilder {

    private final RouteQueryBuilder routeQueryBuilder = new RouteQueryBuilder();

    /**
     * @param length     possibilities
     * @param chosen     chosen possibility
     * @param columns    number of columns
     * @param COMMAND_ID command id
     * @param id         route id
     * @return useful navigation bar
     */
    public List<Map.Entry<String, String>> build(int length, int chosen, int columns, Command COMMAND_ID, ComparableId id, String stopId) {
        if (chosen < 0 && chosen > length - 1)
            throw new IndexOutOfBoundsException("length : " + length + "; chosen : " + chosen);

        List<Map.Entry<String, String>> buttons = new ArrayList<>();

        final int last = length - 1;
        final int xFirst = columns - 2;
        final int xLast = last - xFirst;
        final int between = xFirst - 1;

        if (length <= columns) {
            // 0 1 2
            for (int i = 0; i <= last; i++) {
                buttons.add(entry(i, query(COMMAND_ID, id, i, stopId)));
            }

        } else if (chosen >= xFirst && chosen <= xLast) {
            // « 0 d e f L »
            buttons.add(entry("« " + 0, query(COMMAND_ID, id, 0, stopId)));
            for (int i = chosen - between / 2; i <= chosen + between / 2; i++) {
                buttons.add(entry(i, query(COMMAND_ID, id, i, stopId)));
            }
            buttons.add(entry(last + " »", query(COMMAND_ID, id, last, stopId)));
        } else if (chosen < xFirst) {
            // 0 1 2 xFirst L »
            for (int i = 0; i <= xFirst; i++) {
                buttons.add(entry(i, query(COMMAND_ID, id, i, stopId)));
            }
            buttons.add(entry(last + " »", query(COMMAND_ID, id, last, stopId)));
        } else if (chosen > xLast) {
            // « 0 xLast H I L
            buttons.add(entry("« " + 0, query(COMMAND_ID, id, 0, stopId)));
            for (int i = xLast; i <= last; i++) {
                buttons.add(entry(i, query(COMMAND_ID, id, i, stopId)));
            }
        }

        for (Map.Entry<String, String> entry : buttons) {
            if (entry.getKey().equals(Integer.toString(chosen))) {
                buttons.set(buttons.indexOf(entry), new AbstractMap.SimpleEntry<>("· " + entry.getKey() + " ·", entry.getValue()));
                break;
            }
        }

        return buttons;
    }

    /**
     * It makes more readable 'build(...)'
     *
     * @param index label / button name, what the user see
     * @param query query
     * @return entry
     */
    private AbstractMap.SimpleEntry<String, String> entry(int index, String query) {
        return new AbstractMap.SimpleEntry<>(Integer.toString(index), query);
    }

    /**
     * It makes more readable 'build(...)'
     *
     * @param name  label / button name, what the user see
     * @param query query
     * @return entry
     */
    private AbstractMap.SimpleEntry<String, String> entry(String name, String query) {
        return new AbstractMap.SimpleEntry<>(name, query);
    }

    /**
     * It makes more readable 'build(...)'
     *
     * @param id    routeId
     * @param index index of the route timeTable list
     * @return query
     */
    private String query(Command COMMAND_ID, ComparableId id, int index, String stopId) {
        return routeQueryBuilder
                .setCommand(COMMAND_ID)
                .setId(id)
                .setValue(Integer.toString(index))
                .setStopId(stopId)
                .build();
    }

}
