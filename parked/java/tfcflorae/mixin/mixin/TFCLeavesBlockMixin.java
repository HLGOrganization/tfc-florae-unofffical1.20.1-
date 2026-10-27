package tfcflorae.mixin;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.client.particle.TFCParticles;
import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.IForgeBlockExtension;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.plant.fruit.FruitTreeLeavesBlock;
import net.dries007.tfc.common.blocks.wood.FallenLeavesBlock;
import net.dries007.tfc.common.blocks.wood.ILeavesBlock;
import net.dries007.tfc.common.blocks.wood.TFCLeavesBlock;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.blocks.wood.Wood.BlockType;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.LegacyMaterials;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.Season;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.storage.loot.LootContext.Builder;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfcflorae.Config;
import tfcflorae.client.particle.FallingLeafParticle;
import tfcflorae.client.particle.TFCFParticles;
import tfcflorae.common.blocks.ICooldown;
import tfcflorae.common.blocks.wood.TFCFLeavesBlock;
import tfcflorae.common.blocks.wood.TFCFMangroveLeavesBlock;
import tfcflorae.util.TFCFHelpers;

@Mixin({TFCLeavesBlock.class})
public abstract class TFCLeavesBlockMixin extends Block implements ILeavesBlock, IForgeBlockExtension, IFluidLoggable, ICooldown {
   @Shadow
   private final Supplier<? extends Block> fallenLeaves;
   @Shadow
   private final Supplier<? extends Block> fallenTwig;
   @Unique
   private boolean isDead;
   @Unique
   private long cooldown = Long.MIN_VALUE;

   public TFCLeavesBlockMixin(ExtendedProperties properties, @Nullable Supplier<? extends Block> fallenLeaves, @Nullable Supplier<? extends Block> fallenTwig) {
      super(properties.properties());
      this.isDead = false;
      this.fallenLeaves = fallenLeaves;
      this.fallenTwig = fallenTwig;
   }

   @Inject(
      method = {"<init>"},
      at = {@At("RETURN")}
   )
   private void inject$init(CallbackInfo ci) {
      this.cooldown = Long.MIN_VALUE;
      this.isDead = false;
   }

   @Overwrite(
      remap = false
   )
   public static void doParticles(ServerLevel level, double x, double y, double z, int count) {
      level.sendParticles((SimpleParticleType)TFCParticles.LEAF.get(), x, y, z, count, 0.0, 0.0, 0.0, 0.3F);
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (!(Boolean)state.getValue(TFCLeavesBlock.PERSISTENT)
         && (
            random.nextInt(30) == 0 && (pos.getY() > 110 || Calendars.get(level).getCalendarMonthOfYear().getSeason() == Season.FALL)
               || FallingLeafParticle.isAcceptedLeaves(state) && random.nextInt(10) == 0
         )) {
         BlockPos belowPos = pos.below();
         BlockState belowState = level.getBlockState(belowPos);
         if (belowState.isAir()) {
            double d0 = (double)pos.getX() + random.nextDouble();
            double d1 = (double)pos.getY() - 0.05;
            double d2 = (double)pos.getZ() + random.nextDouble();
            level.addParticle(new BlockParticleOption((ParticleType)TFCFParticles.FALLING_LEAF.get(), state), d0, d1, d2, 0.0, 0.0, 0.0);
         }
      }
   }

   @Inject(
      method = {"randomTick"},
      at = {@At("TAIL")},
      cancellable = true
   )
   private void inject$randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
      Block thisBlock = this.defaultBlockState().getBlock();
      if ((Integer)Config.COMMON.leavesSaplingPlacementChance.get() > 0
         && !(thisBlock instanceof TFCFLeavesBlock)
         && !(thisBlock instanceof TFCFMangroveLeavesBlock)) {
         Season currentSeason = Calendars.get(level).getCalendarMonthOfYear().getSeason();
         if ((currentSeason == Season.FALL || currentSeason == Season.SPRING) && level.getBlockState(pos.below()).isAir()) {
            ChunkDataProvider provider = ChunkDataProvider.get(level);
            ChunkData data = provider.get(level, pos);
            float rainfall = data.getRainfall(pos);
            float rainfallInverted = (500.0F - rainfall) * 0.25F + 1.0F;
            float actualForestDensity = data.getForestDensity();
            float forestDensity = actualForestDensity == 0.0F ? 0.001F : actualForestDensity;
            if (random.nextFloat((float)((Integer)Config.COMMON.leavesSaplingPlacementChance.get()).intValue() / forestDensity * rainfallInverted) == 0.0F) {
               Block sapling = null;

               for (Wood wood : Wood.values()) {
                  if (((RegistryObject)((Map)TFCBlocks.WOODS.get(wood)).get(BlockType.LEAVES)).get() != null
                     && ((Block)((RegistryObject)((Map)TFCBlocks.WOODS.get(wood)).get(BlockType.LEAVES)).get()).equals(thisBlock)) {
                     sapling = (Block)((RegistryObject)((Map)TFCBlocks.WOODS.get(wood)).get(BlockType.SAPLING)).get();
                     break;
                  }
               }

               if (sapling != null) {
                  int x = pos.getX()
                     + (int)Math.round(random.nextGaussian() * (double)((Integer)Config.COMMON.leavesSaplingSpreadDistance.get()).intValue());
                  int z = pos.getZ()
                     + (int)Math.round(random.nextGaussian() * (double)((Integer)Config.COMMON.leavesSaplingSpreadDistance.get()).intValue());
                  int y = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                  BlockPos placementPos = new BlockPos(x, y, z);
                  BlockState placementState = level.getBlockState(placementPos);
                  if ((Helpers.isBlock(placementState, Blocks.PLANTS) || EnvironmentHelpers.isWorldgenReplaceable(placementState))
                     && level.getMaxLocalRawBrightness(placementPos) >= 9
                     && sapling.defaultBlockState().canSurvive(level, placementPos)) {
                     level.setBlock(placementPos, sapling.defaultBlockState(), 3);
                  }
               }
            }
         }
      }

