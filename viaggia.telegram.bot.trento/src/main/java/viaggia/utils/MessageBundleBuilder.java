package viaggia.utils;

import viaggia.Users;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Reads MessageBundle properties files
 *
 * @author Luca Mosetti
 * @since 2017
 */
public class MessageBundleBuilder {

    /**
     * Returns the value of 'msg' contained in the user's Locale ResourceBundle
     *
     * @param userId user's id, useful for its locale
     * @param msg    name
     * @param params params
     * @return value of 'msg' contained in the user's Locale ResourceBundle
     */
    public String getMessage(int userId, String msg, String... params) {
        Locale locale = Users.getLocale(userId);

        ResourceBundle bundle = ResourceBundle.getBundle("MessageBundle", locale);
        MessageFormat formatter = new MessageFormat("");
        formatter.setLocale(locale);
        formatter.applyPattern(bundle.getString(msg));
        return formatter.format(params);
    }
}
