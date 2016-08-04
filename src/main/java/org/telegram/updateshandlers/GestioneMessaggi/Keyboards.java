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

import static org.telegram.updateshandlers.GestioneMessaggi.Commands.*;

/**
 * Created by gekoramy
 */
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

    private static InlineKeyboardButton first(InlineKeyboardButton btn) {
        return btn.setText("« " + btn.getText());
    }

    private static InlineKeyboardButton second(InlineKeyboardButton btn) {
        return btn.setText("‹ " + btn.getText());
    }

    private static InlineKeyboardButton penultimate(InlineKeyboardButton btn) {
        return btn.setText(btn.getText() + " ›");
    }

    private static InlineKeyboardButton last(InlineKeyboardButton btn) {
        return btn.setText(btn.getText() + " »");
    }

    // endregion utilities

    // region keyboard

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
        keyboard.add(keyboardRowButton(ESPANOL));

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

        for (Route r : trains)
            keyboard.add(keyboardRowButton(r.getRouteLongName()));

        keyboard.add(keyboardRowButton(BACKCOMMAND));
        replyKeyboardMarkup.setKeyboard(keyboard);

        Current.setMenu(chatId, Menu.TRAINS);
        return replyKeyboardMarkup;
    }

    // endregion keyboard

    // region inlineKeyboard

    private static InlineKeyboardMarkup inlineKeyboard(String id, int chosen, int lastValue, boolean isAutobus) {
        InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyboard = new ArrayList<>();

        List<InlineKeyboardButton> indexes = new ArrayList<>();
        // region indexes

        // BTNs must be an odd >= 5
        final int BTNs = 7;
        final int BTN_LAST = BTNs - 1;
        final int BTN_PENULTIMATE = BTN_LAST - 1;
        int MAGIC = 1;

        // useful if BTNs change value
        for (int i = 5; i < BTNs; i += 2)
            MAGIC++;

        for (int i = 0; i < BTNs; i++)
            indexes.add(new InlineKeyboardButton());

        if (chosen <= BTN_PENULTIMATE - 1)
            for (int i = 1, value = 1; i < BTN_LAST; i++, value++)
                indexes.get(i).setText(Integer.toString(value));
        else if (chosen >= lastValue - (2 * MAGIC))
            for (int i = 1, value = lastValue - (2 * MAGIC) - 1; i < BTN_LAST; i++, value++)
                indexes.get(i).setText(Integer.toString(value));
        else
            for (int i = 1, value = chosen - MAGIC; i < BTN_LAST; i++, value++)
                indexes.get(i).setText(Integer.toString(value));

        indexes.get(0).setText(Integer.toString(0));
        indexes.get(BTN_LAST).setText(Integer.toString(lastValue));

        for (InlineKeyboardButton btn : indexes)
            btn.setCallbackData(id + '~' + INDEX + '~' + btn.getText());

        if (chosen > BTN_PENULTIMATE - 1) {
            indexes.set(0, first(indexes.get(0)));
            indexes.set(1, second(indexes.get(1)));
        }

        if (chosen < lastValue - (2 * MAGIC)) {
            indexes.set(BTN_PENULTIMATE, penultimate(indexes.get(BTN_PENULTIMATE)));
            indexes.set(BTN_LAST, last(indexes.get(BTN_LAST)));
        }

        for (InlineKeyboardButton btn : indexes)
            if (btn.getText().equals(Integer.toString(chosen)))
                btn.setText("· " + btn.getText() + " ·").setCallbackData(id + '~' + CURRENT + '~' + Integer.toString(-1));

        // endregion indexes
        inlineKeyboard.add(indexes);

        Character character = id.charAt(id.length() - 1);

        if (isAutobus) {
            List<InlineKeyboardButton> andataReturn = new ArrayList<>();
            // region andataReturn
            switch (character) {
                case 'A':
                    andataReturn.add(new InlineKeyboardButton().setText(RETURN).setCallbackData(id + '~' + RETURN + '~' + chosen));
                    break;

                case 'R':
                    andataReturn.add(new InlineKeyboardButton().setText(ANDATA).setCallbackData(id + '~' + ANDATA + '~' + chosen));
                    break;
            }
            // endregion andataReturn
            if (!andataReturn.isEmpty()) inlineKeyboard.add(andataReturn);
        }

        List<InlineKeyboardButton> now = new ArrayList<>();
        // region now
        now.add(new InlineKeyboardButton().setText(NOW).setCallbackData(id + '~' + NOW + '~' + chosen));
        // endregion now
        inlineKeyboard.add(now);

        replyKeyboardMarkup.setKeyboard(inlineKeyboard);
        return replyKeyboardMarkup;
    }

    public static InlineKeyboardMarkup inlineKeyboardAutobus(String id, int chosen, int lastValue) {
        return inlineKeyboard(id, chosen, lastValue, true);
    }

    public static InlineKeyboardMarkup inlineKeyboardTrain(String id, int chosen, int lastValue) {
        return inlineKeyboard(id, chosen, lastValue, false);
    }

    // endregion inlineKeyboard

}
