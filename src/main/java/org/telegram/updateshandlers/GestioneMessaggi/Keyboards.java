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

    // region INLINE KEYBOARD
    public static InlineKeyboardMarkup getInlineKeyboard(int from, String text) {
        InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyboard = new ArrayList<>();

        List<InlineKeyboardButton> keyboardRow1 = new ArrayList<>();
        for (int i = from; i < from + 5; i++) {
            keyboardRow1.add(new InlineKeyboardButton().setText(Integer.toString(i)).setCallbackData(Integer.toString(i)));
        }

        List<InlineKeyboardButton> keyboardRow2 = new ArrayList<>();
        keyboardRow2.add(new InlineKeyboardButton().setText(text).setCallbackData(text));

        inlineKeyboard.add(keyboardRow1);
        inlineKeyboard.add(keyboardRow2);
        replyKeyboardMarkup.setKeyboard(inlineKeyboard);
        return replyKeyboardMarkup;
    }
    // endregion INLINE KEYBOARD
}
