import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Main {
    public static void main(String[] args) {
        MongoUtils.connectToDb();

        try {
            startBot();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        registerSlashCommands();
    }

    static JDA jda;
    static String token;

    public static boolean startBot() throws InterruptedException {
        token = System.getenv("ANTIINVIS_TOKEN");
        JDABuilder jdaBuilder = JDABuilder
                .createDefault(token)
                .enableIntents(GatewayIntent.GUILD_PRESENCES)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableCache(CacheFlag.ONLINE_STATUS);
//        jdaBuilder.setActivity(Activity.playing("a game"));
        try {
            jda = jdaBuilder.build();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        jda.addEventListener(new Listeners());
        jda.awaitReady();
        return true;
    }

    public static void registerSlashCommands() {
        //DEBUG
        Guild testGuild = jda.getGuildById("685606700929384489");
        assert testGuild != null;
        testGuild.upsertCommand("blockallchannels", "Blocks all channels from invisible users.").complete();
        testGuild.upsertCommand("whitelistchannel", "Make channel visible to invisible users.")
                .addOption(OptionType.CHANNEL, "channel", "Channel to whitelist", true)
                .complete();
    }
}
