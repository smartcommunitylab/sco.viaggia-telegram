package org.telegram.updateshandlers.GestioneMessaggi;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gekoramy
 */
public class Current {

    private static Map<Long, User> menus = new HashMap<>();

    public static Menu getMenu(long chatId) {
        menus.putIfAbsent(chatId, new User());
        return menus.get(chatId).getMenu();
    }

    public static Language getLanguage(long chatId) {
        menus.putIfAbsent(chatId, new User());
        return menus.get(chatId).getLanguage();
    }

    private static void setUser(long chatId, Menu menu, Language language) {
        menus.put(chatId, new User(menu, language));
    }

    static void setMenu(long chatId, Menu menu) {
        menus.putIfAbsent(chatId, new User());
        setUser(chatId, menu, menus.get(chatId).getLanguage());
    }

    public static void setLanguage(long chatId, Language language) {
        menus.putIfAbsent(chatId, new User());
        setUser(chatId, menus.get(chatId).getMenu(), language);
    }

    private static class User {
        Menu menu;
        Language language;

        User() {
            this.menu = Menu.START;
            this.language = Language.ENGLISH;
        }

        User(Menu menu, Language language) {
            this.menu = menu;
            this.language = language;
        }

        public Menu getMenu() {
            return menu;
        }

        public void setMenu(Menu menu) {
            this.menu = menu;
        }

        Language getLanguage() {
            return language;
        }

        public void setLanguage(Language language) {
            this.language = language;
        }
    }

}
