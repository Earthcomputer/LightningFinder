package net.earthcomputer.lightningtool;

import java.util.Random;

public enum RNGAdvancer {

	// @formatter:off
	HOPPER {
		@Override
		public void advance(Random rand) {
			rand.nextInt();
		}
	},
	DISPENSER {
		@Override
		public void advance(Random rand) {
			rand.nextLong();
			rand.nextGaussian();
			rand.nextGaussian();
			rand.nextGaussian();
		}
	},
	LAVA {
		@Override
		public void advance(Random rand) {
			for (int i = 0; i < 3; i++) {
				int times = rand.nextInt(3);
				if (times == 0)
					times = 3;
				for (int j = 0; j < times; j++) {
					rand.nextInt(3);
					rand.nextInt(3);
				}
			}
		}
	};
	// @formatter:on

	public abstract void advance(Random rand);

}
