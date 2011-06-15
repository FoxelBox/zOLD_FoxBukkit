package de.doridian.yiffbukkit.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.Date;

import net.minecraft.server.EntityFallingSand;
import net.minecraft.server.EntityFireball;
import net.minecraft.server.EntityPig;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.EntityTNTPrimed;
import net.minecraft.server.EntityWolf;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet53BlockChange;
import net.minecraft.server.WorldServer;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.entity.CraftWolf;
import org.bukkit.entity.Boat;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wolf;
import org.bukkit.util.Vector;

import de.doridian.yiffbukkit.YiffBukkit;
import de.doridian.yiffbukkit.YiffBukkitCommandException;
import de.doridian.yiffbukkit.commands.ICommand;
import de.doridian.yiffbukkit.sheep.CamoSheep;
import de.doridian.yiffbukkit.sheep.PartySheep;

public class Utils {
	private YiffBukkit plugin;
	public Utils(YiffBukkit iface) {
		plugin = iface;
	}

	public static String concatArray(String[] array, int start, String def) {
		if(array.length <= start) return def;
		if(array.length <= start + 1) return array[start];
		String ret = array[start];
		for(int i=start+1;i<array.length;i++) {
			ret += " " + array[i];
		}
		return ret;
	}

	public static String serializeLocation(Location loc) {
		return loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getYaw() + ";" + loc.getPitch() + ";" + loc.getWorld().getName() + ";" + loc.getWorld().getEnvironment().name();
	}

	public Location unserializeLocation(String str) {
		String[] split = str.split(";");
		return new Location(plugin.getOrCreateWorld(split[5], Environment.valueOf(split[6])), Double.valueOf(split[0]), Double.valueOf(split[1]), Double.valueOf(split[2]), Float.valueOf(split[3]), Float.valueOf(split[4]));
	}

	@SuppressWarnings("unchecked")
	public static <T, E> T getPrivateValue(Class<? super E> class1, E instance, String field) {
		try
		{
			Field f = class1.getDeclaredField(field);
			f.setAccessible(true);
			return (T) f.get(instance);
		}
		catch (Exception e) {
			return null;
		}
	}

	public static <T, E> void setPrivateValue(Class<? super T> instanceclass, T instance, String field, E value) {
		try
		{
			Field field_modifiers = Field.class.getDeclaredField("modifiers");
			field_modifiers.setAccessible(true);


			Field f = instanceclass.getDeclaredField(field);
			int modifiers = field_modifiers.getInt(f);
			if ((modifiers & 0x10) != 0)
				field_modifiers.setInt(f, modifiers & 0xFFFFFFEF);
			f.setAccessible(true);
			f.set(instance, value);
		}
		catch (Exception e) { }
	}

	static String[] directions = { "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW" };
	public static String yawToDirection(double yaw) {
		yaw = (yaw%360+630)%360;

		int intdeg = (int) Math.round(yaw / 22.5F);
		if (intdeg < 0) intdeg += 16;
		if (intdeg >= 16) intdeg -= 16;

		return directions[intdeg];
	}

	public static double vectorToYaw(Vector offset) {
		return Math.toDegrees(Math.atan2(-offset.getX(), offset.getZ()));
	}

