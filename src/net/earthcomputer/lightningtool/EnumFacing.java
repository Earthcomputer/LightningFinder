package net.earthcomputer.lightningtool;

public enum EnumFacing {

	DOWN(0, -1, 0), UP(0, 1, 0), WEST(-1, 0, 0), EAST(1, 0, 0), NORTH(0, 0, -1), SOUTH(0, 0, 1);
	
	private final int dx, dy, dz;
	private EnumFacing(int dx, int dy, int dz) {
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
	}
	
	public int getDX() {
		return dx;
	}
	
	public int getDY() {
		return dy;
	}
	
	public int getDZ() {
		return dz;
	}
	
	public static EnumFacing[] horizontal() {
		return new EnumFacing[] { NORTH, EAST, SOUTH, WEST };
	}
	
}
