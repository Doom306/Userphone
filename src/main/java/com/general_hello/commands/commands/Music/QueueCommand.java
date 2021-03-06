package com.general_hello.commands.commands.Music;

import com.general_hello.commands.SlashCommands.SlashCommand;
import com.general_hello.commands.SlashCommands.SlashCommandContext;
import com.general_hello.commands.commands.Emoji.Emoji;
import com.general_hello.commands.commands.Utils.Util;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class QueueCommand extends SlashCommand
{
    public QueueCommand()
    {
        setCommandData(new CommandData("queue", "shows the current queue")
                .addOption(OptionType.INTEGER, "page", "page of the queue", false)
        );
    }


    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        ButtonPaginator.Builder builder = new ButtonPaginator.Builder(event.getJDA())
                .setEventWaiter(new EventWaiter())
                .setItemsPerPage(10)
                .setTimeout(1, TimeUnit.MINUTES);
        int page = 1;
        if (event.getOption("page") != null) page = (int) event.getOption("page").getAsLong();
        GuildAudioPlayer guildAudioPlayer = AudioManager.getAudioPlayer(event.getGuild().getIdLong());
        Queue<AudioTrack> queue = guildAudioPlayer.getScheduler().getQueue();
        if (queue.isEmpty())
        {
            if (guildAudioPlayer.getPlayer().getPlayingTrack() != null)
            {
                if (guildAudioPlayer.getScheduler().isRepeat())
                    ctx.sendSimpleEmbed("\uD83D\uDD01 **Currently playing**: " + Util.titleMarkdown(guildAudioPlayer.getPlayer().getPlayingTrack()));
                else
                    ctx.sendSimpleEmbed("**Currently playing**: " + Util.titleMarkdown(guildAudioPlayer.getPlayer().getPlayingTrack()));
            } else
                ctx.replyError("There is no music playing!").queue();
            return;
        }
        String[] tracks = queue.stream().map(x -> "`[" + FormatUtil.formatTime(x.getDuration()) + "]` " + Util.titleMarkdown(x) + " (<@" + x.getUserData(Long.class) + ">)").toArray(String[]::new);
        builder.setTitle(getQueueTitle(guildAudioPlayer))
                .setItems(tracks)
                .addAllowedUsers(event.getUser().getIdLong())
                .setColor(Color.decode("#452350"));

        int finalPage = page;
        event.replyEmbeds(ctx.getSimpleEmbed(Emoji.LOADING + " Loading..."))
                .queue(hook -> hook.retrieveOriginal().queue(message -> builder.build().paginate(message, finalPage)));
    }

    private String getQueueTitle(GuildAudioPlayer player)
    {
        final StringBuilder sb = new StringBuilder();
        if (player.getPlayer().getPlayingTrack() != null)
        {
            sb.append(player.getPlayer().isPaused() ? "\u23f8" : "\u25b6").append(player.getScheduler().isRepeat() ? "\uD83D\uDD01" : "").append(" ").append(Util.titleMarkdown(player.getPlayer().getPlayingTrack())).append("\n");
        }
        int entries = player.getScheduler().getQueue().size();
        long length = 0;
        for (AudioTrack track : player.getScheduler().getQueue())
        {
            length += track.getDuration();
        }
        return FormatUtil.filter(sb.append(entries).append(entries == 1 ? " entry | `" : " entries | `").append(FormatUtil.formatTime(length)).append("`").toString());
    }
}