	public Entity buildMob(final String[] types, CommandSender commandSender, Player them, Location location) throws YiffBukkitCommandException {
		boolean hasThis = false;
		for (String part : types) {
			if ("THIS".equals(part)) {
				hasThis = true;
				break;
			}
		}

		final World world = location.getWorld();
		final WorldServer notchWorld = ((CraftWorld)world).getHandle();

		Entity thisEnt = null;
		if (hasThis) {
			Vector eyeVector = location.getDirection().clone();
			Vector eyeOrigin = location.toVector().clone();

			for (Entity currentEntity : world.getEntities()) {
				Location eyeLocation;
				if (currentEntity instanceof LivingEntity) {
					eyeLocation = ((LivingEntity)currentEntity).getEyeLocation();
				}
				else if (currentEntity instanceof Boat || currentEntity instanceof Minecart) {
					eyeLocation = currentEntity.getLocation();
				}
				else {
					continue;
				}

				Vector pos = eyeLocation.toVector().clone();
				pos.add(new Vector(0, 0.6, 0));

				pos.subtract(eyeOrigin);

				if (pos.lengthSquared() > 9)
					continue;

				double dot = pos.clone().normalize().dot(eyeVector);

				if (dot < 0.8)
					continue;


				if (currentEntity.equals(commandSender))
					continue;

				thisEnt = currentEntity;
				break;
			}
			if (thisEnt == null) {
				throw new YiffBukkitCommandException("You must face a creature/boat/minecart");
			}
		}

		Entity previous = null;
		Entity first = null;
		for (String part : types) {
			String[] partparts = part.split(":");

			String type = partparts[0];
			String data = partparts.length >= 2 ? partparts[1] : null;

			Entity entity;
			if (type.equalsIgnoreCase("ME")) {
				entity = ICommand.asPlayer(commandSender);
			}
			else if (type.equalsIgnoreCase("THEM")) {
				entity = them;
			}
			else if (type.equalsIgnoreCase("FIREBALL")) {
				final EntityPlayer playerEntity;
				if (them instanceof CraftPlayer)
					playerEntity = ((CraftPlayer)them).getHandle();
				else if (commandSender instanceof CraftPlayer)
					playerEntity = ((CraftPlayer)commandSender).getHandle();
				else
					playerEntity = null;

				final Vector dir = playerEntity.getBukkitEntity().getLocation().getDirection();
				double dx = dir.getX();
				double dy = dir.getY();
				double dz = dir.getZ();

				final EntityFireball notchEntity = new EntityFireball(notchWorld, playerEntity, dx, dy, dz);
				notchEntity.locX = location.getX();
				notchEntity.locY = location.getY();
				notchEntity.locZ = location.getZ();

				double d3 = 0.1D / Math.sqrt(dx * dx + dy * dy + dz * dz);

				notchEntity.c = dx * d3;
				notchEntity.d = dy * d3;
				notchEntity.e = dz * d3;

				notchWorld.addEntity(notchEntity);

				entity = null;//notchEntity.getBukkitEntity();
			}
			else if (type.equalsIgnoreCase("TNT")) {
				EntityTNTPrimed notchEntity = new EntityTNTPrimed(notchWorld, 0, 1, 0);
				notchWorld.addEntity(notchEntity);

				entity = notchEntity.getBukkitEntity();
				entity.teleport(location);
			}
			else if(type.equalsIgnoreCase("SAND") || type.equalsIgnoreCase("GRAVEL")) {
				int material = Material.valueOf(type.toUpperCase()).getId();
				EntityFallingSand notchEntity = new EntityFallingSand(notchWorld, location.getX(), location.getY(), location.getZ(), material);
				notchWorld.addEntity(notchEntity);

				entity = notchEntity.getBukkitEntity();
			}
			else if(type.equalsIgnoreCase("LIGHTNING")) {
				EntityFallingSand notchEntity = new EntityFallingSand(notchWorld, location.getX(), location.getY(), location.getZ(), Material.GRAVEL.getId()) {
					@Override
					public void o_() {
						if (this.a == 0)
							this.die();

						this.lastX = this.locX;
						this.lastY = this.locY;
						this.lastZ = this.locZ;
						++this.b;
						this.motY -= 0.03999999910593033D;
						this.move(this.motX, this.motY, this.motZ);
						this.motX *= 0.9800000190734863D;
						this.motY *= 0.9800000190734863D;
						this.motZ *= 0.9800000190734863D;

						if (this.onGround) {
							org.bukkit.World world = getBukkitEntity().getWorld();
							world.strikeLightning(new Location(world, this.locX, this.locY, this.locZ));
							plugin.playerHelper.sendPacketToPlayersAround(new Location(world, this.locX, this.locY, this.locZ), 200, new Packet53BlockChange((int)Math.floor(this.locX), (int)Math.floor(this.locY), (int)Math.floor(this.locZ), notchWorld));
							this.die();
						}
						else if (this.b > 100 && !this.world.isStatic) {
							this.die();
						}
					}
				};
				notchWorld.addEntity(notchEntity);

				entity = notchEntity.getBukkitEntity();
			}
			else if(type.equalsIgnoreCase("ARROW")) {
				entity = world.spawnArrow(location, new Vector(0, 1, 0), 2, 0);
			}
			else if (type.equalsIgnoreCase("MINECART") || type.equalsIgnoreCase("CART")) {
				entity = world.spawnMinecart(location);
			}
			else if (type.equalsIgnoreCase("BOAT")) {
				entity = world.spawnBoat(location);
			}
			else if (type.equalsIgnoreCase("THIS")) {
				entity = thisEnt;
			}
			else if (type.equalsIgnoreCase("CREEPER")) {
				entity = world.spawnCreature(location, CreatureType.CREEPER);
				if (entity == null) {
					throw new YiffBukkitCommandException("Could not spawn a creeper here. Too bright?");
				}
				final Creeper creeper = (Creeper)entity;

				if ("ELECTRIFIED".equalsIgnoreCase(data) || "CHARGED".equalsIgnoreCase(data) || "POWERED".equalsIgnoreCase(data)) {
					creeper.setPowered(true);
				}
			}
			else if (type.equalsIgnoreCase("SLIME")) {
				entity = world.spawnCreature(location, CreatureType.SLIME);
				final Slime slime = (Slime)entity;

				if (data != null) {

					try {
						int size = Integer.parseInt(data);
						slime.setSize(size);
					}
					catch (NumberFormatException e) { }

				}
			}
			else if (type.equalsIgnoreCase("WOLF")) {
				entity = world.spawnCreature(location, CreatureType.WOLF);
				final Wolf wolf = (Wolf)entity;

				if (data != null) { 
					for (String subData : data.toUpperCase().split(",")) {
						if (subData.isEmpty())
							continue;

						if (subData.equals("ANGRY")) {
							wolf.setAngry(true);
						}
						else if (subData.equals("SITTING") || subData.equals("SIT")) {
							wolf.setSitting(true);
						}
						else if (subData.equals("TAME") || subData.equals("TAMED")) {
							CraftWolf craftWolf = (CraftWolf) wolf;
							EntityWolf eWolf = craftWolf.getHandle();
							if (them == null)
								eWolf.a(commandSender.getName());
							else
								eWolf.a(them.getName());
							eWolf.d(true);
						}
					}
				}
			}
			else if (type.equalsIgnoreCase("SHEEP")) {
				entity = world.spawnCreature(location, CreatureType.SHEEP);
				final Sheep sheep = (Sheep)entity;

				if ("CAMO".equalsIgnoreCase(data) || "CAMOUFLAGE".equalsIgnoreCase(data)) {
					new CamoSheep(plugin, sheep);
				}
				else if ("PARTY".equalsIgnoreCase(data)) {
					new PartySheep(plugin, sheep);
				}
				else if ("SHEARED".equalsIgnoreCase(data) || "SHORN".equalsIgnoreCase(data) || "NUDE".equalsIgnoreCase(data) || "NAKED".equalsIgnoreCase(data)) {
					sheep.setSheared(true);
				}
				else {
					DyeColor dyeColor = DyeColor.WHITE;
					try {
						if ("RAINBOW".equalsIgnoreCase(data) || "RAINBOWS".equalsIgnoreCase(data) || "RANDOM".equalsIgnoreCase(data)) {
							DyeColor[] dyes = DyeColor.values();
							dyeColor = dyes[(int)Math.floor(dyes.length*Math.random())];
						}
						else {
							dyeColor = DyeColor.valueOf(data.toUpperCase());
						}
					}
					catch (Exception e) { }

					sheep.setColor(dyeColor);
				}
			}
			else if (type.equalsIgnoreCase("NPC")) {
				final String name = data == null ? "" : data;
				entity = makeNPC(name, location);
			}
			else {
				try {
					CreatureType creatureType = CreatureType.valueOf(type.toUpperCase());
					entity = world.spawnCreature(location, creatureType);
				}
				catch (IllegalArgumentException e) {
					throw new YiffBukkitCommandException("Creature type "+type+" not found", e);
				}
			}

			if (entity == null)
				throw new YiffBukkitCommandException("Failed to spawn "+type);

			if (previous == null) {
				first = entity;
			}
			else {
				net.minecraft.server.Entity notchPrevious = ((CraftEntity)previous).getHandle();
				if (notchPrevious instanceof EntityPig)
					((EntityPig)notchPrevious).a(true);

				entity.teleport(location);
				previous.setPassenger(entity);
			}

			previous = entity;
		}
		if (first == null) {
			throw new YiffBukkitCommandException("Unknown error occured while spawning entity.");
		}
		return first;
	}

