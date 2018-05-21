package net.earthcomputer.lightningtool;

import java.awt.FlowLayout;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.earthcomputer.lightningtool.SearchResult.Property;

/**
 * Describes a method to advance the RNG after resetting it in order to get
 * better values
 */
public abstract class RNGAdvancer<P extends RNGAdvancer.ParameterHandler> {

	private String name;
	private P parameterHandler = createParameterHandler();
	private JPanel parameterPanel = parameterHandler.createPanel();

	public RNGAdvancer(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public P getParameterHandler() {
		return parameterHandler;
	}

	public JPanel getParameterPanel() {
		return parameterPanel;
	}

	/**
	 * Creates a new parameter handler
	 */
	protected abstract P createParameterHandler();

	/**
	 * Searches for an appropriate advance
	 */
	public abstract SearchResult search(ResettableRandom rand, P parameters, RandomAction action);

	public abstract void addExtraProperties(List<Property<?>> properties);

	/**
	 * Utilizes hoppers choosing a random entity to pull items out from
	 */
	public static final SimpleRNGAdvancer HOPPER = new SimpleRNGAdvancer("Hopper") {
		@Override
		public void advance(ResettableRandom rand) {
			rand.nextInt();
		}
	};
	/**
	 * Utilizes the random pitch of the piston movement sound
	 */
	public static final SimpleRNGAdvancer PISTON = new SimpleRNGAdvancer("Piston") {
		@Override
		public void advance(ResettableRandom rand) {
			rand.nextInt();
		}
	};
	/**
	 * Utilizes the random velocity of items from a dispenser
	 */
	public static final SimpleRNGAdvancer DISPENSER = new ViewDistanceRNGAdvancer("Dispenser") {
		@Override
		public void advance(ResettableRandom rand) {
			rand.nextLong();
			rand.nextGaussian();
			rand.nextGaussian();
			rand.nextGaussian();
		}
	};
	/**
	 * Utilizes the random block to burn in lava random ticks
	 */
	public static final RandomTickRNGAdvancer LAVA = new RandomTickRNGAdvancer("Lava") {
		@Override
		protected void randomTick(ResettableRandom rand) {
			int times = rand.nextInt(3);
			if (times == 0)
				times = 3;
			for (int i = 0; i < times; i++) {
				rand.nextInt();
				rand.nextInt();
			}
		}

		@Override
		protected int maxCallsPerRandomTick() {
			return 7;
		}
	};
	/**
	 * Same as LAVA, but specialized for lightning manipulation
	 */
	public static final LightningRandomTickRNGAdvancer LAVA_LIGHTNING = new LightningRandomTickRNGAdvancer("Lava") {
		@Override
		protected void randomTick(ResettableRandom rand) {
			int times = rand.nextInt(3);
			if (times == 0)
				times = 3;
			for (int i = 0; i < times; i++) {
				rand.nextInt();
				rand.nextInt();
			}
		}

		@Override
		protected int maxCallsPerRandomTick() {
			return 7;
		}
	};

	/**
	 * Tries a single operation different numbers of times
	 */
	public static abstract class SimpleRNGAdvancer extends RNGAdvancer<SimpleRNGAdvancer.SimpleParameterHandler> {

		public static final Property<Integer> ADVANCES = Property.create("advances", 0, Integer.MAX_VALUE,
				Property.minimize());

		public SimpleRNGAdvancer(String name) {
			super(name);
		}

		protected abstract void advance(ResettableRandom rand);

		@Override
		protected SimpleParameterHandler createParameterHandler() {
			return new SimpleParameterHandler();
		}

		@Override
		public void addExtraProperties(List<Property<?>> properties) {
			properties.add(ADVANCES);
		}

		@Override
		public SearchResult search(ResettableRandom rand, SimpleParameterHandler parameters, RandomAction action) {
			int maxExtraRandCalls = parameters.getMaxExtraRandCalls();
			for (int extraRandCalls = 0; extraRandCalls <= maxExtraRandCalls; extraRandCalls++) {
				rand.saveState();
				SearchResult result = action.perform(rand);
				if (result != null) {
					rand.popState();
					return result.withProperty(ADVANCES, extraRandCalls);
				}
				rand.restoreState();
				advance(rand);
			}
			return null;
		}

		public static class SimpleParameterHandler extends ParameterHandler {
			private JTextField maxExtraRandCallsTextField;
			private int maxExtraRandCalls;

			@Override
			public JPanel createPanel() {
				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

				{
					JPanel horPanel = new JPanel();
					((FlowLayout) horPanel.getLayout()).setAlignment(FlowLayout.LEFT);
					horPanel.add(new JLabel("Max advances:"));
					maxExtraRandCallsTextField = new JTextField("0");
					maxExtraRandCallsTextField.setColumns(10);
					horPanel.add(maxExtraRandCallsTextField);
					panel.add(horPanel);
				}

				return panel;
			}

			@Override
			public boolean readFromPanel() {
				try {
					maxExtraRandCalls = Integer.parseInt(maxExtraRandCallsTextField.getText());
					if (maxExtraRandCalls < 0)
						throw new NumberFormatException();
				} catch (NumberFormatException e) {
					return false;
				}

				return true;
			}

			public int getMaxExtraRandCalls() {
				return maxExtraRandCalls;
			}
		}

	}

	public static abstract class ViewDistanceRNGAdvancer extends SimpleRNGAdvancer {
		public ViewDistanceRNGAdvancer(String name) {
			super(name);
		}

		@Override
		protected ViewDistanceParameterHandler createParameterHandler() {
			return new ViewDistanceParameterHandler();
		}

		public static class ViewDistanceParameterHandler extends SimpleParameterHandler
				implements IViewDistanceParameterHandler {
			private JTextField viewDistanceTextField;
			private JTextField xInChunkTextField;
			private JTextField zInChunkTextField;
			private int viewDistance;
			private double xInChunk;
			private double zInChunk;

			@Override
			public JPanel createPanel() {
				JPanel panel = super.createPanel();

				{
					JPanel horPanel = new JPanel();
					((FlowLayout) horPanel.getLayout()).setAlignment(FlowLayout.LEFT);
					horPanel.add(new JLabel("View distance:"));
					viewDistanceTextField = new JTextField("12");
					viewDistanceTextField.setColumns(10);
					horPanel.add(viewDistanceTextField);
					panel.add(horPanel);
				}

				{
					JPanel horPanel = new JPanel();
					((FlowLayout) horPanel.getLayout()).setAlignment(FlowLayout.LEFT);
					horPanel.add(new JLabel("Player pos in chunk:"));
					horPanel.add(Box.createHorizontalStrut(5));

					horPanel.add(new JLabel("X:"));
					xInChunkTextField = new JTextField("8");
					xInChunkTextField.setColumns(10);
					horPanel.add(xInChunkTextField);
					horPanel.add(Box.createHorizontalStrut(10));

					horPanel.add(new JLabel("Z:"));
					zInChunkTextField = new JTextField("8");
					zInChunkTextField.setColumns(10);
					horPanel.add(zInChunkTextField);
					horPanel.add(Box.createHorizontalStrut(20));
					panel.add(horPanel);
				}

				return panel;
			}

			@Override
			public boolean readFromPanel() {
				if (!super.readFromPanel())
					return false;

				try {
					viewDistance = Integer.parseInt(viewDistanceTextField.getText());
					if (viewDistance < 2 || viewDistance > 32)
						throw new NumberFormatException();
				} catch (NumberFormatException e) {
					return false;
				}

				try {
					xInChunk = Double.parseDouble(xInChunkTextField.getText());
					if (xInChunk < 0 || xInChunk >= 16)
						throw new NumberFormatException();
					zInChunk = Double.parseDouble(zInChunkTextField.getText());
					if (zInChunk < 0 || zInChunk >= 16)
						throw new NumberFormatException();
				} catch (NumberFormatException e) {
					return false;
				}

				return true;
			}

			@Override
			public int getViewDistance() {
				return viewDistance;
			}

			@Override
			public double getPlayerXInChunk() {
				return xInChunk;
			}

			@Override
			public double getPlayerZInChunk() {
				return zInChunk;
			}
		}
	}

	/**
	 * Tries different combinations of random tick advancesomness
	 */
	public static abstract class RandomTickRNGAdvancer
			extends RNGAdvancer<RandomTickRNGAdvancer.RandomTickParameterHandler> implements IPlayerChunkMapAware {

		private static final int[] WORST_ARRAY = new int[0];
		public static final Property<int[]> ADVANCES = Property
				.<int[]>create("advances", new int[0], WORST_ARRAY,
						Comparator.<int[]>comparingInt(arr -> arr == WORST_ARRAY ? 1 : 0)
								.thenComparingLong(arr -> Arrays.stream(arr).filter(i -> i != 0).count()))
				.setValueSerializer(arr -> {
					int chunkCount = arr.length;
					int currentNum = -1;
					int beginIndex = -1;
					StringBuilder str = new StringBuilder();
					for (int index = 0; index < chunkCount; index++) {
						if (arr[index] != currentNum) {
							if (beginIndex != -1) {
								if (str.length() != 0)
									str.append(", ");
								if (beginIndex == index - 1) {
									str.append(beginIndex);
								} else {
									str.append(beginIndex).append("-").append(index - 1);
								}
								str.append(": ").append(currentNum);
							}
							beginIndex = index;
							currentNum = arr[index];
						}
					}
					if (beginIndex != chunkCount && currentNum != 0) {
						if (beginIndex == -1) {
							str.append("No chunks to be filled");
						} else {
							str.append(", ");
							if (beginIndex == chunkCount - 1) {
								str.append(beginIndex);
							} else {
								str.append(beginIndex).append("-").append(chunkCount - 1);
							}
							str.append(": ").append(currentNum);
						}
					}
					return "[" + str + "]";
				});

		private static final int MAX_LEAVES_SEARCHED = 1000000;

		public RandomTickRNGAdvancer(String name) {
			super(name);
		}

		private static final int RANDOM_TICK_SPEED = 3;

		protected abstract void randomTick(ResettableRandom rand);

		protected abstract int maxCallsPerRandomTick();

		protected int getChunkCountOverride(int chunkCount, RandomTickParameterHandler parameters) {
			return chunkCount;
		}

		protected boolean hasThunder() {
			return false;
		}

		@Override
		protected RandomTickParameterHandler createParameterHandler() {
			return new RandomTickParameterHandler();
		}

		@Override
		public void addExtraProperties(List<Property<?>> properties) {
			properties.add(ADVANCES);
		}

		@Override
		public SearchResult search(ResettableRandom rand, RandomTickParameterHandler parameters, RandomAction action) {
			int viewDistance = parameters.getViewDistance();
			double playerX = parameters.getPlayerXInChunk();
			double playerZ = parameters.getPlayerZInChunk();
			int chunkCount = 0;
			for (int x = -viewDistance; x <= viewDistance; x++) {
				for (int z = -viewDistance; z <= viewDistance; z++) {
					double dx = (x * 16 + 8) - playerX;
					double dz = (z * 16 + 8) - playerZ;
					if (dx * dx + dz * dz < 128 * 128)
						chunkCount++;
				}
			}

			chunkCount = getChunkCountOverride(chunkCount, parameters);

			int subchunksPerChunk = parameters.getSubchunksPerChunk();

			SearchResult result = null;
			rand.saveState();
			int callsNeeded = chunkCount * subchunksPerChunk * RANDOM_TICK_SPEED * maxCallsPerRandomTick();
			for (int calls = 0; calls <= callsNeeded; calls++) {
				rand.saveState();
				result = action.perform(rand);
				if (result != null) {
					rand.restoreState();
					callsNeeded = rand.getCount();
					break;
				}
				rand.restoreState();
				rand.nextInt();
			}
			rand.restoreState();

			if (result == null)
				return null;

			int[] subchunkCounts = new int[chunkCount];

			if (recursiveSearch(0, chunkCount, callsNeeded, subchunksPerChunk, rand, subchunkCounts, new int[1])) {
				return result.withProperty(ADVANCES, subchunkCounts);
			} else {
				return null;
			}
		}

		protected boolean recursiveSearch(int chunkIndex, int chunkCount, int callsNeeded, int subchunksPerChunk,
				ResettableRandom rand, int[] subchunkCounts, int[] leavesSearched) {
			if (chunkIndex == chunkCount) {
				leavesSearched[0]++;
				return rand.getCount() == callsNeeded;
			}
			if (rand.getCount() > callsNeeded - 2 * (chunkCount - chunkIndex - 1)) {
				leavesSearched[0]++;
				return false;
			}
			if (leavesSearched[0] > MAX_LEAVES_SEARCHED) {
				return false;
			}

			if (hasThunder()) {
				if (rand.nextInt(100000) == 0) {
					rand.nextLong();
				}
			}
			rand.nextInt();

			for (int subchunks = subchunksPerChunk; subchunks >= 0; subchunks--) {
				rand.saveState();
				for (int i = 0; i < subchunks; i++) {
					for (int j = 0; j < RANDOM_TICK_SPEED; j++) {
						randomTick(rand);
					}
				}
				boolean result = recursiveSearch(chunkIndex + 1, chunkCount, callsNeeded, subchunksPerChunk, rand,
						subchunkCounts, leavesSearched);
				rand.restoreState();
				if (result) {
					subchunkCounts[chunkIndex] = subchunks;
					return true;
				}
			}

			return false;
		}

		public static class RandomTickParameterHandler extends ParameterHandler
				implements IViewDistanceParameterHandler {
			private JTextField viewDistanceTextField;
			private JTextField playerXTextField;
			private JTextField playerZTextField;
			private JTextField subchunksPerChunkTextField;
			private int viewDistance;
			private double playerXInChunk;
			private double playerZInChunk;
			private int subchunksPerChunk;

			@Override
			public JPanel createPanel() {
				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

				{
					JPanel horPanel = new JPanel();
					((FlowLayout) horPanel.getLayout()).setAlignment(FlowLayout.LEFT);
					horPanel.add(new JLabel("View distance:"));
					viewDistanceTextField = new JTextField("12");
					viewDistanceTextField.setColumns(10);
					horPanel.add(viewDistanceTextField);
					horPanel.add(Box.createHorizontalStrut(20));
					panel.add(horPanel);
				}

				{
					JPanel horPanel = new JPanel();
					((FlowLayout) horPanel.getLayout()).setAlignment(FlowLayout.LEFT);
					horPanel.add(new JLabel("Player pos in chunk:"));
					horPanel.add(Box.createHorizontalStrut(5));

					horPanel.add(new JLabel("X:"));
					playerXTextField = new JTextField("8");
					playerXTextField.setColumns(10);
					horPanel.add(playerXTextField);
					horPanel.add(Box.createHorizontalStrut(10));

					horPanel.add(new JLabel("Z:"));
					playerZTextField = new JTextField("8");
					playerZTextField.setColumns(10);
					horPanel.add(playerZTextField);
					horPanel.add(Box.createHorizontalStrut(20));
					panel.add(horPanel);
				}

				{
					JPanel horPanel = new JPanel();
					((FlowLayout) horPanel.getLayout()).setAlignment(FlowLayout.LEFT);
					horPanel.add(new JLabel("Subchunks per chunk:"));
					subchunksPerChunkTextField = new JTextField("11");
					subchunksPerChunkTextField.setColumns(10);
					horPanel.add(subchunksPerChunkTextField);
					panel.add(horPanel);
				}

				return panel;
			}

			@Override
			public boolean readFromPanel() {
				try {
					viewDistance = Integer.parseInt(viewDistanceTextField.getText());
					if (viewDistance < 2 || viewDistance > 32) {
						throw new NumberFormatException();
					}
				} catch (NumberFormatException e) {
					return false;
				}

				try {
					playerXInChunk = Double.parseDouble(playerXTextField.getText());
					if (playerXInChunk < 0 || playerXInChunk >= 16)
						throw new NumberFormatException();
					playerZInChunk = Double.parseDouble(playerZTextField.getText());
					if (playerZInChunk < 0 || playerZInChunk >= 16)
						throw new NumberFormatException();
				} catch (NumberFormatException e) {
					return false;
				}

				try {
					subchunksPerChunk = Integer.parseInt(subchunksPerChunkTextField.getText());
					if (subchunksPerChunk < 0 || subchunksPerChunk > 16)
						throw new NumberFormatException();
				} catch (NumberFormatException e) {
					return false;
				}

				return true;
			}

			@Override
			public int getViewDistance() {
				return viewDistance;
			}

			@Override
			public double getPlayerXInChunk() {
				return playerXInChunk;
			}

			@Override
			public double getPlayerZInChunk() {
				return playerZInChunk;
			}

			public int getSubchunksPerChunk() {
				return subchunksPerChunk;
			}
		}

	};

	/**
	 * Specialization of random tick RNG advancer for lightning, because it
	 * needs to stop half way through the random ticks (when it actually creates
	 * the lightning)
	 */
	public static abstract class LightningRandomTickRNGAdvancer extends RandomTickRNGAdvancer {
		public LightningRandomTickRNGAdvancer(String name) {
			super(name);
		}

		@Override
		public boolean hasThunder() {
			return true;
		}

		@Override
		protected LightningRandomTickParameterHandler createParameterHandler() {
			return new LightningRandomTickParameterHandler();
		}

		@Override
		protected int getChunkCountOverride(int chunkCount, RandomTickParameterHandler parameters) {
			return Math.min(((LightningRandomTickParameterHandler) parameters).getChunkIndex(), chunkCount);
		}

		public static class LightningRandomTickParameterHandler extends RandomTickParameterHandler {
			private JTextField chunkIndexTextField;
			private int chunkIndex;

			@Override
			public JPanel createPanel() {
				JPanel panel = super.createPanel();

				{
					JPanel horPanel = new JPanel();
					((FlowLayout) horPanel.getLayout()).setAlignment(FlowLayout.LEFT);
					horPanel.add(new JLabel("Chunk index:"));
					chunkIndexTextField = new JTextField();
					chunkIndexTextField.setColumns(10);
					horPanel.add(chunkIndexTextField);
					panel.add(horPanel);
				}

				return panel;
			}

			@Override
			public boolean readFromPanel() {
				if (!super.readFromPanel())
					return false;

				try {
					chunkIndex = Integer.parseInt(chunkIndexTextField.getText());
					if (chunkIndex < 0)
						throw new NumberFormatException();
				} catch (NumberFormatException e) {
					return false;
				}

				return true;
			}

			public int getChunkIndex() {
				return chunkIndex;
			}
		}
	}

	/**
	 * A manipulator which asks for view distance
	 */
	public static interface IViewDistanceParameterHandler {
		int getViewDistance();

		double getPlayerXInChunk();

		double getPlayerZInChunk();
	}

	/**
	 * A marker interface for manipulators that process the player chunk map
	 * logic themselves
	 */
	public static interface IPlayerChunkMapAware {
	}

	/**
	 * Handles extra options for the RNG advancer
	 */
	public static abstract class ParameterHandler {
		public abstract JPanel createPanel();

		public abstract boolean readFromPanel();
	}

	@FunctionalInterface
	public static interface RandomAction {
		SearchResult perform(ResettableRandom rand);
	}

}
