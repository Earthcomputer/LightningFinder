package net.earthcomputer.lightningtool;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public class ResettableRandom extends Random {

	private static final long serialVersionUID = 1L;

	private long seed;
	private boolean haveNextNextGaussian;
	private double nextNextGaussian;
	private int count = 0;

	private Deque<State> states = new ArrayDeque<>();

	public ResettableRandom() {
	}

	public ResettableRandom(long seed) {
		super(seed);
	}

	@Override
	protected int next(int bits) {
		count++;
		return (int) ((seed = (seed * 0x5deece66dL + 0xbL) & 0xffffffffffffL) >>> (48 - bits));
	}

	@Override
	public synchronized double nextGaussian() {
		if (haveNextNextGaussian) {
			haveNextNextGaussian = false;
			return nextNextGaussian;
		}

		double v1, v2, s;
		do {
			v1 = 2 * nextDouble() - 1;
			v2 = 2 * nextDouble() - 1;
			s = v1 * v1 + v2 * v2;
		} while (s >= 1 || s == 0);

		double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s);
		nextNextGaussian = v2 * multiplier;
		haveNextNextGaussian = true;
		return v1 * multiplier;
	}

	@Override
	public synchronized void setSeed(long seed) {
		setInternalSeed(seed ^ 0x5deece66dL);
	}

	public void setInternalSeed(long seed) {
		this.seed = seed & 0xffffffffffffL;
		haveNextNextGaussian = false;
		count = 0;
	}

	public long getSeed() {
		return seed ^ 0x5deece66dL;
	}

	public long getInternalSeed() {
		return seed;
	}

	public int getCount() {
		return count;
	}

	public void saveState() {
		states.push(new State(seed, haveNextNextGaussian, nextNextGaussian, count));
	}

	public void restoreState() {
		states.pop().restore(this);
	}

	public void popState() {
		states.pop();
	}

	public void clearStates() {
		states.clear();
	}

	private static class State {
		private long seed;
		private boolean haveNextNextGaussian;
		private double nextNextGaussian;
		private int count;

		public State(long seed, boolean haveNextNextGaussian, double nextNextGaussian, int count) {
			this.seed = seed;
			this.haveNextNextGaussian = haveNextNextGaussian;
			this.nextNextGaussian = nextNextGaussian;
			this.count = count;
		}

		public void restore(ResettableRandom rand) {
			rand.seed = seed;
			rand.haveNextNextGaussian = haveNextNextGaussian;
			rand.nextNextGaussian = nextNextGaussian;
			rand.count = count;
		}
	}

}
