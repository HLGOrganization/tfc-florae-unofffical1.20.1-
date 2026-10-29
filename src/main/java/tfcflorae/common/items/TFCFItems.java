package tfcflorae.common.items;
import net.minecraft.core.registries.Registries;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import net.dries007.tfc.common.TFCCreativeTabs;
import net.dries007.tfc.common.blocks.Gem;
import net.dries007.tfc.common.blocks.rock.RockCategory;
import net.dries007.tfc.common.blocks.soil.SoilBlockType.Variant;
import net.dries007.tfc.common.blocks.wood.Wood.BlockType;
import net.dries007.tfc.common.entities.TFCEntities;
import net.dries007.tfc.common.items.JugItem;
import net.dries007.tfc.common.items.MoldItem;
import net.dries007.tfc.common.items.TFCMinecartItem;
import net.dries007.tfc.common.items.VesselItem;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.Metal.ItemType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties.Builder;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.ceramics.Clay;
import tfcflorae.common.blocks.rock.Mineral;
import tfcflorae.common.blocks.rock.TFCFOre;
import tfcflorae.common.blocks.rock.TFCFRock;
import tfcflorae.common.blocks.soil.Colors;
import tfcflorae.common.blocks.soil.TFCFSoil;
import tfcflorae.common.blocks.wood.TFCFWood;
import tfcflorae.common.entities.TFCFBoat;
import tfcflorae.common.entities.TFCFEntities;

