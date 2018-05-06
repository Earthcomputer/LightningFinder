package net.earthcomputer.lightningtool;

import java.util.List;
import java.util.Optional;

public class MobSpawningManipulator extends AbstractManipulator {

	private String mobType;
	private MobList.BiomeType biomeType;
	private int topBlock;

	private int totalFound = 0;

	@Override
	protected boolean parseExtra() {
		mobType = (String) frame.getMobTypeComboBox().getModel().getSelectedItem();
		if (!MobList.isValidMob(mobType)) {
			setErrorMessage("Invalid mob type");
			return false;
		}

		biomeType = (MobList.BiomeType) frame.getBiomeTypeComboBox().getModel().getSelectedItem();
		if (biomeType == null) {
			setErrorMessage("Invalid biome type");
			return false;
		}
		
		try {
			topBlock = Integer.parseInt(frame.getTopBlockTextField().getText());
			if (topBlock < 1 || topBlock > 255)
				throw new NumberFormatException();
		} catch (NumberFormatException e) {
			setErrorMessage("Top block invalid");
			return false;
		}

		return true;
	}

	@Override
	protected Optional<String> testRegion(int x, int z) {
		// Choose position
		int spawnX = rand.nextInt(16);
		int spawnY = rand.nextInt(topBlock);
		int spawnZ = rand.nextInt(16);

		// There's a couple of for loops here, but we only go for the first attempt since that's the only one that's guaranteed

		spawnX += rand.nextInt(6) - rand.nextInt(6);
		spawnY += rand.nextInt(1) - rand.nextInt(1);
		spawnZ += rand.nextInt(6) - rand.nextInt(6);

		// Choose which mob to spawn
		List<MobList.SpawnEntry> spawnEntries = biomeType.getSpawnEntries();
		int totalWeight = spawnEntries.stream().mapToInt(MobList.SpawnEntry::getWeight).sum();
		int weight = rand.nextInt(totalWeight);
		MobList.SpawnEntry spawnEntry = null;
		for (MobList.SpawnEntry entry : spawnEntries) {
			weight -= entry.getWeight();
			if (weight < 0) {
				spawnEntry = entry;
				break;
			}
		}
		assert spawnEntry != null;

		if (!spawnEntry.getMobType().equals(mobType)) {
			return Optional.empty();
		}

		totalFound++;
		if (totalFound == 10)
			stop();

		return Optional.of(String.format("pos = (%d, %d, %d)", spawnX, spawnY, spawnZ));
	}

}
