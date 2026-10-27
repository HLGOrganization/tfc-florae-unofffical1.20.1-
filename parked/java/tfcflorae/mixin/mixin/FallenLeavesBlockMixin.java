package tfcflorae.mixin;

import net.minecraft.util.RandomSource;
import javax.annotation.Nullable;
import net.dries007.tfc.client.particle.TFCParticles;
import net.dries007.tfc.common.blockentities.IFarmland;
import net.dries007.tfc.common.blockentities.FarmlandBlockEntity.NutrientType;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.GroundcoverBlock;
import net.dries007.tfc.common.blocks.wood.FallenLeavesBlock;
import net.dries007.tfc.common.fluids.FluidProperty.FluidKey;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.Season;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin({FallenLeavesBlock.class})
public class FallenLeavesBlockMixin extends GroundcoverBlock {
   @Unique
   private static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;
   @Unique
   private static final VoxelShape[] SHAPES = new VoxelShape[]{
      Shapes.empty(),
      Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
   };
   @Shadow
   private final ExtendedProperties properties;

   public FallenLeavesBlockMixin(ExtendedProperties properties) {
      super(properties, SHAPES[1], null);
      this.properties = properties;
   }

   public ExtendedProperties getExtendedProperties() {
      return this.properties;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{LAYERS, this.getFluidProperty()});
   }

   @Overwrite(
      remap = false
   )
   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return SHAPES[state.getValue(LAYERS)];
   }

   public VoxelShape getVisualShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
      return SHAPES[state.getValue(LAYERS)];
   }

   public boolean useShapeForLightOcclusion(BlockState state) {
      return true;
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      return super.canSurvive(state, level, pos) || level.getBlockEntity(pos.below()) instanceof IFarmland;
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (random.nextInt((Integer)TFCConfig.SERVER.snowMeltChance.get()) == 0 && Calendars.get(level).getCalendarMonthOfYear().getSeason() != Season.FALL) {
         if (level.getBlockEntity(pos.below()) instanceof IFarmland farmland) {
            for (NutrientType nutrient : NutrientType.values()) {
               if (random.nextBoolean()) {
                  float amount = random.nextFloat() * 0.1F;
                  farmland.addNutrient(nutrient, amount);
                  this.addNutrientParticles(level, pos, nutrient, amount);
               }
            }
         }

         if ((Integer)state.getValue(LAYERS) >= 2) {
            level.setBlock(pos, (BlockState)state.setValue(LAYERS, (Integer)state.getValue(LAYERS) - 1), 3);
         } else {
            level.removeBlock(pos, false);
         }

         this.doParticles(
            level,
            (double)((float)pos.getX() + random.nextFloat()),
            (double)((float)pos.getY() + random.nextFloat()),
            (double)((float)pos.getZ() + random.nextFloat()),
            1
         );
      }
   }

   @Unique
   private void addNutrientParticles(ServerLevel level, BlockPos pos, NutrientType nutrient, float amount) {
      if (nutrient == NutrientType.NITROGEN) {
         for (int i = 0; i < (int)(amount > 0.0F ? Mth.clamp(amount * 10.0F, 1.0F, 5.0F) : 0.0F); i++) {
            level.sendParticles(
               (SimpleParticleType)TFCParticles.NITROGEN.get(),
               (double)((float)pos.getX() + level.random.nextFloat()),
               (double)pos.getY() + (double)level.random.nextFloat() / 5.0,
               (double)((float)pos.getZ() + level.random.nextFloat()),
               0,
               0.0,
               0.0,
               0.0,
               1.0
            );
         }
      }

      if (nutrient == NutrientType.PHOSPHOROUS) {
         for (int i = 0; i < (int)(amount > 0.0F ? Mth.clamp(amount * 10.0F, 1.0F, 5.0F) : 0.0F); i++) {
            level.sendParticles(
               (SimpleParticleType)TFCParticles.PHOSPHORUS.get(),
               (double)((float)pos.getX() + level.random.nextFloat()),
               (double)pos.getY() + (double)level.random.nextFloat() / 5.0,
               (double)((float)pos.getZ() + level.random.nextFloat()),
               0,
               0.0,
               0.0,
               0.0,
               1.0
            );
         }
      }

      if (nutrient == NutrientType.POTASSIUM) {
         for (int i = 0; i < (int)(amount > 0.0F ? Mth.clamp(amount * 10.0F, 1.0F, 5.0F) : 0.0F); i++) {
            level.sendParticles(
               (SimpleParticleType)TFCParticles.POTASSIUM.get(),
               (double)((float)pos.getX() + level.random.nextFloat()),
               (double)pos.getY() + (double)level.random.nextFloat() / 5.0,
               (double)((float)pos.getZ() + level.random.nextFloat()),
               0,
               0.0,
               0.0,
               0.0,
               1.0
            );
         }
      }
   }

   @Unique
   private void doParticles(ServerLevel level, double x, double y, double z, int count) {
      level.sendParticles(
         (SimpleParticleType)TFCParticles.LEAF.get(),
         x,
         y,
         z,
         count,
         (double)Helpers.triangle(level.random),
         (double)Helpers.triangle(level.random),
         (double)Helpers.triangle(level.random),
         0.3F
      );
   }

   @Unique
   private void addSandLayer(Level level, BlockState state, BlockPos pos, int extraLayer) {
      if ((Integer)state.getValue(LAYERS) + extraLayer >= 8) {
         if ((Integer)state.getValue(LAYERS) + extraLayer > 8) {
            level.setBlock(pos.above(), (BlockState)this.defaultBlockState().setValue(LAYERS, extraLayer), 3);
         } else {
            level.setBlock(pos, (BlockState)this.defaultBlockState().setValue(LAYERS, 8), 3);
         }
      } else {
         level.setBlock(pos, (BlockState)state.setValue(LAYERS, (Integer)state.getValue(LAYERS) + extraLayer), 3);
      }
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      ItemStack stack = player.getItemInHand(hand);
      if (!Helpers.isItem(stack, this.asItem())) {
         return InteractionResult.PASS;
      } else {
         int sandLayers = (Integer)state.getValue(LAYERS);
         if (sandLayers >= 8 && level.getBlockState(pos.above()).isAir()) {
            level.setBlockAndUpdate(pos.above(), this.defaultBlockState());
         } else {
            level.setBlockAndUpdate(pos, (BlockState)state.setValue(LAYERS, Mth.clamp(sandLayers + 1, 1, 8)));
         }

         if (!player.isCreative()) {
            stack.shrink(1);
         }

         Helpers.playSound(level, pos, SoundType.AZALEA_LEAVES.getPlaceSound());
         level.scheduleTick(pos, this, 3);
         return InteractionResult.SUCCESS;
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Level level = context.getLevel();
      BlockPos pos = context.getClickedPos();
      BlockState state = level.getBlockState(pos);
      if (state.getBlock().equals(this.defaultBlockState().getBlock()) && (Integer)state.getValue(LAYERS) >= 1) {
         int i = (Integer)state.getValue(LAYERS);
         return (BlockState)state.setValue(LAYERS, Mth.clamp(i + 1, 1, 8));
      } else {
         BlockState defaultState = (BlockState)this.defaultBlockState().setValue(LAYERS, 1);
         FluidState fluidState = level.getFluidState(pos);
         FluidKey fluidKey = this.getFluidProperty().canContain(fluidState.getType())
            ? this.getFluidProperty().keyFor(fluidState.getType())
            : this.getFluidProperty().keyFor(Fluids.EMPTY);
         return (BlockState)defaultState.setValue(this.getFluidProperty(), fluidKey);
      }
   }

   public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
      this.playerWillDestroy(level, pos, state, player);
      this.removePileOrSand(level, pos, state);
      return true;
   }

   @Unique
   private void removePileOrSand(LevelAccessor level, BlockPos pos, BlockState state) {
      this.removePileOrSand(level, pos, state, -1);
   }

   @Unique
   private void removePileOrSand(LevelAccessor level, BlockPos pos, BlockState state, int expectedLayers) {
      int layers = (Integer)state.getValue(LAYERS);
      if (expectedLayers < layers) {
         if (layers > 1 && expectedLayers != 0) {
            level.setBlock(pos, (BlockState)state.setValue(LAYERS, expectedLayers == -1 ? layers - 1 : expectedLayers), 3);
         } else if (state.is(this)) {
            level.removeBlock(pos, false);
         }
      }
   }
}
