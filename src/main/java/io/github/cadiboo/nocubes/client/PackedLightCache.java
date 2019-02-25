package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.XYZCache;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
@OnlyIn(Dist.CLIENT)
public class PackedLightCache extends XYZCache implements AutoCloseable {

	private static final ThreadLocal<PackedLightCache> POOL = ThreadLocal.withInitial(() -> new PackedLightCache(0, 0, 0));

	@Nonnull
	private int[] cache;

	private PackedLightCache(final int sizeX, final int sizeY, final int sizeZ) {
		super(sizeX, sizeY, sizeZ);
		cache = new int[sizeX * sizeY * sizeZ];
	}

	@Nonnull
	public int[] getPackedLightCache() {
		return cache;
	}

	@Nonnull
	public static PackedLightCache retain(final int sizeX, final int sizeY, final int sizeZ) {

		final PackedLightCache pooled = POOL.get();

		if (pooled.sizeX == sizeX && pooled.sizeY == sizeY && pooled.sizeZ == sizeZ) {
			return pooled;
		}

		pooled.sizeX = sizeX;
		pooled.sizeY = sizeY;
		pooled.sizeZ = sizeZ;

		final int size = sizeX * sizeY * sizeZ;

		if (pooled.cache.length < size) {
			pooled.cache = new int[size];
		}

		return pooled;
	}

	@Override
	public void close() {
	}

}