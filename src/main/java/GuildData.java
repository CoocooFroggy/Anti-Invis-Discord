import com.google.gson.annotations.SerializedName;
import org.bson.Document;
import org.bson.codecs.pojo.annotations.BsonId;

public class GuildData {
    @BsonId
    @SerializedName(value = "_id")
    String guildId;
    String invisRoleId;

    public String getGuildId() {
        return guildId;
    }

    public String getInvisRoleId() {
        return invisRoleId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public void setInvisRoleId(String invisRoleId) {
        this.invisRoleId = invisRoleId;
    }

    public Document toDocument(){
        Document doc = new Document();

        doc.put("_id", this.getGuildId());
        doc.put("invisRoleId", this.getInvisRoleId());

        return doc;
    }
}
