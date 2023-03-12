package me.redned.simcraft.util.heightmap;

import org.cloudburstmc.math.GenericMath;

/** Heightmap (heightfield) implementation with adjustable height/width scaling and iterative smoothing
 * @author jrenner */
public class HeightMap {
	/** 2d array of heights for each data point */
	private final float[][] heights;
	/** vertical scaling factor for height */
	private final float heightScale;
	/** scaling for width on the x-z plane (distance between each data point) */
	private final float widthScale;
	/** computed minimum height in the heightmap */
	private float min;
	/** computed maximum height in the heightmap */
	private float max;
	/** width (x) of the height map in data points, not world units */
	private int width;
	/** depth (z) of the height map in data points, not world units */
	private int depth;
	/** center of the heightmap in world units */
	private int centerX;
	private int centerY;
	private int centerZ;

	/** number of data points */
	private int numPoints;

	public HeightMap(float[][] heightMap, int width, int height) {
		this(heightMap, width, height, 0);
	}

	public HeightMap(float[][] heightMap, int width, int height, int smoothingPasses) {
		this(heightMap, width, height, 1.0F, 1.0F, smoothingPasses);
	}

	public HeightMap(float[][] heightMap, int width, int height, float heightScale, float widthScale, int smoothingPasses) {
		this.heightScale = heightScale;
		this.widthScale = widthScale;

		this.heights = new float[height << 4][width << 4];

		for (int z = 0; z < this.heights.length; z++) {
			for (int x = 0; x < this.heights[z].length; x++) {
				this.heights[z][x] = heightMap[z >> 4][x >> 4];
			}
		}
		
		smoothVertexPositions(smoothingPasses);
		updateDimensions();
	}

	private void setMinMaxHeights() {
		min = Float.MAX_VALUE;
		max = Float.MIN_VALUE;
		for (int z = 0; z < heights.length; z++) {
			for (int x = 0; x < heights[0].length; x++) {
				float y = heights[z][x];
				if (y < min) min = y;
				if (y > max) max = y;
			}
		}
	}

	private void updateDimensions() {
		setMinMaxHeights();
		setCenter();
	}

	/** Create smoother terrain using averaging of height values
	 * @param passes number of smoothing passes (higher = smoother) */
	private void smoothVertexPositions(int passes) {
		for (int i = 0; i < passes; i++) {
			// smooth along x
			for (int z = 0; z < heights.length; z++) {
				for (int x = 1; x < heights[z].length - 1; x += 1) {
					float prev = heights[z][x - 1];
					float y = heights[z][x];
					float next = heights[z][x + 1];
					float yAvg = (next + prev) / 2f;
					heights[z][x] = (y + yAvg) / 2f;
				}
			}
			// smooth along z
			for (int x = 0; x < heights[0].length; x++) {
				for (int z = 1; z < heights.length - 1; z += 1) {
					float prev = heights[z - 1][x];
					float y = heights[z][x];
					float next = heights[z + 1][x];
					float yAvg = (next + prev) / 2f;
					heights[z][x] = (y + yAvg) / 2f;
				}
			}
		}
		updateDimensions();
	}

	private void setCenter() {
		this.centerX = GenericMath.floor(getWidthWorld() / 2);
		this.centerY = GenericMath.floor((min + max) / 2);
		this.centerZ = GenericMath.floor(getDepthWorld() / 2);
	}

	public float[][] getData() {
		return heights;
	}

	public int getNumberOfPoints() {
		return numPoints;
	}

	/** get the vertical height scaling */
	public float getHeightScale() {
		return heightScale;
	}

	/** get the width between data points in world units */
	public float getWidthScale() {
		return widthScale;
	}

	/** parameters assume heightmap's origin is at world coordinates x:0, z: 0
	 * @return true if point (x,z) is within the bounds of this heightmap */
	public boolean containsPoint(float xf, float zf) {
		xf /= widthScale;
		zf /= widthScale;
		int x = (int) Math.floor(xf);
		int z = (int) Math.floor(zf);
		if (x < 0 || z < 0) return false;
		if (z > heights.length - 1) return false;
		if (x > heights[z].length - 1) return false;
		return true;
	}

	/** get height for single point at x,z coords, accounting for scale, does not interpolate using neighbors
	 *  parameters assume heightmap starts at origin 0,0 */
	public float getHeight(float xf, float zf) {
		int x = worldCoordToIndex(xf);
		int z = worldCoordToIndex(zf);
		if (x < 0) x = 0;
		if (z < 0) z = 0;
		if (z >= heights.length) {
			z = heights.length - 1;
		}
		if (x >= heights[z].length) {
			x = heights[z].length - 1;
		}
		return heights[z][x] * heightScale;
	}

	/**
	 * Get the interpolated height for x,z coords, accounting for scale, interpolated using neighbors.
	 * This will give the interpolated height when the parameters lie somewhere between actual heightmap data points.
	 * parameters assume heightmap's origin is at world coordinates x:0, z: 0
	 * @return the scale-adjusted interpolated height at specified world coordinates */
	public float getInterpolatedHeight(float xf, float zf) {
		int ay;
		int by;
		int cy;
		int dy;

		float baseX = (float) Math.floor(xf / widthScale);
		float baseZ = (float) Math.floor(zf / widthScale);
		float x = baseX * widthScale;
		float z = baseZ * widthScale;
		float x2 = x + widthScale;
		float z2 = z + widthScale;

		ay = GenericMath.floor(getHeight(x, z2));
		by = GenericMath.floor(getHeight(x, z));
		cy = GenericMath.floor(getHeight(x2, z));
		dy = GenericMath.floor(getHeight(x2, z2));

		float zFrac = 1f - (zf - z) / widthScale;
		float xFrac = (xf - x) / widthScale;

		return (1f - zFrac) * ((1-xFrac) * ay + xFrac * dy)
			  + zFrac * ((1-xFrac) * by + xFrac * cy);
	}

	private int worldCoordToIndex(float f) {
		return (int) Math.floor(f / widthScale);
	}

	/** @return lowest elevation in the height map */
	public float getMin() {
		return min;
	}

	/** @return height elevation in the height map */
	public float getMax() {
		return max;
	}

	/** @return length of the map on the x axis in number of points */
	public int getWidth() {
		return width;
	}

	/** @return length of the map on the z axis in number of points */
	public int getDepth() {
		return depth;
	}

	/** @return length of the map on the x axis in world units */
	public float getWidthWorld() {
		return width * widthScale;
	}

	/** @return length of the map on the z axis in world units */
	public float getDepthWorld() {
		return depth * widthScale;
	}

	/** @return the number of data points in the height map */
	public int getNumPoints() {
		return width * depth;
	}

	@Override
	public String toString() {
		return String.format("[HeightMap] min/max: %.1f, %.1f - World w/h: %.1f, %.1f - points: %d, center: %.1f, %.1f, %.1f",
				min, max, getWidthWorld(), getDepthWorld(), getNumPoints(), this.centerX, this.centerY, this.centerZ);
	}
}