	public static HumanEntity makeNPC(String name, Location location) {
		// Get some notch-type references
		final WorldServer worldServer = ((CraftWorld)location.getWorld()).getHandle();
		final MinecraftServer minecraftServer = worldServer.server;

		// Create the new player
		final EntityPlayer eply = new EntityPlayer(minecraftServer, worldServer, name, new ItemInWorldManager(worldServer));

		// Create network manager for the player
		final NetworkManager networkManager = new NetworkManager(new NPCSocket(), eply.name, null);
		// Create NetServerHandler. This will automatically write itself to the player and networkmanager
		new NetServerHandler(minecraftServer, networkManager, eply);

		// Finally, put the entity into the world.
		worldServer.addEntity(eply);

		// The entity should neither show up in the world player list...
		worldServer.players.remove(eply);

		// ...nor in the server player list (i.e. /list /who and the likes)
		minecraftServer.serverConfigurationManager.players.remove(eply);

		// finally obtain a bukkit entity,
		final HumanEntity bukkitEntity = (HumanEntity) eply.getBukkitEntity();

		// teleport it to the target location
		bukkitEntity.teleport(location);

		// and return it
		return bukkitEntity;
	}

	static class NPCSocket extends Socket {
		final OutputStream os = new ByteArrayOutputStream();
		final InputStream is = new ByteArrayInputStream(new byte[0]);

