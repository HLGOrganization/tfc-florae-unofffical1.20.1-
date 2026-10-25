package tfcflorae.common.blocks.devices;

import net.dries007.tfc.client.particle.TFCParticles;
import net.dries007.tfc.common.TFCTags.Items;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.AnvilBlock;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.Metal.Tier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.IItemHandler;
import tfcflorae.common.blockentities.TFCFAnvilBlockEntity;
import tfcflorae.common.blockentities.TFCFBlockEntities;

public class TFCFAnvilBlock extends AnvilBlock {
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   private static final VoxelShape SHAPE_X = box(0.0, 0.0, 3.0, 16.0, 11.0, 13.0);
   private static final VoxelShape SHAPE_Z = box(3.0, 0.0, 0.0, 13.0, 11.0, 16.0);
   private final int tier;

   public static InteractionResult interactWithAnvil(Level level, BlockPos pos, Player player, InteractionHand hand) {
      TFCFAnvilBlockEntity anvil = (TFCFAnvilBlockEntity)level.getBlockEntity(pos, TFCFBlockEntities.ANVIL.get()).orElse(null);
      if (anvil == null) {
         return InteractionResult.PASS;
      } else {
         IItemHandler inventory = (IItemHandler)anvil.getCapability(Capabilities.ITEM).resolve().orElse(null);
         if (inventory == null) {
            return InteractionResult.PASS;
         } else if (!player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer) {
               Helpers.openScreen(serverPlayer, anvil.anvilProvider(), pos);
            }

            return InteractionResult.SUCCESS;
         } else {
            ItemStack playerStack = player.getItemInHand(hand);
            if (playerStack.isEmpty()) {
               for (int slot : TFCFAnvilBlockEntity.SLOTS_BY_HAND_EXTRACT) {
                  ItemStack anvilStack = inventory.getStackInSlot(slot);
                  if (!anvilStack.isEmpty()) {
                     ItemStack result = inventory.extractItem(slot, 1, false);
                     player.setItemInHand(hand, result);
                     return InteractionResult.SUCCESS;
                  }
               }
            } else if (Helpers.isItem(playerStack, Items.HAMMERS)) {
               InteractionResult weldResult = anvil.weld(player);
               if (weldResult == InteractionResult.SUCCESS) {
                  if (level instanceof ServerLevel server) {
                     double x = (double)pos.getX() + Mth.nextDouble(level.random, 0.2, 0.8);
                     double z = (double)pos.getX() + Mth.nextDouble(level.random, 0.2, 0.8);
                     double y = (double)pos.getY() + Mth.nextDouble(level.random, 0.8, 1.0);
                     server.sendParticles(
                        (SimpleParticleType)TFCParticles.SPARK.get(),
                        x,
                        y,
                        z,
                        5,
                        (double)Helpers.uniform(level.random, -5.0F, 5.0F),
                        1.5 + (double)level.random.nextFloat(),
                        (double)Helpers.uniform(level.random, -5.0F, 5.0F),
                        5.0
                     );
                  }

                  level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.6F, 1.0F);
                  return InteractionResult.SUCCESS;
               }

               if (weldResult == InteractionResult.FAIL) {
                  return InteractionResult.SUCCESS;
               }
            } else {
               ItemStack insertStack = playerStack.copy();

               for (int slotx : TFCFAnvilBlockEntity.SLOTS_BY_HAND_INSERT) {
                  ItemStack resultStack = inventory.insertItem(slotx, insertStack, false);
                  if (insertStack.getCount() > resultStack.getCount()) {
                     player.setItemInHand(hand, resultStack);
                     return InteractionResult.SUCCESS;
                  }
               }
            }

            return InteractionResult.PASS;
         }
      }
   }

   public TFCFAnvilBlock(ExtendedProperties properties, Tier tier) {
      this(properties, tier.ordinal());
   }

   public TFCFAnvilBlock(ExtendedProperties properties, int tier) {
      super(properties, tier);
      this.tier = tier;
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      return interactWithAnvil(level, pos, player, hand);
   }
}
