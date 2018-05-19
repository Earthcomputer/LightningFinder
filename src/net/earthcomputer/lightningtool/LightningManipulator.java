package net.earthcomputer.lightningtool;

import java.util.Optional;
import java.util.function.DoublePredicate;

public class LightningManipulator extends AbstractManipulator {

	private DoublePredicate trapValuePredicate;
	private int chunkCount;

	public static final RNGAdvancer<?>[] ADVANCERS = { RNGAdvancer.DISPENSER, RNGAdvancer.LAVA_LIGHTNING };

	@Override
	protected boolean parseExtra() {
		if (frame.getRdbtnChargedCreepers().isSelected()) {
			trapValuePredicate = d -> d >= 0.0675;
		} else if (frame.getRdbtnHorseTraps().isSelected()) {
			trapValuePredicate = d -> d < 0.06;
		} else {
			trapValuePredicate = d -> true;
		}

		int viewDistance;
		double playerX, playerZ;
		if (advancerParameterHandler instanceof RNGAdvancer.IViewDistanceParameterHandler) {
			RNGAdvancer.IViewDistanceParameterHandler viewDistanceHandler = (RNGAdvancer.IViewDistanceParameterHandler) advancerParameterHandler;
			viewDistance = viewDistanceHandler.getViewDistance();
			playerX = viewDistanceHandler.getPlayerXInChunk();
			playerZ = viewDistanceHandler.getPlayerZInChunk();
		} else {
			viewDistance = 12;
			playerX = 8;
			playerZ = 8;
		}

		chunkCount = 0;
		for (int x = -viewDistance; x <= viewDistance; x++) {
			for (int z = -viewDistance; z <= viewDistance; z++) {
				double dx = (x * 16 + 8) - playerX;
				double dz = (z * 16 + 8) - playerZ;
				if (dx * dx + dz * dz <= 128 * 128) {
					chunkCount++;
				}
			}
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <P extends RNGAdvancer.ParameterHandler> Optional<String> testRegionWithAdvancer(int x, int z) {
		for (int i = 0; i < 4; i++)
			rand.nextInt();

		if (advancer instanceof RNGAdvancer.IPlayerChunkMapAware) {
			return ((RNGAdvancer<P>) advancer).search(rand, (P) advancerParameterHandler, rand -> testForLightning());
		} else {
			return super.testRegionWithAdvancer(x, z);
		}
	}

	@Override
	protected Optional<String> testRegion(int x, int z) {
		for (int i = 0; i < 4; i++)
			rand.nextInt();

		for (int chunkIdx = 0; chunkIdx < chunkCount; chunkIdx++) {
			Optional<String> result = testForLightning();
			if (result.isPresent()) {
				return Optional.of(result.get() + ", chunkIdx = " + chunkIdx);
			}
			rand.nextInt();
		}

		return Optional.empty();
	}

	private Optional<String> testForLightning() {
		int lightningValue = rand.nextInt(100000);
		if (lightningValue == 0) {
			double trapValue = rand.nextDouble();
			if (trapValuePredicate.test(trapValue)) {
				return Optional.of(String.format("trap value = %f", trapValue));
			}
		}
		return Optional.empty();
	}

	@Override
	protected String getRegionSeparator(int newX, int newZ) {
		return "------ REGION (" + newX + ", " + newZ + ") ------";
	}

}
