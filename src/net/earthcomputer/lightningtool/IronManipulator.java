package net.earthcomputer.lightningtool;

import java.util.Optional;

public class IronManipulator extends AbstractManipulator {

	private int playerChunks;

	@Override
	protected boolean parseExtra() {
		int viewDistance;
		try {
			viewDistance = Integer.parseInt(frame.getIronViewDistanceTextField().getText());
			if (viewDistance < 2 || viewDistance > 32)
				throw new NumberFormatException();
		} catch (NumberFormatException e) {
			setErrorMessage("View distance invalid");
			return false;
		}

		double playerX, playerZ;
		try {
			playerX = Double.parseDouble(frame.getPlayerXInChunkTextField().getText());
			if (playerX < 0 || playerX >= 16)
				throw new NumberFormatException();
			playerZ = Double.parseDouble(frame.getPlayerZInChunkTextField().getText());
			if (playerZ < 0 || playerZ >= 16)
				throw new NumberFormatException();
		} catch (NumberFormatException e) {
			setErrorMessage("Player pos in chunk invalid");
			return false;
		}

		playerChunks = 0;
		for (int x = -viewDistance; x <= viewDistance; x++) {
			for (int z = -viewDistance; z <= viewDistance; z++) {
				double dx = (x * 16 + 8) - playerX;
				double dz = (z * 16 + 8) - playerZ;
				if (dx * dx + dz * dz < 128 * 128)
					playerChunks++;
			}
		}

		return true;
	}

	@Override
	protected Optional<String> testRegion(int x, int z) {
		for (int i = 0; i < 4; i++)
			rand.nextInt();

		for (int i = 0; i < playerChunks; i++)
			rand.nextInt();

		rand.nextInt(50);

		if (rand.nextInt(7000) == 0) {
			stop();
			int golemX = rand.nextInt(16) - 8;
			int golemY = rand.nextInt(6) - 3;
			int golemZ = rand.nextInt(16) - 8;
			return Optional.of(String.format("golem pos = (%d, %d, %d)", golemX, golemY, golemZ));
		}

		return Optional.empty();
	}

}
