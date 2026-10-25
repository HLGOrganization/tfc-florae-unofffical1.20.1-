package tfcflorae.common.blocks.devices;

import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.devices.DryingBricksBlock;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.blockentities.TFCFBlockEntities;
import tfcflorae.common.blockentities.TFCFTickCounterBlockEntity;

public class TFCFDryingBricksBlock extends DryingBricksBlock {
   public static final IntegerProperty COUNT = TFCBlockStateProperties.COUNT_1_4;
   public static final BooleanProperty DRIED = TFCBlockStateProperties.DRIED;
   public static final VoxelShape SHAPE = box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
   private final Supplier<? extends Item> dryItem;

   public TFCFDryingBricksBlock(ExtendedProperties properties, Supplier<? extends Item> dryItem) {
      super(properties, dryItem);
      this.dryItem = dryItem;
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.getStateDefinition().any()).setValue(COUNT, 1)).setValue(DRIED, false));
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      ItemStack held = player.getItemInHand(hand);
      if (Helpers.isItem(held, this.asItem()) && !player.isShiftKeyDown() && !(Boolean)state.getValue(DRIED)) {
         int count = (Integer)state.getValue(COUNT);
         if (count < 4) {
            level.setBlockAndUpdate(pos, (BlockState)state.setValue(COUNT, count + 1));
            SoundType soundType = this.getSoundType(state, level, pos, player);
            level.playSound(null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
            TFCFTickCounterBlockEntity.reset(level, pos);
            if (!player.isCreative()) {
               held.shrink(1);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
         }
      } else if (held.isEmpty() && player.isShiftKeyDown()) {
         int count = (Integer)state.getValue(COUNT);
         ItemStack drop = new ItemStack((ItemLike)(state.getValue(DRIED) ? (ItemLike)this.dryItem.get() : this.asItem()));
         ItemHandlerHelper.giveItemToPlayer(player, drop);
         if (count > 1) {
            level.setBlockAndUpdate(pos, (BlockState)state.setValue(COUNT, count - 1));
         } else {
            level.destroyBlock(pos, false);
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      }

      return InteractionResult.PASS;
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      TFCFTickCounterBlockEntity.reset(level, pos);
      super.setPlacedBy(level, pos, state, placer, stack);
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
      level.getBlockEntity(pos, TFCFBlockEntities.TICK_COUNTER.get()).ifPresent(counter -> {
         if (level.isRainingAt(pos.above())) {
            counter.resetCounter();
         } else {
            int ticks = (Integer)TFCConfig.SERVER.mudBricksTicks.get();
            if (ticks > -1 && counter.getTicksSinceUpdate() > (long)ticks) {
               level.setBlockAndUpdate(pos, (BlockState)state.setValue(DRIED, true));
            }
         }
      });
   }
}
