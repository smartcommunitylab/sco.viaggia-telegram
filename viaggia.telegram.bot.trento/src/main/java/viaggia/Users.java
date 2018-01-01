package viaggia;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.util.Locale;

/**
 * Created by Luca Mosetti in 2017
 * <p>
 * When a new conversation with this bot is started
 * there're saved some information about the User (Not the Chat!)
 * <p>
 * The language preference
 */
public class Users {

    private static final String MONGO_HOST = "localhost";
    private static final int MONGO_PORT = 27017;
    private static final String DATABASE = "users";
    private static final String COLLECTION = "collection";

    private static final MongoCollection<Document> mongoCollection =
            new MongoClient(MONGO_HOST, MONGO_PORT).getDatabase(DATABASE).getCollection(COLLECTION);

    static void putUser(int userId) {
        if (mongoCollection.find(Filters.eq(UserAdapter.ID, userId)).first() == null)
            mongoCollection.insertOne(UserAdapter.toDocument(userId, Locale.ITALY));
    }

    public static Locale getLocale(int userId) {
        if (mongoCollection.find(Filters.eq(UserAdapter.ID, userId)).first() == null)
            mongoCollection.insertOne(UserAdapter.toDocument(userId, Locale.ITALY));
        return UserAdapter.getLocale(mongoCollection.find(Filters.eq(UserAdapter.ID, userId)).first());
    }

    public static void setLocale(int userId, Locale locale) {
        mongoCollection.replaceOne(
                Filters.eq(UserAdapter.ID, userId),
                UserAdapter.toDocument(userId, locale),
                new UpdateOptions().upsert(true));
    }

    public static long count() {
        return mongoCollection.count();
    }

    private static class UserAdapter {
        private static final String ID = "_id";
        private static final String LOCALE = "locale";

        private static Locale getLocale(Document document) {
            return Locale.forLanguageTag((String) document.get(LOCALE));
        }

        private static Document toDocument(int userId, Locale locale) {
            return new Document(ID, userId).append(LOCALE, locale.toLanguageTag());
        }
    }
}
