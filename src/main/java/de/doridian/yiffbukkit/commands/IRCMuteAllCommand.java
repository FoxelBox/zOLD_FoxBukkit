package de.doridian.yiffbukkit.commands;

import de.doridian.yiffbukkit.commands.ICommand.Help;
import de.doridian.yiffbukkit.commands.ICommand.Names;
import de.doridian.yiffbukkit.commands.ICommand.Permission;
import de.doridian.yiffbukkit.irc.Ircbot;
import org.bukkit.command.CommandSender;

@Names({"ircmuteall", "ircmall"})
@Help("Mutes the IRC Chat")
@Permission("yiffbukkit.irc.muteall")
public class IRCMuteAllCommand extends ICommand {
	@Override
	public void run(CommandSender commandSender, String[] args, String argStr) {
		plugin.ircbot.sendToPublicChannel(commandSender.getName() + " muted IRC Chat");
		playerHelper.sendServerMessage(commandSender.getName() + " muted IRC Chat");
		plugin.ircbot.setMode(Ircbot.PUBLICCHANNEL, "+m");
	}
}