package net.earthcomputer.lightningtool;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MobList {

	private MobList() {
	}

	public static final String WITHER_SKELETON = "WitherSkeleton";
	public static final String STRAY = "Stray";
	public static final String HUSK = "Husk";
	public static final String ZOMBIE_VILLAGER = "ZombieVillager";
	public static final String CREEPER = "Creeper";
	public static final String SKELETON = "Skeleton";
	public static final String SPIDER = "Spider";
	public static final String ZOMBIE = "Zombie";
	public static final String SLIME = "Slime";
	public static final String GHAST = "Ghast";
	public static final String ZOMBIE_PIGMAN = "ZombiePigman";
	public static final String ENDERMAN = "Enderman";
	public static final String BLAZE = "Blaze";
	public static final String MAGMA_CUBE = "MagmaCube";
	public static final String WITCH = "Witch";
	public static final String GUARDIAN = "Guardian";
	public static final String OCELOT = "Ocelot";

	public static final String DONKEY = "Donkey";
	public static final String PIG = "Pig";
	public static final String SHEEP = "Sheep";
	public static final String COW = "Cow";
	public static final String CHICKEN = "Chicken";
	public static final String WOLF = "Wolf";
	public static final String MOOSHROOM = "Mooshroom";
	public static final String HORSE = "Horse";
	public static final String RABBIT = "Rabbit";
	public static final String POLAR_BEAR = "PolarBear";
	public static final String LLAMA = "Llama";
	public static final String PARROT = "Parrot";

	private static final Set<String> VALID_MOBS = new HashSet<>();
	private static final String[] VALID_MOBS_ARRAY;

	static {
		for (Field field : MobList.class.getDeclaredFields()) {
			if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())
					&& Modifier.isFinal(field.getModifiers())) {
				if (field.getType() == String.class) {
					String value;
					try {
						value = (String) field.get(null);
					} catch (Exception e) {
						throw new AssertionError(e);
					}
					VALID_MOBS.add(value);
				}
			}
		}

		VALID_MOBS_ARRAY = VALID_MOBS.toArray(new String[0]);
		Arrays.sort(VALID_MOBS_ARRAY);
	}

	public static boolean isValidMob(String mob) {
		return VALID_MOBS.contains(mob);
	}

	public static String[] getValidMobsArray() {
		return VALID_MOBS_ARRAY;
	}

	public static class SpawnEntry {
		private String mobType;
		private int weight;
		private int groupMin;
		private int groupMax;

		public SpawnEntry(String mobType, int weight, int groupMin, int groupMax) {
			this.mobType = mobType;
			this.weight = weight;
			this.groupMin = groupMin;
			this.groupMax = groupMax;
		}

		public String getMobType() {
			return mobType;
		}

		public int getWeight() {
			return weight;
		}

		public int getGroupMin() {
			return groupMin;
		}

		public int getGroupMax() {
			return groupMax;
		}
	}

	public static enum BiomeType {
		// @formatter:off
		NORMAL,
		BEACH,
		DESERT,
		END,
		FLOWER_FOREST,
		FOREST,
		NETHER,
		EXTREME_HILLS,
		JUNGLE,
		MESA,
		MUSHROOM_ISLAND,
		OCEAN,
		PLAINS,
		RIVER,
		SAVANNA,
		SAVANNA_PLATEAU,
		SNOW,
		SWAMP,
		TAIGA,
		WITCH_HUT,
		MONUMENT,
		FORTRESS;
		// @formatter:on

		private List<SpawnEntry> hostileSpawnEntries = new ArrayList<>();
		private List<SpawnEntry> passiveSpawnEntries = new ArrayList<>();

		private BiomeType() {
			addHostileSpawnEntry(SPIDER, 100, 4, 4);
			addHostileSpawnEntry(ZOMBIE, 95, 4, 4);
			addHostileSpawnEntry(ZOMBIE_VILLAGER, 5, 1, 1);
			addHostileSpawnEntry(SKELETON, 100, 4, 4);
			addHostileSpawnEntry(CREEPER, 100, 4, 4);
			addHostileSpawnEntry(SLIME, 100, 4, 4);
			addHostileSpawnEntry(ENDERMAN, 10, 1, 4);
			addHostileSpawnEntry(WITCH, 5, 1, 4);

			addPassiveSpawnEntry(SHEEP, 12, 4, 4);
			addPassiveSpawnEntry(PIG, 10, 4, 4);
			addPassiveSpawnEntry(CHICKEN, 10, 4, 4);
			addPassiveSpawnEntry(COW, 8, 4, 4);
		}

		public List<SpawnEntry> getHostileSpawnEntries() {
			return hostileSpawnEntries;
		}

		private void addHostileSpawnEntry(String mobType, int weight, int groupMin, int groupMax) {
			hostileSpawnEntries.add(new SpawnEntry(mobType, weight, groupMin, groupMax));
		}

		private void removeHostileSpawnEntry(String mobType) {
			Iterator<SpawnEntry> itr = hostileSpawnEntries.iterator();
			while (itr.hasNext()) {
				if (itr.next().mobType.equals(mobType)) {
					itr.remove();
				}
			}
		}

		public List<SpawnEntry> getPassiveSpawnEntries() {
			return passiveSpawnEntries;
		}

		private void addPassiveSpawnEntry(String mobType, int weight, int groupMin, int groupMax) {
			passiveSpawnEntries.add(new SpawnEntry(mobType, weight, groupMin, groupMax));
		}

		static {
			BEACH.passiveSpawnEntries.clear();

			DESERT.removeHostileSpawnEntry(ZOMBIE);
			DESERT.removeHostileSpawnEntry(ZOMBIE_VILLAGER);
			DESERT.addHostileSpawnEntry(ZOMBIE, 19, 4, 4);
			DESERT.addHostileSpawnEntry(ZOMBIE_VILLAGER, 1, 4, 4);
			DESERT.addHostileSpawnEntry(HUSK, 80, 4, 4);
			DESERT.passiveSpawnEntries.clear();
			DESERT.addPassiveSpawnEntry(RABBIT, 4, 2, 3);

			END.hostileSpawnEntries.clear();
			END.addHostileSpawnEntry(ENDERMAN, 10, 4, 4);
			END.passiveSpawnEntries.clear();

			FLOWER_FOREST.addPassiveSpawnEntry(RABBIT, 4, 2, 3);

			FOREST.addPassiveSpawnEntry(WOLF, 5, 4, 4);

			NETHER.hostileSpawnEntries.clear();
			NETHER.addHostileSpawnEntry(GHAST, 50, 4, 4);
			NETHER.addHostileSpawnEntry(ZOMBIE_PIGMAN, 100, 4, 4);
			NETHER.addHostileSpawnEntry(MAGMA_CUBE, 2, 4, 4);
			NETHER.addHostileSpawnEntry(ENDERMAN, 1, 4, 4);
			NETHER.passiveSpawnEntries.clear();

			EXTREME_HILLS.addPassiveSpawnEntry(LLAMA, 5, 4, 6);

			JUNGLE.addHostileSpawnEntry(OCELOT, 2, 1, 1);
			JUNGLE.addPassiveSpawnEntry(PARROT, 40, 1, 2);
			JUNGLE.addPassiveSpawnEntry(CHICKEN, 10, 4, 4);

			MESA.passiveSpawnEntries.clear();

			MUSHROOM_ISLAND.hostileSpawnEntries.clear();
			MUSHROOM_ISLAND.passiveSpawnEntries.clear();
			MUSHROOM_ISLAND.addPassiveSpawnEntry(MOOSHROOM, 8, 4, 8);

			OCEAN.passiveSpawnEntries.clear();

			PLAINS.addPassiveSpawnEntry(HORSE, 5, 2, 6);
			PLAINS.addPassiveSpawnEntry(DONKEY, 1, 1, 3);

			RIVER.passiveSpawnEntries.clear();

			SAVANNA.addPassiveSpawnEntry(HORSE, 1, 2, 6);
			SAVANNA.addPassiveSpawnEntry(DONKEY, 1, 1, 1);

			SAVANNA_PLATEAU.addPassiveSpawnEntry(HORSE, 1, 2, 6);
			SAVANNA_PLATEAU.addPassiveSpawnEntry(DONKEY, 1, 1, 1);
			SAVANNA_PLATEAU.addPassiveSpawnEntry(LLAMA, 8, 4, 4);

			SNOW.removeHostileSpawnEntry(SKELETON);
			SNOW.addHostileSpawnEntry(SKELETON, 20, 4, 4);
			SNOW.addHostileSpawnEntry(STRAY, 80, 4, 4);
			SNOW.passiveSpawnEntries.clear();
			SNOW.addPassiveSpawnEntry(RABBIT, 10, 2, 3);
			SNOW.addPassiveSpawnEntry(POLAR_BEAR, 1, 1, 2);

			SWAMP.addHostileSpawnEntry(SLIME, 1, 1, 1);

			TAIGA.addPassiveSpawnEntry(WOLF, 8, 4, 4);
			TAIGA.addPassiveSpawnEntry(RABBIT, 4, 2, 3);

			WITCH_HUT.hostileSpawnEntries.clear();
			WITCH_HUT.addHostileSpawnEntry(WITCH, 1, 1, 1);

			MONUMENT.hostileSpawnEntries.clear();
			MONUMENT.addHostileSpawnEntry(GUARDIAN, 1, 2, 4);

			FORTRESS.hostileSpawnEntries.clear();
			FORTRESS.addHostileSpawnEntry(BLAZE, 10, 2, 3);
			FORTRESS.addHostileSpawnEntry(ZOMBIE_PIGMAN, 5, 4, 4);
			FORTRESS.addHostileSpawnEntry(WITHER_SKELETON, 8, 5, 5);
			FORTRESS.addHostileSpawnEntry(SKELETON, 2, 5, 5);
			FORTRESS.addHostileSpawnEntry(MAGMA_CUBE, 3, 4, 4);
		}
	}

}
