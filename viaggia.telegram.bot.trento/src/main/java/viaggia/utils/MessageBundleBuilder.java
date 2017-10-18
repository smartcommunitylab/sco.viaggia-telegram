package viaggia.utils;

import org.telegram.telegrambots.api.objects.User;
import viaggia.Users;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by Luca Mosetti on 2017
 * <p>
 * Useful class which:
 * - save the user in a local variable
 * - define the method getMessage(...)
 */
public class MessageBundleBuilder {

    private User user;

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the value of 'msg' contained in the user's Locale ResourceBundle
     *
     * @param msg    name
     * @param params params
     * @return value of 'msg' contained in the user's Locale ResourceBundle
     */
    public String getMessage(String msg, String... params) {
        Locale locale = Users.getLocale(user.getId());

        ResourceBundle bundle = ResourceBundle.getBundle("MessageBundle", locale);
        MessageFormat formatter = new MessageFormat("");
        formatter.setLocale(locale);
        formatter.applyPattern(bundle.getString(msg));
        return formatter.format(params);
    }
}
