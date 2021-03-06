package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.client.render.SmoothLightingFluidBlockRenderer;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.cache.SmoothableCache;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.ChunkRender;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.client.renderer.chunk.ChunkRenderTask;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;

import javax.annotation.Nonnull;
import java.util.Arrays;

import static io.github.cadiboo.nocubes.util.StateHolder.GRASS_BLOCK_DEFAULT;
import static io.github.cadiboo.nocubes.util.StateHolder.GRASS_BLOCK_SNOWY;
import static io.github.cadiboo.nocubes.util.StateHolder.PODZOL_SNOWY;
import static io.github.cadiboo.nocubes.util.StateHolder.SNOW_LAYER_DEFAULT;

/**
 * Util that is only used on the Physical Client i.e. Rendering code
 *
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
public final class ClientUtil {

	public static final BlockRenderLayer[] BLOCK_RENDER_LAYER_VALUES = BlockRenderLayer.values();
	public static final int BLOCK_RENDER_LAYER_VALUES_LENGTH = BLOCK_RENDER_LAYER_VALUES.length;
	static final int[] NEGATIVE_1_8000 = new int[8000];
	private static final int[][] OFFSETS_ORDERED = {
			// check 6 immediate neighbours
			{+0, -1, +0},
			{+0, +1, +0},
			{-1, +0, +0},
			{+1, +0, +0},
			{+0, +0, -1},
			{+0, +0, +1},
			// check 12 non-immediate, non-corner neighbours
			{-1, -1, +0},
			{-1, +0, -1},
			{-1, +0, +1},
			{-1, +1, +0},
			{+0, -1, -1},
			{+0, -1, +1},
			// {+0, +0, +0}, // Don't check self
			{+0, +1, -1},
			{+0, +1, +1},
			{+1, -1, +0},
			{+1, +0, -1},
			{+1, +0, +1},
			{+1, +1, +0},
			// check 8 corner neighbours
			{+1, +1, +1},
			{+1, +1, -1},
			{-1, +1, +1},
			{-1, +1, -1},
			{+1, -1, +1},
			{+1, -1, -1},
			{-1, -1, +1},
			{-1, -1, -1},
	};
	static {
		Arrays.fill(ClientUtil.NEGATIVE_1_8000, -1);
	}

	/**
	 * Returns a state and sets the texturePooledMutablePos to the pos it found
	 *
	 * @return a state
	 */
	@Nonnull
	public static BlockState getTexturePosAndState(
			final int posX, final int posY, final int posZ,
			@Nonnull final PooledMutableBlockPos texturePooledMutablePos,
			@Nonnull final StateCache stateCache,
			@Nonnull final SmoothableCache smoothableCache,
			final byte relativePosX, final byte relativePosY, final byte relativePosZ,
			final boolean tryForBetterTexturesSnow, final boolean tryForBetterTexturesGrass
	) {

		final boolean[] smoothableCacheArray = smoothableCache.getSmoothableCache();
		final BlockState[] blockCacheArray = stateCache.getBlockStates();

		final int stateCacheStartPaddingX = stateCache.startPaddingX;
		final int stateCacheStartPaddingY = stateCache.startPaddingY;
		final int stateCacheStartPaddingZ = stateCache.startPaddingZ;

		final int stateCacheSizeX = stateCache.sizeX;
		final int stateCacheSizeY = stateCache.sizeY;

		if (Config.betterTextures) {
			if (tryForBetterTexturesSnow) {
				try (final ModProfiler ignored = ModProfiler.get().start("getTexturePosAndState-tryForBetterTextures-snow")) {
					BlockState betterTextureState = blockCacheArray[stateCache.getIndex(
							relativePosX + stateCacheStartPaddingX,
							relativePosY + stateCacheStartPaddingY,
							relativePosZ + stateCacheStartPaddingZ,
							stateCacheSizeX, stateCacheSizeY
					)];

					if (isStateSnow(betterTextureState)) {
						texturePooledMutablePos.setPos(posX, posY, posZ);
						return betterTextureState;
					}
					for (int[] offset : OFFSETS_ORDERED) {
						betterTextureState = blockCacheArray[stateCache.getIndex(
								relativePosX + offset[0] + stateCacheStartPaddingX,
								relativePosY + offset[1] + stateCacheStartPaddingY,
								relativePosZ + offset[2] + stateCacheStartPaddingZ,
								stateCacheSizeX, stateCacheSizeY
						)];
						if (isStateSnow(betterTextureState)) {
							texturePooledMutablePos.setPos(posX + offset[0], posY + offset[1], posZ + offset[2]);
							return betterTextureState;
						}
					}
				}
			}
			if (tryForBetterTexturesGrass) {
				try (final ModProfiler ignored = ModProfiler.get().start("getTexturePosAndState-tryForBetterTextures-grass")) {
					BlockState betterTextureState = blockCacheArray[stateCache.getIndex(
							relativePosX + stateCacheStartPaddingX,
							relativePosY + stateCacheStartPaddingY,
							relativePosZ + stateCacheStartPaddingZ,
							stateCacheSizeX, stateCacheSizeY
					)];

					if (isStateGrass(betterTextureState)) {
						texturePooledMutablePos.setPos(posX, posY, posZ);
						return betterTextureState;
					}
					for (int[] offset : OFFSETS_ORDERED) {
						betterTextureState = blockCacheArray[stateCache.getIndex(
								relativePosX + offset[0] + stateCacheStartPaddingX,
								relativePosY + offset[1] + stateCacheStartPaddingY,
								relativePosZ + offset[2] + stateCacheStartPaddingZ,
								stateCacheSizeX, stateCacheSizeY
						)];
						if (isStateGrass(betterTextureState)) {
							texturePooledMutablePos.setPos(posX + offset[0], posY + offset[1], posZ + offset[2]);
							return betterTextureState;
						}
					}
				}
			}
		}

		final int smoothableCacheStartPaddingX = smoothableCache.startPaddingX;
		final int smoothableCacheStartPaddingY = smoothableCache.startPaddingY;
		final int smoothableCacheStartPaddingZ = smoothableCache.startPaddingZ;

		final int smoothableCacheSizeX = smoothableCache.sizeX;
		final int smoothableCacheSizeY = smoothableCache.sizeY;

		try (final ModProfiler ignored = ModProfiler.get().start("getTexturePosAndState")) {

			// If pos passed in is smoothable return state from that pos
			if (smoothableCacheArray[smoothableCache.getIndex(
					relativePosX + smoothableCacheStartPaddingX,
					relativePosY + smoothableCacheStartPaddingY,
					relativePosZ + smoothableCacheStartPaddingZ,
					smoothableCacheSizeX, smoothableCacheSizeY
			)]) {
				texturePooledMutablePos.setPos(posX, posY, posZ);
				return blockCacheArray[stateCache.getIndex(
						relativePosX + stateCacheStartPaddingX,
						relativePosY + stateCacheStartPaddingY,
						relativePosZ + stateCacheStartPaddingZ,
						stateCacheSizeX, stateCacheSizeY
				)];
			}

			// Start at state of pos passed in
			BlockState state = blockCacheArray[stateCache.getIndex(
					relativePosX + stateCacheStartPaddingX,
					relativePosY + stateCacheStartPaddingY,
					relativePosZ + stateCacheStartPaddingZ,
					stateCacheSizeX, stateCacheSizeY
			)];

			for (int[] offset : OFFSETS_ORDERED) {
				if (smoothableCacheArray[smoothableCache.getIndex(
						relativePosX + offset[0] + smoothableCacheStartPaddingX,
						relativePosY + offset[1] + smoothableCacheStartPaddingY,
						relativePosZ + offset[2] + smoothableCacheStartPaddingZ,
						smoothableCacheSizeX, smoothableCacheSizeY
				)]) {
					texturePooledMutablePos.setPos(posX + offset[0], posY + offset[1], posZ + offset[2]);
					state = blockCacheArray[stateCache.getIndex(
							relativePosX + offset[0] + stateCacheStartPaddingX,
							relativePosY + offset[1] + stateCacheStartPaddingY,
							relativePosZ + offset[2] + stateCacheStartPaddingZ,
							stateCacheSizeX, stateCacheSizeY
					)];
					break;
				}
			}
			return state;
		}
	}

	public static boolean isStateSnow(final BlockState checkState) {
		if (checkState == SNOW_LAYER_DEFAULT) return true;
		if (checkState == GRASS_BLOCK_SNOWY) return true;
		return checkState == PODZOL_SNOWY;
	}

	private static boolean isStateGrass(final BlockState checkState) {
		return checkState == GRASS_BLOCK_DEFAULT;
	}

	@Nonnull
	@Deprecated
	public static BlockRenderLayer getCorrectRenderLayer(@Nonnull final BlockState state) {
		return getCorrectRenderLayer(state.getBlock().getRenderLayer());
	}

	@Nonnull
	@Deprecated
	public static BlockRenderLayer getCorrectRenderLayer(@Nonnull final IFluidState state) {
		return getCorrectRenderLayer(state.getRenderLayer());
	}

	@Nonnull
	public static BlockRenderLayer getCorrectRenderLayer(@Nonnull final BlockRenderLayer blockRenderLayer) {
		return BLOCK_RENDER_LAYER_VALUES[getCorrectRenderLayer(blockRenderLayer.ordinal())];
	}

	// TODO: Optimise `Minecraft.getInstance().gameSettings.mipmapLevels`? Store it in a field somewhere?
	public static int getCorrectRenderLayer(final int blockRenderLayerOrdinal) {
		switch (blockRenderLayerOrdinal) {
			default:
			case 0: // SOLID
			case 3: // TRANSLUCENT
				return blockRenderLayerOrdinal;
			case 1: // CUTOUT_MIPPED
			case 2: // CUTOUT
				if (Minecraft.getInstance().gameSettings.mipmapLevels == 0)
					return 2; // CUTOUT
				else
					return 1; // CUTOUT_MIPPED
		}
	}

	public static BufferBuilder startOrContinueBufferBuilder(final ChunkRenderTask generator, final int blockRenderLayerOrdinal, final CompiledChunk compiledChunk, final BlockRenderLayer blockRenderLayer, ChunkRender chunkRender, BlockPos renderChunkPosition) {
		return startOrContinueBufferBuilder(compiledChunk, blockRenderLayer, chunkRender, renderChunkPosition, generator.getRegionRenderCacheBuilder().getBuilder(blockRenderLayerOrdinal));
	}

	public static BufferBuilder startOrContinueBufferBuilder(final CompiledChunk compiledChunk, final BlockRenderLayer blockRenderLayer, final ChunkRender chunkRender, final BlockPos renderChunkPosition, final BufferBuilder bufferBuilder) {
		if (!compiledChunk.isLayerStarted(blockRenderLayer)) {
			compiledChunk.setLayerStarted(blockRenderLayer);
			chunkRender.preRenderBlocks(bufferBuilder, renderChunkPosition);
		}
		return bufferBuilder;
	}

	public static void tryReloadRenderers() {
		final WorldRenderer worldRenderer = Minecraft.getInstance().worldRenderer;
		if (worldRenderer != null) {
			worldRenderer.loadRenderers();
		}
	}

	public static IChunk getChunk(final int currentChunkPosX, final int currentChunkPosZ, final IEnviromentBlockReader reader) {
//		if (reader instanceof IWorld) { // This should never be the case...
//			return ((IWorld) reader).getChunk(currentChunkPosX, currentChunkPosZ);
//		} else
		if (reader instanceof ChunkRenderCache) {
			final ChunkRenderCache renderChunkCache = (ChunkRenderCache) reader;
			final int x = currentChunkPosX - renderChunkCache.chunkStartX;
			final int z = currentChunkPosZ - renderChunkCache.chunkStartZ;
			return renderChunkCache.chunks[x][z];
		} else if (OptiFineCompatibility.get().isChunkCacheOF(reader)) {
			final ChunkRenderCache renderChunkCache = OptiFineCompatibility.get().getChunkRenderCache(reader);
			final int x = currentChunkPosX - renderChunkCache.chunkStartX;
			final int z = currentChunkPosZ - renderChunkCache.chunkStartZ;
			return renderChunkCache.chunks[x][z];
		}
		final CrashReport crashReport = CrashReport.makeCrashReport(new IllegalStateException(), "Invalid ChunkRenderCache: " + reader);
		crashReport.makeCategory("NoCubes getting Chunk");
		throw new ReportedException(crashReport);
	}

	public static void setupChunkRenderCache(final ChunkRenderCache _this, final int chunkStartX, final int chunkStartZ, final Chunk[][] chunks, final BlockPos start, final BlockPos end) {
		final int startX = start.getX();
		final int startY = start.getY();
		final int startZ = start.getZ();

		final int cacheSizeX = end.getX() - startX + 1;
		final int cacheSizeY = end.getY() - startY + 1;
		final int cacheSizeZ = end.getZ() - startZ + 1;

		final int size = cacheSizeX * cacheSizeY * cacheSizeZ;
		final BlockState[] blockStates = new BlockState[size];
		final IFluidState[] fluidStates = new IFluidState[size];

		int cx = (startX >> 4) - chunkStartX;
		int cz = (startZ >> 4) - chunkStartZ;
		Chunk currentChunk = chunks[cx][cz];

		try (PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain()) {
			int index = 0;
			for (int z = 0; z < cacheSizeZ; ++z) {
				for (int y = 0; y < cacheSizeY; ++y) {
					for (int x = 0; x < cacheSizeX; ++x, ++index) {

						final int posX = startX + x;
						final int posY = startY + y;
						final int posZ = startZ + z;

						final int ccx = ((startX + x) >> 4) - chunkStartX;
						final int ccz = ((startZ + z) >> 4) - chunkStartZ;

						boolean changed = false;
						if (cx != ccx) {
							cx = ccx;
							changed = true;
						}
						if (cz != ccz) {
							cz = ccz;
							changed = true;
						}
						if (changed) {
							currentChunk = chunks[cx][cz];
						}

						// TODO: Use System.arrayCopy on the chunk sections
						pooledMutableBlockPos.setPos(posX, posY, posZ);
//						blockStates[index] = currentChunk.getBlockState(pooledMutableBlockPos);
//						fluidStates[index] = currentChunk.getFluidState(posX, posY, posZ);
						final BlockState blockState = currentChunk.getBlockState(pooledMutableBlockPos);
						blockStates[index] = blockState;
						fluidStates[index] = blockState.getFluidState();
					}
				}
			}
		}

		_this.cacheSizeX = cacheSizeX;
		_this.cacheSizeY = cacheSizeY;
		_this.cacheSizeZ = cacheSizeZ;

		_this.blockStates = blockStates;
		_this.fluidStates = fluidStates;
	}

	public static void replaceFluidRenderer() {
		NoCubes.LOGGER.debug("Replacing fluid renderer");
		Minecraft.getInstance().getBlockRendererDispatcher().fluidRenderer = ClientEventSubscriber.smoothLightingBlockFluidRenderer = new SmoothLightingFluidBlockRenderer();
		NoCubes.LOGGER.debug("Replaced fluid renderer");
	}

}
