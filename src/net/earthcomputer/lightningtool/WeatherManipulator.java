package net.earthcomputer.lightningtool;

import java.util.ArrayList;
import java.util.List;

import net.earthcomputer.lightningtool.SearchResult.Property;

public class WeatherManipulator extends AbstractManipulator {

	public static final int THUNDER_OFF_RANGE = 168000;
	public static final int THUNDER_ON_RANGE = 12000;
	public static final int RAIN_OFF_RANGE = 168000;
	public static final int RAIN_ON_RANGE = 12000;
	public static final int THUNDER_OFF_BASE = 12000;
	public static final int THUNDER_ON_BASE = 3600;
	public static final int RAIN_OFF_BASE = 12000;
	public static final int RAIN_ON_BASE = 12000;
	public static final int THUNDER_OFF_MAX = THUNDER_OFF_BASE + THUNDER_OFF_RANGE - 1;
	public static final int THUNDER_ON_MAX = THUNDER_ON_BASE + THUNDER_ON_RANGE - 1;
	public static final int RAIN_OFF_MAX = RAIN_OFF_BASE + RAIN_OFF_RANGE - 1;
	public static final int RAIN_ON_MAX = RAIN_ON_BASE + RAIN_ON_RANGE - 1;

	private boolean shouldManipulateThunder;
	private boolean thunderTurningOn;
	private int targetThunderTime;
	private boolean shouldManipulateRain;
	private boolean rainTurningOn;
	private int targetRainTime;

	private Property<Integer> thunderTimeProperty;
	private Property<Integer> rainTimeProperty;
	private Property<Long> combinedProperty;

	public static final RNGAdvancer<?>[] ADVANCERS = { RNGAdvancer.HOPPER };

	@Override
	protected boolean parseExtra() {
		shouldManipulateThunder = frame.getChckbxManipulateThunder().isSelected();
		if (shouldManipulateThunder) {
			thunderTurningOn = frame.getRdbtnThunderTurningOn().isSelected();
			try {
				targetThunderTime = Integer.parseInt(frame.getThunderTimeTextField().getText());
				if (thunderTurningOn) {
					if (targetThunderTime < THUNDER_ON_BASE || targetThunderTime > THUNDER_ON_MAX) {
						throw new NumberFormatException();
					}
				} else {
					if (targetThunderTime < THUNDER_OFF_BASE || targetThunderTime > THUNDER_OFF_MAX) {
						throw new NumberFormatException();
					}
				}
			} catch (NumberFormatException e) {
				setErrorMessage("Invalid thunder time");
				return false;
			}
		}

		shouldManipulateRain = frame.getChckbxManipulateRain().isSelected();
		if (shouldManipulateRain) {
			rainTurningOn = frame.getRdbtnRainTurningOn().isSelected();
			try {
				targetRainTime = Integer.parseInt(frame.getRainTimeTextField().getText());
				if (rainTurningOn) {
					if (targetRainTime < RAIN_ON_BASE || targetRainTime > RAIN_ON_MAX) {
						throw new NumberFormatException();
					}
				} else {
					if (targetRainTime < RAIN_OFF_BASE || targetRainTime > RAIN_OFF_MAX) {
						throw new NumberFormatException();
					}
				}
			} catch (NumberFormatException e) {
				setErrorMessage("Invalid rain time");
				return false;
			}
		}

		if (!shouldManipulateThunder && !shouldManipulateRain) {
			setErrorMessage("Neither rain nor thunder are selected");
			return false;
		}

		return true;
	}

	@Override
	protected SearchResult testRegion(int x, int z) {
		SearchResult result = createSearchResult();
		int thunderTime = 0;
		int rainTime = 0;
		if (shouldManipulateThunder) {
			if (thunderTurningOn) {
				thunderTime = rand.nextInt(THUNDER_ON_RANGE) + THUNDER_ON_BASE;
			} else {
				thunderTime = rand.nextInt(THUNDER_OFF_RANGE) + THUNDER_OFF_BASE;
			}
			result = result.withProperty(thunderTimeProperty, thunderTime);
		}
		if (shouldManipulateRain) {
			if (rainTurningOn) {
				rainTime = rand.nextInt(RAIN_ON_RANGE) + RAIN_ON_BASE;
			} else {
				rainTime = rand.nextInt(RAIN_OFF_RANGE) + RAIN_OFF_BASE;
			}
			result = result.withProperty(rainTimeProperty, rainTime);
		}
		if (shouldManipulateThunder && shouldManipulateRain) {
			long r = (long)targetRainTime - rainTime;
			long t = (long)targetThunderTime - thunderTime;
			long weight = r*r + t*t;
			result = result.withProperty(combinedProperty, weight);
		}
		return result;
	}

	@Override
	protected SearchResult createSearchResult() {
		List<Property<?>> properties = new ArrayList<>();
		properties.add(DISTANCE);
		boolean both = shouldManipulateThunder && shouldManipulateRain;
		if (shouldManipulateThunder) {
			if (thunderTimeProperty == null)
				thunderTimeProperty = Property.create("thunder", targetThunderTime, Integer.MAX_VALUE,
						both ? Property.indifferent() : Property.distanceTo(targetThunderTime));
			properties.add(thunderTimeProperty);
		}
		if (shouldManipulateRain) {
			if (rainTimeProperty == null)
				rainTimeProperty = Property.create("rain", targetRainTime, Integer.MAX_VALUE,
						both ? Property.indifferent() : Property.distanceTo(targetRainTime));
			properties.add(rainTimeProperty);
		}
		if (both) {
			if (combinedProperty == null) {
				combinedProperty = Property.create("weight", (long)0, Long.MAX_VALUE, Property.minimize());
			}
			properties.add(combinedProperty);
		}
		advancer.addExtraProperties(properties);
		return new SearchResult(properties);
	}

}
