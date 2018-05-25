package net.earthcomputer.lightningtool;

import java.util.ArrayList;
import java.util.List;

import net.earthcomputer.lightningtool.SearchResult.Property;

public class IronManipulator extends AbstractManipulator {

	private int playerChunks;

	public static final RNGAdvancer<?>[] ADVANCERS = { RNGAdvancer.DISPENSER, RNGAdvancer.LAVA };

	public static final Property<BlockPos> GOLEM_POS = Property.create("golem pos", new BlockPos(0, 0, 0),
			new BlockPos(0, 0, 0), Property.indifferent());

	@Override
	protected boolean parseExtra() {
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

	@SuppressWarnings("unchecked")
	@Override
	protected <P extends RNGAdvancer.ParameterHandler> SearchResult testRegionWithAdvancer(int x, int z) {
		for (int i = 0; i < 4; i++)
			rand.nextInt();

		if (advancer instanceof RNGAdvancer.IPlayerChunkMapAware) {
			return ((RNGAdvancer<P>) advancer).search(rand, (P) advancerParameterHandler, rand -> testIronGolem());
		} else {
			return super.testRegionWithAdvancer(x, z);
		}
	}

	@Override
	protected SearchResult testRegion(int x, int z) {
		for (int i = 0; i < playerChunks; i++)
			rand.nextInt();

		return testIronGolem();
	}

	private SearchResult testIronGolem() {
		rand.nextInt(50);

		if (rand.nextInt(7000) == 0) {
			stop();
			int golemX = rand.nextInt(16) - 8;
			int golemY = rand.nextInt(6) - 3;
			int golemZ = rand.nextInt(16) - 8;
			return createSearchResult().withProperty(GOLEM_POS, new BlockPos(golemX, golemY, golemZ));
		}

		return null;
	}

	@Override
	protected SearchResult createSearchResult() {
		List<Property<?>> properties = new ArrayList<>();
		properties.add(DISTANCE);
		properties.add(GOLEM_POS);
		advancer.addExtraProperties(properties);
		return new SearchResult(properties);
	}

}
