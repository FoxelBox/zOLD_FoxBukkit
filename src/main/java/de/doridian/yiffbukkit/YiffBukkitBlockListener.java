package de.doridian.yiffbukkit;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.TileEntity;
import net.minecraft.server.TileEntitySign;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.PluginManager;

import de.doridian.yiffbukkit.util.PlayerHelper;

/**
 * Handle events for all Block related events
 * @author Doridian
 */
public class YiffBukkitBlockListener extends BlockListener {
	private final YiffBukkit plugin;
	public static final Map<Material,Integer> blocklevels = new HashMap<Material,Integer>();
	static {
		blocklevels.put(Material.TNT, 4);
		blocklevels.put(Material.BEDROCK, 4);

		blocklevels.put(Material.OBSIDIAN, 1);

		blocklevels.put(Material.WATER, 1);
		blocklevels.put(Material.WATER_BUCKET, 1);
		blocklevels.put(Material.LAVA, 3);
		blocklevels.put(Material.LAVA_BUCKET, 3);

		blocklevels.put(Material.FLINT_AND_STEEL, 3);
		blocklevels.put(Material.FIRE, 3);
	}
	private PlayerHelper playerHelper;

	public YiffBukkitBlockListener(YiffBukkit instance) {
		plugin = instance;
		playerHelper = plugin.playerHelper;

		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvent(Event.Type.BLOCK_PLACE, this, Priority.Normal, plugin);
		pm.registerEvent(Event.Type.BLOCK_CANBUILD, this, Priority.Normal, plugin);
		//pm.registerEvent(Event.Type.BLOCK_BREAK, this, Priority.Normal, plugin);
		pm.registerEvent(Event.Type.BLOCK_DAMAGE, this, Priority.Normal, plugin);
		pm.registerEvent(Event.Type.SIGN_CHANGE, this, Priority.Highest, plugin);
		pm.registerEvent(Event.Type.BLOCK_PHYSICS, this, Priority.Highest, plugin);
	}

	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		Player ply = event.getPlayer();
		if(playerHelper.isPlayerDisabled(ply)) {
			event.setBuild(false);
			return;
		}

		Material block = event.getBlock().getType();
		Integer selflvl = playerHelper.GetPlayerLevel(ply);
		if(selflvl < 0 || (blocklevels.containsKey(block) && selflvl < blocklevels.get(block))) {
			playerHelper.SendServerMessage(ply.getName() + " tried to spawn illegal block " + block.toString());
			event.setBuild(false);
		}
	}

	@Override
	public void onBlockDamage(BlockDamageEvent event) {
		Player ply = event.getPlayer();
		if(playerHelper.isPlayerDisabled(ply)) {
			event.setCancelled(true);
			return;
		}

		if(playerHelper.GetPlayerLevel(ply) < 0 && event.getInstaBreak()) {
			playerHelper.SendServerMessage(ply.getName() + " tried to illegaly break a block!");
			event.setCancelled(true);
		}
	}

	@Override
	public void onSignChange(SignChangeEvent event) {
		Block block = event.getBlock();
		TileEntity tileEntity = ((CraftWorld)block.getWorld()).getHandle().getTileEntity(block.getX(),block.getY(),block.getZ());
		TileEntitySign tileEntitySign = (TileEntitySign) tileEntity;
		for (String line : tileEntitySign.a) {
			if (!line.isEmpty()) {
				for (int index = 0; index < 4; ++index) {
					event.setLine(index, tileEntitySign.a[index]);
				}
			}
		}
	}

	@Override
	public void onBlockPhysics(BlockPhysicsEvent event) {
		if (event.getChangedType() == Material.PORTAL)
			event.setCancelled(true);
	}
	
	@Override
	public void onBlockCanBuild(BlockCanBuildEvent event)
	{
		if (event.isBuildable() == false) {
			if (event.getMaterial() == Material.FENCE) {
				event.setBuildable(true);
			}
		}
	}
}
