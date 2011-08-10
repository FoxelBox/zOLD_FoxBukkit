package de.doridian.yiffbukkit.transmute;

import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.server.DataWatcher;
import net.minecraft.server.EntityTypes;
import net.minecraft.server.Packet20NamedEntitySpawn;
import net.minecraft.server.Packet29DestroyEntity;
import net.minecraft.server.Packet40EntityMetadata;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import de.doridian.yiffbukkit.YiffBukkitCommandException;
import de.doridian.yiffbukkit.util.Utils;

public abstract class Shape {
	final protected Transmute transmute;
	final protected int entityID;
	final protected Player player;
	final protected Entity entity;
	protected DataWatcher datawatcher;

	protected Shape(Transmute transmute, Player player, Entity entity) {
		this.transmute = transmute;
		this.player = player;
		this.entity = entity;
		entityID = entity.getEntityId();
		datawatcher = new DataWatcher();
	}

	public void deleteEntity() {
		if (entity instanceof Player)
			transmute.plugin.playerHelper.sendPacketToPlayersAround(entity.getLocation(), 1024, new Packet29DestroyEntity(entity.getEntityId()), (Player) entity);
		else
			transmute.plugin.playerHelper.sendPacketToPlayersAround(entity.getLocation(), 1024, new Packet29DestroyEntity(entity.getEntityId()));
	}

	public void createOriginalEntity() {
		if (entity instanceof Player)
			transmute.plugin.playerHelper.sendPacketToPlayersAround(entity.getLocation(), 1024, transmute.ignorePacket(createPlayerSpawnPacket()), (Player) entity);
		else
			transmute.plugin.playerHelper.sendPacketToPlayersAround(entity.getLocation(), 1024, transmute.ignorePacket(createPlayerSpawnPacket()));
	}

	private Packet20NamedEntitySpawn createPlayerSpawnPacket() {
		return new Packet20NamedEntitySpawn(((CraftPlayer)entity).getHandle());
	}

	public byte getDataByte(int index) {
		try {
			return datawatcher.a(index);
		}
		catch (NullPointerException e) {
			return 0;
		}
	}

	public int getDataInteger(int index) {
		try {
			return datawatcher.b(index);
		}
		catch (NullPointerException e) {
			return 0;
		}
	}

	public String getDataString(int index) {
		try {
			return datawatcher.c(index);
		}
		catch (NullPointerException e) {
			return null;
		}

	}

	public void setData(int index, Object value) {
		Packet40EntityMetadata p40 = createMetadataPacket(index, value);

		transmute.plugin.playerHelper.sendPacketToPlayersAround(entity.getLocation(), 1024, p40, player);
	}

	private Packet40EntityMetadata createMetadataPacket(int index, Object value) {
		try {
			// create entry
			datawatcher.a(index, value.getClass().getConstructor(String.class).newInstance("0"));

			// mark dirty
			datawatcher.watch(index, value.getClass().getConstructor(String.class).newInstance("1"));
		}
		catch (Exception e) { }

		// put the actual data in
		datawatcher.watch(index, value);

		return new Packet40EntityMetadata(entityID, datawatcher);
	}

	abstract public void createTransmutedEntity();
	abstract public void createTransmutedEntity(Player forPlayer);

	abstract public void runAction(String action) throws YiffBukkitCommandException;

	public static Shape getShape(Transmute transmute, Player player, Entity entity, String mobType) {
		return getShape(transmute, player, entity, typeNameToClass(mobType));
	}

	public static Shape getShape(Transmute transmute, Player player, Entity entity, Class<? extends net.minecraft.server.Entity> mobType) {
		return getShape(transmute, player, entity, classToId(mobType));
	}

	public static Shape getShape(Transmute transmute, Player player, Entity entity, int mobType) {
		switch (mobType) {
		case 49:
		case 50:
		case 51:
		case 52:
		case 53:
		case 54:
		case 55:
		case 56:
		case 57:
		case 90:
		case 91:
		case 92:
		case 93:
		case 94:
		case 95:
			return getShapeImpl(transmute, player, entity, mobType, MobShape.class);
		default:
			throw new RuntimeException("Invalid shape.");
		}
	}
	
	private static Shape getShapeImpl(Transmute transmute, Player player, Entity entity, int mobType, Class<? extends Shape> shapeClass) {
		try {
			return shapeClass.getConstructor(Transmute.class, Player.class, Entity.class, int.class).newInstance(transmute, player, entity, mobType);
		} catch (Exception e) {
			throw new RuntimeException("Error instantiating shape.", e);
		}
	}
	
	private static final Class<? extends net.minecraft.server.Entity> typeNameToClass(String mobType) {
		Map<String, Class<? extends net.minecraft.server.Entity>> typeNameToClass = Utils.getPrivateValue(EntityTypes.class, null, "a");

		for (Entry<String, Class<? extends net.minecraft.server.Entity>> entry : typeNameToClass.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(mobType))
				return entry.getValue();
		}

		return null;
		//return typeNameToClass.get(mobType);
	}
	private static final int classToId(Class<? extends net.minecraft.server.Entity> mobType) {
		Map<Class<? extends net.minecraft.server.Entity>, Integer> classToId = Utils.getPrivateValue(EntityTypes.class, null, "d");

		return classToId.get(mobType);
	}

}
