package net.earthcomputer.lightningtool;

import java.util.Optional;
import java.util.Random;

public class FortuneManipulator extends AbstractManipulator {

	private Ore ore;
	private int dropAmount;
	private boolean dropAmountExact;
	private boolean manipulateXp;
	private int xpDropAmount = -1;
	private boolean xpDropAmountExact;
	private int fortuneLevel;

	private int bestQuantityDropped = -1;
	private int bestXpDropped = -1;

	@Override
	protected boolean parseExtra() {
		ore = (Ore) frame.getOreComboBox().getSelectedItem();

		try {
			dropAmount = Integer.parseInt(frame.getDropAmountTextField().getText());
		} catch (NumberFormatException e) {
			setErrorMessage("Drop amount invalid");
			return false;
		}
		dropAmountExact = frame.getChckbxDropAmountExact().isSelected();

		manipulateXp = frame.getChckbxManipulateXp().isSelected();
		if (manipulateXp) {
			try {
				xpDropAmount = Integer.parseInt(frame.getXpDroppedTextField().getText());
			} catch (NumberFormatException e) {
				setErrorMessage("XP drop amount invalid");
				return false;
			}
			xpDropAmountExact = frame.getChckbxXpExact().isSelected();
		}

		try {
			fortuneLevel = Integer.parseInt(frame.getFortuneLevelTextField().getText());
			if (fortuneLevel < 0) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			setErrorMessage("Fortune level invalid");
			return false;
		}

		return true;
	}

	@Override
	protected Optional<String> testRegion(int x, int z) {
		int quantityDropped = ore.quantityDropped(fortuneLevel, rand);
		boolean quantityDroppedWanted;
		if (dropAmountExact) {
			quantityDroppedWanted = dropAmount == quantityDropped;
		} else {
			quantityDroppedWanted = quantityDropped > bestQuantityDropped;
		}
		if (quantityDroppedWanted) {
			bestXpDropped = -1;
			bestQuantityDropped = quantityDropped;
		}

		boolean xpDroppedWanted;
		int xpDropped = ore.xpDropped(rand);
		if (!manipulateXp) {
			xpDroppedWanted = false;
		} else if (xpDropAmountExact) {
			xpDroppedWanted = xpDropAmount == xpDropped;
		} else {
			xpDroppedWanted = xpDropped > bestXpDropped;
		}
		xpDroppedWanted &= quantityDropped >= bestQuantityDropped;
		if (xpDroppedWanted) {
			bestXpDropped = xpDropped;
		}

		if (!quantityDroppedWanted && !xpDroppedWanted) {
			return Optional.empty();
		}

		if (quantityDroppedWanted && (!manipulateXp || xpDroppedWanted) && quantityDropped >= dropAmount
				&& xpDropped >= xpDropAmount) {
			stop();
		}

		return Optional.of(String.format("Quantity = %d, XP = %d", quantityDropped, xpDropped));
	}

	public static enum Ore {
		COAL(0, 2), DIAMOND(3, 7), REDSTONE(1, 5), LAPIS(2, 5), EMERALD(3, 7), QUARTZ(2, 5);

		private int minXp;
		private int maxXp;

		private Ore(int minXp, int maxXp) {
			this.minXp = minXp;
			this.maxXp = maxXp;
		}

		public int quantityDropped(int fortune, Random rand) {
			if (this == REDSTONE) {
				return 4 + rand.nextInt(2) + rand.nextInt(fortune + 1);
			}
			int fortuneBonus;
			if (fortune == 0) {
				fortuneBonus = 0;
			} else {
				fortuneBonus = rand.nextInt(fortune + 2) - 1;
				if (fortuneBonus < 0)
					fortuneBonus = 0;
			}
			if (this == LAPIS) {
				return (4 + rand.nextInt(5)) * (fortuneBonus + 1);
			} else {
				return 1 + fortuneBonus;
			}
		}

		public int xpDropped(Random rand) {
			return minXp + rand.nextInt(maxXp - minXp + 1);
		}
	}

}
