package tfcflorae.common.blockentities;

import java.util.Arrays;
import java.util.Locale;
import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.blockentities.TFCBlockEntity;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.util.RandomSource;
import tfcflorae.common.blocks.rock.Mineral;
import tfcflorae.common.blocks.rock.MineralSheetBlock;

public class MineralSheetBlockEntity extends TFCBlockEntity {
   public final ItemStack[] stacks = new ItemStack[6];
   public final Mineral[] cachedMinerals = new Mineral[6];
   public static final DirectionProperty FACING = MineralSheetBlock.FACING;
   public static final BooleanProperty MIRROR = MineralSheetBlock.MIRROR;

   public int faceToIndex(Direction face) {
      BlockState state = this.getBlockState();
      if (face.getAxis() == Axis.Y) {
         return face.ordinal();
      } else {
         Mirror mirror = state.getValue(MIRROR) ? Mirror.FRONT_BACK : Mirror.NONE;

         Rotation rot = switch ((Direction)state.getValue(FACING)) {
            case SOUTH -> Rotation.CLOCKWISE_180;
            case EAST -> Rotation.COUNTERCLOCKWISE_90;
            case WEST -> Rotation.CLOCKWISE_90;
            default -> Rotation.NONE;
         };
         return mirror.mirror(rot.rotate(face)).ordinal();
      }
   }

   public MineralSheetBlockEntity(BlockPos pos, BlockState state) {
      super(TFCFBlockEntities.MINERAL_SHEET.get(), pos, state);
      Arrays.fill(this.stacks, ItemStack.EMPTY);
   }

   public BlockEntityType<?> getType() {
      return (BlockEntityType<?>)TFCFBlockEntities.MINERAL_SHEET.get();
   }

   public void addSheet(Direction direction, ItemStack stack) {
      int index = this.faceToIndex(direction);
      this.stacks[index] = stack;
      this.cachedMinerals[index] = null;
      this.markForSync();
   }

   public ItemStack removeSheet(Direction direction) {
      int index = this.faceToIndex(direction);
      ItemStack stack = this.stacks[index];
      this.stacks[index] = ItemStack.EMPTY;
      this.cachedMinerals[index] = null;
      this.markForSync();
      return stack;
   }

   public ItemStack getSheet(Direction direction) {
      return this.stacks[this.faceToIndex(direction)].copy();
   }

   public Mineral getOrCacheMineral(Direction direction) {
      int index = this.faceToIndex(direction);
      ItemStack stack = this.stacks[index];
      Mineral mineral = this.cachedMinerals[index];
      if (mineral == null) {
         mineral = Mineral.getMineral(stack);
         if (mineral == null) {
            mineral = null;
         }

         this.cachedMinerals[index] = mineral;
      }

      return mineral;
   }

   public void setAllMineralsFromOutsideWorld(Mineral mineral) {
      Arrays.fill(this.cachedMinerals, mineral);
   }

   public void saveAdditional(CompoundTag tag) {
      tag.put("stacks", Helpers.writeItemStacksToNbt(this.stacks));
      super.saveAdditional(tag);
   }

   public void loadAdditional(CompoundTag tag) {
      Helpers.readItemStacksFromNbt(this.stacks, tag.getList("stacks", 10));
      Arrays.fill(this.cachedMinerals, null);
      super.loadAdditional(tag);
   }

   public String mineralNameRandom(Mineral mineral, RandomSource random) {
      String suffix = String.valueOf(random.nextInt(4));
      return "block/mineral/" + mineral.name().toLowerCase(Locale.ROOT) + "_" + suffix;
   }

   public String mineralName(Mineral mineral, Direction direction, RandomSource random) {
      String suffix = String.valueOf(random.nextInt(4));
      if (suffix == null) {
         suffix = "0";
      }

      switch (direction) {
         case SOUTH:
            return "block/mineral/" + mineral.name().toLowerCase(Locale.ROOT) + "_" + suffix;
         case EAST:
            return "block/mineral/" + mineral.name().toLowerCase(Locale.ROOT) + "_" + suffix;
         case WEST:
            return "block/mineral/" + mineral.name().toLowerCase(Locale.ROOT) + "_" + suffix;
         case NORTH:
            return "block/mineral/" + mineral.name().toLowerCase(Locale.ROOT) + "_" + suffix;
         case UP:
            return "block/mineral/" + mineral.name().toLowerCase(Locale.ROOT) + "_" + suffix;
         case DOWN:
            return "block/mineral/" + mineral.name().toLowerCase(Locale.ROOT) + "_" + suffix;
         default:
            return "block/mineral/" + mineral.name().toLowerCase(Locale.ROOT) + "_" + suffix;
      }
   }
}
