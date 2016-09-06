package it.smartcommunitylab.viaggia.telegram.updateshandlers.messagging;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gekoramy
 */
public class Current {

    private static Map<Long, User> menus = new HashMap<>();

    public static Menu getMenu(long chatId) {
        if (!menus.containsKey(chatId))
        	menus.put(chatId, new User());
        return menus.get(chatId).menu;
    }

    public static Language getLanguage(long chatId) {
    	if (!menus.containsKey(chatId))
        	menus.put(chatId, new User());
        return menus.get(chatId).language;
    }

    private static void setUser(long chatId, Menu menu, Language language) {
        menus.put(chatId, new User(menu, language));
    }

    static void setMenu(long chatId, Menu menu) {
    	if (!menus.containsKey(chatId))
        	menus.put(chatId, new User());
        setUser(chatId, menu, menus.get(chatId).language);
    }

    public static void setLanguage(long chatId, Language language) {
    	if (!menus.containsKey(chatId))
        	menus.put(chatId, new User());
        setUser(chatId, menus.get(chatId).menu, language);
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

    }

}
