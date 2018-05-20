package net.earthcomputer.lightningtool;

public final class BlockPos {

	private int x, y, z;

	public BlockPos(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	@Override
	public int hashCode() {
		return (x + 31 * y) * 31 + z;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof BlockPos && equals((BlockPos) other);
	}

	public boolean equals(BlockPos other) {
		return x == other.x && y == other.y && z == other.z;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}

}
