package net.earthcomputer.lightningtool;

import java.awt.Color;

public class WirelessTool {

	private WirelessTool() {
	}
	
	public static final RNGAdvancer<?>[] ADVANCERS = { RNGAdvancer.DISPENSER };
	
	public static void recalculate(MainFrame frame) {
		// Read input
		long worldSeed = AbstractManipulator.parseWorldSeed(frame.getWorldSeedTextField().getText());
		int regionX, regionZ;
		try {
			regionX = Integer.parseInt(frame.getSearchFromXTextField().getText());
			regionZ = Integer.parseInt(frame.getSearchFromZTextField().getText());
		} catch (NumberFormatException e) {
			setErrorMessage(frame, "Invalid \"from\" coordinates");
			return;
		}
		regionX += 128;
		regionZ += 128;
		if (regionX < 0)
			regionX -= 80 * 16 - 1;
		if (regionZ < 0)
			regionZ -= 80 * 16 - 1;
		regionX /= 80 * 16;
		regionZ /= 80 * 16;
		int bits;
		try {
			bits = Integer.parseInt(frame.getWirelessBitsTextField().getText());
			if (bits < 1 || bits > 48)
				throw new NumberFormatException();
		} catch (NumberFormatException e) {
			setErrorMessage(frame, "Wireless bits invalid");
			return;
		}
		int receivers;
		try {
			receivers = Integer.parseInt(frame.getReceiverCountTextField().getText());
			if (receivers < 1 || receivers > 64)
				throw new NumberFormatException();
		} catch (NumberFormatException e) {
			setErrorMessage(frame, "Receiver count invalid");
			return;
		}
		EnumFacing dispenserDirection = (EnumFacing) frame.getDispenserDirectionComboBox().getSelectedItem();
		
		frame.getOutputTextArea().setText("");
		
		// Setup RNG
		ResettableRandom worldRand = new ResettableRandom();
		AbstractManipulator.resetSeed(worldRand, regionX, regionZ, worldSeed);
		
		frame.getRNGAdvancerParameterHandler().readFromPanel();
		int extraRand = ((RNGAdvancer.SimpleRNGAdvancer.SimpleParameterHandler) frame.getRNGAdvancerParameterHandler()).getMaxExtraRandCalls();
		for (int i = 0; i < extraRand; i++) {
			RNGAdvancer.DISPENSER.advance(worldRand);
		}
		
		// Simulate
		for (int receiver = 0; receiver < receivers; receiver++) {
			StringBuilder sb = new StringBuilder();
			for (int bit = 0; bit < bits; bit++) {
				if (bit != 0 && bit % 4 == 0)
					sb.append(" ");
				if (intersectsTripwire(worldRand, dispenserDirection))
					sb.append("1");
				else
					sb.append("0");
			}
			if (receiver == 0) {
				frame.getLblOutput().setForeground(Color.BLACK);
				frame.getLblOutput().setText(sb.toString());
			}
			frame.getOutputTextArea().append((receiver + 1) + ": " + sb + "\n");
		}
	}
	
	private static boolean intersectsTripwire(ResettableRandom rand, EnumFacing dispenserDirection) {
		// Initialize item with dispenser settings
		double yPos = 0.5 - 0.15625;
		double forwardPos = 0.2;
		double yMotion, forwardMotion;
		
		forwardMotion = rand.nextDouble() * 0.1 + 0.2;
		yMotion = 0.20000000298023224;
		if (dispenserDirection.getDX() != 0) {
			forwardMotion += rand.nextGaussian() * 0.007499999832361937 * 6;
			yMotion += rand.nextGaussian() * 0.007499999832361937 * 6;
		} else {
			yMotion += rand.nextGaussian() * 0.007499999832361937 * 6;
			forwardMotion += rand.nextGaussian() * 0.007499999832361937 * 6;
		}
		rand.nextGaussian();
		
		// Simulate item to see if it intersects tripwire
		while (yMotion >= 0 && (forwardPos + 0.125 < 1 || yPos >= 1)) {
			// water movement in base tick
			if (yPos < 1)
				forwardMotion += 0.014;
			// gravity
			yMotion -= 0.03999999910593033;
			// motion
			forwardPos += forwardMotion;
			yPos += yMotion;
			// collide with tripwire
			if (yPos + 0.25 - 0.001 >= 1 && forwardPos - 0.125 + 0.001 < 1)
				return true;
			// friction
			forwardMotion *= 0.98;
			yMotion *= 0.9800000190734863;
			// water movement
			if (yPos < 1)
				forwardMotion += 0.014;
		}
		return false;
	}
	
	private static void setErrorMessage(MainFrame frame, String message) {
		frame.getLblOutput().setForeground(Color.RED);
		frame.getLblOutput().setText(message);
	}
	
}
