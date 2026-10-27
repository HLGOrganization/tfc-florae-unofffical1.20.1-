package tfcflorae.world.layer;

import java.util.function.IntPredicate;
import java.util.function.Predicate;
import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.Area;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.TransformLayer;

public enum ZoomBiomesLayer implements TransformLayer {
   NORMAL_LAND {
      @Override
      public int choose(AreaContext context, int first, int second, int third, int fourth, int center) {
         Predicate<IntPredicate> matcher = p -> p.test(first) || p.test(second) || p.test(third) || p.test(fourth);
         if (matcher.test(i -> !TFCLayers.isOceanOrMarker(i))) {
            if (first != second) {
               if (first == third) {
                  return second != fourth ? first : context.choose(first, second);
               } else if (first == fourth) {
                  return second != third ? first : context.choose(first, second);
               } else if (second == third || second == fourth) {
                  return second;
               } else {
                  return third == fourth ? third : context.choose(first, second, third, fourth);
               }
            } else {
               return first != third && third == fourth ? context.choose(first, third) : first;
            }
         } else {
            return center;
         }
      }
   },
   FUZZY_LAND {
      @Override
      public int choose(AreaContext context, int first, int second, int third, int fourth, int center) {
         Predicate<IntPredicate> matcher = p -> p.test(first) || p.test(second) || p.test(third) || p.test(fourth);
         return matcher.test(i -> !TFCLayers.isOceanOrMarker(i)) ? context.choose(first, second, third, fourth) : center;
      }
   },
   NORMAL_OCEAN {
      @Override
      public int choose(AreaContext context, int first, int second, int third, int fourth, int center) {
         Predicate<IntPredicate> matcher = p -> p.test(first) || p.test(second) || p.test(third) || p.test(fourth);
         if (matcher.test(i -> TFCLayers.isOceanOrMarker(i))) {
            if (first != second) {
               if (first == third) {
                  return second != fourth ? first : context.choose(first, second);
               } else if (first == fourth) {
                  return second != third ? first : context.choose(first, second);
               } else if (second == third || second == fourth) {
                  return second;
               } else {
                  return third == fourth ? third : context.choose(first, second, third, fourth);
               }
            } else {
               return first != third && third == fourth ? context.choose(first, third) : first;
            }
         } else {
            return center;
         }
      }
   },
   FUZZY_OCEAN {
      @Override
      public int choose(AreaContext context, int first, int second, int third, int fourth, int center) {
         Predicate<IntPredicate> matcher = p -> p.test(first) || p.test(second) || p.test(third) || p.test(fourth);
         return matcher.test(i -> TFCLayers.isOceanOrMarker(i)) ? context.choose(first, second, third, fourth) : center;
      }
   };

   private ZoomBiomesLayer() {
   }

   public int apply(AreaContext context, Area area, int x, int z) {
      int parentX = x >> 1;
      int parentZ = z >> 1;
      int offsetX = x & 1;
      int offsetZ = z & 1;
      int northWest = area.get(parentX, parentZ);
      context.setSeed((long)parentX, (long)parentZ);
      if (offsetX == 0 && offsetZ == 0) {
         return northWest;
      } else if (offsetX == 0) {
         return context.choose(northWest, area.get(parentX, parentZ + 1));
      } else {
         return offsetZ == 0
            ? context.choose(northWest, area.get(parentX + 1, parentZ))
            : this.choose(
               context, northWest, area.get(parentX, parentZ + 1), area.get(parentX + 1, parentZ), area.get(parentX + 1, parentZ + 1), area.get(x, z)
            );
      }
   }

   public abstract int choose(AreaContext var1, int var2, int var3, int var4, int var5, int var6);
}
