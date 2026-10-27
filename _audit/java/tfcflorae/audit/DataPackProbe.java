package tfcflorae.audit;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.ForgeRegistries;

/** Temporary harness: never part of the normal source set or distributed jar. */
@Mod.EventBusSubscriber(modid = "tfcflorae", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DataPackProbe {
    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        if (!Boolean.getBoolean("tfcflorae.audit")) return;
        event.enqueueWork(DataPackProbe::run);
    }

    private static void save(Path output, Object value) throws Exception {
        Files.createDirectories(output.getParent());
        Files.writeString(output, new GsonBuilder().setPrettyPrinting().create().toJson(value) + "\n", StandardCharsets.UTF_8);
    }

    private static <T extends Comparable<T>> List<String> propertyValues(Property<T> property) {
        return property.getPossibleValues().stream().map(property::getName).sorted().toList();
    }

    private static void probeDataParsers(Path root, Map<String, Object> result) throws Exception {
        Map<String, String> managerClasses = Map.ofEntries(
            Map.entry("food_items", "common.capabilities.food.FoodCapability"),
            Map.entry("fuels", "util.Fuel"),
            Map.entry("item_heats", "common.capabilities.heat.HeatCapability"),
            Map.entry("item_sizes", "common.capabilities.size.ItemSizeManager"),
            Map.entry("knapping_types", "util.KnappingType"),
            Map.entry("panning", "util.Pannable"),
            Map.entry("sluicing", "util.Sluiceable"),
            Map.entry("supports", "util.Support"),
            Map.entry("climate_ranges", "util.climate.ClimateRange")
        );
        Map<String, Integer> counts = new TreeMap<>();
        List<Map<String, String>> failures = new ArrayList<>();
        List<Map<String, String>> skipped = new ArrayList<>();
        boolean firmalifeLoaded = ForgeRegistries.ITEMS.getEntries().stream()
            .anyMatch(entry -> entry.getKey().location().getNamespace().equals("firmalife"));
        Path dataRoot = root.resolve("src/main/resources/data");
        try (var namespaces = Files.list(dataRoot)) {
            for (Path namespace : namespaces.filter(Files::isDirectory).toList()) {
                Path recipes = namespace.resolve("recipes");
                if (Files.isDirectory(recipes)) {
                    try (var paths = Files.walk(recipes)) {
                        for (Path path : paths.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                            String relative = recipes.relativize(path).toString().replace('\\', '/');
                            ResourceLocation id = new ResourceLocation(namespace.getFileName().toString(), relative.substring(0, relative.length() - 5));
                            JsonObject json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                            String type = json.has("type") ? json.get("type").getAsString() : "";
                            if (!firmalifeLoaded && json.toString().contains("firmalife:")) {
                                skipped.add(Map.of("path", root.relativize(path).toString(), "reason", "Optional Firmalife data requires the mod's items"));
                                continue;
                            }
                            if (type.startsWith("firmalife:") && !BuiltInRegistries.RECIPE_SERIALIZER.containsKey(new ResourceLocation(type))) {
                                skipped.add(Map.of("path", root.relativize(path).toString(), "reason", "External mod serializer not installed: " + type));
                                continue;
                            }
                            try {
                                RecipeManager.fromJson(id, json);
                                counts.merge("recipes", 1, Integer::sum);
                            } catch (Throwable error) {
                                failures.add(Map.of("path", root.relativize(path).toString(), "error", error.toString()));
                            }
                        }
                    }
                }
                for (var entry : managerClasses.entrySet()) {
                    Path folder = namespace.resolve("tfc").resolve(entry.getKey());
                    if (!Files.isDirectory(folder)) continue;
                    Object manager = Class.forName("net.dries007.tfc." + entry.getValue()).getField("MANAGER").get(null);
                    try (var paths = Files.walk(folder)) {
                        for (Path path : paths.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                            String relative = folder.relativize(path).toString().replace('\\', '/');
                            ResourceLocation id = new ResourceLocation(namespace.getFileName().toString(), relative.substring(0, relative.length() - 5));
                            JsonObject json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                            try {
                                if (manager instanceof net.dries007.tfc.util.RegisteredDataManager<?> registered) {
                                    var field = net.dries007.tfc.util.RegisteredDataManager.class.getDeclaredField("factory");
                                    field.setAccessible(true);
                                    @SuppressWarnings("unchecked")
                                    var factory = (java.util.function.BiFunction<ResourceLocation, JsonObject, ?>) field.get(registered);
                                    factory.apply(id, json);
                                } else {
                                    ((net.dries007.tfc.util.DataManager<?>) manager).read(id, json);
                                }
                                counts.merge(entry.getKey(), 1, Integer::sum);
                            } catch (Throwable error) {
                                failures.add(Map.of("path", root.relativize(path).toString(), "error", error.toString()));
                            }
                        }
                    }
                }
            }
        }
        result.put("data_parser_counts", counts);
        result.put("data_parser_failures", failures);
        result.put("data_parser_skipped", skipped);
        result.put("data_parsers_passed", failures.isEmpty());
        save(root.resolve("reports/runtime-data-parsers.json"), Map.of("counts", counts, "failures", failures, "skipped", skipped));
    }

    public static void run() {
        final Path root = Path.of(System.getProperty("tfcflorae.audit.root"));
        final boolean baseline = Boolean.getBoolean("tfcflorae.audit.baseline");
        final Map<String, Object> result = new LinkedHashMap<>();
        result.put("baseline", baseline);
        result.put("runtime_tfc_version", System.getProperty("tfcflorae.audit.tfcVersion", "unknown"));
        result.put("mod_load_error", System.getProperty("tfcflorae.audit.mod_load_error", ""));
        try {
            Map<String, List<String>> registries = new TreeMap<>();
            BuiltInRegistries.REGISTRY.entrySet().forEach(entry -> {
                registries.put(entry.getKey().location().toString(), entry.getValue().keySet().stream().map(Object::toString).sorted().toList());
            });
            Map<String, Object> blockStates = new TreeMap<>();
            ForgeRegistries.BLOCKS.getEntries().forEach(entry -> {
                Map<String, List<String>> properties = new TreeMap<>();
                entry.getValue().getStateDefinition().getProperties().forEach(property -> {
                    properties.put(property.getName(), propertyValues(property));
                });
                blockStates.put(entry.getKey().location().toString(), properties);
            });
            save(root.resolve("reports/runtime-registries.json"), Map.of(
                "runtime_tfc_version", System.getProperty("tfcflorae.audit.tfcVersion", "unknown"),
                "registries", registries,
                "block_states", blockStates
            ));
            System.out.println("[FLORAE-AUDIT] Runtime registry dump saved");
            probeDataParsers(root, result);

            List<PackResources> packs = new ArrayList<>();
            packs.add(new FilePackResources("audit-vanilla", root.resolve("mc-cache/client-1.20.1.jar").toFile(), false));
            Path tfc = Path.of(System.getProperty("tfcflorae.audit.tfcJar"));
            packs.add(new FilePackResources("audit-tfc", tfc.toFile(), false));
            packs.add(new PathPackResources("audit-florae-source", root.resolve("src/main/resources"), false));
            if (baseline) packs.add(new PathPackResources("audit-baseline-veins", root.resolve("_audit/baseline"), false));
            try (var manager = new MultiPackResourceManager(PackType.SERVER_DATA, packs)) {
                var staticRegistries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
                var loaded = RegistryDataLoader.load(manager, staticRegistries, RegistryDataLoader.WORLDGEN_REGISTRIES);
                Map<String, Integer> sizes = new TreeMap<>();
                loaded.registries().forEach(entry -> sizes.put(entry.key().location().toString(), entry.value().size()));
                result.put("worldgen_registry_sizes", sizes);
                result.put("worldgen_passed", true);
            }
            save(root.resolve(baseline ? "reports/runtime-probe-baseline.json" : "reports/runtime-probe-fixed.json"), result);
            if (!baseline) save(root.resolve("reports/runtime-probe-tfc-" + result.get("runtime_tfc_version") + ".json"), result);
            System.out.println("[FLORAE-AUDIT] Registry codecs passed; exiting before EULA/world/network setup");
            System.exit(0);
        } catch (Throwable error) {
            result.put("worldgen_passed", false);
            StringWriter stack = new StringWriter();
            error.printStackTrace(new PrintWriter(stack));
            result.put("error", stack.toString());
            try {
                save(root.resolve(baseline ? "reports/runtime-probe-baseline.json" : "reports/runtime-probe-fixed.json"), result);
                if (!baseline) save(root.resolve("reports/runtime-probe-tfc-" + result.get("runtime_tfc_version") + ".json"), result);
            } catch (Throwable saveError) {
                saveError.printStackTrace();
            }
            error.printStackTrace();
            System.out.println("[FLORAE-AUDIT] Registry codecs failed; exiting before EULA/world/network setup");
            System.exit(2);
        }
    }
}
