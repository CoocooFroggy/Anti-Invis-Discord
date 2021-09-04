import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoUtils {
    static MongoDatabase database;

    public static void connectToDb() {
        MongoClient client = MongoClients.create(System.getenv("MONGO_URI"));
        database = client.getDatabase("anti-invis-db");
    }
}
