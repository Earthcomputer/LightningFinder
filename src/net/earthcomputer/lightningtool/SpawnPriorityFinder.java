package net.earthcomputer.lightningtool;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JTable;

public class SpawnPriorityFinder {

	private SpawnPriorityFinder() {
	}

	public static void calculate(MainFrame frame) {
		List<ChunkPos> playerPositions = new ArrayList<>();
		JTable table = frame.getSpawnPriorityPlayerTable();
		for (int i = 0; i < table.getRowCount(); i++) {
			Object x = table.getValueAt(i, 1);
			Object z = table.getValueAt(i, 2);
			if (x == null || z == null) {
				frame.getLblOutput().setForeground(Color.RED);
				frame.getLblOutput().setText("Incomplete player position table");
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

		frame.getOutputTextArea().setText("");

		Set<ChunkPos> eligibleChunks = new HashSet<>();
		for (ChunkPos player : playerPositions) {
			for (int dx = -7; dx <= 7; dx++) {
				for (int dz = -7; dz <= 7; dz++) {
					eligibleChunks.add(new ChunkPos(player.x + dx, player.z + dz));
				}
			}
		}

		ChunkPos firstPos = eligibleChunks.iterator().next();
		String output = "First chunk = " + firstPos;
		output += ", aka (" + (firstPos.x * 16) + ", " + (firstPos.z * 16) + ") to (" + (firstPos.x * 16 + 15) + ", "
				+ (firstPos.z * 16 + 15) + ")";

		frame.getLblOutput().setForeground(Color.BLACK);
		frame.getLblOutput().setText(output);
		frame.getOutputTextArea().append(output);
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
