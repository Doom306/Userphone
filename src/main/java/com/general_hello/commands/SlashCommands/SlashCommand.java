package com.general_hello.commands.SlashCommands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class SlashCommand
{

    private CommandData commandData;
    private final List<Permission> requiredUserPermissions;
    private final List<Permission> requiredBotPermissions;
    private boolean isGlobal;
    private final List<Long> enabledGuilds;
    private boolean runnableInDM;

    public boolean isRunnableInDM()
    {
        return runnableInDM;
    }

    public void setRunnableInDM(boolean runnableInDM)
    {
        this.runnableInDM = runnableInDM;
    }

    public String getCommandName()
    {
        return commandData.getName();
    }

    public String getCommandDescription()
    {
        return commandData.getDescription();
    }

    public List<OptionData> getOptions()
    {
        return commandData.getOptions();
    }

    public CommandData getCommandData()
    {
        return commandData;
    }

    public void setCommandData(CommandData commandData)
    {
        this.commandData = commandData;
    }

    public List<Permission> getRequiredUserPermissions()
    {
        return requiredUserPermissions;
    }

    public void setRequiredUserPermissions(Permission... permissions)
    {
        this.requiredUserPermissions.addAll(Arrays.asList(permissions));
    }

    public List<Permission> getRequiredBotPermissions()
    {
        return requiredBotPermissions;
    }

    public void setRequiredBotPermissions(Permission... permissions)
    {
        this.requiredBotPermissions.addAll(Arrays.asList(permissions));
    }

    public boolean isGlobal()
    {
        return isGlobal;
    }

    public void global(boolean global)
    {
        isGlobal = global;
    }

    public List<Long> getEnabledGuilds()
    {
        return enabledGuilds;
    }

    public void setEnabledGuilds(Long... enabledGuilds)
    {
        this.enabledGuilds.addAll(Arrays.asList(enabledGuilds));
    }


    public SlashCommand()
    {
        this.requiredBotPermissions = new ArrayList<>();
        this.requiredUserPermissions = new ArrayList<>();
        this.commandData = null;
        this.isGlobal = true;
        this.enabledGuilds = new ArrayList<>();
        this.runnableInDM = false; }

    /**
     * Executes requested slash command
     *
     * @param event  The SlashCommandEvent
     * @param sender The member who sent the command (null if sent via DM)
     * @param ctx    Helpful methods in context of the event
     */
    public abstract void executeCommand(@Nonnull SlashCommandEvent event, @Nullable Member sender, @Nonnull SlashCommandContext ctx) throws SQLException;

}