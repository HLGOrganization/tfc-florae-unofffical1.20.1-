package tfcflorae.common.blocks.plant;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.TFCTallGrassBlock;
import net.dries007.tfc.common.blocks.plant.ITallPlant.Part;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class TFCFTallGrassBlock extends TFCTallGrassBlock {
   protected static final EnumProperty<Part> PART = TFCBlockStateProperties.TALL_PLANT_PART;
   protected static final VoxelShape PLANT_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
   protected static final VoxelShape SHORTER_PLANT_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 8.0, 14.0);

   public static TFCFTallGrassBlock create(final RegistryPlant plant, ExtendedProperties properties) {
      return new TFCFTallGrassBlock(properties) {
         public RegistryPlant getPlant() {
            return plant;
         }
      };
   }

   protected TFCFTallGrassBlock(ExtendedProperties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(PART, Part.LOWER));
   }

   public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
      if (entity instanceof LivingEntity livingEntity && !livingEntity.isInvulnerableTo(entity.damageSources().magic())) {
         livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
      }
   }
}
