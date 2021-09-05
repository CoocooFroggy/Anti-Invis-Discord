import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Listeners extends ListenerAdapter {
    @Override
    public void onUserUpdateOnlineStatus(@NotNull UserUpdateOnlineStatusEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();

        OnlineStatus oldStatus = event.getOldOnlineStatus();
        OnlineStatus newStatus = event.getNewOnlineStatus();

        // If they're going online
        if (InvisUtils.isOnline(newStatus)) {
            // From an offline state
            if (!InvisUtils.isOnline(oldStatus)) {
                // Then remove the offline role
                InvisUtils.removeInvisRole(guild, member);
            }
        }

        // If they're going offline
        if (!InvisUtils.isOnline(newStatus)) {
            // From an online state
            if (InvisUtils.isOnline(oldStatus)) {
                // Then add the invis role
                InvisUtils.addInvisRole(guild, member);
            }
        }
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        switch (event.getName()) {
            case "blockallchannels": {
                if (event.getMember() == null)
                    return;
                if (event.getGuild() == null)
                    return;
                if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
                    event.reply("You need \"Manage Server\" permissions to use this command.").setEphemeral(true).queue();
                    return;
                }
                // In a guild with manage server perms

                InteractionHook hook = event.deferReply().complete();

                Guild guild = event.getGuild();
                Role invisRole = InvisUtils.getInvisRole(guild);

                ArrayList<GuildChannel> failedChannels = new ArrayList<>();
                // Loop through all channels
                List<GuildChannel> channels = guild.getChannels();
                for (GuildChannel channel : channels) {
                    try {
                        if (channel instanceof TextChannel) {
                            TextChannel textChannel = (TextChannel) channel;
                            // Make them not visible to invis role
                            textChannel.getManager().putPermissionOverride(invisRole, null, Collections.singleton(Permission.VIEW_CHANNEL)).complete();
                        } else if (channel instanceof Category) {
                            Category category = (Category) channel;
                            category.getManager().putPermissionOverride(invisRole, null, Collections.singleton(Permission.VIEW_CHANNEL)).complete();
                        } else if (channel instanceof VoiceChannel) {
                            VoiceChannel voiceChannel = (VoiceChannel) channel;
                            voiceChannel.getManager().putPermissionOverride(invisRole, null, Collections.singleton(Permission.VIEW_CHANNEL)).complete();
                        }
                    } catch (RuntimeException exception) {
                        failedChannels.add(channel);
                    }
                }

                StringBuilder finalMessage = new StringBuilder();
                finalMessage.append("Finished blacklisting all channels for ").append(invisRole.getAsMention()).append(".\n");

                if (!failedChannels.isEmpty()) {
                    finalMessage.append("Failed to set permissions for the following channels:\n");
                    for (GuildChannel channel : failedChannels) {
                        if (channel instanceof Category) {
                            finalMessage.append(channel.getName()).append(" (Category)").append("\n");
                        } else {
                            finalMessage.append(channel.getAsMention()).append("\n");
                        }
                    }
                }

                hook.editOriginal(finalMessage.toString()).queue();
                break;
            }
            case "whitelistchannel": {
                if (event.getMember() == null)
                    return;
                if (event.getGuild() == null)
                    return;
                if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
                    event.reply("You need \"Manage Server\" permissions to use this command.").setEphemeral(true).queue();
                    return;
                }

                InteractionHook hook = event.deferReply().complete();

                // Required option "channel"
                GuildChannel channel = Objects.requireNonNull(event.getOption("channel")).getAsGuildChannel();

                Guild guild = event.getGuild();
                Role invisRole = InvisUtils.getInvisRole(guild);

                try {
                    if (channel instanceof TextChannel) {
                        TextChannel textChannel = (TextChannel) channel;
                        // Make them not visible to invis role
                        textChannel.getManager().putPermissionOverride(invisRole, Collections.singleton(Permission.VIEW_CHANNEL), null).complete();
                    } else if (channel instanceof Category) {
                        Category category = (Category) channel;
                        category.getManager().putPermissionOverride(invisRole, Collections.singleton(Permission.VIEW_CHANNEL), null).complete();
                    } else if (channel instanceof VoiceChannel) {
                        VoiceChannel voiceChannel = (VoiceChannel) channel;
                        voiceChannel.getManager().putPermissionOverride(invisRole, Collections.singleton(Permission.VIEW_CHANNEL), null).complete();
                    }
                } catch (RuntimeException exception) {
                    hook.editOriginal("Failed to set permission overrides for " + channel.getAsMention() + ".").queue();
                    return;
                }

                hook.editOriginal("Added " + channel.getName() + " to whitelist.").queue();
                break;
            }
            case "help": {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setFooter(event.getUser().getName(), event.getUser().getAvatarUrl());
                eb.setTitle("Anti-Invis Bot Help Menu");
                eb.addField("`/help`", "Opens this menu.", true);
                eb.addField("`/blockallchannels`", "Blocks all channels from being viewed by invisible users.", true);
                eb.addField("`/whitelistchannel [channel]`", "Makes a channel viewable for invisible users.", true);
            }
        }
    }
}
