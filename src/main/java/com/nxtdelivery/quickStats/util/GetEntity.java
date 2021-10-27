package com.nxtdelivery.quickStats.util;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class GetEntity {
	private static Minecraft mc = Minecraft.getMinecraft();

	/**
	 * A modified versions of mc.EntityRenderer.getMouseOver(). this version has
	 * near unlimited range.
	 */
	public static Entity get(float partialTicks) {
		Entity entity = mc.getRenderViewEntity();
		Entity pointedEntity = null;

		if (entity != null) {
			if (mc.theWorld != null) {
				mc.mcProfiler.startSection("pick");
				mc.pointedEntity = null;
				double d0 = (double) 200F; // this should be far enough
				mc.objectMouseOver = entity.rayTrace(d0, partialTicks);
				double d1 = d0;
				Vec3 vec3 = entity.getPositionEyes(partialTicks);
				boolean flag = false;
				int i = 3;

				if (mc.objectMouseOver != null) {
					d1 = mc.objectMouseOver.hitVec.distanceTo(vec3);
				}

				Vec3 vec31 = entity.getLook(partialTicks);
				Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
				pointedEntity = null;
				Vec3 vec33 = null;
				float f = 1.0F;
				List<Entity> list = mc.theWorld.getEntitiesInAABBexcluding(entity,
						entity.getEntityBoundingBox().addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0)
								.expand((double) f, (double) f, (double) f),
						Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>() {
							public boolean apply(Entity p_apply_1_) {
								return p_apply_1_.canBeCollidedWith();
							}
						}));
				double d2 = d1;

				for (int j = 0; j < list.size(); ++j) {
					Entity entity1 = (Entity) list.get(j);
					float f1 = entity1.getCollisionBorderSize();
					AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand((double) f1, (double) f1,
							(double) f1);
					MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

					if (axisalignedbb.isVecInside(vec3)) {
						if (d2 >= 0.0D) {
							pointedEntity = entity1;
							vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
							d2 = 0.0D;
						}
					} else if (movingobjectposition != null) {
						double d3 = vec3.distanceTo(movingobjectposition.hitVec);

						if (d3 < d2 || d2 == 0.0D) {
							if (entity1 == entity.ridingEntity && !entity.canRiderInteract()) {
								if (d2 == 0.0D) {
									pointedEntity = entity1;
									vec33 = movingobjectposition.hitVec;
								}
							} else {
								pointedEntity = entity1;
								vec33 = movingobjectposition.hitVec;
								d2 = d3;
							}
						}
					}
				}

				if (pointedEntity != null && flag && vec3.distanceTo(vec33) > 3.0D) {
					pointedEntity = null;
					mc.objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec33,
							(EnumFacing) null, new BlockPos(vec33));
				}

				if (pointedEntity != null && (d2 < d1 || mc.objectMouseOver == null)) {
					mc.objectMouseOver = new MovingObjectPosition(pointedEntity, vec33);

					if (pointedEntity instanceof EntityLivingBase || pointedEntity instanceof EntityItemFrame) {
						mc.pointedEntity = pointedEntity;
					}
				}

				mc.mcProfiler.endSection();
			}
		}
		return pointedEntity;
	}
}
