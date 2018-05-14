package net.earthcomputer.lightningtool;

import java.awt.Color;
import java.util.Optional;
import java.util.Random;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public abstract class AbstractManipulator {

	private Thread thread;
	private static int nextWorkerThread = 0;

	protected MainFrame frame;

	protected long worldSeed;
	protected int fromX, fromZ;
	protected int maxExtraRandCalls;
	protected volatile boolean searching;

	protected int extraRandCalls;
	protected RNGAdvancer advancer;

	protected Random rand = new Random();

	private long lastProgressBarUpdateTime = System.nanoTime();

	public void startSearch(MainFrame frame) {
		this.frame = frame;

		JTextField seedField = frame.getWorldSeedTextField();
		worldSeed = parseWorldSeed(seedField.getText());

		try {
			fromX = Integer.parseInt(frame.getSearchFromXTextField().getText());
			fromZ = Integer.parseInt(frame.getSearchFromZTextField().getText());
		} catch (NumberFormatException e) {
			setErrorMessage("Invalid \"from\" coordinates");
			return;
		}
		if (fromX < 0)
			fromX -= 80 * 16 - 1;
		if (fromZ < 0)
			fromZ -= 80 * 16 - 1;
		fromX /= 80 * 16;
		fromZ /= 80 * 16;

		try {
			maxExtraRandCalls = Integer.parseInt(frame.getExtraRandCallsTextField().getText());
			if (maxExtraRandCalls < 0 || maxExtraRandCalls >= 65536) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			setErrorMessage("Invalid extra rand calls");
			return;
		}
		advancer = (RNGAdvancer) frame.getAdvancerComboBox().getSelectedItem();

		if (!parseExtra()) {
			return;
		}

		SwingUtilities.invokeLater(() -> {
			frame.getLblOutput().setForeground(Color.BLACK);
			frame.getLblOutput().setText("Output appears here");
			frame.getOutputTextArea().setText("");
			frame.getProgressBar().setIndeterminate(true);
			frame.getProgressBar().setString("");
			frame.getProgressBar().setStringPainted(true);
		});
		searching = true;
		thread = new Thread(this::doSearch);
		thread.setName("Searching Worker Thread " + (nextWorkerThread++));
		thread.setDaemon(true);
		thread.start();
	}

	public static long parseWorldSeed(String seed) {
		try {
			return Long.parseLong(seed);
		} catch (NumberFormatException e) {
			return seed.hashCode();
		}
	}

	protected boolean parseExtra() {
		return true;
	}

	private void doSearch() {
		int count = 0;
		for (int r = 0; searching; r++) {
			for (int dx = -r; dx <= r && searching; dx++) {
				int dz = r - Math.abs(dx);
				searchRegion(fromX + dx, fromZ + dz, ++count);
			}
			for (int dx = r - 1; dx > -r && searching; dx--) {
				int dz = Math.abs(dx) - r;
				searchRegion(fromX + dx, fromZ + dz, ++count);
			}
		}
	}

	private void searchRegion(int x, int z, int count) {
		boolean hadOutput = false;
		for (extraRandCalls = 0; extraRandCalls <= maxExtraRandCalls && searching; extraRandCalls++) {
			resetSeed(x, z);
			Optional<String> result = testRegion(x, z);
			if (result.isPresent()) {
				String separator = hadOutput ? null : getRegionSeparator(x, z);
				if (separator == null)
					separator = "";
				else
					separator += "\n";
				final String separator_f = separator;
				hadOutput = true;
				String output = String.format("(%d, %d) d = %d, extra rand = %d; %s", x * 80 * 16, z * 80 * 16,
						Math.abs((fromX - x) * 80 * 16) + Math.abs((fromZ - z) * 80 * 16), extraRandCalls,
						result.get());
				SwingUtilities.invokeLater(() -> {
					frame.getLblOutput().setForeground(Color.BLACK);
					frame.getLblOutput().setText(output);
					frame.getOutputTextArea().append(separator_f + output + "\n");
				});
			}
		}

		long currentTime = System.nanoTime();
		if (currentTime - lastProgressBarUpdateTime > 100_000_000) {
			lastProgressBarUpdateTime = currentTime;
			SwingUtilities.invokeLater(() -> frame.getProgressBar().setString(count + " regions searched"));
		}
	}

	public static void resetSeed(Random rand, int x, int z, long worldSeed) {
		rand.setSeed(x * 341873128712L + z * 132897987541L + worldSeed + 10387319);
		for (int i = 0; i < 4; i++)
			rand.nextInt();
	}

	protected void resetSeed(int x, int z) {
		resetSeed(rand, x, z, worldSeed);
		for (int i = 0; i < extraRandCalls; i++)
			advancer.advance(rand);
	}

	protected abstract Optional<String> testRegion(int x, int z);

	protected String getRegionSeparator(int newX, int newZ) {
		return null;
	}

	public void stop() {
		searching = false;
		SwingUtilities.invokeLater(() -> {
			frame.getProgressBar().setIndeterminate(false);
			frame.getProgressBar().setStringPainted(false);
		});
	}

	protected void setErrorMessage(String message) {
		frame.getLblOutput().setForeground(Color.RED);
		frame.getLblOutput().setText(message);
	}

}