		@Override
		public OutputStream getOutputStream() throws IOException {
			return os;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return is;
		}
	}


	public static Vector toWorldAxis(Location location, Vector axis) {
		final double yaw = Math.toRadians(location.getYaw());
		final double pitch = Math.toRadians(location.getPitch());

		final double cos_y = Math.cos(yaw);
		final double sin_y = Math.sin(yaw);
		final double cos_p = Math.cos(pitch);
		final double sin_p = Math.sin(pitch);

		final Vector forward = new Vector(
				-sin_y*cos_p,
				-sin_p,
				cos_y*cos_p
		);
		final Vector up = new Vector(
				-sin_y*sin_p,
				cos_p,
				cos_y*sin_p
		);
		final Vector left = new Vector(
				cos_y,
				0,
				sin_y
		);

		return forward.multiply(axis.getX()).add(up.multiply(axis.getY())).add(left.multiply(axis.getZ()));
	}

	public static Vector toLocalAxis(Location location, Vector axis) {
		final double yaw = Math.toRadians(location.getYaw());
		final double pitch = Math.toRadians(location.getPitch());

		final double cos_y = Math.cos(yaw);
		final double sin_y = Math.sin(yaw);
		final double cos_p = Math.cos(pitch);
		final double sin_p = Math.sin(pitch);

		final Vector xAxis = new Vector(
				-sin_y*cos_p,
				-sin_y*sin_p,
				cos_y
		);

		final Vector yAxis = new Vector(
				-sin_p,
				cos_p,
				0
		);

		final Vector zAxis = new Vector(
				cos_y*cos_p,
				cos_y*sin_p,
				sin_y
		);

		return xAxis.multiply(axis.getX()).add(yAxis.multiply(axis.getY())).add(zAxis.multiply(axis.getZ()));
	}

	public static Vector toWorld(Location location, Vector position) {
		return toWorldAxis(location, position).add(location.toVector());
	}

	public static Vector toLocal(Location location, Vector position) {
		return toWorldAxis(location, position.clone().subtract(location.toVector()));
	}

	public static String readableDate(Date date) {
		if (date == null)
			return "never";

		long difference = (System.currentTimeMillis() - date.getTime()) / 1000;

		if (difference < 0)
			return date+" (in the future)";

		if (difference == 0)
			return date+" (right now)";

		final long seconds = difference % 60L;
		difference -= seconds;
		difference /= 60;
		final long minutes = difference % 60L;
		difference -= minutes;
		difference /= 60;
		final long hours = difference % 24L;
		difference -= hours;
		difference /= 24;
		final long days = difference %7L;
		difference -= days;
		difference /= 7;
		final long weeks = difference;

		String ago = "ago)";
		if (seconds > 0)
			ago = seconds+"s "+ago;
		if (minutes > 0)
			ago = minutes+"m "+ago;
		if (hours > 0)
			ago = hours+"h "+ago;
		if (days > 0)
			ago = days+"d "+ago;
		if (weeks > 0)
			ago = weeks+"w "+ago;

		return date+" ("+ago;
	}
}
