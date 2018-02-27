package viaggia;

import gekoramy.chatbase.ChatbaseHandler;
import gekoramy.chatbase.ChatbaseSender;
import gekoramy.chatbase.model.CustomEvent.CustomEventBuilder;
import gekoramy.chatbase.model.CustomEvent.property.PropertyFloat;
import gekoramy.chatbase.model.CustomEvent.property.PropertyString;
import gekoramy.chatbase.model.GenericMessage.GenericMessageAgentBuilder;
import gekoramy.chatbase.model.GenericMessage.GenericMessageUserBuilder;
import gekoramy.chatbase.model.GenericMessage.Platform;
import gekoramy.telegram.bot.model.Monitor;
import org.telegram.telegrambots.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.api.methods.send.*;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageLiveLocation;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.inlinequery.InlineQuery;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Luca Mosetti
 * @since 02/2018
 */
class ChatbaseMonitor implements Monitor {

    private final GenericMessageAgentBuilder builderGMAgent;
    private final GenericMessageUserBuilder builderGMUser;
    private final CustomEventBuilder builderCE;
    private ChatbaseSender chatbaseSender;

    ChatbaseMonitor(String botVersion) {
        builderGMAgent = new GenericMessageAgentBuilder()
                .setVersion(botVersion)
                .setPlatform(Platform.TELEGRAM.getPlatform());

        builderGMUser = new GenericMessageUserBuilder()
                .setVersion(botVersion)
                .setPlatform(Platform.TELEGRAM.getPlatform());

        builderCE = new CustomEventBuilder()
                .setVersion(botVersion)
                .setPlatform(Platform.TELEGRAM.getPlatform());
    }

    void setApiKey(String apiKey) {
        builderGMAgent.setApi_key(apiKey);
        builderGMUser.setApi_key(apiKey);
        builderCE.setApi_key(apiKey);
    }

    void setChatbaseHandler(ChatbaseHandler chatbaseHandler) {
        this.chatbaseSender = new ChatbaseSender(chatbaseHandler);
    }

    @Override
    public void sent(User user, SendMessage sendMessage) {
        chatbaseSender.postGMRequest(builderGMAgent
                .setUser_id(Integer.toString(user.getId()))
                .setTime_stamp(System.currentTimeMillis())
                .setMessage(sendMessage.getText())
                .setCustom_session_id(sendMessage.getChatId())
                .build());
    }

    @Override
    public void sent(User user, SendLocation sendLocation) {
        chatbaseSender.postGMRequest(builderGMAgent
                .setUser_id(Integer.toString(user.getId()))
                .setTime_stamp(System.currentTimeMillis())
                .setMessage("lat:" + sendLocation.getLatitude() + "\nlon:" + sendLocation.getLongitude())
                .setCustom_session_id(sendLocation.getChatId())
                .build());
    }

    @Override
    public void sent(User user, SendVenue sendVenue) {
        chatbaseSender.postGMRequest(builderGMAgent
                .setUser_id(Integer.toString(user.getId()))
                .setTime_stamp(System.currentTimeMillis())
                .setMessage(sendVenue.getAddress() + "\nlat:" + sendVenue.getLatitude() + "\nlon:" + sendVenue.getLongitude())
                .setCustom_session_id(sendVenue.getChatId())
                .build());
    }

    @Override
    public void sent(User user, EditMessageText editMessageText) {
        chatbaseSender.postGMRequest(builderGMAgent
                .setUser_id(Integer.toString(user.getId()))
                .setTime_stamp(System.currentTimeMillis())
                .setMessage(editMessageText.getText())
                .setCustom_session_id(editMessageText.getChatId())
                .build());
    }

    @Override
    public void sent(User user, EditMessageReplyMarkup editMessageReplyMarkup) {
        chatbaseSender.postGMRequest(builderGMAgent
                .setUser_id(Integer.toString(user.getId()))
                .setTime_stamp(System.currentTimeMillis())
                .setMessage(null)
                .setCustom_session_id(editMessageReplyMarkup.getChatId())
                .build());
    }

    @Override
    public void sent(User user, EditMessageLiveLocation editMessageLiveLocation) {

    }

    @Override
    public void sent(User user, AnswerInlineQuery answerInlineQuery) {

    }

    @Override
    public void sent(User user, SendChatAction sendChatAction) {

    }

    @Override
    public void sent(User user, SendContact sendContact) {

    }

    @Override
    public void sent(User user, SendGame sendGame) {

    }

    @Override
    public void received(long timeStamp, String cmd, boolean not_handled, Message msg) {
        if (msg.hasText())
            chatbaseSender.postGMRequest(builderGMUser
                    .setUser_id(Integer.toString(msg.getFrom().getId()))
                    .setTime_stamp(System.currentTimeMillis())
                    .setMessage(msg.getText())
                    .setIntent(cmd)
                    .setNot_handled(not_handled)
                    .setCustom_session_id(Long.toString(msg.getChatId()))
                    .build());

        else if (msg.hasLocation())
            chatbaseSender.postCERequest(builderCE
                    .setUser_id(Integer.toString(msg.getFrom().getId()))
                    .setTimestamp_millis((int) timeStamp)
                    .setIntent(cmd)
                    .setProperties(Arrays.asList(
                            new PropertyFloat("latitude", msg.getLocation().getLatitude()),
                            new PropertyFloat("longitude", msg.getLocation().getLongitude())
                    ))
                    .build());
    }

    @Override
    public void received(long timeStamp, String cmd, boolean not_handled, CallbackQuery cbq) {
        chatbaseSender.postCERequest(builderCE
                .setUser_id(Integer.toString(cbq.getFrom().getId()))
                .setTimestamp_millis((int) timeStamp)
                .setIntent(cmd + "-cbq")
                .setProperties(Collections.singletonList(
                        new PropertyString("data", cbq.getData())
                ))
                .build());
    }

    @Override
    public void received(long timeStamp, String cmd, boolean not_handled, InlineQuery iq) {
        chatbaseSender.postCERequest(builderCE
                .setUser_id(Integer.toString(iq.getFrom().getId()))
                .setTimestamp_millis((int) timeStamp)
                .setIntent(cmd + "-inline")
                .setProperties(Collections.singletonList(
                        new PropertyString("query", iq.getQuery())
                ))
                .build());
    }
}
