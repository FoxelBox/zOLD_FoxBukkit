package de.doridian.yiffbukkit.jail.commands;

import de.doridian.yiffbukkit.jail.JailException;
import de.doridian.yiffbukkit.main.commands.system.ICommand;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Help;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Names;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Permission;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Usage;
import de.doridian.yiffbukkit.main.util.PlayerFindException;
import de.doridian.yiffbukkitsplit.util.PlayerHelper;

import org.bukkit.entity.Player;

@Names("jail")
@Help("Sends someone to a previously defined jail cell.")
@Usage("<name> [release]")
@Permission("yiffbukkit.jail.jail")
public class JailCommand extends ICommand {
	@Override
	public void Run(Player ply, String[] args, String argStr) throws PlayerFindException, JailException {
		if (args.length == 0) {
			PlayerHelper.sendDirectedMessage(ply, "Not enough arguments.");
			return;
		}

		Player otherply = playerHelper.matchPlayerSingle(args[0]);

		if (args.length == 1) {
			plugin.jailEngine.jailPlayer(otherply, true);
			playerHelper.sendServerMessage(ply.getName()+" sent "+otherply.getName()+" to jail.");
		}
		else if (args[1].equals("release") || args[1].equals("rel") || args[1].equals("r")) {
			plugin.jailEngine.jailPlayer(otherply, false);
			playerHelper.sendServerMessage(ply.getName()+" released "+otherply.getName()+" from jail.");
		}
		else {
			PlayerHelper.sendDirectedMessage(ply, "Invalid argument.");
		}
	}
}