package com.minecraftabnormals.upgrade_aquatic.common.entities.thrasher.ai;

import com.minecraftabnormals.abnormals_core.core.endimator.entity.EndimatedEntity;
import com.minecraftabnormals.abnormals_core.core.util.NetworkUtil;
import com.minecraftabnormals.upgrade_aquatic.common.entities.thrasher.ThrasherEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;

import java.util.EnumSet;

public class ThrasherThrashGoal extends Goal {
	public ThrasherEntity thrasher;
	private float originalYaw;
	private float thrashedTicks;

	public ThrasherThrashGoal(ThrasherEntity thrasher) {
		this.thrasher = thrasher;
		this.setFlags(EnumSet.of(Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		Entity passenger = !thrasher.getPassengers().isEmpty() ? this.thrasher.getPassengers().get(0) : null;
		if (passenger instanceof PlayerEntity) {
			if (((PlayerEntity) passenger).isCreative() || passenger.isSpectator()) {
				return false;
			}
		}
		return !this.thrasher.isStunned() && passenger != null && this.thrasher.isNoEndimationPlaying() && this.thrasher.getRandom().nextFloat() < 0.1F;
	}

	@Override
	public boolean canContinueToUse() {
		Entity passenger = !thrasher.getPassengers().isEmpty() ? this.thrasher.getPassengers().get(0) : null;
		if (passenger instanceof PlayerEntity) {
			if (((PlayerEntity) passenger).isCreative() || passenger.isSpectator()) {
				return false;
			}
		}
		return !this.thrasher.isStunned() && this.thrashedTicks <= 55 && passenger != null;
	}

	@Override
	public void start() {
		this.originalYaw = this.thrasher.yRot;
		this.thrasher.setHitsTillStun(this.thrasher.getRandom().nextInt(2) + 2);
		NetworkUtil.setPlayingAnimationMessage(this.thrasher, ThrasherEntity.THRASH_ANIMATION);
	}

	@Override
	public void stop() {
		this.originalYaw = 0;
		this.thrashedTicks = 0;
		NetworkUtil.setPlayingAnimationMessage(this.thrasher, EndimatedEntity.BLANK_ANIMATION);
	}

	@Override
	public void tick() {
		this.thrashedTicks++;

		this.thrasher.getNavigation().stop();

		this.thrasher.yRotO = this.thrasher.yRot;

		this.thrasher.yBodyRot = (this.originalYaw) + 75 * MathHelper.cos(this.thrasher.tickCount * 0.5F) * 1F;
		this.thrasher.yRot = (this.originalYaw) + 75 * MathHelper.cos(this.thrasher.tickCount * 0.5F) * 1F;

		Entity entity = this.thrasher.getPassengers().get(0);

		if (entity instanceof PlayerEntity) {
			this.disablePlayersShield((PlayerEntity) entity);
		}

		entity.setShiftKeyDown(false);

		if (this.thrashedTicks % 5 == 0 && this.thrashedTicks > 0) {
			this.thrasher.playSound(this.thrasher.getThrashingSound(), 1.0F, Math.max(0.75F, this.thrasher.getRandom().nextFloat()));
			entity.hurt(DamageSource.mobAttack(this.thrasher), (float) this.thrasher.getAttribute(Attributes.ATTACK_DAMAGE).getValue());
		}
	}

	private void disablePlayersShield(PlayerEntity player) {
		player.getCooldowns().addCooldown(Items.SHIELD, 30);
	}
}