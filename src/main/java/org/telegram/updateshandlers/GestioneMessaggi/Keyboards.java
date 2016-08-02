package org.telegram.updateshandlers.GestioneMessaggi;

import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;
import org.apache.commons.lang.math.NumberUtils;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.telegram.updateshandlers.GestioneMessaggi.Commands.*;

public class Keyboards {

    // region utilities

    private static ReplyKeyboardMarkup keyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboad(false);

        return replyKeyboardMarkup;
    }

    private static KeyboardRow keyboardRowButton(String value) {
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(value);

        return keyboardRow;
    }

    private static KeyboardRow keyboardRowLocation() {
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(new KeyboardButton("LOCATION").setRequestLocation(true));

        return keyboardRow;
    }

    private static ReplyKeyboardMarkup keyboardZone(long chatId, List<Parking> zone, Menu menu) {
        ReplyKeyboardMarkup replyKeyboardMarkup = keyboard();
        List<KeyboardRow> keyboard = new ArrayList<>();

        for (Parking p : zone)
            keyboard.add(keyboardRowButton(p.getName()));

        keyboard.add(keyboardRowLocation());
        keyboard.add(keyboardRowButton(BACKCOMMAND));
        replyKeyboardMarkup.setKeyboard(keyboard);

        Current.setMenu(chatId, menu);
        return replyKeyboardMarkup;
    }

    // endregion utilities

    public static ReplyKeyboardMarkup keyboardStart(long chatId) {
        ReplyKeyboardMarkup replyKeyboardMarkup = keyboard();
        List<KeyboardRow> keyboard = new ArrayList<>();

        keyboard.add(keyboardRowButton(TAXICOMMAND));
        keyboard.add(keyboardRowButton(AUTOBUSCOMMAND));
        keyboard.add(keyboardRowButton(TRAINSCOMMAND));
        keyboard.add(keyboardRowButton(PARKINGSCOMMAND));
        keyboard.add(keyboardRowButton(BIKESHARINGSCOMMAND));

        replyKeyboardMarkup.setKeyboard(keyboard);

        Current.setMenu(chatId, Menu.START);
        return replyKeyboardMarkup;
    }

    public static ReplyKeyboardMarkup keyboardLanguage(long chatId) {
        ReplyKeyboardMarkup replyKeyboardMarkup = keyboard();
        List<KeyboardRow> keyboard = new ArrayList<>();

        keyboard.add(keyboardRowButton(ITALIANO));
        keyboard.add(keyboardRowButton(ENGLISH));
        keyboard.add(keyboardRowButton(ESPAÑOL));

        replyKeyboardMarkup.setKeyboard(keyboard);

        keyboard.add(keyboardRowButton(BACKCOMMAND));
        Current.setMenu(chatId, Menu.LANGUAGE);
        return replyKeyboardMarkup;
    }

    public static ReplyKeyboardMarkup keyboardParkings(long chatId, List<Parking> parkings) {
        return keyboardZone(chatId, parkings, Menu.PARKINGS);
    }

    public static ReplyKeyboardMarkup keyboardBikeSharings(long chatId, List<Parking> bikeSharings) {
        return keyboardZone(chatId, bikeSharings, Menu.BIKESHARINGS);
    }

    public static ReplyKeyboardMarkup keyboardAutobus(long chatId, List<Route> autobus) {
        ReplyKeyboardMarkup replyKeyboardMarkup = keyboard();
        List<KeyboardRow> keyboard = new ArrayList<>();
        List<String> autobusWithoutRepeats = new ArrayList<>();
        List<String> autobusNum = new ArrayList<>();
        List<String> autobusTxt = new ArrayList<>();

        for (Route route : autobus)
            if (!autobusWithoutRepeats.contains(route.getRouteShortName()))
                autobusWithoutRepeats.add(route.getRouteShortName());

        for (String string : autobusWithoutRepeats)
            if (NumberUtils.isNumber(string)) autobusNum.add(string);
            else autobusTxt.add(string);

        autobusNum.sort(Comparator.comparing(Integer::parseInt));

        autobusWithoutRepeats.clear();
        autobusWithoutRepeats.addAll(autobusNum);
        autobusWithoutRepeats.addAll(autobusTxt);

        keyboard.add(new KeyboardRow());
        int elementsInARow = 7;
        int i = 0;
        for (String string : autobusWithoutRepeats) {
            if (keyboard.get(i).size() == elementsInARow) {
                i++;
                keyboard.add(new KeyboardRow());
            }
            keyboard.get(i).add(string);
        }


        keyboard.add(keyboardRowButton(BACKCOMMAND));
        replyKeyboardMarkup.setKeyboard(keyboard);

        Current.setMenu(chatId, Menu.AUTOBUS);
        return replyKeyboardMarkup;
    }

    public static ReplyKeyboardMarkup keyboardTrains(long chatId, List<Route> trains) {
        ReplyKeyboardMarkup replyKeyboardMarkup = keyboard();
        List<KeyboardRow> keyboard = new ArrayList<>();

        // EQUALS for (Route r : trains) keyboard.add(keyboardRowButton(r.getRouteLongName()));
        keyboard.addAll(trains.stream().map(r -> keyboardRowButton(r.getRouteLongName())).collect(Collectors.toList()));

        keyboard.add(keyboardRowButton(BACKCOMMAND));
        replyKeyboardMarkup.setKeyboard(keyboard);

        Current.setMenu(chatId, Menu.TRAINS);
        return replyKeyboardMarkup;
    }

    public static InlineKeyboardMarkup inlineKeyboard(int choosed, int lastValue, String text) {
        // BTNs must be an odd >= 5
        final int BTNs = 5;
        final int BTN_LAST = BTNs - 1;
        final int BTN_PENULTIMATE = BTN_LAST - 1;
        int MAGIC = 1;

        // useful if BTNs change value
        for (int i = 5; i < BTNs; i += 2)
            MAGIC++;

        InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyboard = new ArrayList<>();

        List<InlineKeyboardButton> keyboardRow1 = new ArrayList<>();

        for (int i = 0; i < BTNs; i++)
            keyboardRow1.add(new InlineKeyboardButton());

        if (choosed <= BTN_PENULTIMATE - 1) {
            // return 0 | 1 | 2 | 3 ›| 6 »
            for (int i = 1; i < BTN_LAST; i++)
                keyboardRow1.get(i).setText(Integer.toString(i)).setCallbackData(Integer.toString(i));

            keyboardRow1.get(0).setText(Integer.toString(0)).setCallbackData(Integer.toString(0));
            keyboardRow1.get(BTN_PENULTIMATE).setText(keyboardRow1.get(BTN_PENULTIMATE).getText() + " ›");
            keyboardRow1.get(BTN_LAST).setText(Integer.toString(lastValue) + " »").setCallbackData(Integer.toString(lastValue));
        } else if (choosed >= lastValue - (2 * MAGIC)) {
            // return « 0 |‹ 3 | 4 | 5 | 6
            for (int i = 1, value = lastValue - (2 * MAGIC) - 1; i < BTN_LAST; i++, value++)
                keyboardRow1.get(i).setText(Integer.toString(value)).setCallbackData(Integer.toString(value));

            keyboardRow1.get(0).setText("« " + Integer.toString(0)).setCallbackData(Integer.toString(0));
            keyboardRow1.get(1).setText("‹ " + keyboardRow1.get(1).getText());
            keyboardRow1.get(BTN_LAST).setText(Integer.toString(lastValue)).setCallbackData(Integer.toString(lastValue));
        } else {
            // return « 0 |‹ 2 |· 3 ·| 4 ›| 6 »
            for (int i = 1, value = choosed - MAGIC; i < BTN_LAST; i++, value++)
                keyboardRow1.get(i).setText(Integer.toString(value)).setCallbackData(Integer.toString(value));

            keyboardRow1.get(0).setText("« " + Integer.toString(0)).setCallbackData(Integer.toString(0));
            keyboardRow1.get(1).setText("‹ " + keyboardRow1.get(1).getText());
            keyboardRow1.get(BTN_PENULTIMATE).setText(keyboardRow1.get(BTN_PENULTIMATE).getText() + " ›");
            keyboardRow1.get(BTN_LAST).setText(Integer.toString(lastValue) + " »").setCallbackData(Integer.toString(lastValue));
        }

        for (InlineKeyboardButton btn : keyboardRow1)
            if (btn.getText().equals(Integer.toString(choosed)))
                btn.setText("· " + btn.getText() + " ·");


        inlineKeyboard.add(keyboardRow1);
        if (text != null) {
            List<InlineKeyboardButton> keyboardRow2 = new ArrayList<>();
            keyboardRow2.add(new InlineKeyboardButton().setText(text).setCallbackData(text));
            inlineKeyboard.add(keyboardRow2);
        }
        replyKeyboardMarkup.setKeyboard(inlineKeyboard);
        return replyKeyboardMarkup;
    }
}
