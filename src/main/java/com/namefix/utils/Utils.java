package com.namefix.utils;

import com.namefix.DeadeyeMod;
import com.namefix.integrations.PointBlankIntegration;
import com.namefix.integrations.TACZIntegration;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.vicmatskiv.pointblank.item.GunItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Utils {
    public static final Matrix4f lastProjMat = new Matrix4f();
    public static final Matrix4f lastModMat = new Matrix4f();
    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();
    public static final int[] lastViewport = new int[4];

    public static HitResult raycastEntity(PlayerEntity player, double maxDistance) {
        Entity cameraEntity = MinecraftClient.getInstance().cameraEntity;
        if (cameraEntity != null) {
            Vec3d cameraPos = player.getCameraPosVec(1.0f);
            Vec3d rot = player.getRotationVec(1.0f);
            Vec3d rayCastContext = cameraPos.add(rot.x * maxDistance, rot.y * maxDistance, rot.z * maxDistance);
            Box box = cameraEntity.getBoundingBox().stretch(rot.multiply(maxDistance)).expand(1d, 1d, 1d);
            return ProjectileUtil.raycast(cameraEntity, cameraPos, rayCastContext, box, (entity -> /* any custom parameters here */ !entity.isSpectator() && entity.canHit()), maxDistance);
        }
        return null;
    }

    public static Vec2f getHeadingFromTarget(Entity entity, EntityAnchorArgumentType.EntityAnchor anchorPoint, Vec3d target) {
        Vec3d vec3d = anchorPoint.positionAt(entity);
        double d = target.x - vec3d.x;
        double e = target.y - vec3d.y;
        double f = target.z - vec3d.z;
        double g = Math.sqrt(d * d + f * f);
        float pitch = MathHelper.wrapDegrees((float)(-(MathHelper.atan2(e, g) * 57.2957763671875)));
        float yaw = MathHelper.wrapDegrees((float)(MathHelper.atan2(f, d) * 57.2957763671875) - 90.0F);
        return new Vec2f(pitch, yaw);
    }

    public static DeadeyeMod.TargetingInteractionType getTargetingInteractionType(ItemStack item) {
        if(item.getItem() instanceof net.elidhan.anim_guns.item.GunItem) return DeadeyeMod.TargetingInteractionType.SAG_GUN;
        if(item.getItem() instanceof RangedWeaponItem) return DeadeyeMod.TargetingInteractionType.BOW;

        if(
                item.getItem().equals(Items.SNOWBALL) ||
                item.getItem().equals(Items.EGG) ||
                item.getItem().equals(Items.SPLASH_POTION) ||
                item.getItem().equals(Items.LINGERING_POTION) ||
                item.getItem().equals(Items.ENDER_PEARL) ||
                item.getItem().equals(Items.ENDER_EYE) ||
                item.getItem().equals(Items.EXPERIENCE_BOTTLE)
        ) return DeadeyeMod.TargetingInteractionType.THROWABLE;

        if(PointBlankIntegration.isLoaded && item.getItem() instanceof GunItem) return DeadeyeMod.TargetingInteractionType.POINT_BLANK_GUN;
        if(TACZIntegration.isLoaded && item.getItem() instanceof AbstractGunItem) return DeadeyeMod.TargetingInteractionType.TACZ_GUN;
        return DeadeyeMod.TargetingInteractionType.DEFAULT;
    }

    public static boolean isInteractionGun(DeadeyeMod.TargetingInteractionType interactionType) {
        return interactionType == DeadeyeMod.TargetingInteractionType.POINT_BLANK_GUN || interactionType == DeadeyeMod.TargetingInteractionType.TACZ_GUN || interactionType == DeadeyeMod.TargetingInteractionType.SAG_GUN;
    }

    @Environment(EnvType.CLIENT)
    public static Vec3d worldSpaceToScreenSpace(Vec3d pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.getEntityRenderDispatcher().camera;
        int displayHeight = client.getWindow().getHeight();
        Vector3f target = new Vector3f();

        double deltaX = pos.x - camera.getPos().x;
        double deltaY = pos.y - camera.getPos().y;
        double deltaZ = pos.z - camera.getPos().z;

        Vector4f transformedCoordinates = new Vector4f((float) deltaX, (float) deltaY, (float) deltaZ, 1.f).mul(
                lastWorldSpaceMatrix);

        Matrix4f matrixProj = new Matrix4f(lastProjMat);
        Matrix4f matrixModel = new Matrix4f(lastModMat);

        matrixProj.mul(matrixModel)
                .project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), lastViewport,
                        target);

        return new Vec3d(target.x / client.getWindow().getScaleFactor(),
                (displayHeight - target.y) / client.getWindow().getScaleFactor(), target.z);
    }

    public static boolean screenSpaceCoordinateIsVisible(Vec3d pos) {
        return pos != null && pos.z > -1 && pos.z < 1;
    }
}
