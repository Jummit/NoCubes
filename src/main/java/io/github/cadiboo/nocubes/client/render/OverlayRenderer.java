package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.util.Face;
import io.github.cadiboo.nocubes.client.render.util.FaceList;
import io.github.cadiboo.nocubes.client.render.util.Vec;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.SurfaceNets;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public final class OverlayRenderer {

	static Mesh cache;

	@SubscribeEvent
	public static void onRenderWorldLastEvent(final RenderWorldLastEvent event) {
		if (!Screen.hasAltDown())
			return;

		final Minecraft minecraft = Minecraft.getInstance();
		Entity viewer = minecraft.gameRenderer.getActiveRenderInfo().getRenderViewEntity();
		if (viewer == null)
			return;

		final World world = viewer.world;
		if (world == null)
			return;

		if (cache == null || world.getGameTime() % 5 == 0) {
			if (cache != null) {
				final FaceList faces = cache.faces;
				for (final Face face : faces) {
					for (final Vec vertex : face.getVertices())
						vertex.close();
					face.close();
				}
				faces.close();
			}
			cache = makeMesh(world, viewer);
		}

		final ActiveRenderInfo activeRenderInfo = minecraft.gameRenderer.getActiveRenderInfo();

		final Vector3d projectedView = activeRenderInfo.getProjectedView();
		double d0 = projectedView.getX();
		double d1 = projectedView.getY();
		double d2 = projectedView.getZ();
		final MatrixStack matrixStack = event.getMatrixStack();

		final IRenderTypeBuffer.Impl bufferSource = minecraft.getRenderTypeBuffers().getBufferSource();
		final IVertexBuilder bufferBuilder = bufferSource.getBuffer(RenderType.getLines());

//		final BlockPos viewerPos = new BlockPos(viewer.getPosition());
//		BlockPos.getAllInBoxMutable(viewerPos.add(-5, -5, -5), viewerPos.add(5, 5, 5)).forEach(blockPos -> {
//			if (NoCubes.smoothableHandler.isSmoothable(viewer.world.getBlockState(blockPos)))
//				drawShape(matrixStack, bufferBuilder, VoxelShapes.fullCube(), -d0 + blockPos.getX(), -d1 + blockPos.getY(), -d2 + blockPos.getZ(), 0.0F, 1.0F, 1.0F, 0.4F);
//		});
//
//		// Draw nearby collisions
//		viewer.world.getCollisionShapes(viewer, viewer.getBoundingBox().grow(5.0D)).forEach(voxelShape -> {
//			drawShape(matrixStack, bufferBuilder, voxelShape, -d0, -d1, -d2, 0.0F, 1.0F, 1.0F, 0.4F);
//		});
//		// Draw player intersecting collisions
//		viewer.world.getCollisionShapes(viewer, viewer.getBoundingBox()).forEach(voxelShape -> {
//			drawShape(matrixStack, bufferBuilder, voxelShape, -d0, -d1, -d2, 1.0F, 0.0F, 0.0F, 0.4F);
//		});

		Matrix4f matrix4f = matrixStack.getLast().getMatrix();
		for (final Face face : cache.faces) {
			Vec v0 = face.v0;
			Vec v1 = face.v1;
			Vec v2 = face.v2;
			Vec v3 = face.v3;
			final float red = 0F;
			final float blue = 1F;
			final float green = 1F;
			final float alpha = 1F;
			bufferBuilder.pos(matrix4f, (float) (v0.x + -d0), (float) (v0.y + -d1), (float) (v0.z + -d2)).color(red, green, blue, alpha).endVertex();
			bufferBuilder.pos(matrix4f, (float) (v1.x + -d0), (float) (v1.y + -d1), (float) (v1.z + -d2)).color(red, green, blue, alpha).endVertex();
			bufferBuilder.pos(matrix4f, (float) (v1.x + -d0), (float) (v1.y + -d1), (float) (v1.z + -d2)).color(red, green, blue, alpha).endVertex();
			bufferBuilder.pos(matrix4f, (float) (v2.x + -d0), (float) (v2.y + -d1), (float) (v2.z + -d2)).color(red, green, blue, alpha).endVertex();
			bufferBuilder.pos(matrix4f, (float) (v2.x + -d0), (float) (v2.y + -d1), (float) (v2.z + -d2)).color(red, green, blue, alpha).endVertex();
			bufferBuilder.pos(matrix4f, (float) (v3.x + -d0), (float) (v3.y + -d1), (float) (v3.z + -d2)).color(red, green, blue, alpha).endVertex();
			bufferBuilder.pos(matrix4f, (float) (v3.x + -d0), (float) (v3.y + -d1), (float) (v3.z + -d2)).color(red, green, blue, alpha).endVertex();
			bufferBuilder.pos(matrix4f, (float) (v0.x + -d0), (float) (v0.y + -d1), (float) (v0.z + -d2)).color(red, green, blue, alpha).endVertex();

			// Normals
			Vec n0 = Vec.normal(v3, v0, v1);
			Vec n1 = Vec.normal(v0, v1, v2);
			Vec n2 = Vec.normal(v1, v2, v3);
			Vec n3 = Vec.normal(v2, v3, v0);
			final float mul = -0.25F;
			{
				Vec n = n0;
				Vec v = v0;
				float nx = (float) (n.x) * mul;
				float ny = (float) (n.y) * mul;
				float nz = (float) (n.z) * mul;
				bufferBuilder.pos(matrix4f, (float) (v.x + -d0), (float) (v.y + -d1), (float) (v.z + -d2)).color(0F, 0F, 1F, 1F).endVertex();
				bufferBuilder.pos(matrix4f, (float) (v.x + nx + -d0), (float) (v.y + ny + -d1), (float) (v.z + nz + -d2)).color(0F, 0F, 1F, 1F).endVertex();
				n.close();
			}
			{
				Vec n = n1;
				Vec v = v1;
				float nx = (float) (n.x) * mul;
				float ny = (float) (n.y) * mul;
				float nz = (float) (n.z) * mul;
				bufferBuilder.pos(matrix4f, (float) (v.x + -d0), (float) (v.y + -d1), (float) (v.z + -d2)).color(0F, 0F, 1F, 1F).endVertex();
				bufferBuilder.pos(matrix4f, (float) (v.x + nx + -d0), (float) (v.y + ny + -d1), (float) (v.z + nz + -d2)).color(0F, 0F, 1F, 1F).endVertex();
				n.close();
			}
			{
				Vec n = n2;
				Vec v = v2;
				float nx = (float) (n.x) * mul;
				float ny = (float) (n.y) * mul;
				float nz = (float) (n.z) * mul;
				bufferBuilder.pos(matrix4f, (float) (v.x + -d0), (float) (v.y + -d1), (float) (v.z + -d2)).color(0F, 0F, 1F, 1F).endVertex();
				bufferBuilder.pos(matrix4f, (float) (v.x + nx + -d0), (float) (v.y + ny + -d1), (float) (v.z + nz + -d2)).color(0F, 0F, 1F, 1F).endVertex();
				n.close();
			}
			{
				Vec n = n3;
				Vec v = v3;
				float nx = (float) (n.x) * mul;
				float ny = (float) (n.y) * mul;
				float nz = (float) (n.z) * mul;
				bufferBuilder.pos(matrix4f, (float) (v.x + -d0), (float) (v.y + -d1), (float) (v.z + -d2)).color(0F, 0F, 1F, 1F).endVertex();
				bufferBuilder.pos(matrix4f, (float) (v.x + nx + -d0), (float) (v.y + ny + -d1), (float) (v.z + nz + -d2)).color(0F, 0F, 1F, 1F).endVertex();
				n.close();
			}
		}

		RayTraceResult lookingAt = minecraft.objectMouseOver;
		if (NoCubesConfig.Client.render && lookingAt != null && lookingAt.getType() == RayTraceResult.Type.BLOCK) {
			BlockRayTraceResult lookingAtBlock = ((BlockRayTraceResult) lookingAt);
			BlockPos lookingAtPos = lookingAtBlock.getPos();
			BlockState state = world.getBlockState(lookingAtPos);
			if (NoCubes.smoothableHandler.isSmoothable(state)) {
				final int x = lookingAtPos.getX();
				final int y = lookingAtPos.getY();
				final int z = lookingAtPos.getZ();
				SurfaceNets.generate(
					x, y, z,
					1, 1, 1,
					world, NoCubes.smoothableHandler::isSmoothable,
					(pos, face) -> {
						Vec v0 = face.v0.add(x, y, z);
						Vec v1 = face.v1.add(x, y, z);
						Vec v2 = face.v2.add(x, y, z);
						Vec v3 = face.v3.add(x, y, z);
						final float red = 1F;
						final float blue = 1F;
						final float green = 1F;
						final float alpha = 1F;
						bufferBuilder.pos(matrix4f, (float) (v0.x + -d0), (float) (v0.y + -d1), (float) (v0.z + -d2)).color(red, green, blue, alpha).endVertex();
						bufferBuilder.pos(matrix4f, (float) (v1.x + -d0), (float) (v1.y + -d1), (float) (v1.z + -d2)).color(red, green, blue, alpha).endVertex();
						bufferBuilder.pos(matrix4f, (float) (v1.x + -d0), (float) (v1.y + -d1), (float) (v1.z + -d2)).color(red, green, blue, alpha).endVertex();
						bufferBuilder.pos(matrix4f, (float) (v2.x + -d0), (float) (v2.y + -d1), (float) (v2.z + -d2)).color(red, green, blue, alpha).endVertex();
						bufferBuilder.pos(matrix4f, (float) (v2.x + -d0), (float) (v2.y + -d1), (float) (v2.z + -d2)).color(red, green, blue, alpha).endVertex();
						bufferBuilder.pos(matrix4f, (float) (v3.x + -d0), (float) (v3.y + -d1), (float) (v3.z + -d2)).color(red, green, blue, alpha).endVertex();
						bufferBuilder.pos(matrix4f, (float) (v3.x + -d0), (float) (v3.y + -d1), (float) (v3.z + -d2)).color(red, green, blue, alpha).endVertex();
						bufferBuilder.pos(matrix4f, (float) (v0.x + -d0), (float) (v0.y + -d1), (float) (v0.z + -d2)).color(red, green, blue, alpha).endVertex();
						return true;
					}
				);
			}
		}

		// Hack to finish buffer because RenderWorldLastEvent seems to fire after vanilla normally finishes them
		bufferSource.finish(RenderType.getLines());
	}

	private static void drawShape(MatrixStack matrixStackIn, IVertexBuilder bufferIn, VoxelShape shapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha) {
		Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();
		shapeIn.forEachEdge((x0, y0, z0, x1, y1, z1) -> {
			bufferIn.pos(matrix4f, (float) (x0 + xIn), (float) (y0 + yIn), (float) (z0 + zIn)).color(red, green, blue, alpha).endVertex();
			bufferIn.pos(matrix4f, (float) (x1 + xIn), (float) (y1 + yIn), (float) (z1 + zIn)).color(red, green, blue, alpha).endVertex();
		});
	}

	private static Mesh makeMesh(final World world, final Entity viewer) {
		final Mesh mesh = new Mesh();
//		BlockPos base = new BlockPos(viewer.chunkCoordX << 4, viewer.chunkCoordY << 4, viewer.chunkCoordZ << 4);
		BlockPos base = viewer.getPosition().add(0, 2, 0);

		final int meshSizeX = 16;
		final int meshSizeY = 16;
		final int meshSizeZ = 16;

		// Make this mesh centred around the base
		final int startX = base.getX() - meshSizeX / 2;
		final int startY = base.getY() - meshSizeY / 2;
		final int startZ = base.getZ() - meshSizeZ / 2;

		io.github.cadiboo.nocubes.mesh.SurfaceNets.generate(
			startX, startY, startZ,
			meshSizeX, meshSizeY, meshSizeZ,
			viewer.world, NoCubes.smoothableHandler::isSmoothable,
			(pos, face) -> mesh.faces.add(Face.of(
				Vec.of(face.v0.add(startX, startY, startZ)),
				Vec.of(face.v1.add(startX, startY, startZ)),
				Vec.of(face.v2.add(startX, startY, startZ)),
				Vec.of(face.v3.add(startX, startY, startZ))
			))
		);

//		BlockPos.getAllInBoxMutable(base.add(-8, -8, -8), base.add(7, 7, 7)).forEach(blockPos -> {
//			final BlockState state = viewer.world.getBlockState(blockPos);
//			if (NoCubes.smoothableHandler.isSmoothable(state)) {
//				final VoxelShape shape = state.getCollisionShape(world, blockPos);
//				shape.forEachBox((x0, y0, z0, x1, y1, z1) -> {
//					x0 += blockPos.getX();
//					y0 += blockPos.getY();
//					z0 += blockPos.getZ();
//					x1 += blockPos.getX();
//					y1 += blockPos.getY();
//					z1 += blockPos.getZ();
//					// Bottom
//					{
//						Vec v0 = Vec.of(x1, y0, z1);
//						Vec v1 = Vec.of(x0, y0, z1);
//						Vec v2 = Vec.of(x0, y0, z0);
//						Vec v3 = Vec.of(x1, y0, z0);
//						mesh.faces.add(Face.of(v0, v1, v2, v3));
//					}
//					// Top
//					{
//						Vec v0 = Vec.of(x1, y1, z1);
//						Vec v1 = Vec.of(x0, y1, z1);
//						Vec v2 = Vec.of(x0, y1, z0);
//						Vec v3 = Vec.of(x1, y1, z0);
//						mesh.faces.add(Face.of(v0, v1, v2, v3));
//					}
//					// south (pos z)
//					{
//						Vec v0 = Vec.of(x1, y1, z1);
//						Vec v1 = Vec.of(x0, y1, z1);
//						Vec v2 = Vec.of(x0, y0, z1);
//						Vec v3 = Vec.of(x1, y0, z1);
//						mesh.faces.add(Face.of(v0, v1, v2, v3));
//					}
//					// north (neg z)
//					{
//						Vec v0 = Vec.of(x1, y1, z0);
//						Vec v1 = Vec.of(x0, y1, z0);
//						Vec v2 = Vec.of(x0, y0, z0);
//						Vec v3 = Vec.of(x1, y0, z0);
//						mesh.faces.add(Face.of(v0, v1, v2, v3));
//					}
//					// east (pos x)
//					{
//						Vec v0 = Vec.of(x1, y1, z1);
//						Vec v1 = Vec.of(x1, y1, z0);
//						Vec v2 = Vec.of(x1, y0, z0);
//						Vec v3 = Vec.of(x1, y0, z1);
//						mesh.faces.add(Face.of(v0, v1, v2, v3));
//					}
//					// west (neg x)
//					{
//						Vec v0 = Vec.of(x0, y1, z1);
//						Vec v1 = Vec.of(x0, y1, z0);
//						Vec v2 = Vec.of(x0, y0, z0);
//						Vec v3 = Vec.of(x0, y0, z1);
//						mesh.faces.add(Face.of(v0, v1, v2, v3));
//					}
//				});
//			}
//		});

//		final double x = viewer.getPosX();
//		final double y = viewer.getPosY();
//		final double z = viewer.getPosZ();
//		Vec v0 = Vec.of(x + 0.5, y - 1, z + 0.5);
//		Vec v1 = Vec.of(x - 0.5, y - 1, z + 0.5);
//		Vec v2 = Vec.of(x - 0.5, y - 1, z - 0.5);
//		Vec v3 = Vec.of(x + 0.5, y - 1, z - 0.5);
//
//		mesh.faces.add(Face.of(v0, v1, v2, v3));

		return mesh;
	}

	private static Face toFace(final Vec v0, final Vec v1) {
		Vec v2 = Vec.of(v1);
		v2.y += 1;
		Vec v3 = Vec.of(v0);
		v3.y += 1;
		return Face.of(v0, v1, v2, v3);
	}

}