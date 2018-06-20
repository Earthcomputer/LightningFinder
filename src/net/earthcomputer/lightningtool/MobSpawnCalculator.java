package net.earthcomputer.lightningtool;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class MobSpawnCalculator {

	private MobSpawnCalculator() {
	}

	private static final int MOB_TYPE_HOSTILE = 0;
	private static final int MOB_TYPE_PASSIVE = 1;
	//private static final int MOB_TYPE_BAT = 2;
	//private static final int MOB_TYPE_SQUID = 3;
	private static final int MOB_TYPE_COUNT = 4;

	private static final int[] MOB_Y_HEIGHT_COLUMN_INDEXES = { 4, 9, 13, 17 };

	public static final RNGAdvancer<?>[] ADVANCERS = { /* RNGAdvancer.HOPPER */ }; // TODO: implement advancers for this

	public static void recalculate(MainFrame frame, boolean eraseTable, boolean ignoreErrors) {
		if (!ignoreErrors) {
			frame.getLblOutput().setForeground(Color.BLACK);
			frame.getLblOutput().setText("Output appears here");
		}

		long worldSeed = AbstractManipulator.parseWorldSeed(frame.getWorldSeedTextField().getText());
		int regionX, regionZ;
		try {
			regionX = Integer.parseInt(frame.getSearchFromXTextField().getText());
			regionZ = Integer.parseInt(frame.getSearchFromZTextField().getText());
		} catch (NumberFormatException e) {
			if (!ignoreErrors)
				setErrorMessage(frame, "Invalid \"from\" coordinates");
			return;
		}
		if (regionX < 0)
			regionX -= 80 * 16 - 1;
		if (regionZ < 0)
			regionZ -= 80 * 16 - 1;
		regionX /= 80 * 16;
		regionZ /= 80 * 16;

		// Read player positions
		List<ChunkPos> playerPositions = new ArrayList<>();
		JTable table = frame.getSpawnPriorityPlayerTable();
		for (int i = 0; i < table.getRowCount(); i++) {
			Object x = table.getValueAt(i, 1);
			Object z = table.getValueAt(i, 2);
			if (x == null || z == null) {
				if (!ignoreErrors)
					setErrorMessage(frame, "Incomplete player position table");
				return;
			}

			int ix = (Integer) x;
			int iz = (Integer) z;
			if (ix < 0)
				ix -= 15;
			if (iz < 0)
				iz -= 15;
			ix /= 16;
			iz /= 16;

			playerPositions.add(new ChunkPos(ix, iz));
		}

		table = frame.getMobChunksTable();
		DefaultTableModel model = (DefaultTableModel) table.getModel();

		MobList.BiomeType biome = (MobList.BiomeType) frame.getBiomeTypeComboBox().getSelectedItem();

		// Enumerate eligible chunks
		Set<ChunkPos> eligibleChunks = new HashSet<>();
		for (ChunkPos player : playerPositions) {
			for (int dx = -7; dx <= 7; dx++) {
				for (int dz = -7; dz <= 7; dz++) {
					eligibleChunks.add(new ChunkPos(player.x + dx, player.z + dz));
				}
			}
		}

		// Read y-height values
		@SuppressWarnings("unchecked")
		List<Integer>[] yHeights = new List[MOB_TYPE_COUNT];
		for (int i = 0; i < MOB_TYPE_COUNT; i++) {
			yHeights[i] = new ArrayList<>();
		}
		if (!eraseTable) {
			for (int chunk = 0; chunk < table.getRowCount(); chunk++) {
				for (int mobType = 0; mobType < MOB_TYPE_COUNT; mobType++) {
					Integer n = (Integer) table.getValueAt(chunk, MOB_Y_HEIGHT_COLUMN_INDEXES[mobType]);
					if (n == null) {
						n = -1;
					}
					yHeights[mobType].add(n);
				}
			}
		} else {
			// Erase table
			for (int i = table.getRowCount() - 1; i >= 0; i--) {
				model.removeRow(i);
			}
		}

		// Reset the seed
		ResettableRandom rand = new ResettableRandom();
		AbstractManipulator.resetSeed(rand, regionX, regionZ, worldSeed);

		// Simulate the spawning algorithm
		for (int mobType = 0; mobType < MOB_TYPE_COUNT; mobType++) {
			// Reset the seed after passive because passive only happens occasionally
			if (mobType == MOB_TYPE_PASSIVE) {
				rand.saveState();
			} else if (mobType == MOB_TYPE_PASSIVE + 1) {
				rand.restoreState();
			}

			Iterator<ChunkPos> eligibleChunksItr = eligibleChunks.iterator();
			int chunkNo = 0;
			while (eligibleChunksItr.hasNext()) {
				// Add row in table if it does not exist
				ChunkPos chunk = eligibleChunksItr.next();
				if (chunkNo >= table.getRowCount()) {
					model.addRow(new Object[table.getColumnCount()]);
					model.setValueAt(chunkNo + 1, chunkNo, 0);
					model.setValueAt(chunk.getX(), chunkNo, 1);
					model.setValueAt(chunk.getZ(), chunkNo, 2);
				}

				// Choose spawn location
				int xPos = chunk.getX() * 16 + rand.nextInt(16);
				int yHeight = chunkNo >= yHeights[mobType].size() ? -1 : yHeights[mobType].get(chunkNo);
				if (yHeight <= 0) {
					yHeight = 63;
					model.setValueAt(yHeight, chunkNo, MOB_Y_HEIGHT_COLUMN_INDEXES[mobType]);
				}
				int yPos = rand.nextInt(yHeight + 1);
				int zPos = chunk.getZ() * 16 + rand.nextInt(16);

				rand.saveState();

				xPos += rand.nextInt(6) - rand.nextInt(6);
				yPos += rand.nextInt(1) - rand.nextInt(1);
				zPos += rand.nextInt(6) - rand.nextInt(6);

				// Choose mob type
				boolean willSpawnMob = true;

				if (mobType == MOB_TYPE_HOSTILE || mobType == MOB_TYPE_PASSIVE) {
					List<MobList.SpawnEntry> spawnEntries;
					if (mobType == MOB_TYPE_HOSTILE)
						spawnEntries = biome.getHostileSpawnEntries();
					else
						spawnEntries = biome.getPassiveSpawnEntries();
					int totalWeight = spawnEntries.stream().mapToInt(MobList.SpawnEntry::getWeight).sum();
					MobList.SpawnEntry spawnEntry = null;
					if (totalWeight != 0) {
						int weight = rand.nextInt(totalWeight);
						for (MobList.SpawnEntry entry : spawnEntries) {
							weight -= entry.getWeight();
							if (weight < 0) {
								spawnEntry = entry;
								break;
							}
						}
					}
					String mobClass;
					if (spawnEntry == null) {
						mobClass = null;
						willSpawnMob = false;
					} else {
						mobClass = spawnEntry.getMobType();
					}
					model.setValueAt(mobClass, chunkNo, MOB_Y_HEIGHT_COLUMN_INDEXES[mobType] - 1);
				} else {
					rand.nextInt(); // A weighted list of 1 element
				}
				rand.restoreState();

				// Write position to table
				if (willSpawnMob) {
					model.setValueAt(xPos, chunkNo, MOB_Y_HEIGHT_COLUMN_INDEXES[mobType] + 1);
					model.setValueAt(yPos, chunkNo, MOB_Y_HEIGHT_COLUMN_INDEXES[mobType] + 2);
					model.setValueAt(zPos, chunkNo, MOB_Y_HEIGHT_COLUMN_INDEXES[mobType] + 3);
				} else {
					model.setValueAt(null, chunkNo, MOB_Y_HEIGHT_COLUMN_INDEXES[mobType] + 1);
					model.setValueAt(null, chunkNo, MOB_Y_HEIGHT_COLUMN_INDEXES[mobType] + 2);
					model.setValueAt(null, chunkNo, MOB_Y_HEIGHT_COLUMN_INDEXES[mobType] + 3);
				}

				chunkNo++;
			}
		}
	}

	private static void setErrorMessage(MainFrame frame, String message) {
		frame.getLblOutput().setForeground(Color.RED);
		frame.getLblOutput().setText(message);
	}

	public static class ChunkPos {
		private int x;
		private int z;

		public ChunkPos(int x, int z) {
			this.x = x;
			this.z = z;
		}

		public int getX() {
			return x;
		}

		public int getZ() {
			return z;
		}

		@Override
		public int hashCode() {
			int scrambledX = 1664525 * x + 1013904223;
			int scrambledZ = 1664525 * (z ^ 0xdeadbeef) + 1013904223;
			return scrambledX ^ scrambledZ;
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof ChunkPos && equals((ChunkPos) other);
		}

		public boolean equals(ChunkPos other) {
			return x == other.x && z == other.z;
		}

		@Override
		public String toString() {
			return "(" + x + ", " + z + ")";
		}
	}
}
