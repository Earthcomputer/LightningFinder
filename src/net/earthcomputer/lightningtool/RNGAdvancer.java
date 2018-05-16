package net.earthcomputer.lightningtool;

import java.util.Random;

public enum RNGAdvancer {

	// @formatter:off
	HOPPER {
		@Override
		public void advance(Random rand) {
			rand.nextInt();
		}
	};
	// @formatter:on

	public abstract void advance(Random rand);

}
