package tfcflorae.common.entities;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.entities.EntityHelpers;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.Brain.Provider;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import tfcflorae.client.TFCFSounds;
import tfcflorae.common.entities.ai.TadpoleAi;
import tfcflorae.common.items.TFCFItems;

public class Tadpole extends AbstractFish {
   public static final int MAX_TADPOLE_AGE = Math.abs(-24000);
   private int age;
   protected static final ImmutableList<SensorType<? extends Sensor<? super Tadpole>>> SENSORS = ImmutableList.of(
      SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.HURT_BY
   );
   protected static final ImmutableList<MemoryModuleType<?>> MEMORIES = ImmutableList.of(
      MemoryModuleType.LOOK_TARGET,
      MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
      MemoryModuleType.WALK_TARGET,
      MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
      MemoryModuleType.PATH,
      MemoryModuleType.NEAREST_VISIBLE_ADULT
   );

   public Tadpole(EntityType<? extends AbstractFish> type, Level level) {
      super(type, level);
      this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 0.1F, true);
      this.lookControl = new SmoothSwimmingLookControl(this, 10);
   }

   protected PathNavigation createNavigation(Level level) {
      return new WaterBoundPathNavigation(this, level);
   }

   protected Provider<Tadpole> brainProvider() {
      return Brain.provider(MEMORIES, SENSORS);
   }

   protected Brain<?> makeBrain(Dynamic<?> dynamic) {
      return TadpoleAi.create(this.brainProvider().makeBrain(dynamic));
   }

   public Brain<Tadpole> getBrain() {
      return (Brain<Tadpole>) super.getBrain();
   }

   protected SoundEvent getFlopSound() {
      return (SoundEvent)TFCFSounds.TADPOLE_FLOP.get();
   }

   protected void customServerAiStep() {
      this.level().getProfiler().push("tadpoleBrain");
      this.getBrain().tick((ServerLevel)this.level(), this);
      this.level().getProfiler().pop();
      this.level().getProfiler().push("tadpoleActivityUpdate");
      TadpoleAi.updateActivities(this);
      this.level().getProfiler().pop();
      super.customServerAiStep();
   }

   public static Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 1.0).add(Attributes.MAX_HEALTH, 6.0);
   }

   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide) {
         this.setAge(this.age + 1);
      }
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("Age", this.age);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setAge(tag.getInt("Age"));
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return null;
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource source) {
      return TFCFSounds.TADPOLE.hurt().get();
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return TFCFSounds.TADPOLE.death().get();
   }

   protected InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (this.isSlimeBall(stack)) {
         this.eatSlimeBall(player, stack);
         return InteractionResult.sidedSuccess(this.level().isClientSide());
      } else {
         return EntityHelpers.bucketMobPickup(player, hand, this).orElse(super.mobInteract(player, hand));
      }
   }

   protected void sendDebugPackets() {
      super.sendDebugPackets();
      DebugPackets.sendEntityBrain(this);
   }

   public boolean fromBucket() {
      return true;
   }

   public void setFromBucket(boolean fromBucket) {
   }

   public void saveToBucketTag(ItemStack stack) {
      Bucketable.saveDefaultDataToBucketTag(this, stack);
      CompoundTag nbtCompound = stack.getOrCreateTag();
      nbtCompound.putInt("Age", this.getAge());
   }

   public void loadFromBucketTag(CompoundTag nbt) {
      Bucketable.loadDefaultDataFromBucketTag(this, nbt);
      if (nbt.contains("Age")) {
         this.setAge(nbt.getInt("Age"));
      }
   }

   public ItemStack getBucketItemStack() {
      return new ItemStack((ItemLike)TFCFItems.TADPOLE_BUCKET.get());
   }

   public SoundEvent getPickupSound() {
      return (SoundEvent)TFCFSounds.BUCKED_FILL_TADPOLE.get();
   }

   private boolean isSlimeBall(ItemStack stack) {
      return Frog.FOOD.test(stack);
   }

   private void eatSlimeBall(Player player, ItemStack stack) {
      this.decrementItem(player, stack);
      this.increaseAge((int)((float)(this.getTicksUntilGrowth() / 20) * 0.1F));
      this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
   }

   private void decrementItem(Player player, ItemStack stack) {
      if (!player.getAbilities().instabuild) {
         stack.shrink(1);
      }
   }

   private int getAge() {
      return this.age;
   }

   private void increaseAge(int seconds) {
      this.setAge(this.age + seconds * 20);
   }

   private void setAge(int age) {
      this.age = age;
      if (this.age >= MAX_TADPOLE_AGE) {
         this.growUp();
      }
   }

   private void growUp() {
      if (this.level() instanceof ServerLevel server) {
         Frog frog = (Frog)((EntityType)TFCFEntities.FROG.get()).create(this.level());
         if (frog == null) {
            return;
         }

         frog.setIsMale(this.random.nextBoolean());
         frog.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
         frog.finalizeSpawn(server, this.level().getCurrentDifficultyAt(frog.blockPosition()), MobSpawnType.CONVERSION, null, null);
         frog.setNoAi(this.isNoAi());
         if (this.hasCustomName()) {
            frog.setCustomName(this.getCustomName());
            frog.setCustomNameVisible(this.isCustomNameVisible());
         }

         frog.setPersistenceRequired();
         this.playSound((SoundEvent)TFCFSounds.TADPOLE_GROW_UP.get(), 0.15F, 1.0F);
         server.addFreshEntityWithPassengers(frog);
         this.discard();
      }
   }

   private int getTicksUntilGrowth() {
      return Math.max(0, MAX_TADPOLE_AGE - this.age);
   }


   protected float getBlockSpeedFactor() {
      return Helpers.isBlock(this.level().getBlockState(this.blockPosition()), Blocks.PLANTS) ? 1.0F : super.getBlockSpeedFactor();
   }

   public boolean canBeLeashed(Player player) {
      return !this.isLeashed() && !(this instanceof Enemy);
   }
}
