package tfcflorae.common.blocks.wood;
import net.minecraft.world.level.storage.loot.LootParams;

import java.util.Collections;
import java.util.List;
import net.dries007.tfc.common.blocks.EntityBlockExtension;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.IForgeBlockExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import tfcflorae.common.blockentities.ChiseledBookshelfBlockEntity;
import tfcflorae.common.procedures.ChiseledBookshelfOnBlockRightClickedProcedure;

public class ChiseledBookshelfBlock extends Block implements IForgeBlockExtension, EntityBlockExtension {
   public static final DirectionProperty FACING = DirectionalBlock.FACING;
   public static final IntegerProperty BOOKS = IntegerProperty.create("books", 0, 6);
   private final ExtendedProperties properties;

   public ChiseledBookshelfBlock(ExtendedProperties properties) {
      super(properties.properties());
      this.properties = properties;
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(FACING, Direction.NORTH));
   }

   public ExtendedProperties getExtendedProperties() {
      return this.properties;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, BOOKS});
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite())).setValue(BOOKS, 0);
   }

   public BlockState rotate(BlockState state, Rotation rot) {
      return (BlockState)state.setValue(FACING, rot.rotate((Direction)state.getValue(FACING)));
   }

   public BlockState mirror(BlockState state, Mirror mirrorIn) {
      return state.rotate(mirrorIn.getRotation((Direction)state.getValue(FACING)));
   }

   public float getEnchantPowerBonus(BlockState state, LevelReader world, BlockPos pos) {
      return (float)((Integer)state.getValue(BOOKS)).intValue() * 0.33F;
   }

   public PushReaction getPistonPushReaction(BlockState state) {
      return PushReaction.BLOCK;
   }

   public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
      List<ItemStack> dropsOriginal = super.getDrops(state, builder);
      return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(this, 1));
   }

   public InteractionResult use(BlockState blockstate, Level world, BlockPos pos, Player entity, InteractionHand hand, BlockHitResult hit) {
      super.use(blockstate, world, pos, entity, hand, hit);
      int x = pos.getX();
      int y = pos.getY();
      int z = pos.getZ();
      double hitX = hit.getLocation().x;
      double hitY = hit.getLocation().y;
      double hitZ = hit.getLocation().z;
      Direction direction = hit.getDirection();
      ChiseledBookshelfOnBlockRightClickedProcedure.execute(world, (double)x, (double)y, (double)z, entity);
      return InteractionResult.SUCCESS;
   }

   public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
      BlockEntity tileEntity = worldIn.getBlockEntity(pos);
      MenuProvider menuProvider = (MenuProvider)tileEntity;
      return tileEntity instanceof MenuProvider ? menuProvider : null;
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new ChiseledBookshelfBlockEntity(pos, state);
   }

   public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int eventID, int eventParam) {
      super.triggerEvent(state, world, pos, eventID, eventParam);
      BlockEntity blockEntity = world.getBlockEntity(pos);
      return blockEntity == null ? false : blockEntity.triggerEvent(eventID, eventParam);
   }

   public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
      if (state.getBlock() != newState.getBlock()) {
         if (world.getBlockEntity(pos) instanceof ChiseledBookshelfBlockEntity be) {
            Containers.dropContents(world, pos, be);
            world.updateNeighbourForOutputSignal(pos, this);
         }

         super.onRemove(state, world, pos, newState, isMoving);
      }
   }

   public boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
      return world.getBlockEntity(pos) instanceof ChiseledBookshelfBlockEntity be ? AbstractContainerMenu.getRedstoneSignalFromContainer(be) : 0;
   }
}
