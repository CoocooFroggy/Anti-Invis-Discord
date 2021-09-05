import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
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

        jdaBuilder.setActivity(Activity.watching("for invisible users"));
        jdaBuilder.setStatus(OnlineStatus.DO_NOT_DISTURB);

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
//        Guild testGuild = jda.getGuildById("685606700929384489");
//        assert testGuild != null;
        jda.upsertCommand("help", "Opens the help menu with a list of commands.").complete();
        jda.upsertCommand("blockallchannels", "Blocks all channels from being viewed by invisible users.").complete();
        jda.upsertCommand("whitelistchannel", "Makes a channel viewable for invisible users.")
                .addOption(OptionType.CHANNEL, "channel", "Channel to whitelist", true)
                .complete();
    }
}
