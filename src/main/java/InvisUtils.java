import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.bson.Document;

import java.awt.*;

import static com.mongodb.client.model.Filters.eq;

public class InvisUtils {
    public static boolean isOnline(OnlineStatus status) {
        if (status.equals(OnlineStatus.OFFLINE) || status.equals(OnlineStatus.INVISIBLE))
            return false;
        else if (status.equals(OnlineStatus.ONLINE) || status.equals(OnlineStatus.IDLE) || status.equals(OnlineStatus.DO_NOT_DISTURB) || status.equals(OnlineStatus.UNKNOWN))
            return true;
        else return true;
        // Unknown status will just return true (not invis), so we don't make any mistakes
    }

    public static void addInvisRole(Guild guild, Member member) {
        guild.addRoleToMember(member, getInvisRole(guild)).queue();
    }

    public static void removeInvisRole(Guild guild, Member member) {
        guild.removeRoleFromMember(member, getInvisRole(guild)).queue();
    }

    public static Role getInvisRole(Guild guild) {
        Role invisRole;
        GuildData guildData = getGuildData(guild.getId());
        if (guildData == null) {
            invisRole = createInvisRole(guild);
        } else {
            invisRole = guild.getRoleById(guildData.getInvisRoleId());
            if (invisRole == null) {
                invisRole = createInvisRole(guild);
            }
        }

        return invisRole;
    }

    public static Role createInvisRole(Guild guild) {
        Role invisRole;
        invisRole = guild.createRole()
                .setName("Invisible")
                .setColor(new Color(69, 69, 69))
                .setPermissions()
                .complete();

        // Save the role ID in DB
        GuildData guildData = getGuildData(guild.getId());
        if (guildData == null) {
            guildData = new GuildData();
        }
        guildData.setGuildId(guild.getId());
        guildData.setInvisRoleId(invisRole.getId());

        setGuildData(guildData);

        return invisRole;
    }

    public static GuildData getGuildData(String guildId) {
        MongoCollection<Document> collection = MongoUtils.database.getCollection("Guilds");
        Gson gson = new Gson();
        Document document = collection.find(eq("_id", guildId)).first();

        if (document == null) {
            return null;
        } else {
            return gson.fromJson(document.toJson(), GuildData.class);
        }
    }

    public static void setGuildData(GuildData guildData) {
        MongoCollection<Document> collection = MongoUtils.database.getCollection("Guilds");
        boolean alreadyExists = collection.find(Filters.eq("_id", guildData.getGuildId())).limit(1).iterator().hasNext();
        if (alreadyExists) {
            collection.replaceOne(Filters.eq("_id", guildData.getGuildId()), guildData.toDocument());
        } else {
            collection.insertOne(guildData.toDocument());
        }
    }
}
