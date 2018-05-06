package net.earthcomputer.lightningtool;

import java.awt.Color;

public class Scheduler {

	public static final int BED_TIME = 100;

	private MainFrame frame;
	private int thunderOffTime;
	private int thunderOnTime;
	private int rainOffTime;
	private int rainOnTime;
	private boolean thunderOffExtraRand;
	private boolean thunderOnExtraRand;
	private boolean rainOffExtraRand;
	private boolean rainOnExtraRand;
	/**
	 * The time between when the slime block piston is fired and tile entities
	 * are first ticked in the target area
	 */
	private int signalDelay;

	public boolean readFromFrame(MainFrame frame) {
		this.frame = frame;

		try {
			thunderOffTime = Integer.parseInt(frame.getThunderOffTimeTextField().getText());
			if (thunderOffTime < WeatherManipulator.THUNDER_OFF_BASE
					|| thunderOffTime > WeatherManipulator.THUNDER_OFF_MAX) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			setErrorMessage("Thunder off-time invalid");
			return false;
		}

		try {
			thunderOnTime = Integer.parseInt(frame.getThunderOnTimeTextField().getText());
			if (thunderOnTime < WeatherManipulator.THUNDER_ON_BASE
					|| thunderOnTime > WeatherManipulator.THUNDER_ON_MAX) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			setErrorMessage("Thunder on-time invalid");
			return false;
		}

		try {
			rainOffTime = Integer.parseInt(frame.getRainOffTimeTextField().getText());
			if (rainOffTime < WeatherManipulator.RAIN_OFF_BASE || rainOffTime > WeatherManipulator.RAIN_OFF_MAX) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			setErrorMessage("Rain off-time invalid");
			return false;
		}

		try {
			rainOnTime = Integer.parseInt(frame.getRainOnTimeTextField().getText());
			if (rainOnTime < WeatherManipulator.RAIN_ON_BASE || rainOnTime > WeatherManipulator.RAIN_ON_MAX) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			setErrorMessage("Rain on-time invalid");
			return false;
		}

		thunderOffExtraRand = frame.getChckbxThunderOffUsesExtraRand().isSelected();
		thunderOnExtraRand = frame.getChckbxThunderOnUsesExtraRand().isSelected();
		rainOffExtraRand = frame.getChckbxRainOffUsesExtraRand().isSelected();
		rainOnExtraRand = frame.getChckbxRainOnUsesExtraRand().isSelected();

		try {
			signalDelay = Integer.parseInt(frame.getMessageSendTimeTextField().getText());
			if (signalDelay < 0 || signalDelay > BED_TIME) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			setErrorMessage("Signal delay invalid");
			return false;
		}

		return true;
	}

	private void setErrorMessage(String message) {
		frame.getLblOutput().setForeground(Color.RED);
		frame.getLblOutput().setText(message);
	}

	public void calcSchedule() {
		frame.getOutputTextArea().setText("");
		
		int prepareForRainThunderOff = BED_TIME;
		int sendRainThunderOffTime = prepareForRainThunderOff - signalDelay + 1;
		if (thunderOffExtraRand || rainOffExtraRand)
			sendRainThunderOffTime--;
		addOutput("Send thunder+rain off signal at " + sendRainThunderOffTime);
		
		int prepareForThunderOn = (prepareForRainThunderOff + 1) + thunderOffTime;
		int sendThunderOnTime = prepareForThunderOn - signalDelay + 1;
		if (thunderOnExtraRand)
			sendThunderOnTime--;
		addOutput("Send thunder on signal at " + sendThunderOnTime);
		
		int prepareForRainOn = (prepareForRainThunderOff + 1) + rainOffTime;
		int sendRainOnTime = prepareForRainOn - signalDelay + 1;
		if (rainOnExtraRand)
			sendRainOnTime--;
		addOutput("Send rain on signal at " + sendRainOnTime);
		
		int nextSleepTime = (prepareForThunderOn + 1) + thunderOnTime;
		nextSleepTime -= BED_TIME;
		nextSleepTime -= 3; // For piston retraction
		addOutput("Next sleep time at " + nextSleepTime);
	}
	
	private void addOutput(String message) {
		frame.getOutputTextArea().append(message + "\n");
		frame.getLblOutput().setForeground(Color.BLACK);
		frame.getLblOutput().setText("See output for results");
	}

}
