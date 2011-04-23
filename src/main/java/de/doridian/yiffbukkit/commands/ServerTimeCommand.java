package de.doridian.yiffbukkit.commands;

import java.util.Hashtable;

import org.bukkit.entity.Player;

import de.doridian.yiffbukkit.YiffBukkitCommandException;
import de.doridian.yiffbukkit.commands.ICommand.*;

@Names("servertime")
@Help("Forces/fixes current time *serverside*.")
@Usage("[normal|night|day|morning|afternoon|<0-23>]")
@Level(3)
public class ServerTimeCommand extends ICommand {
	Hashtable<String,Long> timeSwatches = new Hashtable<String,Long>();
	{
		timeSwatches.put("night", 0L);
		timeSwatches.put("morning", 6L);
		timeSwatches.put("day", 12L);
		timeSwatches.put("afternoon", 18L);
	};

	@Override
	public void Run(Player ply, String[] args, String argStr) throws YiffBukkitCommandException {
		long displayTime;

		if (argStr.isEmpty() || argStr.equalsIgnoreCase("normal")) {
			setTime(ply, null, null);
			return;
		}
		else if (timeSwatches.containsKey(argStr.toLowerCase())) {
			displayTime = timeSwatches.get(argStr.toLowerCase());
		}
		else {
			try
			{
				displayTime = Long.valueOf(argStr);
			}
			catch (Exception e) {
				throw new YiffBukkitCommandException("Usage: " + GetUsage(), e);
			}
		}

		setTime(ply, ((displayTime+18)%24)*1000, displayTime);
	}

	protected void setTime(Player ply, Long setTime, Long displayTime) {
		playerHelper.frozenServerTime = setTime;
		if (setTime == null) {
			playerHelper.SendServerMessage(ply.getName() + " reset the server time back to normal!");
		}
		else {
			playerHelper.SendServerMessage(ply.getName() + " forced the server time to be: " + displayTime + ":00");
		}
	}
}
