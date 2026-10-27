package tfcflorae.common.entities;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.util.RandomSource;
import java.util.function.Predicate;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.dries007.tfc.common.entities.EntityHelpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.Brain.Provider;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import tfcflorae.client.TFCFSounds;
import tfcflorae.client.model.entity.animation.api.TFCFAnimationState;
import tfcflorae.common.TFCFTags;
import tfcflorae.common.entities.access.api.Poses;
import tfcflorae.common.entities.ai.FrogAi;
import tfcflorae.common.entities.ai.TFCFBrain;

public class Frog extends Animal {
   public static final Ingredient FOOD = Ingredient.of(TFCFTags.Items.FROG_TEMPTATION_ITEMS);
   public static final ImmutableList<? extends SensorType<? extends Sensor<? super Frog>>> SENSORS = ImmutableList.of(
      SensorType.NEAREST_LIVING_ENTITIES,
      SensorType.HURT_BY,
      (SensorType)TFCFBrain.FROG_ATTACKABLES.get(),
      (SensorType)TFCFBrain.FROG_TEMPTATIONS.get(),
      (SensorType)TFCFBrain.IS_IN_WATER_SENSOR.get()
   );
   public static final ImmutableList<MemoryModuleType<?>> MEMORIES = ImmutableList.of(
      MemoryModuleType.LOOK_TARGET,
      MemoryModuleType.NEAREST_LIVING_ENTITIES,
      MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
      MemoryModuleType.WALK_TARGET,
      MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
      MemoryModuleType.PATH,
      MemoryModuleType.BREED_TARGET,
      MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS,
      MemoryModuleType.LONG_JUMP_MID_JUMP,
      MemoryModuleType.ATTACK_TARGET,
      MemoryModuleType.TEMPTING_PLAYER,
      MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
      new MemoryModuleType[]{
         MemoryModuleType.IS_TEMPTED,
         MemoryModuleType.HURT_BY,
         MemoryModuleType.HURT_BY_ENTITY,
         MemoryModuleType.NEAREST_ATTACKABLE,
         (MemoryModuleType)TFCFBrain.IS_IN_WATER_MEMORY.get(),
         (MemoryModuleType)TFCFBrain.IS_PREGNANT.get(),
         (MemoryModuleType)TFCFBrain.UNREACHABLE_TONGUE_TARGETS.get()
      }
   );
   public static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(Frog.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<OptionalInt> TARGET = SynchedEntityData.defineId(Frog.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);
   public static final EntityDataAccessor<Boolean> DATA_IS_MALE = SynchedEntityData.defineId(Frog.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<Float> DATA_FAMILIARITY = SynchedEntityData.defineId(Frog.class, EntityDataSerializers.FLOAT);
   public static final EntityDataAccessor<Integer> ID_SIZE = SynchedEntityData.defineId(Frog.class, EntityDataSerializers.INT);
   public static final int MAX_SIZE = 20;
   public static final int MIN_SIZE = 9;
   public long lastMated = Long.MIN_VALUE;
   public long nextFeedTime = Long.MIN_VALUE;
   public static final Predicate<LivingEntity> SCARY_MOB = entity -> entity instanceof Player && ((Player)entity).isCreative()
         ? false
         : entity.getType() == EntityType.AXOLOTL || entity.getMobType() != MobType.WATER;
   public static final TargetingConditions targetingConditions = TargetingConditions.forNonCombat().ignoreInvisibilityTesting().ignoreLineOfSight().selector(SCARY_MOB);
   public final TFCFAnimationState longJumpingAnimationState = new TFCFAnimationState();
   public final TFCFAnimationState croakingAnimationState = new TFCFAnimationState();
   public final TFCFAnimationState usingTongueAnimationState = new TFCFAnimationState();
   public final TFCFAnimationState walkingAnimationState = new TFCFAnimationState();
   public final TFCFAnimationState swimmingAnimationState = new TFCFAnimationState();
   public final TFCFAnimationState idlingInWaterAnimationState = new TFCFAnimationState();

   public Frog(EntityType<? extends Animal> type, Level level) {
      super(type, level);
      this.lookControl = new Frog.FrogLookController(this);
      this.setPathfindingMalus(BlockPathTypes.WATER, 4.0F);
      this.setPathfindingMalus(BlockPathTypes.TRAPDOOR, -1.0F);
      this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 0.1F, true);
      this.setMaxUpStep(1.0F);
   }

   protected Provider<Frog> brainProvider() {
      return Brain.provider(MEMORIES, SENSORS);
   }

   protected Brain<?> makeBrain(Dynamic<?> dynamic) {
      return FrogAi.create(this.brainProvider().makeBrain(dynamic));
   }

   public Brain<Frog> getBrain() {
      return (Brain<Frog>) super.getBrain();
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(VARIANT, 0);
      this.entityData.define(TARGET, OptionalInt.empty());
      this.entityData.define(ID_SIZE, 9);
      this.entityData.define(DATA_IS_MALE, true);
      this.entityData.define(DATA_FAMILIARITY, 0.0F);
   }

   public void clearFrogTarget() {
      this.entityData.set(TARGET, OptionalInt.empty());
   }

   public Optional<Entity> getFrogTarget() {
      return ((OptionalInt)this.entityData.get(TARGET)).stream().<Entity>mapToObj(this.level()::getEntity).filter(Objects::nonNull).findFirst();
   }

   public void setFrogTarget(Entity entity) {
      this.entityData.set(TARGET, OptionalInt.of(entity.getId()));
   }

   public int getHeadRotSpeed() {
      return 35;
   }

   public int getMaxHeadYRot() {
      return 5;
   }

   public Frog.Variant getVariant() {
      return Frog.Variant.byId((Integer)this.entityData.get(VARIANT));
   }

   public void setVariant(Frog.Variant variant) {
      this.entityData.set(VARIANT, variant.getId());
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("variant", this.getVariant().getId());
      tag.putInt("size", this.getSize() - 1);
      tag.putBoolean("male", this.isMale());
      tag.putFloat("familiarity", this.getFamiliarity());
      tag.putLong("lastMated", this.lastMated);
      tag.putLong("nextFeed", this.nextFeedTime);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setVariant(Frog.Variant.byId(tag.getInt("variant")));
      this.setSize(tag.getInt("size") + 1, false);
      this.setIsMale(tag.getBoolean("male"));
      this.setFamiliarity(tag.getFloat("familiarity"));
      this.lastMated = tag.getLong("lastMated");
      this.nextFeedTime = tag.getLong("nextFeed");
   }

   public float getFamiliarity() {
      return (Float)this.entityData.get(DATA_FAMILIARITY);
   }

   public void setFamiliarity(float familiarity) {
      this.entityData.set(DATA_FAMILIARITY, familiarity);
   }

   public void setIsMale(boolean male) {
      this.entityData.set(DATA_IS_MALE, male);
   }

   public boolean isMale() {
      return (Boolean)this.entityData.get(DATA_IS_MALE);
   }

   public void setMated(long ticks) {
      this.lastMated = ticks;
   }

   public boolean canMate(Animal animal) {
      if (animal != this && animal instanceof Frog other) {
         float min = 0.3F;
         return other.isMale() != this.isMale()
            && this.beenLongEnoughToMate()
            && other.beenLongEnoughToMate()
            && this.getFamiliarity() > 0.3F
            && other.getFamiliarity() > 0.3F
            && this.fedRecently()
            && other.fedRecently();
      } else {
         return false;
      }
   }

   public boolean beenLongEnoughToMate() {
      return Calendars.get(this.level()).getTicks() > this.lastMated + 288000L;
   }

   public boolean canBreatheUnderwater() {
      return true;
   }

   public boolean shouldWalk() {
      return this.onGround() && this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6 && !this.isInWaterOrBubble();
   }

   public boolean shouldSwim() {
      return this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6 && this.isInWaterOrBubble();
   }

   public void refreshDimensions() {
      double x = this.getX();
      double y = this.getY();
      double z = this.getZ();
      super.refreshDimensions();
      this.setPos(x, y, z);
   }

   public EntityDimensions getDimensions(Pose pose) {
      return super.getDimensions(pose).scale(this.getVisualScale());
   }

   public float getVisualScale() {
      return 0.05F * (float)this.getSize();
   }

   public Pair<Integer, Integer> getSizeRangeForSpawning() {
      return Pair.of(9, Mth.floor(20.0F * this.sizePoisonous()));
   }

   public int getSize() {
      return (Integer)this.entityData.get(ID_SIZE);
   }

   public void setSize(int size, boolean heal) {
      size = Mth.clamp(size, 9, Mth.floor(20.0F * this.sizePoisonous()));
      this.reapplyPosition();
      this.refreshDimensions();
      Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue((double)size);
      if (heal) {
         this.setHealth(this.getMaxHealth());
      }

      this.entityData.set(ID_SIZE, size);
   }

   protected void customServerAiStep() {
      this.level().getProfiler().push("frogBrain");
      this.getBrain().tick((ServerLevel)this.level(), this);
      this.level().getProfiler().pop();
      this.level().getProfiler().push("frogActivityUpdate");
      FrogAi.updateActivities(this);
      this.level().getProfiler().pop();
      super.customServerAiStep();
   }

   public void tick() {
      if (this.level().isClientSide()) {
         if (this.shouldWalk()) {
            this.walkingAnimationState.startIfNotRunning(this.tickCount);
         } else {
            this.walkingAnimationState.stop();
         }

         if (this.shouldSwim()) {
            this.idlingInWaterAnimationState.stop();
            this.swimmingAnimationState.startIfNotRunning(this.tickCount);
         } else if (this.isInWaterOrBubble()) {
            this.swimmingAnimationState.stop();
            this.idlingInWaterAnimationState.startIfNotRunning(this.tickCount);
         } else {
            this.swimmingAnimationState.stop();
            this.idlingInWaterAnimationState.stop();
         }
      }

      super.tick();
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
      if (DATA_POSE.equals(data)) {
         if (this.isInPose(Pose.LONG_JUMPING)) {
            this.longJumpingAnimationState.start(this.tickCount);
         } else {
            this.longJumpingAnimationState.stop();
         }

         if (this.isInPose(Poses.CROAKING.get())) {
            this.croakingAnimationState.start(this.tickCount);
         } else {
            this.croakingAnimationState.stop();
         }

         if (this.isInPose(Poses.USING_TONGUE.get())) {
            this.usingTongueAnimationState.start(this.tickCount);
         } else {
            this.usingTongueAnimationState.stop();
         }
      }

      if (ID_SIZE.equals(data)) {
         this.refreshDimensions();
      }

      super.onSyncedDataUpdated(data);
   }

   public boolean isInPose(Pose pose) {
      return this.getPose() == pose;
   }

   @Nullable
   public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mob) {
      if (mob == this) {
         Frog frog = (Frog)((EntityType)TFCFEntities.FROG.get()).create(level);
         if (frog != null) {
            frog.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, UniformInt.of(100, 140).sample(this.random));
         }

         return frog;
      } else {
         if (mob instanceof Frog animal) {
            this.spawnChildFromBreeding(level, animal);
         }

         return null;
      }
   }

   public boolean isBaby() {
      return false;
   }

   public void setBaby(boolean baby) {
   }

   public void spawnChildFromBreeding(ServerLevel level, Animal partner) {
      Optional.ofNullable(this.getLoveCause()).or(() -> Optional.ofNullable(partner.getLoveCause())).ifPresent(player -> {
         player.awardStat(Stats.ANIMALS_BRED);
         CriteriaTriggers.BRED_ANIMALS.trigger(player, this, partner, null);
      });
      this.resetLove();
      partner.resetLove();
      level.broadcastEntityEvent(this, (byte)18);
   }

   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor accessor, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData groupData, @Nullable CompoundTag tag
   ) {
      SpawnGroupData spawn = super.finalizeSpawn(accessor, difficulty, spawnType, groupData, tag);
      Pair<Integer, Integer> pair = this.getSizeRangeForSpawning();
      this.setSize(Mth.nextInt(this.random, (Integer)pair.getFirst(), (Integer)pair.getSecond()), true);
      BlockPos pos = this.blockPosition();
      ServerLevel level = accessor.getLevel();
      float temperature = 10.0F;
      if (level != null && pos != null && level.isLoaded(pos) && level.getChunkSource().hasChunk(pos.getX(), pos.getZ())) {
         ChunkData data = EntityHelpers.getChunkDataForSpawning(level, pos);
         temperature = data.getAverageTemp(pos);
      } else if (level == null || pos == null) {
         temperature = (float)this.random.nextInt(23);
      }

      int specialColour = this.random.nextInt(8);
      this.setVariantRandom(specialColour, temperature);
      FrogAi.coolDownLongJump(this, accessor.getRandom());
      this.setIsMale(this.random.nextBoolean());
      return spawn;
   }

   public float sizePoisonous() {
      return this.isPoisonousFrog() ? 0.5F : 1.0F;
   }

   public void setVariantRandom(int chance, float temperature) {
      if (temperature <= 4.0F) {
         if (chance == 0) {
            this.setVariant(Frog.Variant.MINI_MUM);
         } else if (chance == 1) {
            this.setVariant(Frog.Variant.BUSHVELD_RAIN);
         } else if (chance == 2) {
            this.setVariant(Frog.Variant.PARAMO_MARSUPIAL);
         } else if (chance == 3) {
            this.setVariant(Frog.Variant.CAATINGA_HORNED);
         } else {
            this.setVariant(Frog.Variant.GREEN_TREE);
         }
      } else if (temperature > 15.0F) {
         if (chance == 0) {
            this.setVariant(Frog.Variant.RED_HEADED_POISON);
         } else if (chance == 1) {
            this.setVariant(Frog.Variant.TARAPOTO_POISON);
         } else if (chance == 2) {
            this.setVariant(Frog.Variant.PAINTED_MANTELLA);
         } else if (chance == 3) {
            this.setVariant(Frog.Variant.STRAWBERRY_POISON_DART);
         } else {
            this.setVariant(Frog.Variant.ARUM);
         }
      } else if (chance == 0) {
         this.setVariant(Frog.Variant.BLUE_BACK_REED);
      } else if (chance == 1) {
         this.setVariant(Frog.Variant.GRAY_TREE);
      } else if (chance == 2) {
         this.setVariant(Frog.Variant.MARBLED_PYGMY);
      } else if (chance == 3) {
         this.setVariant(Frog.Variant.SONORAN_GREEN);
      } else {
         this.setVariant(Frog.Variant.WOOD);
      }
   }

   public static Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 1.0).add(Attributes.MAX_HEALTH, 4.0).add(Attributes.ATTACK_DAMAGE, 10.0);
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return TFCFSounds.FROG.ambient().get();
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource source) {
      return TFCFSounds.FROG.hurt().get();
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return TFCFSounds.FROG.death().get();
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(TFCFSounds.FROG.step().get(), 0.15F, 1.0F);
   }

   public boolean isPushedByFluid() {
      return false;
   }

   protected void sendDebugPackets() {
      super.sendDebugPackets();
      DebugPackets.sendEntityBrain(this);
   }

   protected int calculateFallDamage(float fallDistance, float damageMultiplier) {
      return super.calculateFallDamage(fallDistance, damageMultiplier) - 5;
   }

   public void travel(Vec3 input) {
      if (this.isEffectiveAi() && this.isInWater()) {
         this.moveRelative(this.getSpeed(), input);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
      } else {
         super.travel(input);
      }
   }

   public boolean canCutCorner(BlockPathTypes type) {
      return type != BlockPathTypes.WATER_BORDER;
   }

   public static boolean isValidFrogFood(LivingEntity entity) {
      return (!(entity instanceof Slime slime) || slime.getSize() <= 1) && entity.getType().is(TFCFTags.Entities.FROG_FOOD);
   }

   public boolean isValidFrogFoodS(LivingEntity entity) {
      return (!(entity instanceof Slime slime) || slime.getSize() <= 1 * this.getSize()) && entity.getType().is(TFCFTags.Entities.FROG_FOOD);
   }

   protected PathNavigation createNavigation(Level level) {
      return new Frog.FrogPathNavigator(this, level);
   }

   public boolean isFood(ItemStack stack) {
      return stack.getCapability(FoodCapability.CAPABILITY).filter(IFood::isRotten).isPresent() ? false : FOOD.test(stack);
   }

   public boolean fedRecently() {
      return Calendars.get(this.level()).getTicks() < this.nextFeedTime;
   }

   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack held = player.getItemInHand(hand);
      if (this.isFood(held)) {
         if (!this.level().isClientSide) {
            long ticks = Calendars.SERVER.getTicks();
            if (ticks > this.nextFeedTime) {
               this.setFamiliarity(this.getFamiliarity() + 0.1F);
               this.nextFeedTime = ticks + 24000L;
               this.usePlayerItem(player, hand, held);
               this.playSound((SoundEvent)TFCFSounds.FROG_EAT.get(), 1.0F, 1.0F);
            }
         }

         return InteractionResult.SUCCESS;
      } else {
         return super.mobInteract(player, hand);
      }
   }

   public void aiStep() {
      super.aiStep();
      if (this.isAlive() && this.isPoisonousFrog()) {
         for (Mob mob : this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(0.3), target -> targetingConditions.test(this, target))) {
            if (mob.isAlive() && !(mob instanceof Frog)) {
               this.touch(mob);
            }
         }
      }
   }

   public void touch(Mob mob) {
      if (this.isPoisonousFrog() && mob.hurt(this.damageSources().mobAttack(this), 2.0F) && !(mob instanceof Frog)) {
         mob.addEffect(new MobEffectInstance(MobEffects.POISON, Mth.floor(60.0F / ((float)this.getSize() * 0.3F)), 0));
         mob.addEffect(new MobEffectInstance(MobEffects.CONFUSION, Mth.floor(30.0F / ((float)this.getSize() * 0.3F)), 0));
         this.playSound(SoundEvents.PUFFER_FISH_STING, 1.0F, 1.0F);
      }
   }

   public void playerTouch(Player player) {
      if (this.isPoisonousFrog() && player instanceof ServerPlayer && player.hurt(this.damageSources().mobAttack(this), 2.0F)) {
         if (!this.isSilent()) {
            ((ServerPlayer)player).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.PUFFER_FISH_STING, 0.0F));
         }

         player.addEffect(new MobEffectInstance(MobEffects.POISON, Mth.floor(60.0F / ((float)this.getSize() * 0.3F)), 0));
         player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, Mth.floor(30.0F / ((float)this.getSize() * 0.3F)), 0));
      }
   }

   public boolean isPoisonousFrog() {
      return this.getVariant() == Frog.Variant.PAINTED_MANTELLA
         || this.getVariant() == Frog.Variant.RED_HEADED_POISON
         || this.getVariant() == Frog.Variant.STRAWBERRY_POISON_DART
         || this.getVariant() == Frog.Variant.TARAPOTO_POISON;
   }

   public boolean isColdFrog() {
      return this.getVariant() == Frog.Variant.MINI_MUM
         || this.getVariant() == Frog.Variant.BUSHVELD_RAIN
         || this.getVariant() == Frog.Variant.PARAMO_MARSUPIAL
         || this.getVariant() == Frog.Variant.CAATINGA_HORNED
         || this.getVariant() == Frog.Variant.GREEN_TREE;
   }

   public boolean isTemperateFrog() {
      return this.getVariant() == Frog.Variant.BLUE_BACK_REED
         || this.getVariant() == Frog.Variant.GRAY_TREE
         || this.getVariant() == Frog.Variant.MARBLED_PYGMY
         || this.getVariant() == Frog.Variant.SONORAN_GREEN
         || this.getVariant() == Frog.Variant.WOOD;
   }

   public boolean isWarmFrog() {
      return this.getVariant() == Frog.Variant.RED_HEADED_POISON
         || this.getVariant() == Frog.Variant.TARAPOTO_POISON
         || this.getVariant() == Frog.Variant.PAINTED_MANTELLA
         || this.getVariant() == Frog.Variant.STRAWBERRY_POISON_DART
         || this.getVariant() == Frog.Variant.ARUM;
   }

   public static boolean checkFrogSpawnRules(EntityType<? extends Animal> type, LevelAccessor accessor, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
      return accessor.getBlockState(pos.below()).is(TFCFTags.Blocks.FROGS_SPAWNABLE_ON) && isBrightEnoughToSpawn(accessor, pos);
   }

   class FrogLookController extends LookControl {
      FrogLookController(Mob mobEntity) {
         super(mobEntity);
      }

      protected boolean resetXRotOnTick() {
         return Frog.this.getFrogTarget().isEmpty();
      }
   }

   static class FrogNodeEvaluator extends AmphibiousNodeEvaluator {
      public final MutableBlockPos preferredBlock = new MutableBlockPos();

      public FrogNodeEvaluator(boolean penalizeDeepWater) {
         super(penalizeDeepWater);
      }

      public BlockPathTypes getBlockPathType(BlockGetter getter, int x, int y, int z) {
         this.preferredBlock.set(x, y - 1, z);
         BlockState state = getter.getBlockState(this.preferredBlock);
         return state.is(TFCFTags.Blocks.FROG_PREFER_JUMP_TO) ? BlockPathTypes.OPEN : getBlockPathTypeStatic(getter, this.preferredBlock.move(Direction.UP));
      }
   }

   static class FrogPathNavigator extends WaterBoundPathNavigation {
      FrogPathNavigator(Frog frog, Level level) {
         super(frog, level);
      }

      protected PathFinder createPathFinder(int range) {
         this.nodeEvaluator = new Frog.FrogNodeEvaluator(true);
         this.nodeEvaluator.setCanPassDoors(true);
         return new PathFinder(this.nodeEvaluator, range);
      }

      protected boolean canUpdatePath() {
         return true;
      }

      public boolean isStableDestination(BlockPos pos) {
         return !this.level.getBlockState(pos.below()).isAir();
      }
   }

   public static enum Variant {
      ARUM(0, "arum"),
      WOOD(1, "wood"),
      GREEN_TREE(2, "green_tree"),
      BLUE_BACK_REED(3, "blue_back_reed"),
      CAATINGA_HORNED(4, "cattinga_horned"),
      GRAY_TREE(5, "gray_tree"),
      MARBLED_PYGMY(6, "marbled_pygmy"),
      PAINTED_MANTELLA(7, "painted_mantella"),
      PARAMO_MARSUPIAL(8, "paramo_marsupial"),
      RED_HEADED_POISON(9, "red_headed_poison"),
      SONORAN_GREEN(10, "sonoran_green"),
      STRAWBERRY_POISON_DART(11, "strawberry_poison_dart"),
      TARAPOTO_POISON(12, "tarapoto_poison"),
      MINI_MUM(13, "mini_mum"),
      BUSHVELD_RAIN(14, "bushveld_rain");

      public static final Frog.Variant[] VARIANTS = Arrays.stream(values()).sorted(Comparator.comparingInt(Frog.Variant::getId)).toArray(Frog.Variant[]::new);
      public final int id;
      public final String name;

      private Variant(int id, String name) {
         this.id = id;
         this.name = name;
      }

      public int getId() {
         return this.id;
      }

      public String getName() {
         return this.name;
      }

      public static Frog.Variant byId(int id) {
         if (id < 0 || id >= VARIANTS.length) {
            id = 0;
         }

         return VARIANTS[id];
      }
   }
}
