package tfcflorae.common.blocks.wood;

import java.util.Optional;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.EntityBlockExtension;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.IForgeBlockExtension;
import net.dries007.tfc.common.container.RestrictedChestContainer;
import net.dries007.tfc.common.container.TFCContainerTypes;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner.Combiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.blockentities.TFCFBlockEntities;

public class TFCFChestBlock extends ChestBlock implements IForgeBlockExtension, EntityBlockExtension {
   private final String textureLocation;
   private final ExtendedProperties extendedProperties;
   private static final Combiner<ChestBlockEntity, Optional<MenuProvider>> MENU_PROVIDER_COMBINER = new Combiner<ChestBlockEntity, Optional<MenuProvider>>() {
      public Optional<MenuProvider> acceptDouble(final ChestBlockEntity chest1, final ChestBlockEntity chest2) {
         final Container container = new CompoundContainer(chest1, chest2);
         return Optional.of(new MenuProvider() {
            @Nullable
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
               if (chest1.canOpen(player) && chest2.canOpen(player)) {
                  chest1.unpackLootTable(inventory.player);
                  chest2.unpackLootTable(inventory.player);
                  return new RestrictedChestContainer((MenuType)TFCContainerTypes.CHEST_9x4.get(), id, inventory, container, 4);
               } else {
                  return null;
               }
            }

            public Component getDisplayName() {
               if (chest1.hasCustomName()) {
                  return chest1.getDisplayName();
               } else {
                  return (Component)(chest2.hasCustomName() ? chest2.getDisplayName() : Component.translatable("container.chestDouble"));
               }
            }
         });
      }

      public Optional<MenuProvider> acceptSingle(ChestBlockEntity chest) {
         return Optional.of(chest);
      }

      public Optional<MenuProvider> acceptNone() {
         return Optional.empty();
      }
   };

   public TFCFChestBlock(ExtendedProperties properties, String textureLocation) {
      this(properties, textureLocation, TFCFBlockEntities.CHEST::get);
   }

   public TFCFChestBlock(ExtendedProperties properties, String textureLocation, Supplier<BlockEntityType<? extends ChestBlockEntity>> typeSupplier) {
      super(properties.properties(), typeSupplier);
      this.textureLocation = textureLocation;
      this.extendedProperties = properties;
   }

   public String getTextureLocation() {
      return this.textureLocation;
   }

   public ExtendedProperties getExtendedProperties() {
      return this.extendedProperties;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return super.newBlockEntity(pos, state);
   }

   @Nullable
   public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
      return (MenuProvider)((Optional)this.combine(state, level, pos, false).apply(MENU_PROVIDER_COMBINER)).orElse(null);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> givenType) {
      return super.getTicker(level, state, givenType);
   }
}