      double tempThreshold = (Double)Config.COMMON.foliageDecayThreshold.get();
      int check = random.nextInt((Integer)Config.COMMON.fruitingLeavesUpdateChance.get());
      if (!(state.getBlock() instanceof FruitTreeLeavesBlock) && Helpers.isBlock(state, Blocks.SEASONAL_LEAVES)) {
         if (!this.isDead && check == 0) {
            if ((double)Climate.getTemperature(level, pos) < tempThreshold && (double)TFCFHelpers.getAverageDailyTemperature(level, pos) < tempThreshold) {
               this.isDead = true;
               level.blockUpdated(pos, state.getBlock());
               if (level.getBlockState(pos.above()).getBlock() instanceof SnowLayerBlock) {
                  level.setBlock(pos.above(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
               }

               return;
            }
         } else if (this.isDead
            && this.getCooldown(level, this.cooldown)
            && check == 1
            && (double)Climate.getTemperature(level, pos) >= tempThreshold
            && (double)TFCFHelpers.getAverageDailyTemperature(level, pos) >= tempThreshold) {
            this.setCooldown(level, 72000L, this.cooldown);
            this.isDead = false;
            level.blockUpdated(pos, state.getBlock());
            return;
         }
      }
   }

   @Inject(
      method = {"entityInside"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = true
   )
   private void inject$entityInside(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
      if (this.isDead) {
         ci.cancel();
      }
   }

   public RenderShape getRenderShape(BlockState state) {
      return this.isDead ? RenderShape.INVISIBLE : super.getRenderShape(state);
   }

   protected boolean isAir(BlockState state) {
      return this.isDead ? true : super.isAir(state);
   }

   public List<ItemStack> getDrops(BlockState state, Builder builder) {
      return this.isDead ? Collections.emptyList() : super.getDrops(state, builder);
   }

   public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
      return this.isDead ? true : super.canBeReplaced(state, context);
   }

   @Inject(
      method = {"getShadeBrightness"},
      at = {@At("TAIL")},
      cancellable = true,
      remap = true
   )
   private void inject$getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
      if (this.isDead) {
         cir.setReturnValue(1.0F);
      } else {
         cir.getReturnValue();
      }
   }

   @Overwrite(
      remap = false
   )
   public void createDestructionEffects(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, boolean replaceOnlyAir) {
      BlockState twig = this.getFallenTwig();
      BlockState leaf = this.getFallenLeaves();
      if (twig != null || leaf != null) {
         MutableBlockPos cursor = new MutableBlockPos();
         cursor.set(pos);
         BlockState stateAt = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();

         while (stateAt.getBlock() instanceof ILeavesBlock || LegacyMaterials.isReplaceable(stateAt)) {
            cursor.move(0, -1, 0);
            stateAt = level.getBlockState(cursor);
         }

         cursor.move(0, 1, 0);
         stateAt = level.getBlockState(cursor);
         if (stateAt.getBlock() instanceof FallenLeavesBlock && twig != null) {
            if ((Integer)stateAt.getValue(BlockStateProperties.LAYERS) == 8) {
               level.setBlock(cursor.above(), (BlockState)twig.setValue(BlockStateProperties.LAYERS, 1), 3);
            } else {
               stateAt.setValue(BlockStateProperties.LAYERS, (Integer)stateAt.getValue(BlockStateProperties.LAYERS) + 1);
            }
         } else if (LegacyMaterials.isReplaceable(stateAt)) {
            BlockState placeState = twig == null ? leaf : twig;
            if (leaf != null && twig != null && random.nextFloat() < 0.5F) {
               placeState = leaf;
            }

            if (placeState.canSurvive(level, cursor)) {
               if (replaceOnlyAir && !stateAt.isAir()) {
                  return;
               }

               level.setBlockAndUpdate(cursor, placeState);
            }
         }
      }
   }

   @Shadow
   public BlockState getFallenLeaves() {
      return this.fallenLeaves == null ? null : this.fallenLeaves.get().defaultBlockState();
   }

   @Shadow
   public BlockState getFallenTwig() {
      return this.fallenTwig == null ? null : this.fallenTwig.get().defaultBlockState();
   }
}
