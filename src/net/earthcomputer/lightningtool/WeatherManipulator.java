package net.earthcomputer.lightningtool;

import java.util.Optional;

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
	private int minTotalErrorRain = Integer.MAX_VALUE;
	private int minTotalErrorThunder = Integer.MAX_VALUE;

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
	protected Optional<String> testRegion(int x, int z) {
		int totalError = 0;
		String result = null;
		int thunderTime = Integer.MAX_VALUE;
		int rainTime = Integer.MAX_VALUE;
		if (shouldManipulateThunder) {
			if (thunderTurningOn) {
				thunderTime = rand.nextInt(THUNDER_ON_RANGE) + THUNDER_ON_BASE;
			} else {
				thunderTime = rand.nextInt(THUNDER_OFF_RANGE) + THUNDER_OFF_BASE;
			}
			result = "thunder = " + thunderTime;
			totalError = Math.max(totalError, Math.abs(targetThunderTime - thunderTime));
		}
		if (shouldManipulateRain) {
			if (rainTurningOn) {
				rainTime = rand.nextInt(RAIN_ON_RANGE) + RAIN_ON_BASE;
			} else {
				rainTime = rand.nextInt(RAIN_OFF_RANGE) + RAIN_OFF_BASE;
			}
			if (result == null)
				result = "rain = " + rainTime;
			else
				result += ", rain = " + rainTime;
			totalError = Math.max(totalError, Math.abs(rainTime - targetRainTime));
		}

		boolean goodResult = false;
		if (rainTime <= thunderTime && totalError < minTotalErrorRain) {
			minTotalErrorRain = totalError;
			goodResult = true;
		}
		if (thunderTime <= rainTime && totalError < minTotalErrorThunder) {
			minTotalErrorThunder = totalError;
			goodResult = true;
		}

		if (goodResult)
			return Optional.of(result);
		else
			return Optional.empty();
	}

}
