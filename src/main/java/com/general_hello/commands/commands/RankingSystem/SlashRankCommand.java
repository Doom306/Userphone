package com.general_hello.commands.commands.RankingSystem;

import com.general_hello.commands.SlashCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public class SlashRankCommand extends SlashCommand {
    public SlashRankCommand()
    {
        setCommandData(new CommandData("rank", "Shows your rank")
                .addOption(OptionType.USER, "user", "Gets the rank of another member", false)
        );
        setRunnableInDM(false);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx) throws SQLException {
        InteractionHook commandHook = event.getHook();
        event.deferReply(false).queue();
        OptionMapping optionData = event.getOption("user");
        if (optionData == null)
        {
            User user = event.getUser();
            long xp = RankingSystem.getTotalXP(event.getGuild().getIdLong(), user.getIdLong());
            if (xp < 100)
            {
                commandHook.sendMessage("You are not yet ranked!").queue();
                return;
            }
            commandHook.sendMessage("").addFile(RankingSystem.generateLevelCard(user, event.getGuild()), "card.png").queue();
        } else
        {
            User user = optionData.getAsUser();
            long xp = RankingSystem.getTotalXP(event.getGuild().getIdLong(), user.getIdLong());
            if (xp < 100)
            {
                commandHook.sendMessage("This member is not yet ranked!").queue();
                return;
            }
            byte[] levelCard = RankingSystem.generateLevelCard(user, event.getGuild());
            if (levelCard == null)
            {
                commandHook.sendMessage("This member is not yet ranked!").queue();
                return;
            }
            commandHook.sendMessage("").addFile(levelCard, "card.png").queue();
        }
    }
}