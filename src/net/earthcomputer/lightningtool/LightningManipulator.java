package net.earthcomputer.lightningtool;

import java.util.Optional;
import java.util.function.DoublePredicate;

public class LightningManipulator extends AbstractManipulator {

	private DoublePredicate trapValuePredicate;
	private int chunkCount;
	private int wantedChunkIteration;
	private boolean exact;
	
	private int bestChunkIdx = Integer.MAX_VALUE;

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
		try {
			viewDistance = Integer.parseInt(frame.getViewDistanceTextField().getText());
			if (viewDistance > 32 || viewDistance < 1) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			setErrorMessage("Invalid view distance");
			return false;
		}

		chunkCount = 0;
		for (int dx = -viewDistance; dx <= viewDistance; dx++) {
			for (int dz = -viewDistance; dz <= viewDistance; dz++) {
				if (dx * dx + dz * dz <= 8 * 8) {
					chunkCount++;
				}
			}
		}
		
		try {
			wantedChunkIteration = Integer.parseInt(frame.getWantedChunkIterationTextField().getText());
			if (wantedChunkIteration < 0 || wantedChunkIteration >= chunkCount) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			setErrorMessage("Invalid wanted chunk iteration");
			return false;
		}
		
		exact = frame.getChckbxExact().isSelected();

		return true;
	}

	@Override
	protected Optional<String> testRegion(int x, int z) {
		for (int i = 0; i < 4; i++)
			rand.nextInt();

		for (int chunkIdx = 0; chunkIdx < chunkCount; chunkIdx++) {
			int lightningValue = rand.nextInt(100000);
			if (lightningValue == 0) {
				double trapValue = rand.nextDouble();
				if (trapValuePredicate.test(trapValue)) {
					boolean isWanted = false;
					if (exact) {
						isWanted = chunkIdx == wantedChunkIteration;
					} else {
						isWanted = chunkIdx < bestChunkIdx;
					}
					
					if (isWanted) {
						bestChunkIdx = chunkIdx;
						if (chunkIdx <= wantedChunkIteration)
							stop();
						return Optional.of(String.format("chunk ind = %d, trap value = %f", chunkIdx, trapValue));
					}
				}
			}
			rand.nextInt();
		}

		return Optional.empty();
	}

}
