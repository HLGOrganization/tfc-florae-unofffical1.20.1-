package tfcflorae.common.blockentities;
import net.minecraft.core.registries.Registries;

import net.dries007.tfc.common.blockentities.TFCBlockEntity;
import net.dries007.tfc.common.blocks.GroundcoverBlockType;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class SandPileBlockEntity extends TFCBlockEntity {
   private BlockState internalState = Blocks.AIR.defaultBlockState();
   @Nullable
   private BlockState aboveState = null;

   public SandPileBlockEntity(BlockPos pos, BlockState state) {
      super(TFCFBlockEntities.SAND_PILE.get(), pos, state);
   }

   public void setHiddenStates(BlockState internalState, @Nullable BlockState aboveState, boolean byPlayer) {
      if (Helpers.isBlock(internalState, net.dries007.tfc.common.TFCTags.Blocks.CONVERTS_TO_HUMUS) && !byPlayer) {
         this.internalState = ((Block)((RegistryObject)TFCBlocks.GROUNDCOVER.get(GroundcoverBlockType.HUMUS)).get()).defaultBlockState();
      } else {
         this.internalState = internalState;
      }

      this.aboveState = aboveState;
   }

   public BlockState getInternalState() {
      return this.internalState;
   }

   @Nullable
   public BlockState getAboveState() {
      return this.aboveState;
   }

   protected void loadAdditional(CompoundTag tag) {
      this.internalState = NbtUtils.readBlockState(net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("internalState"));
      this.aboveState = tag.contains("aboveState", 10) ? NbtUtils.readBlockState(net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("aboveState")) : null;
      super.loadAdditional(tag);
   }

   protected void saveAdditional(CompoundTag tag) {
      tag.put("internalState", NbtUtils.writeBlockState(this.internalState));
      if (this.aboveState != null) {
         tag.put("aboveState", NbtUtils.writeBlockState(this.aboveState));
      }

      super.saveAdditional(tag);
   }
}
