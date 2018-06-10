package net.earthcomputer.lightningtool;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoublePredicate;

import net.earthcomputer.lightningtool.SearchResult.Property;

public class LightningManipulator extends AbstractManipulator {

	private DoublePredicate trapValuePredicate;
	private int chunkCount;

	public static final RNGAdvancer<?>[] ADVANCERS = { RNGAdvancer.DISPENSER, RNGAdvancer.LAVA_LIGHTNING };

	public static final Property<Integer> CHUNK_INDEX = Property.create("chunk index", 0, Integer.MAX_VALUE,
			Property.minimize());
	public static final Property<Double> TRAP_VALUE = Property.create("trap value", Double.MAX_VALUE, Double.MAX_VALUE,
			Property.indifferent());

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
	protected <P extends RNGAdvancer.ParameterHandler> void testRegionWithAdvancer(int x, int z,
			Consumer<SearchResult> resultConsumer) {
		for (int i = 0; i < 4; i++)
			rand.nextInt();

		if (advancer instanceof RNGAdvancer.IPlayerChunkMapAware) {
			((RNGAdvancer<P>) advancer).search(rand, (P) advancerParameterHandler, rand -> testForLightning(),
					resultConsumer);
		} else {
			super.testRegionWithAdvancer(x, z, resultConsumer);
		}
	}

	@Override
	protected SearchResult testRegion(int x, int z) {
		for (int chunkIdx = 0; chunkIdx < chunkCount; chunkIdx++) {
			SearchResult result = testForLightning();
			if (result != null) {
				return result.withProperty(CHUNK_INDEX, chunkIdx);
			}
			rand.nextInt();
		}

		return null;
	}

	private SearchResult testForLightning() {
		int lightningValue = rand.nextInt(100000);
		if (lightningValue == 0) {
			rand.nextInt();
			double trapValue = rand.nextDouble();
			if (trapValuePredicate.test(trapValue)) {
				return createSearchResult().withProperty(TRAP_VALUE, trapValue);
			}
		}
		return null;
	}

	@Override
	protected SearchResult createSearchResult() {
		List<Property<?>> properties = new ArrayList<>();
		properties.add(DISTANCE);
		if (!(advancer instanceof RNGAdvancer.IPlayerChunkMapAware))
			properties.add(CHUNK_INDEX);
		properties.add(TRAP_VALUE);
		advancer.addExtraProperties(properties);
		return new SearchResult(properties);
	}

}
