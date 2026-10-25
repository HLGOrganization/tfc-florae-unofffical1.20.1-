package tfcflorae.common.items;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat.Type;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import tfcflorae.common.entities.TFCFBoat;

public class TFCFBoatItem extends BoatItem {
   private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
   private final Supplier<? extends EntityType<TFCFBoat>> boat;

   public TFCFBoatItem(Supplier<? extends EntityType<TFCFBoat>> boat, Properties properties) {
      super(false, net.minecraft.world.entity.vehicle.Boat.Type.OAK, properties);
      this.boat = boat;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack itemstack = player.getItemInHand(hand);
      HitResult hitresult = getPlayerPOVHitResult(level, player, Fluid.ANY);
      if (hitresult.getType() == net.minecraft.world.phys.HitResult.Type.MISS) {
         return InteractionResultHolder.pass(itemstack);
      } else {
         Vec3 vec3 = player.getViewVector(1.0F);
         List<Entity> list = level.getEntities(player, player.getBoundingBox().expandTowards(vec3.scale(5.0)).inflate(1.0), ENTITY_PREDICATE);
         if (!list.isEmpty()) {
            Vec3 vec31 = player.getEyePosition();

            for (Entity entity : list) {
               AABB aabb = entity.getBoundingBox().inflate((double)entity.getPickRadius());
               if (aabb.contains(vec31)) {
                  return InteractionResultHolder.pass(itemstack);
               }
            }
         }

         if (hitresult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            TFCFBoat boat = (TFCFBoat)this.boat.get().create(level);
            if (boat != null) {
               boat.setPos(hitresult.getLocation().x, hitresult.getLocation().y, hitresult.getLocation().z);
               boat.setYRot(player.getYRot());
               if (!level.noCollision(boat, boat.getBoundingBox().inflate(-0.1))) {
                  return InteractionResultHolder.fail(itemstack);
               } else {
                  if (!level.isClientSide) {
                     level.addFreshEntity(boat);
                     level.gameEvent(player, GameEvent.ENTITY_PLACE, BlockPos.containing(hitresult.getLocation()));
                     if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                     }
                  }

                  player.awardStat(Stats.ITEM_USED.get(this));
                  return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
               }
            } else {
               return InteractionResultHolder.fail(itemstack);
            }
         } else {
            return InteractionResultHolder.pass(itemstack);
         }
      }
   }
}