public final class TFCFItems {
   public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "tfcflorae");
   public static final Map<Colors, RegistryObject<Item>> WEATHERED_TERRACOTTA = Helpers.mapOfKeys(
      Colors.class, type -> register("sand/weathered_terracotta_shard/" + type.name(), TFCCreativeTabs.EARTH)
   );
   public static final RegistryObject<Item> COKE = register("ore/coke", TFCCreativeTabs.ORES);
   public static final Map<TFCFOre, RegistryObject<Item>> ORES = Helpers.mapOfKeys(
      TFCFOre.class, ore -> !ore.isGraded() && ore != TFCFOre.ANTHRACITE, type -> register("ore/" + type.name(), TFCCreativeTabs.ORES)
   );
   public static final Map<TFCFOre, Map<TFCFOre.Grade, RegistryObject<Item>>> GRADED_ORES = Helpers.mapOfKeys(
      TFCFOre.class,
      TFCFOre::isGraded,
      ore -> Helpers.mapOfKeys(TFCFOre.Grade.class, grade -> register("ore/" + grade.name() + "_" + ore.name(), TFCCreativeTabs.ORES))
   );
   public static final Map<TFCFPowder, RegistryObject<Item>> POWDERS = Helpers.mapOfKeys(
      TFCFPowder.class, powder -> powder != TFCFPowder.ANTHRACITE,
      powder -> register("powder/" + powder.name(), TFCCreativeTabs.MISC)
   );
   public static final RegistryObject<Item> BOG_IRON = register("ore/small_bog_iron", TFCCreativeTabs.ORES);
   public static final RegistryObject<Item> GLOWSTONE_SHARDS = register("crystal/shard/glowstone", TFCCreativeTabs.ORES);
   public static final Map<Gem, RegistryObject<Item>> GEM_SHARDS = Helpers.mapOfKeys(
      Gem.class,
      gem -> gem == net.dries007.tfc.common.blocks.Gem.AMETHYST || gem == net.dries007.tfc.common.blocks.Gem.RUBY,
      gem -> register("crystal/shard/" + gem.name(), TFCCreativeTabs.ORES)
   );
   public static final Map<Mineral, RegistryObject<Item>> MINERALS = Helpers.mapOfKeys(
      Mineral.class, mineral -> register("mineral/" + mineral.name(), TFCCreativeTabs.ORES)
   );
   public static final Map<Clay, RegistryObject<Item>> CLAY_MINERALS = Helpers.mapOfKeys(
      Clay.class, Clay::hasRock, mineral -> register("mineral/" + mineral.name(), TFCCreativeTabs.ORES)
   );
   public static final Map<TFCFRock, RegistryObject<Item>> BRICKS = Helpers.mapOfKeys(
      TFCFRock.class,
      // tuff 的砖由 TFE 提供，TFCF 不再重复注册物品
      type -> type != TFCFRock.TUFF,
      type -> register("brick/" + type.name(), TFCCreativeTabs.ROCKS)
   );
   public static final Map<TFCFFood, RegistryObject<Item>> FOOD = foodMapper();
   public static final Map<TFCFWood, RegistryObject<Item>> LUMBER = lumberWoodMapper();
   public static final Map<TFCFWood, RegistryObject<Item>> SUPPORTS = supportWoodMapper();
   public static final Map<TFCFWood, RegistryObject<Item>> BOATS = boatsWoodMapper();
   public static final Map<TFCFWood, RegistryObject<Item>> CHEST_MINECARTS = minecartChestWoodMapper();
   public static final Map<TFCFWood, RegistryObject<Item>> SIGNS = signWoodMapper();
   public static final RegistryObject<Item> HUMUS_MUD_BRICK = register("mud_brick/humus", TFCCreativeTabs.DECORATIONS);
   public static final Map<Clay, RegistryObject<Item>> CLAY_BALLS = clayBallMapper();
   public static final Map<Clay, RegistryObject<Item>> CLAY_ITEMS = clayItemMapper();
   public static final Map<Clay, Map<DyeColor, RegistryObject<Item>>> CLAY_VESSELS = clayVesselMapper();
   public static final Map<Clay, Map<ItemType, RegistryObject<Item>>> CLAY_MOLDS = clayMoldMapper();
   public static final Map<Variant, RegistryObject<Item>> SOIL_PILE_TFC = Helpers.mapOfKeys(
      Variant.class, variant -> register("soil_pile/" + variant.name(), TFCCreativeTabs.EARTH)
   );
   public static final Map<TFCFSoil.TFCFVariant, RegistryObject<Item>> SOIL_PILE_TFCF = Helpers.mapOfKeys(
      TFCFSoil.TFCFVariant.class, variant -> register("soil_pile/" + variant.name(), TFCCreativeTabs.EARTH)
   );
   public static final RegistryObject<Item> BRUSHES = register(
      "tools/brush", (Supplier<Item>)(() -> new BrushItem(new Properties().stacksTo(1).durability(250)))
   );
   public static final RegistryObject<Item> WALKING_CANES = register(
      "tools/walking_cane", (Supplier<Item>)(() -> new WalkingCaneItem(new Properties().stacksTo(1).durability(200)))
   );
   public static final RegistryObject<Item> MULBERRY_LEAVES = register("wood/leaf/mulberry", TFCCreativeTabs.MISC);
   public static final RegistryObject<Item> SILK_MOTH_EGG = register("animal/product/silk_moth_egg", TFCCreativeTabs.MISC);
   public static final RegistryObject<Item> SILK_WORM_HATCHERY = register("animal/product/silk_moth_hatchery", TFCCreativeTabs.MISC);
   public static final RegistryObject<Item> SILK_WORM = register("animal/product/silk_worm", TFCCreativeTabs.MISC);
   public static final RegistryObject<Item> SILK_WORM_COCOON = register("animal/product/silk_worm_cocoon", TFCCreativeTabs.MISC);
   public static final RegistryObject<Item> SILK_WORM_COCOON_BOILED = register("animal/product/silk_worm_cocoon_boiled", TFCCreativeTabs.MISC);
   // 蝌蚪桶随蝌蚪一起移除

   public static final RegistryObject<Item> SILKMOTH_EGG = registerSpawnEgg(TFCFEntities.SILKMOTH, 14126807, 14286841);
   // 青蛙 / 蝌蚪 / 鹦鹉刷怪蛋已随实体移除



   public TFCFItems() {
   }

   private static Map<Clay, RegistryObject<Item>> clayBallMapper() {
      Map<Clay, RegistryObject<Item>> Map = new HashMap<>();

      for (Clay clay : Clay.values()) {
         if (!clay.hasRock()) {
            String clayName = clay.getSerializedName().toLowerCase(Locale.ROOT);
            Map.put(clay, register("ceramic/" + clayName + "/clay", TFCCreativeTabs.MISC));
         }
      }

      return Map;
   }

   private static Map<Clay, RegistryObject<Item>> clayItemMapper() {
      Map<Clay, RegistryObject<Item>> Map = new HashMap<>();

      for (Clay clay : Clay.values()) {
         if (!clay.hasRock()) {
            String clayName = clay.getSerializedName().toLowerCase(Locale.ROOT);
            Map.put(clay, register("ceramic/" + clayName + "/unfired_brick", TFCCreativeTabs.MISC));
            Map.put(clay, register("ceramic/" + clayName + "/fired_brick", TFCCreativeTabs.MISC));
            Map.put(clay, register("ceramic/" + clayName + "/unfired/flower_pot", TFCCreativeTabs.MISC));
            Map.put(clay, register("ceramic/" + clayName + "/unfired/pan", TFCCreativeTabs.MISC));
            Map.put(clay, register("ceramic/" + clayName + "/unfired/bowl", TFCCreativeTabs.MISC));
            Map.put(clay, register("ceramic/" + clayName + "/fired/bowl", TFCCreativeTabs.MISC));
            Map.put(clay, register("ceramic/" + clayName + "/unfired/jug", TFCCreativeTabs.MISC));
            Map.put(
               clay,
               register(
                  "ceramic/" + clayName + "/fired/jug",
                  (Supplier<Item>)(() -> new JugItem(
                        new Properties().stacksTo(1),
                        TFCConfig.SERVER.jugCapacity::get,
                        net.dries007.tfc.common.TFCTags.Fluids.USABLE_IN_JUG
                     ))
               )
            );
            Map.put(clay, register("ceramic/" + clayName + "/unfired/pot", TFCCreativeTabs.MISC));
            Map.put(clay, register("ceramic/" + clayName + "/fired/pot", TFCCreativeTabs.MISC));
            Map.put(clay, register("ceramic/" + clayName + "/unfired/spindle_head", TFCCreativeTabs.MISC));
            Map.put(clay, register("ceramic/" + clayName + "/fired/spindle_head", TFCCreativeTabs.MISC));
            Map.put(clay, register("ceramic/" + clayName + "/unfired/vessel", TFCCreativeTabs.MISC));
            Map.put(
               clay,
               register(
                  "ceramic/" + clayName + "/fired/vessel", (Supplier<Item>)(() -> new VesselItem(new Properties().stacksTo(1)))
               )
            );
            Map.put(clay, register("ceramic/" + clayName + "/unfired/large_vessel", TFCCreativeTabs.MISC));
         }
      }

      return Map;
   }

   private static Map<Clay, Map<DyeColor, RegistryObject<Item>>> clayVesselMapper() {
      Map<Clay, Map<DyeColor, RegistryObject<Item>>> Map = new HashMap<>();

      for (Clay clay : Clay.values()) {
         if (!clay.hasRock()) {
            Map<DyeColor, RegistryObject<Item>> subMap = new HashMap<>();

            for (DyeColor color : DyeColor.values()) {
               String clayName = clay.getSerializedName().toLowerCase(Locale.ROOT);
               String colorName = color.name().toLowerCase(Locale.ROOT);
               subMap.put(color, register("ceramic/" + clayName + "/unfired/glazed_vessel/" + colorName, TFCCreativeTabs.MISC));
               subMap.put(
                  color,
                  register(
                     "ceramic/" + clayName + "/fired/glazed_vessel/" + colorName,
                     (Supplier<Item>)(() -> new VesselItem(new Properties().stacksTo(1)))
                  )
               );
            }

            Map.put(clay, subMap);
         }
      }

      return Map;
   }

   private static Map<Clay, Map<ItemType, RegistryObject<Item>>> clayMoldMapper() {
      Map<Clay, Map<ItemType, RegistryObject<Item>>> Map = new HashMap<>();

      for (Clay clay : Clay.values()) {
         if (!clay.hasRock()) {
            Map<ItemType, RegistryObject<Item>> subMap = new HashMap<>();

            for (ItemType type : ItemType.values()) {
               if (type.hasMold()) {
                  String clayName = clay.getSerializedName().toLowerCase(Locale.ROOT);
                  String typeName = type.name().toLowerCase(Locale.ROOT);
                  subMap.put(type, register("ceramic/" + clayName + "/unfired/mold/" + typeName, TFCCreativeTabs.MISC));
                  subMap.put(
                     type,
                     register(
                        "ceramic/" + clayName + "/fired/mold/" + typeName,
                        (Supplier<Item>)(() -> new MoldItem(type, new Properties().stacksTo(1)))
                     )
                  );
               }
            }

            Map.put(clay, subMap);
         }
      }

      return Map;
   }

   private static Map<TFCFWood, RegistryObject<Item>> lumberWoodMapper() {
      Map<TFCFWood, RegistryObject<Item>> Map = new HashMap<>();

      for (TFCFWood wood : TFCFWood.class.getEnumConstants()) {
         if (TFCFBlocks.WOODS.get(wood) != null) {
            Map.put(wood, register("wood/lumber/" + wood.name(), TFCCreativeTabs.WOOD));
         }
      }

      return Map;
   }

   private static Map<TFCFWood, RegistryObject<Item>> supportWoodMapper() {
      Map<TFCFWood, RegistryObject<Item>> Map = new HashMap<>();

      for (TFCFWood wood : TFCFWood.class.getEnumConstants()) {
         if (TFCFBlocks.WOODS.get(wood) != null) {
            Map.put(
               wood,
               register(
                  "wood/support/" + wood.name(),
                  (Supplier<Item>)(() -> new StandingAndWallBlockItem(
                        (Block)TFCFBlocks.WOODS.get(wood).get(BlockType.VERTICAL_SUPPORT).get(),
                        (Block)TFCFBlocks.WOODS.get(wood).get(BlockType.HORIZONTAL_SUPPORT).get(),
                        new Properties(),
                        net.minecraft.core.Direction.DOWN
                     ))
               )
            );
         }
      }

      return Map;
   }

   private static Map<TFCFWood, RegistryObject<Item>> boatsWoodMapper() {
      Map<TFCFWood, RegistryObject<Item>> Map = new HashMap<>();

      for (TFCFWood wood : TFCFWood.class.getEnumConstants()) {
         if (TFCFBlocks.WOODS.get(wood) != null) {
            if (wood == TFCFWood.BAMBOO) {
               Map.put(
                  wood,
                  register(
                     "wood/boat/" + wood.name(),
                     (Supplier<Item>)(() -> new TFCFBoatItem(
                           (Supplier<? extends EntityType<TFCFBoat>>)TFCFEntities.BOATS.get(wood), new Properties()
                        ))
                  )
               );
            } else {
               Map.put(
                  wood,
                  register(
                     "wood/boat/" + wood.name(),
                     (Supplier<Item>)(() -> new TFCFBoatItem(
                           (Supplier<? extends EntityType<TFCFBoat>>)TFCFEntities.BOATS.get(wood), new Properties()
                        ))
                  )
               );
            }
         }
      }

      return Map;
   }

   private static Map<TFCFWood, RegistryObject<Item>> minecartChestWoodMapper() {
      Map<TFCFWood, RegistryObject<Item>> Map = new HashMap<>();

      for (TFCFWood wood : TFCFWood.class.getEnumConstants()) {
         if (TFCFBlocks.WOODS.get(wood) != null) {
            Map.put(
               wood,
               register(
                  "wood/chest_minecart/" + wood.name(),
                  (Supplier<Item>)(() -> new TFCMinecartItem(
                        new Properties(),
                        TFCEntities.CHEST_MINECART,
                        () -> ((Block)TFCFBlocks.WOODS.get(wood).get(BlockType.CHEST).get()).asItem()
                     ))
               )
            );
         }
      }

      return Map;
   }

   private static Map<TFCFWood, RegistryObject<Item>> signWoodMapper() {
      Map<TFCFWood, RegistryObject<Item>> Map = new HashMap<>();

      for (TFCFWood wood : TFCFWood.class.getEnumConstants()) {
         if (TFCFBlocks.WOODS.get(wood) != null) {
            Map.put(
               wood,
               register(
                  "wood/sign/" + wood.name(),
                  (Supplier<Item>)(() -> new SignItem(
                        new Properties(),
                        (Block)TFCFBlocks.WOODS.get(wood).get(BlockType.SIGN).get(),
                        (Block)TFCFBlocks.WOODS.get(wood).get(BlockType.WALL_SIGN).get()
                     ))
               )
            );
         }
      }

      return Map;
   }

   private static Map<TFCFFood, RegistryObject<Item>> foodMapper() {
      Map<TFCFFood, RegistryObject<Item>> Map = new HashMap<>();

      for (TFCFFood food : TFCFFood.class.getEnumConstants()) {
         if (food.hasEffect()) {
            if (food == TFCFFood.DRAGONBERRY) {
               Map.put(
                  food,
                  register(
                     "food/" + food.name(),
                     (Supplier<Item>)(() -> new Item(
                           new Properties()
                              .food(
                                 new Builder()
                                    .nutrition(4)
                                    .saturationMod(0.3F)
                                    .effect(new MobEffectInstance(MobEffects.POISON, 50, 0), 0.3F)
                                    .alwaysEat()
                                    .build()
                              )
                              
                        ))
                  )
               );
            } else if (food == TFCFFood.ROASTED_SULPHUR_SHELF) {
               Map.put(
                  food,
                  register(
                     "food/" + food.name(),
                     (Supplier<Item>)(() -> new Item(
                           new Properties()
                              .food(
                                 new Builder()
                                    .nutrition(4)
                                    .saturationMod(0.3F)
                                    .effect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 0.5F)
                                    .effect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 300, 0), 0.25F)
                                    .alwaysEat()
                                    .build()
                              )
                              
                        ))
                  )
               );
            } else if (food == TFCFFood.ROASTED_TURKEY_TAIL) {
               Map.put(
                  food,
                  register(
                     "food/" + food.name(),
                     (Supplier<Item>)(() -> new Item(
                           new Properties()
                              .food(
                                 new Builder()
                                    .nutrition(4)
                                    .saturationMod(0.3F)
                                    .effect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 0.5F)
                                    .effect(new MobEffectInstance(MobEffects.BLINDNESS, 300, 0), 0.25F)
                                    .alwaysEat()
                                    .build()
                              )
                              
                        ))
                  )
               );
            } else if (food == TFCFFood.ROASTED_AMANITA) {
               Map.put(
                  food,
                  register(
                     "food/" + food.name(),
                     (Supplier<Item>)(() -> new Item(
                           new Properties()
                              .food(
                                 new Builder()
                                    .nutrition(4)
                                    .saturationMod(0.3F)
                                    .effect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 0.5F)
                                    .effect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 0), 0.5F)
                                    .effect(new MobEffectInstance(MobEffects.WITHER, 150, 1), 1.0F)
                                    .alwaysEat()
                                    .build()
                              )
                              
                        ))
                  )
               );
            } else if (food == TFCFFood.ROASTED_DEATH_CAP) {
               Map.put(
                  food,
                  register(
                     "food/" + food.name(),
                     (Supplier<Item>)(() -> new Item(
                           new Properties()
                              .food(
                                 new Builder()
                                    .nutrition(4)
                                    .saturationMod(0.3F)
                                    .effect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 0.5F)
                                    .effect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 0), 0.5F)
                                    .effect(new MobEffectInstance(MobEffects.WITHER, 150, 1), 1.0F)
                                    .alwaysEat()
                                    .build()
                              )
                              
                        ))
                  )
               );
            } else if (food == TFCFFood.ROASTED_WOOLLY_GOMPHUS) {
               Map.put(
                  food,
                  register(
                     "food/" + food.name(),
                     (Supplier<Item>)(() -> new Item(
                           new Properties()
                              .food(
                                 new Builder()
                                    .nutrition(4)
                                    .saturationMod(0.3F)
                                    .effect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 0.5F)
                                    .effect(new MobEffectInstance(MobEffects.POISON, 300, 0), 0.25F)
                                    .alwaysEat()
                                    .build()
                              )
                              
                        ))
                  )
               );
            } else if (food == TFCFFood.ROASTED_CHANTERELLE) {
               Map.put(
                  food,
                  register(
                     "food/" + food.name(),
                     (Supplier<Item>)(() -> new Item(
                           new Properties()
                              .food(
                                 new Builder()
                                    .nutrition(4)
                                    .saturationMod(0.3F)
                                    .effect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0), 0.5F)
                                    .alwaysEat()
                                    .build()
                              )
                              
                        ))
                  )
               );
            } else if (food == TFCFFood.ROASTED_STINKHORN) {
               Map.put(
                  food,
                  register(
                     "food/" + food.name(),
                     (Supplier<Item>)(() -> new Item(
                           new Properties()
                              .food(
                                 new Builder()
                                    .nutrition(4)
                                    .saturationMod(0.3F)
                                    .effect(new MobEffectInstance(MobEffects.UNLUCK, 200, 0), 0.5F)
                                    .alwaysEat()
                                    .build()
                              )
                              
                        ))
                  )
               );
            } else if (food == TFCFFood.ROASTED_INDIGO_MILK_CAP) {
               Map.put(
                  food,
                  register(
                     "food/" + food.name(),
                     (Supplier<Item>)(() -> new Item(
                           new Properties()
                              .food(
                                 new Builder()
                                    .nutrition(4)
                                    .saturationMod(0.3F)
                                    .effect(new MobEffectInstance(MobEffects.WATER_BREATHING, 50, 0), 0.3F)
                                    .alwaysEat()
                                    .build()
                              )
                              
                        ))
                  )
               );
            } else if (food == TFCFFood.ROASTED_LIONS_MANE) {
               Map.put(
                  food,
                  register(
                     "food/" + food.name(),
                     (Supplier<Item>)(() -> new Item(
                           new Properties()
                              .food(
                                 new Builder()
                                    .nutrition(4)
                                    .saturationMod(0.3F)
                                    .effect(new MobEffectInstance(MobEffects.REGENERATION, 75, 0), 0.3F)
                                    .effect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0), 0.4F)
                                    .alwaysEat()
                                    .build()
                              )
                              
                        ))
                  )
               );
            } else if (food == TFCFFood.ROASTED_BLACK_POWDERPUFF) {
               Map.put(
                  food,
                  register(
                     "food/" + food.name(),
                     (Supplier<Item>)(() -> new Item(
                           new Properties()
                              .food(
                                 new Builder()
                                    .nutrition(4)
                                    .saturationMod(0.3F)
                                    .effect(new MobEffectInstance(MobEffects.INVISIBILITY, 25, 0), 0.5F)
                                    .alwaysEat()
                                    .build()
                              )
                              
                        ))
                  )
               );
            } else if (food == TFCFFood.EUCALYPTUS_LEAVES) {
               Map.put(
                  food,
                  register(
                     "food/" + food.name(),
                     (Supplier<Item>)(() -> new Item(
                           new Properties()
                              .food(
                                 new Builder()
                                    .nutrition(0)
                                    .saturationMod(0.0F)
                                    .effect(new MobEffectInstance(MobEffects.POISON, 35, 0), 0.2F)
                                    .alwaysEat()
                                    .build()
                              )
                              
                        ))
                  )
               );
            } else {
               Map.put(food, register("food/" + food.name(), (Supplier<Item>)(() -> new Item(food.createProperties()))));
            }
         } else {
            Map.put(food, register("food/" + food.name(), (Supplier<Item>)(() -> new Item(food.createProperties()))));
         }
      }

      return Map;
   }

   private static Properties prop() {
      return new Properties();
   }

   private static <T extends EntityType<? extends Mob>> RegistryObject<Item> registerSpawnEgg(RegistryObject<T> entity, int color1, int color2) {
      return register(
         "spawn_egg/" + entity.getId().getPath(),
         (Supplier<Item>)(() -> new ForgeSpawnEggItem(entity, color1, color2, new Properties()))
      );
   }

   private static RegistryObject<Item> register(String name, TFCCreativeTabs.CreativeTabHolder group) {
      return register(name, (Supplier<Item>)(() -> new Item(new Properties())));
   }

   private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> item) {
      return ITEMS.register(name.toLowerCase(Locale.ROOT), item);
   }
}
