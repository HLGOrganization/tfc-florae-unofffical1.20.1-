package tfcflorae.common.blocks.spidercave;

import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.IForgeBlockExtension;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.blocks.TFCFBlocks;

public class EggBlock extends Block implements IForgeBlockExtension, IFluidLoggable {
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final FluidProperty FLUID = TFCBlockStateProperties.WATER;
   public static final IntegerProperty AGE = TFCBlockStateProperties.AGE_3;
   private final ExtendedProperties properties;

   public EggBlock(ExtendedProperties properties) {
      super(properties.properties());
      this.properties = properties;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(Fluids.EMPTY)))
               .setValue(FACING, Direction.NORTH))
            .setValue(AGE, 0)
      );
   }

   public ExtendedProperties getExtendedProperties() {
      return this.properties;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, AGE, this.getFluidProperty()});
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (random.nextDouble() < (Double)TFCConfig.SERVER.plantGrowthChance.get()) {
         state = (BlockState)state.setValue(AGE, Math.min((Integer)state.getValue(AGE) + 1, 3));
      }
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockState belowState = level.getBlockState(pos.below());
      return belowState.isFaceSturdy(level, pos, Direction.UP) && !(belowState.getBlock() instanceof EggBlock);
   }

   public void onProjectileHit(Level level, BlockState state, BlockHitResult result, Projectile projectile) {
      level.playSound((Player)null, result.getBlockPos(), SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.7F, 0.9F + level.random.nextFloat() * 0.2F);
      level.destroyBlock(result.getBlockPos(), false);
      this.spawnSpider(level, result.getBlockPos());
   }

   public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float val) {
      if (entity.getType() != EntityType.SPIDER
         && entity.getType() != EntityType.CAVE_SPIDER
         && entity.getType() != EntityType.SILVERFISH
         && entity.getType() != EntityType.ENDERMITE) {
         level.playSound((Player)null, pos, SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.7F, 0.9F + level.random.nextFloat() * 0.2F);
         level.destroyBlock(pos, false);
         this.spawnSpider(level, pos);
      }
   }

   public void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack itemStack) {
      if (level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemStack) == 0) {
         this.spawnSpider(level, pos);
      }
   }

   public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
      if (level instanceof ServerLevel) {
         this.spawnSpider((ServerLevel)level, pos);
      }
   }

   public void spawnSpider(Level level, BlockPos pos) {
      RandomSource random = level.getRandom();
      int chance = random.nextInt(4);
      Block block = level.getBlockState(pos).getBlock();

      for (int amount = 1; amount <= random.nextInt(this.spiderSpawnAmount(block)); amount++) {
         if (chance >= 3 || block == TFCFBlocks.LARGE_SPIDER_EGG.get()) {
            Spider spider = (Spider)EntityType.SPIDER.create(level);
            spider.moveTo((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5, 0.0F, 0.0F);
            level.addFreshEntity(spider);
         } else if (block != TFCFBlocks.LARGE_SPIDER_EGG.get()) {
            CaveSpider spider = (CaveSpider)EntityType.CAVE_SPIDER.create(level);
            spider.moveTo((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5, 0.0F, 0.0F);
            level.addFreshEntity(spider);
         }
      }
   }

   public int spiderSpawnAmount(Block block) {
      if (block == TFCFBlocks.LARGE_SPIDER_EGG.get()) {
         return 1;
      } else if (block == TFCFBlocks.SPIDER_EGGS.get()) {
         return 3;
      } else {
         return block == TFCFBlocks.SPIDER_EGG.get() ? 1 : 1;
      }
   }

   public int getExpDrop(BlockState state, LevelReader world, BlockPos pos, int fortune, int silkTouch) {
      RandomSource random = RandomSource.create();
      return 20 + random.nextInt(20) + random.nextInt(20);
   }

   public BlockState updateShape(BlockState state, Direction pDirection, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
      FluidHelpers.tickFluid(level, currentPos, state);
      return state;
   }

   public FluidProperty getFluidProperty() {
      return FLUID;
   }

   public FluidState getFluidState(BlockState state) {
      return super.getFluidState(state);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   @Nullable
   public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
   }

   @Nullable
   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
   }
}
