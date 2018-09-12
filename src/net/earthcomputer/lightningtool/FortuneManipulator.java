package net.earthcomputer.lightningtool;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.earthcomputer.lightningtool.SearchResult.Property;

public class FortuneManipulator extends AbstractManipulator {

	private Ore ore;
	private int dropAmount;
	private boolean dropAmountExact;
	private boolean manipulateXp;
	private int xpDropAmount = -1;
	private boolean xpDropAmountExact;
	private int fortuneLevel;

	private Property<Integer> dropAmountProperty;
	private Property<Integer> xpDropAmountProperty;

	public static final RNGAdvancer<?>[] ADVANCERS = { RNGAdvancer.HOPPER };

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
	protected SearchResult testRegion(int x, int z) {
		int quantityDropped = ore.quantityDropped(fortuneLevel, rand);
		int xpDropped = ore.xpDropped(rand);

		return createSearchResult().withProperty(dropAmountProperty, quantityDropped).withProperty(xpDropAmountProperty,
				xpDropped);
	}

	@Override
	protected SearchResult createSearchResult() {
		List<Property<?>> properties = new ArrayList<>();
		properties.add(DISTANCE);
		if (dropAmountProperty == null) {
			if (dropAmountExact) {
				dropAmountProperty = Property.create("drop amount", dropAmount, 0, Property.distanceTo(dropAmount));
			} else {
				dropAmountProperty = Property.create("drop amount", dropAmount, 0, Property.maximize());
			}
		}
		properties.add(dropAmountProperty);
		if (xpDropAmountProperty == null) {
			if (manipulateXp) {
				if (xpDropAmountExact) {
					xpDropAmountProperty = Property.create("xp", xpDropAmount, 0, Property.distanceTo(xpDropAmount));
				} else {
					xpDropAmountProperty = Property.create("xp", xpDropAmount, 0, Property.maximize());
				}
			} else {
				xpDropAmountProperty = Property.create("xp", 0, 0, Property.indifferent());
			}
		}
		properties.add(xpDropAmountProperty);
		advancer.addExtraProperties(properties);
		return new SearchResult(properties);
	}

	public static enum Ore {
		COAL(0, 2), DIAMOND(3, 7), REDSTONE(1, 5), LAPIS(2, 5), EMERALD(3, 7), QUARTZ(2, 5), SAPLING(0, 0), POTATO(0, 0), NETHERWART(0, 0);

		private int minXp;
		private int maxXp;

		private Ore(int minXp, int maxXp) {
			this.minXp = minXp;
			this.maxXp = maxXp;
		}

		public int quantityDropped(int fortune, Random rand) {
		    int fortuneBonus = 0;
		    if(this == SAPLING){ // saplings
		        return rand.nextInt(20) == 0 ? 1 : 0;
		    }
		    if(this == POTATO){ // potato
		        return rand.nextInt(50) == 0 ? 1 : 0;
		    }
		    if(this == NETHERWART){ // netherwart
		        fortuneBonus =+ 2 + rand.nextInt(3);
		        
                if (fortune > 0)
                {
                    fortuneBonus =+ rand.nextInt(fortune + 1);
                }
                return fortuneBonus;
            }
			if (this == REDSTONE) {
				return 4 + rand.nextInt(2) + rand.nextInt(fortune + 1);
			}
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
