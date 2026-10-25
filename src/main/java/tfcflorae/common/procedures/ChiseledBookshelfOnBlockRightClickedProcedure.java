package tfcflorae.common.procedures;
import net.minecraft.core.registries.Registries;

import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class ChiseledBookshelfOnBlockRightClickedProcedure {
   public ChiseledBookshelfOnBlockRightClickedProcedure() {
   }

   public ItemStack getItemStack(LevelAccessor world, BlockPos pos, int slotid) {
      AtomicReference<ItemStack> _retval = new AtomicReference<>(ItemStack.EMPTY);
      BlockEntity _ent = world.getBlockEntity(pos);
      if (_ent != null) {
         _ent.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
            .ifPresent(capability -> _retval.set(capability.getStackInSlot(slotid).copy()));
      }

      return _retval.get();
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double repeated = 0.0;
         double books = 0.0;
         ItemStack itemgiven = ItemStack.EMPTY;
         LivingEntity _livEnt = (LivingEntity)entity;
         itemgiven = (entity instanceof LivingEntity ? _livEnt.getMainHandItem() : ItemStack.EMPTY).copy();
         if (!world.isClientSide()) {
            for (int index0 = 0; index0 < 6; index0++) {
               if ((new Object() {
                        public ItemStack getItemStack(LevelAccessor world, BlockPos pos, int slotid) {
                           AtomicReference<ItemStack> _retval = new AtomicReference<>(ItemStack.EMPTY);
                           BlockEntity _ent = world.getBlockEntity(pos);
                           if (_ent != null) {
                              _ent.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
                                 .ifPresent(capability -> _retval.set(capability.getStackInSlot(slotid).copy()));
                           }

                           return _retval.get();
                        }
                     })
                     .getItemStack(world, BlockPos.containing(x, y, z), (int)repeated)
                     .getItem()
                  != ItemStack.EMPTY.getItem()) {
                  books++;
               }

               repeated++;
            }

            if (!itemgiven.getDescriptionId().contains("book") || itemgiven.getDescriptionId().contains("bookshelf")) {
               if (--books >= 0.0) {
                  if (entity instanceof Player _player) {
                     ItemStack _setstack = (new Object() {
                           public ItemStack getItemStack(LevelAccessor world, BlockPos pos, int slotid) {
                              AtomicReference<ItemStack> _retval = new AtomicReference<>(ItemStack.EMPTY);
                              BlockEntity _ent = world.getBlockEntity(pos);
                              if (_ent != null) {
                                 _ent.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
                                    .ifPresent(capability -> _retval.set(capability.getStackInSlot(slotid).copy()));
                              }

                              return _retval.get();
                           }
                        })
                        .getItemStack(world, BlockPos.containing(x, y, z), (int)books);
                     _setstack.setCount(1);
                     ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
                  }

                  BlockEntity _ent = world.getBlockEntity(BlockPos.containing(x, y, z));
                  if (_ent != null) {
                     int _slotid = (int)books;
                     ItemStack _setstack = ItemStack.EMPTY;
                     _setstack.setCount(1);
                     _ent.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(capability -> {
                        if (capability instanceof IItemHandlerModifiable) {
                           ((IItemHandlerModifiable)capability).setStackInSlot(_slotid, _setstack);
                        }
                     });
                  }
               }
            } else if (books < 6.0) {
               BlockEntity _ent = world.getBlockEntity(BlockPos.containing(x, y, z));
               if (_ent != null) {
                  final int _slotid = (int) books;
                  final net.minecraft.world.item.ItemStack _stack = itemgiven.copy();
                  itemgiven.setCount(1);
                  _ent.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(capability -> {
                     if (capability instanceof IItemHandlerModifiable) {
                        ((IItemHandlerModifiable)capability).setStackInSlot(_slotid, _stack);
                     }
                  });
               }

               LivingEntity livingEntity = (LivingEntity)entity;
               (entity instanceof LivingEntity ? livingEntity.getMainHandItem() : ItemStack.EMPTY).shrink(1);
               books++;
               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        null,
                        BlockPos.containing(x, y, z),
                        (SoundEvent)ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("item.book.put")),
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x,
                        y,
                        z,
                        (SoundEvent)ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("item.book.put")),
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F,
                        false
                     );
                  }
               }
            }

            int _value = (int)books;
            BlockPos _pos = BlockPos.containing(x, y, z);
            BlockState _bs = world.getBlockState(_pos);
            if (_bs.getBlock().getStateDefinition().getProperty("books") instanceof IntegerProperty _integerProp && _integerProp.getPossibleValues().contains(_value)) {
               world.setBlock(_pos, (BlockState)_bs.setValue(_integerProp, _value), 3);
            }
         }
      }
   }
}
