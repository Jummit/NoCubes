package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientProxy;
import io.github.cadiboo.nocubes.client.LazyPackedLightCache;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.config.Config;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeColorHelper;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public final class ExtendedFluidBlockRenderer {

	public static boolean renderExtendedFluid(
			final double x, final double y, final double z,
			@Nonnull final BlockPos fluidPos,
			@Nonnull final IBlockAccess worldIn,
			//TODO: eventually do better fluid rendering for 0.3.0
			@Nonnull final IBlockState smoothableState,
			@Nonnull final IBlockState state,
			@Nonnull final BufferBuilder buffer,
			//TODO: eventually do better lighting for 0.3.0
			@Nonnull final LazyPackedLightCache packedLightCache
	) {

		final SmoothLightingBlockFluidRenderer fluidRenderer = ClientProxy.fluidRenderer;

		PooledMutableBlockPos renderPos = PooledMutableBlockPos.retain(x, y, z);
		OptiFineCompatibility.pushShaderThing(state, renderPos, worldIn, buffer);
		PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
		try {
			BlockLiquid blockLiquid = (BlockLiquid) state.getBlock();
			final Material material = state.getMaterial();
			final boolean isLava = material == Material.LAVA;
			final TextureAtlasSprite[] atextureatlassprite = isLava ? fluidRenderer.atlasSpritesLava : fluidRenderer.atlasSpritesWater;

			final float red;
			final float green;
			final float blue;
			if (isLava) {
				red = 1.0F;
				green = 1.0F;
				blue = 1.0F;
			} else {
				final int waterColor = BiomeColorHelper.getWaterColorAtPos(worldIn, renderPos);
				red = (float) (waterColor >> 16 & 0xFF) / 255.0F;
				green = (float) (waterColor >> 8 & 0xFF) / 255.0F;
				blue = (float) (waterColor & 0xFF) / 255.0F;
			}

			boolean shouldRenderUp = state.shouldSideBeRendered(worldIn, renderPos, EnumFacing.UP);
			shouldRenderUp &= !(Config.renderSmoothTerrain && worldIn.getBlockState(renderPos.up()).nocubes_isTerrainSmoothable());
			boolean shouldRenderDown = state.shouldSideBeRendered(worldIn, renderPos, EnumFacing.DOWN);
			shouldRenderDown &= !(Config.renderSmoothTerrain && worldIn.getBlockState(renderPos.down()).nocubes_isTerrainSmoothable());
			boolean shouldRenderNorth = state.shouldSideBeRendered(worldIn, renderPos, EnumFacing.NORTH);
			shouldRenderNorth &= !(Config.renderSmoothTerrain && worldIn.getBlockState(renderPos.north()).nocubes_isTerrainSmoothable());
			boolean shouldRenderSouth = state.shouldSideBeRendered(worldIn, renderPos, EnumFacing.SOUTH);
			shouldRenderSouth &= !(Config.renderSmoothTerrain && worldIn.getBlockState(renderPos.south()).nocubes_isTerrainSmoothable());
			boolean shouldRenderWest = state.shouldSideBeRendered(worldIn, renderPos, EnumFacing.WEST);
			shouldRenderWest &= !(Config.renderSmoothTerrain && worldIn.getBlockState(renderPos.west()).nocubes_isTerrainSmoothable());
			boolean shouldRenderEast = state.shouldSideBeRendered(worldIn, renderPos, EnumFacing.EAST);
			shouldRenderEast &= !(Config.renderSmoothTerrain && worldIn.getBlockState(renderPos.east()).nocubes_isTerrainSmoothable());

			if (!shouldRenderUp && !shouldRenderDown && !shouldRenderEast && !shouldRenderWest && !shouldRenderNorth && !shouldRenderSouth) {
				return false;
			}

			boolean wasAnythingRendered = false;
			float fluidHeight = fluidRenderer.getFluidHeight(worldIn, fluidPos, material);
			float fluidHeightSouth = fluidRenderer.getFluidHeight(worldIn, fluidPos.south(), material);
			float fluidHeightEastSouth = fluidRenderer.getFluidHeight(worldIn, fluidPos.east().south(), material);
			float fluidHeightEast = fluidRenderer.getFluidHeight(worldIn, fluidPos.east(), material);

//			final double x = (double) pos.getX();
//			final double y = (double) pos.getY();
//			final double z = (double) pos.getZ();

			if (shouldRenderUp) {

				// Commented out to fix transparent lines between bottom of sides.
				// The only reason that I can think of for this code to exist in the first place
				// is to try and solve z-fighting issues.
//				fluidHeight -= 0.001F;
//				fluidHeightSouth -= 0.001F;
//				fluidHeightEastSouth -= 0.001F;
//				fluidHeightEast -= 0.001F;

				if (!fluidRenderer.colors()) {
					if (!fluidRenderer.smoothLighting()) {
						final int combinedLightUpMax = state.getPackedLightmapCoords(worldIn, renderPos);
						wasAnythingRendered |= fluidRenderer.renderUp(
								buffer, atextureatlassprite,
								red, green, blue,
								red, green, blue,
								red, green, blue,
								red, green, blue,
								fluidHeight, fluidHeightSouth, fluidHeightEastSouth, fluidHeightEast,
								x, y, z,
								combinedLightUpMax, combinedLightUpMax, combinedLightUpMax, combinedLightUpMax,
								blockLiquid.shouldRenderSides(worldIn, renderPos.up()), blockLiquid.getFlow(worldIn, fluidPos, state), MathHelper.getPositionRandom(renderPos)
						);
					} else {
						wasAnythingRendered |= fluidRenderer.renderUp(
								buffer, atextureatlassprite,
								red, green, blue,
								red, green, blue,
								red, green, blue,
								red, green, blue,
								fluidHeight, fluidHeightSouth, fluidHeightEastSouth, fluidHeightEast,
								x, y, z,
								state.getPackedLightmapCoords(worldIn, renderPos), state.getPackedLightmapCoords(worldIn, renderPos.south()), state.getPackedLightmapCoords(worldIn, renderPos.east().south()), state.getPackedLightmapCoords(worldIn, renderPos.east()),
								blockLiquid.shouldRenderSides(worldIn, renderPos.up()), blockLiquid.getFlow(worldIn, fluidPos, state), MathHelper.getPositionRandom(renderPos)
						);
					}
				} else {
					final float red0;
					final float green0;
					final float blue0;
					final float red1;
					final float green1;
					final float blue1;
					final float red2;
					final float green2;
					final float blue2;
					final float red3;
					final float green3;
					final float blue3;
					if (isLava) {
						red0 = 1.0F;
						green0 = 1.0F;
						blue0 = 1.0F;
						red1 = 1.0F;
						green1 = 1.0F;
						blue1 = 1.0F;
						red2 = 1.0F;
						green2 = 1.0F;
						blue2 = 1.0F;
						red3 = 1.0F;
						green3 = 1.0F;
						blue3 = 1.0F;
					} else {
//							final int waterColor0 = BiomeColors.getWaterColor(worldIn, renderPos);
//							red0 = (float) (waterColor0 >> 16 & 0xFF) / 255.0F;
//							green0 = (float) (waterColor0 >> 8 & 0xFF) / 255.0F;
//							blue0 = (float) (waterColor0 & 0xFF) / 255.0F;
						red0 = red;
						green0 = green;
						blue0 = blue;
						final int waterColor1 = BiomeColorHelper.getWaterColorAtPos(worldIn, renderPos.south());
						red1 = (float) (waterColor1 >> 16 & 0xFF) / 255.0F;
						green1 = (float) (waterColor1 >> 8 & 0xFF) / 255.0F;
						blue1 = (float) (waterColor1 & 0xFF) / 255.0F;
						final int waterColor2 = BiomeColorHelper.getWaterColorAtPos(worldIn, renderPos.east().south());
						red2 = (float) (waterColor2 >> 16 & 0xFF) / 255.0F;
						green2 = (float) (waterColor2 >> 8 & 0xFF) / 255.0F;
						blue2 = (float) (waterColor2 & 0xFF) / 255.0F;
						final int waterColor3 = BiomeColorHelper.getWaterColorAtPos(worldIn, renderPos.east());
						red3 = (float) (waterColor3 >> 16 & 0xFF) / 255.0F;
						green3 = (float) (waterColor3 >> 8 & 0xFF) / 255.0F;
						blue3 = (float) (waterColor3 & 0xFF) / 255.0F;
					}

					if (!fluidRenderer.smoothLighting()) {
						final int combinedLightUpMax = state.getPackedLightmapCoords(worldIn, renderPos);
						wasAnythingRendered |= fluidRenderer.renderUp(
								buffer, atextureatlassprite,
								red0, green0, blue0,
								red1, green1, blue1,
								red2, green2, blue2,
								red3, green3, blue3,
								fluidHeight, fluidHeightSouth, fluidHeightEastSouth, fluidHeightEast,
								x, y, z,
								combinedLightUpMax, combinedLightUpMax, combinedLightUpMax, combinedLightUpMax,
								blockLiquid.shouldRenderSides(worldIn, renderPos.up()), blockLiquid.getFlow(worldIn, fluidPos, state), MathHelper.getPositionRandom(renderPos)
						);
					} else {
						wasAnythingRendered |= fluidRenderer.renderUp(
								buffer, atextureatlassprite,
								red0, green0, blue0,
								red1, green1, blue1,
								red2, green2, blue2,
								red3, green3, blue3,
								fluidHeight, fluidHeightSouth, fluidHeightEastSouth, fluidHeightEast,
								x, y, z,
								state.getPackedLightmapCoords(worldIn, renderPos), state.getPackedLightmapCoords(worldIn, renderPos.south()), state.getPackedLightmapCoords(worldIn, renderPos.east().south()), state.getPackedLightmapCoords(worldIn, renderPos.east()),
								blockLiquid.shouldRenderSides(worldIn, renderPos.up()), blockLiquid.getFlow(worldIn, fluidPos, state), MathHelper.getPositionRandom(renderPos)
						);
					}
				}
			}

			if (shouldRenderDown) {
				if (!fluidRenderer.colors()) {
					if (!fluidRenderer.smoothLighting()) {
						final int downCombinedLightUpMax = state.getPackedLightmapCoords(worldIn, renderPos.down());
						wasAnythingRendered |= fluidRenderer.renderDown(
								downCombinedLightUpMax, downCombinedLightUpMax, downCombinedLightUpMax, downCombinedLightUpMax,
								buffer, atextureatlassprite[0],
								red, green, blue,
								red, green, blue,
								red, green, blue,
								red, green, blue,
								x, y, z
						);
					} else {
						final BlockPos down = renderPos.down();
						wasAnythingRendered |= fluidRenderer.renderDown(
								state.getPackedLightmapCoords(worldIn, down), state.getPackedLightmapCoords(worldIn, down.south()), state.getPackedLightmapCoords(worldIn, down.east().south()), state.getPackedLightmapCoords(worldIn, down.east()),
								buffer, atextureatlassprite[0],
								red, green, blue,
								red, green, blue,
								red, green, blue,
								red, green, blue,
								x, y, z
						);
					}
				} else {

					// I've fucked up somehow. I shouldn't need to offset this south
					final BlockPos down = renderPos.down().south();

					final float red0;
					final float green0;
					final float blue0;
					final float red1;
					final float green1;
					final float blue1;
					final float red2;
					final float green2;
					final float blue2;
					final float red3;
					final float green3;
					final float blue3;
					if (isLava) {
						red0 = 1.0F;
						green0 = 1.0F;
						blue0 = 1.0F;
						red1 = 1.0F;
						green1 = 1.0F;
						blue1 = 1.0F;
						red2 = 1.0F;
						green2 = 1.0F;
						blue2 = 1.0F;
						red3 = 1.0F;
						green3 = 1.0F;
						blue3 = 1.0F;
					} else {
						final int waterColor0 = BiomeColorHelper.getWaterColorAtPos(worldIn, down);
						red0 = (float) (waterColor0 >> 16 & 0xFF) / 255.0F;
						green0 = (float) (waterColor0 >> 8 & 0xFF) / 255.0F;
						blue0 = (float) (waterColor0 & 0xFF) / 255.0F;
						// I've fucked up somehow. I shouldn't need to offset this north, it should be south
						final int waterColor1 = BiomeColorHelper.getWaterColorAtPos(worldIn, down.north());
						red1 = (float) (waterColor1 >> 16 & 0xFF) / 255.0F;
						green1 = (float) (waterColor1 >> 8 & 0xFF) / 255.0F;
						blue1 = (float) (waterColor1 & 0xFF) / 255.0F;
						// I've fucked up somehow. I shouldn't need to offset this north, it should be south
						final int waterColor2 = BiomeColorHelper.getWaterColorAtPos(worldIn, down.east().north());
						red2 = (float) (waterColor2 >> 16 & 0xFF) / 255.0F;
						green2 = (float) (waterColor2 >> 8 & 0xFF) / 255.0F;
						blue2 = (float) (waterColor2 & 0xFF) / 255.0F;
						final int waterColor3 = BiomeColorHelper.getWaterColorAtPos(worldIn, down.east());
						red3 = (float) (waterColor3 >> 16 & 0xFF) / 255.0F;
						green3 = (float) (waterColor3 >> 8 & 0xFF) / 255.0F;
						blue3 = (float) (waterColor3 & 0xFF) / 255.0F;
					}

					if (!fluidRenderer.smoothLighting()) {
						final int downCombinedLightUpMax = state.getPackedLightmapCoords(worldIn, down);
						wasAnythingRendered |= fluidRenderer.renderDown(
								downCombinedLightUpMax, downCombinedLightUpMax, downCombinedLightUpMax, downCombinedLightUpMax,
								buffer, atextureatlassprite[0],
								red0, green0, blue0,
								red1, green1, blue1,
								red2, green2, blue2,
								red3, green3, blue3,
								x, y, z
						);
					} else {
						wasAnythingRendered |= fluidRenderer.renderDown(
								state.getPackedLightmapCoords(worldIn, down), state.getPackedLightmapCoords(worldIn, down.south()), state.getPackedLightmapCoords(worldIn, down.east().south()), state.getPackedLightmapCoords(worldIn, down.east()),
								buffer, atextureatlassprite[0],
								red0, green0, blue0,
								red1, green1, blue1,
								red2, green2, blue2,
								red3, green3, blue3,
								x, y, z
						);
					}
				}
			}

			for (int facingIndex = 0; facingIndex < 4; ++facingIndex) {
				final float y0;
				final float y1;
				final double x0;
				final double z0;
				final double x1;
				final double z1;
				final EnumFacing enumfacing;
				final boolean shouldRenderSide;
				if (facingIndex == 0) {
					y0 = fluidHeight;
					y1 = fluidHeightEast;
					x0 = x;
					x1 = x + 1.0D;
					// Commented out to fix transparent lines between bottom of sides.
					// The only reason that I can think of for this code to exist in the first place
					// is to try and solve z-fighting issues.
					z0 = z;// + (double) 0.001F;
					z1 = z;// + (double) 0.001F;
					enumfacing = EnumFacing.NORTH;
					shouldRenderSide = shouldRenderNorth;
				} else if (facingIndex == 1) {
					y0 = fluidHeightEastSouth;
					y1 = fluidHeightSouth;
					x0 = x + 1.0D;
					x1 = x;
					// Commented out to fix transparent lines between bottom of sides.
					// The only reason that I can think of for this code to exist in the first place
					// is to try and solve z-fighting issues.
					z0 = z + 1.0D;// - (double) 0.001F;
					z1 = z + 1.0D;// - (double) 0.001F;
					enumfacing = EnumFacing.SOUTH;
					shouldRenderSide = shouldRenderSouth;
				} else if (facingIndex == 2) {
					y0 = fluidHeightSouth;
					y1 = fluidHeight;
					// Commented out to fix transparent lines between bottom of sides.
					// The only reason that I can think of for this code to exist in the first place
					// is to try and solve z-fighting issues.
					x0 = x;// + (double) 0.001F;
					x1 = x;// + (double) 0.001F;
					z0 = z + 1.0D;
					z1 = z;
					enumfacing = EnumFacing.WEST;
					shouldRenderSide = shouldRenderWest;
				} else {
					y0 = fluidHeightEast;
					y1 = fluidHeightEastSouth;
					// Commented out to fix transparent lines between bottom of sides.
					// The only reason that I can think of for this code to exist in the first place
					// is to try and solve z-fighting issues.
					x0 = x + 1.0D;// - (double) 0.001F;
					x1 = x + 1.0D;// - (double) 0.001F;
					z0 = z;
					z1 = z + 1.0D;
					enumfacing = EnumFacing.EAST;
					shouldRenderSide = shouldRenderEast;
				}

				if (shouldRenderSide) {
					final BlockPos offset = renderPos.offset(enumfacing);
					TextureAtlasSprite textureatlassprite2 = atextureatlassprite[1];
					if (!isLava) {
						IBlockState blockstate = worldIn.getBlockState(offset);
						if (blockstate.getBlockFaceShape(worldIn, offset, enumfacing) == BlockFaceShape.SOLID) {
							textureatlassprite2 = fluidRenderer.atlasSpriteWaterOverlay;
						}
					}

					if (!fluidRenderer.colors()) {
						if (!fluidRenderer.smoothLighting()) {
							final int combinedLightUpMax = state.getPackedLightmapCoords(worldIn, offset);
							wasAnythingRendered = fluidRenderer.renderSide(
									buffer, textureatlassprite2,
									red, green, blue,
									red, green, blue,
									red, green, blue,
									red, green, blue,
									facingIndex,
									y, y0, y1,
									x0, x1,
									z0, z1,
									combinedLightUpMax, combinedLightUpMax, combinedLightUpMax, combinedLightUpMax,
									textureatlassprite2 != fluidRenderer.atlasSpriteWaterOverlay
							);
						} else {
							wasAnythingRendered = fluidRenderer.renderSide(
									buffer, textureatlassprite2,
									red, green, blue,
									red, green, blue,
									red, green, blue,
									red, green, blue,
									facingIndex,
									y, y0, y1,
									x0, x1,
									z0, z1,
									state.getPackedLightmapCoords(worldIn, pooledMutableBlockPos.setPos(x0, y + y0, z0)),
									state.getPackedLightmapCoords(worldIn, pooledMutableBlockPos.setPos(x1, y + y1, z1)),
									state.getPackedLightmapCoords(worldIn, pooledMutableBlockPos.setPos(x1, y, z1)),
									state.getPackedLightmapCoords(worldIn, pooledMutableBlockPos.setPos(x0, y, z0)),
									textureatlassprite2 != fluidRenderer.atlasSpriteWaterOverlay
							);
						}
					} else {
						final float red0;
						final float green0;
						final float blue0;
						final float red1;
						final float green1;
						final float blue1;
						final float red2;
						final float green2;
						final float blue2;
						final float red3;
						final float green3;
						final float blue3;
						if (isLava) {
							red0 = 1.0F;
							green0 = 1.0F;
							blue0 = 1.0F;
							red1 = 1.0F;
							green1 = 1.0F;
							blue1 = 1.0F;
							red2 = 1.0F;
							green2 = 1.0F;
							blue2 = 1.0F;
							red3 = 1.0F;
							green3 = 1.0F;
							blue3 = 1.0F;
						} else {
							final int waterColor0 = BiomeColorHelper.getWaterColorAtPos(worldIn, pooledMutableBlockPos.setPos(x0, y + y0, z0));
							red0 = (float) (waterColor0 >> 16 & 0xFF) / 255.0F;
							green0 = (float) (waterColor0 >> 8 & 0xFF) / 255.0F;
							blue0 = (float) (waterColor0 & 0xFF) / 255.0F;
							final int waterColor1 = BiomeColorHelper.getWaterColorAtPos(worldIn, pooledMutableBlockPos.setPos(x1, y + y1, z1));
							red1 = (float) (waterColor1 >> 16 & 0xFF) / 255.0F;
							green1 = (float) (waterColor1 >> 8 & 0xFF) / 255.0F;
							blue1 = (float) (waterColor1 & 0xFF) / 255.0F;
							final int waterColor2 = BiomeColorHelper.getWaterColorAtPos(worldIn, pooledMutableBlockPos.setPos(x1, y, z1));
							red2 = (float) (waterColor2 >> 16 & 0xFF) / 255.0F;
							green2 = (float) (waterColor2 >> 8 & 0xFF) / 255.0F;
							blue2 = (float) (waterColor2 & 0xFF) / 255.0F;
							final int waterColor3 = BiomeColorHelper.getWaterColorAtPos(worldIn, pooledMutableBlockPos.setPos(x0, y, z0));
							red3 = (float) (waterColor3 >> 16 & 0xFF) / 255.0F;
							green3 = (float) (waterColor3 >> 8 & 0xFF) / 255.0F;
							blue3 = (float) (waterColor3 & 0xFF) / 255.0F;
						}

						if (!fluidRenderer.smoothLighting()) {
							final int combinedLightUpMax = state.getPackedLightmapCoords(worldIn, offset);
							wasAnythingRendered = fluidRenderer.renderSide(
									buffer, textureatlassprite2,
									red0, green0, blue0,
									red1, green1, blue1,
									red2, green2, blue2,
									red3, green3, blue3,
									facingIndex,
									y, y0, y1,
									x0, x1,
									z0, z1,
									combinedLightUpMax, combinedLightUpMax, combinedLightUpMax, combinedLightUpMax,
									textureatlassprite2 != fluidRenderer.atlasSpriteWaterOverlay
							);
						} else {
							wasAnythingRendered = fluidRenderer.renderSide(
									buffer, textureatlassprite2,
									red0, green0, blue0,
									red1, green1, blue1,
									red2, green2, blue2,
									red3, green3, blue3,
									facingIndex,
									y, y0, y1,
									x0, x1,
									z0, z1,
									state.getPackedLightmapCoords(worldIn, pooledMutableBlockPos.setPos(x0, y + y0, z0)),
									state.getPackedLightmapCoords(worldIn, pooledMutableBlockPos.setPos(x1, y + y1, z1)),
									state.getPackedLightmapCoords(worldIn, pooledMutableBlockPos.setPos(x1, y, z1)),
									state.getPackedLightmapCoords(worldIn, pooledMutableBlockPos.setPos(x0, y, z0)),
									textureatlassprite2 != fluidRenderer.atlasSpriteWaterOverlay
							);
						}
					}
				}
			}

			return wasAnythingRendered;
		} finally {
			pooledMutableBlockPos.release();
			OptiFineCompatibility.popShaderThing(buffer);
			renderPos.release();
		}
	}

}
