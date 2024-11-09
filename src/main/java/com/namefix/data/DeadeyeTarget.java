package com.namefix.data;

import com.namefix.utils.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class DeadeyeTarget {
    public Entity target;
    Vec3d initialPose;
    Vec3d markOffset;
    public int renderTick = 0;

    public DeadeyeTarget(Entity target, Vec3d initialPose) {
        this.target = target;
        this.initialPose = initialPose;
        this.markOffset = target.getPos().subtract(initialPose);
    }

    // Returns current mark position
    public Vec3d getCurrentOffset() {
        return target.getPos().subtract(markOffset);
    }

    // Returns current angle to the target (pitch,yaw)
    @Environment(EnvType.CLIENT)
    public Vec2f getCurrentHeading() {
        return Utils.getHeadingFromTarget(MinecraftClient.getInstance().player, EntityAnchorArgumentType.EntityAnchor.EYES, getCurrentOffset());
    }

}
