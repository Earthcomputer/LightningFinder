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
		DESERT,
		END,
		NETHER,
		JUNGLE,
		SNOW,
		SWAMP,
		WITCH_HUT,
		MONUMENT,
		FORTRESS;
		// @formatter:on

		private List<SpawnEntry> spawnEntries = new ArrayList<>();

		private BiomeType() {
			addSpawnEntry(SPIDER, 100, 4, 4);
			addSpawnEntry(ZOMBIE, 95, 4, 4);
			addSpawnEntry(ZOMBIE_VILLAGER, 5, 1, 1);
			addSpawnEntry(SKELETON, 100, 4, 4);
			addSpawnEntry(CREEPER, 100, 4, 4);
			addSpawnEntry(SLIME, 100, 4, 4);
			addSpawnEntry(ENDERMAN, 10, 1, 4);
			addSpawnEntry(WITCH, 5, 1, 4);
		}
		
		public List<SpawnEntry> getSpawnEntries() {
			return spawnEntries;
		}

		private void addSpawnEntry(String mobType, int weight, int groupMin, int groupMax) {
			spawnEntries.add(new SpawnEntry(mobType, weight, groupMin, groupMax));
		}

		private void removeSpawnEntry(String mobType) {
			Iterator<SpawnEntry> itr = spawnEntries.iterator();
			while (itr.hasNext()) {
				if (itr.next().mobType.equals(mobType)) {
					itr.remove();
				}
			}
		}

		static {
			DESERT.removeSpawnEntry(ZOMBIE);
			DESERT.removeSpawnEntry(ZOMBIE_VILLAGER);
			DESERT.addSpawnEntry(ZOMBIE, 19, 4, 4);
			DESERT.addSpawnEntry(ZOMBIE_VILLAGER, 1, 4, 4);
			DESERT.addSpawnEntry(HUSK, 80, 4, 4);

			END.spawnEntries.clear();
			END.addSpawnEntry(ENDERMAN, 10, 4, 4);
			
			NETHER.spawnEntries.clear();
			NETHER.addSpawnEntry(GHAST, 50, 4, 4);
			NETHER.addSpawnEntry(ZOMBIE_PIGMAN, 100, 4, 4);
			NETHER.addSpawnEntry(MAGMA_CUBE, 2, 4, 4);
			NETHER.addSpawnEntry(ENDERMAN, 1, 4, 4);
			
			JUNGLE.addSpawnEntry(OCELOT, 2, 1, 1);
			
			SNOW.removeSpawnEntry(SKELETON);
			SNOW.addSpawnEntry(SKELETON, 20, 4, 4);
			SNOW.addSpawnEntry(STRAY, 80, 4, 4);
			
			SWAMP.addSpawnEntry(SLIME, 1, 1, 1);
			
			WITCH_HUT.spawnEntries.clear();
			WITCH_HUT.addSpawnEntry(WITCH, 1, 1, 1);
			
			MONUMENT.spawnEntries.clear();
			MONUMENT.addSpawnEntry(GUARDIAN, 1, 2, 4);
			
			FORTRESS.spawnEntries.clear();
			FORTRESS.addSpawnEntry(BLAZE, 10, 2, 3);
			FORTRESS.addSpawnEntry(ZOMBIE_PIGMAN, 5, 4, 4);
			FORTRESS.addSpawnEntry(WITHER_SKELETON, 8, 5, 5);
			FORTRESS.addSpawnEntry(SKELETON, 2, 5, 5);
			FORTRESS.addSpawnEntry(MAGMA_CUBE, 3, 4, 4);
		}
	}

}
