package net.earthcomputer.lightningtool;

import java.awt.FlowLayout;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

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
	public abstract void search(ResettableRandom rand, P parameters, RandomAction action,
			Consumer<SearchResult> resultConsumer);

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
	public static final SimpleRNGAdvancer DISPENSER = new SimpleRNGAdvancer("Dispenser") {
		@Override
		public void advance(ResettableRandom rand) {
			rand.nextLong();
			rand.nextGaussian();
			rand.nextGaussian();
			rand.nextGaussian();
		}
	};
	/**
	 * Utilizes the random velocity of items from a dispenser, with a view distance option
	 */
	public static final SimpleRNGAdvancer DISPENSER_VIEW_DISTANCE = new ViewDistanceRNGAdvancer("Dispenser") {
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
		public void search(ResettableRandom rand, SimpleParameterHandler parameters, RandomAction action,
				Consumer<SearchResult> resultConsumer) {
			int maxExtraRandCalls = parameters.getMaxExtraRandCalls();
			for (int extraRandCalls = 0; extraRandCalls <= maxExtraRandCalls; extraRandCalls++) {
				rand.saveState();
				SearchResult result = action.perform(rand);
				if (result != null) {
					resultConsumer.accept(result.withProperty(ADVANCES, extraRandCalls));
				}
				rand.restoreState();
				advance(rand);
			}
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
					xInChunkTextField = new JTextField("8.5");
					xInChunkTextField.setColumns(10);
					horPanel.add(xInChunkTextField);
					horPanel.add(Box.createHorizontalStrut(10));

					horPanel.add(new JLabel("Z:"));
					zInChunkTextField = new JTextField("8.5");
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
		public void search(ResettableRandom rand, RandomTickParameterHandler parameters, RandomAction action,
				Consumer<SearchResult> resultConsumer) {
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

			int maxCalls = chunkCount * (
							subchunksPerChunk * RANDOM_TICK_SPEED * maxCallsPerRandomTick()
							+ (hasThunder() ? 2 : 1)
			);
			int actualCalls;
			rand.saveState();
			if (hasThunder())
				rand.nextInt();
			for (actualCalls = hasThunder() ? 1 : 0; actualCalls <= maxCalls; actualCalls++) {
				rand.saveState();
				result = action.perform(rand);
				rand.restoreState();
				if (result != null)
					break;
				rand.nextInt();
			}
			rand.restoreState();

			if (result != null) {
				int[] subchunkCounts = new int[chunkCount];
				if (getSubchunkCountsWithCalls(rand, actualCalls, subchunkCounts, chunkCount, subchunksPerChunk)) {
					result = result.withProperty(ADVANCES, subchunkCounts);
					resultConsumer.accept(result);
				}
			}
		}

		private boolean getSubchunkCountsWithCalls(ResettableRandom rand, int targetCalls, int[] subchunkCounts,
				int chunkCount, int subchunksPerChunk) {
			/*
			 * Here we use dynamic programming to get a random call path with
			 * the exact right number of random calls.
			 * 
			 * Think of the possible random calls as a directed acyclic graph,
			 * with each node being a possible random state (seed) along the
			 * way, and each arc joining the potential state after 1 chunk to a
			 * potential state of the next chunk.
			 * 
			 * Because we are finding a path of exact length, we have to store a
			 * list at each node rather than a fixed number of values like we
			 * can in Dijkstra. Because of this, the total amount of data stored
			 * may get very large, and so we use a paged list so we don't run
			 * out of memory.
			 * 
			 * However, it is still possible to do Dijkstra-esque elimination
			 * when two path lengths arriving at a node are equal. In this case,
			 * the path with the least total subchunks of random ticks is
			 * chosen.
			 * 
			 * Finally, some paths may also be eliminated early if we can be
			 * sure that they will never reach the destination with the right
			 * length.
			 * 
			 * At each node a list of previous nodes is stored, along with the
			 * path lengths and total subchunk counts.
			 */
			class Node {
				ArrayList<Integer> prevNodes = new ArrayList<>();
				ArrayList<Integer> pathLengths = new ArrayList<>();
				ArrayList<Integer> subchunkCounts = new ArrayList<>();
			}
			PagedList<Node> graph = new PagedList<>(1000, new PagedList.ElementSerializer<Node>() {
				@Override
				public void serialize(DataOutputStream out, Node e) throws IOException {
					out.writeInt(e.prevNodes.size());
					for (int i = 0, size = e.prevNodes.size(); i < size; i++) {
						out.writeInt(e.prevNodes.get(i));
						out.writeInt(e.pathLengths.get(i));
						out.writeInt(e.subchunkCounts.get(i));
					}
				}

				@Override
				public Node deserialize(DataInputStream in) throws IOException {
					Node n = new Node();
					int size = in.readInt();
					n.prevNodes.ensureCapacity(size);
					n.pathLengths.ensureCapacity(size);
					n.subchunkCounts.ensureCapacity(size);
					for (int i = 0; i < size; i++) {
						n.prevNodes.add(in.readInt());
						n.pathLengths.add(in.readInt());
						n.subchunkCounts.add(in.readInt());
					}
					return n;
				}
			});
			Node firstNode = new Node();
			firstNode.prevNodes.add(-1);
			firstNode.pathLengths.add(0);
			firstNode.subchunkCounts.add(0);
			graph.add(firstNode);

			for (int call = 0; call <= targetCalls; call++) {
				if (call == graph.size())
					graph.add(new Node());
				Node node = graph.get(call);
				rand.saveState();
				int initialCallCount = rand.getCount();
				if (hasThunder())
					rand.nextInt(100000);
				for (int subchunks = 0; subchunks < subchunksPerChunk; subchunks++) {
					int relativeCalls = rand.getCount() - initialCallCount;
					int nextCalls = call + relativeCalls + 1; // + 1 for the rand.nextInt between random ticks

					int callsLeft = targetCalls - nextCalls;
					if (callsLeft < 0)
						break;

					while (graph.size() <= nextCalls)
						graph.add(new Node());
					Node nextNode = graph.get(nextCalls);
					for (int path = 0; path < node.prevNodes.size(); path++) {
						int chunksLeft = chunkCount - node.pathLengths.get(path);
						if (callsLeft - 1 > chunksLeft * (
								subchunksPerChunk * RANDOM_TICK_SPEED * maxCallsPerRandomTick()
								+ (hasThunder() ? 2 : 1)))
							continue;
						if (callsLeft + 1 < chunksLeft)
							continue;

						int nextPathLength = node.pathLengths.get(path) + 1;
						int nextSubchunkCount = node.subchunkCounts.get(path) + subchunks;
						int indexToAdd = Collections.binarySearch(nextNode.pathLengths, nextPathLength);
						if (indexToAdd >= 0) {
							// there is a path of the same length already leading to that node
							if (nextNode.subchunkCounts.get(indexToAdd) > nextSubchunkCount) {
								nextNode.prevNodes.set(indexToAdd, call);
								nextNode.subchunkCounts.set(indexToAdd, nextSubchunkCount);
							}
						} else {
							indexToAdd = -(indexToAdd + 1);
							nextNode.prevNodes.add(indexToAdd, call);
							nextNode.pathLengths.add(indexToAdd, nextPathLength);
							nextNode.subchunkCounts.add(indexToAdd, nextSubchunkCount);
						}
					}

					for (int i = 0; i < RANDOM_TICK_SPEED; i++) {
						randomTick(rand);
					}
				}
				rand.restoreState();
				rand.nextInt();
			}

			try {
				Node node = graph.get(targetCalls);
				int pathLength = chunkCount;
				int path = Collections.binarySearch(node.pathLengths, pathLength);
				if (path < 0)
					return false;
				int subchunks = node.subchunkCounts.get(path);

				while (pathLength != 0) {
					node = graph.get(node.prevNodes.get(path));
					pathLength--;
					path = Collections.binarySearch(node.pathLengths, pathLength);
					subchunkCounts[pathLength] = subchunks - (subchunks = node.subchunkCounts.get(path));
				}
			} finally {
				graph.close();
			}

			return true;
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
					playerXTextField = new JTextField("8.5");
					playerXTextField.setColumns(10);
					horPanel.add(playerXTextField);
					horPanel.add(Box.createHorizontalStrut(10));

					horPanel.add(new JLabel("Z:"));
					playerZTextField = new JTextField("8.5");
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

		class ChunkCoords{
			int x;
			int z;
			ChunkCoords(int x, int z){
				this.x = x;
				this.z = z;
			}
			public boolean equals(Object obj) {
				if(obj instanceof ChunkCoords) {
					ChunkCoords o = (ChunkCoords) obj;
					return o.x == this.x && o.z == this.z;
				}
				return false;
			}
		}
		
		List<ChunkCoords> tickedChunks = new ArrayList<ChunkCoords>();

		
		@Override
		protected int getChunkCountOverride(int chunkCount, RandomTickParameterHandler parameters) {
			LightningRandomTickParameterHandler param = (LightningRandomTickParameterHandler) parameters;
			
			if(param.wasUpdated()) {
				int viewDistance = param.getViewDistance();
				double playerX = param.getPlayerXInChunk();
				double playerZ = param.getPlayerZInChunk();
				int chunkRelX = param.getChunkRelX();
				int chunkRelZ = param.getChunkRelZ();
				tickedChunks = new ArrayList<ChunkCoords>();
				double playerMargin = Double.MAX_VALUE;
				
				boolean lightningStruck = false;
				
				for (int x = -viewDistance; x <= viewDistance; x++) {
					for (int z = -viewDistance; z <= viewDistance; z++) {
						double dx = (x * 16 + 8) - playerX;
						double dz = (z * 16 + 8) - playerZ;
						if (dx * dx + dz * dz < 128 * 128)
						{
							if(!lightningStruck) {
								ChunkCoords c = new ChunkCoords(x,z);
								if(c.x == chunkRelX && c.z == chunkRelZ) {
									lightningStruck=true;
								}
								tickedChunks.add(c);
								chunkCount++;
							}
						}
						double distance = dx * dx + dz * dz;
						distance = Math.sqrt(Math.abs(distance));
						playerMargin = Double.min(playerMargin, Math.abs(distance-128));
					}
				}
				MainFrame.sendOutput("Chunk index: " + tickedChunks.size());
				MainFrame.sendOutput("Player position error margin: " + playerMargin);
				for(int z = -viewDistance; z<=viewDistance; ++z) {
					String s = "";
					for(int x = -viewDistance; x<= viewDistance; ++x) {
						ChunkCoords c = new ChunkCoords(x,z);
						if(x==chunkRelX && z==chunkRelZ || x==0 && z==0) {
							s+="\u25a3";
						}else {
							s+= tickedChunks.contains(c)? "\u25a0" : "\u25a1";
						}
					}
					MainFrame.sendOutput(s);
				}
			}
			return tickedChunks.size();
		}

		public static class LightningRandomTickParameterHandler extends RandomTickParameterHandler {
			private JTextField chunkRelXTextField;
			private JTextField chunkRelZTextField;
			private int chunkRelX;
			private int chunkRelZ;
			boolean updated;

			@Override
			public JPanel createPanel() {
				JPanel panel = super.createPanel();

				{
					JPanel horPanel = new JPanel();
					((FlowLayout) horPanel.getLayout()).setAlignment(FlowLayout.LEFT);
					horPanel.add(new JLabel("Relative chunk coordinates:"));
					chunkRelXTextField = new JTextField("0");
					chunkRelZTextField = new JTextField("0");
					chunkRelXTextField.setColumns(10);
					chunkRelZTextField.setColumns(10);
					horPanel.add(chunkRelXTextField);
					horPanel.add(chunkRelZTextField);
					panel.add(horPanel);
				}

				return panel;
			}

			@Override
			public boolean readFromPanel() {
				if (!super.readFromPanel())
					return false;
				
				updated = true;

				try {
					chunkRelX = Integer.parseInt(chunkRelXTextField.getText());
					chunkRelZ = Integer.parseInt(chunkRelZTextField.getText());
				} catch (NumberFormatException e) {
					return false;
				}

				return true;
			}

			public int getChunkRelX() {
				return chunkRelX;
			}
			public int getChunkRelZ() {
				return chunkRelZ;
			}
			public boolean wasUpdated() {
				boolean u = updated;
				updated = false;
				return u;
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
