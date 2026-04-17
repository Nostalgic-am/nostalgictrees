package com.nostalgictrees.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import com.google.gson.JsonArray;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class InMemoryPackResources implements PackResources {

    private static final Gson GSON = new GsonBuilder().create();
    private final PackLocationInfo locationInfo;
    private final Map<PackType, Map<Identifier, byte[]>> resources = new EnumMap<>(PackType.class);
    private final JsonObject packMeta;

    public InMemoryPackResources(PackLocationInfo locationInfo) {
        this.locationInfo = locationInfo;
        resources.put(PackType.CLIENT_RESOURCES, new HashMap<>());
        resources.put(PackType.SERVER_DATA, new HashMap<>());

        // Build pack.mcmeta JSON
        // 26.1: Packs declaring pack_format > 64 MUST include min_format and max_format
        // or the loader throws JsonParseException at pack discovery.
        packMeta = new JsonObject();
        JsonObject pack = new JsonObject();
        pack.addProperty("description", "Nostalgic Trees Generated Resources");
        pack.addProperty("pack_format", 84);

        JsonArray minFormat = new JsonArray();
        minFormat.add(84);
        minFormat.add(0);
        pack.add("min_format", minFormat);

        JsonArray maxFormat = new JsonArray();
        maxFormat.add(84);
        maxFormat.add(2147483647); // "infinite upper bound" convention
        pack.add("max_format", maxFormat);

        packMeta.add("pack", pack);
    }

    public void putResource(PackType type, Identifier location, byte[] data) {
        resources.get(type).put(location, data);
    }

    public void putJson(PackType type, Identifier location, String json) {
        putResource(type, location, json.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public PackLocationInfo location() {
        return locationInfo;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... path) {
        if (path.length == 1 && path[0].equals("pack.mcmeta")) {
            byte[] data = GSON.toJson(packMeta).getBytes(StandardCharsets.UTF_8);
            return () -> new ByteArrayInputStream(data);
        }
        return null;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(PackType type, Identifier location) {
        byte[] data = resources.get(type).get(location);
        if (data != null) {
            return () -> new ByteArrayInputStream(data);
        }
        return null;
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
        Map<Identifier, byte[]> typeResources = resources.get(type);
        for (Map.Entry<Identifier, byte[]> entry : typeResources.entrySet()) {
            Identifier loc = entry.getKey();
            if (loc.getNamespace().equals(namespace) && loc.getPath().startsWith(path)) {
                byte[] data = entry.getValue();
                output.accept(loc, () -> new ByteArrayInputStream(data));
            }
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        Set<String> namespaces = new HashSet<>();
        for (Identifier loc : resources.get(type).keySet()) {
            namespaces.add(loc.getNamespace());
        }
        return namespaces;
    }

    /*
     * 26.1 change: MetadataSectionSerializer<T> → MetadataSectionType<T>.
     * Old fromJson(JsonObject) method is gone; MetadataSectionType is now a record
     * holding a Codec<T> which we use to parse the JSON element.
     */
    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionType<T> metadataType) throws IOException {
        JsonElement element = packMeta.get(metadataType.name());
        if (element == null) {
            return null;
        }
        DataResult<T> result = metadataType.codec().parse(JsonOps.INSTANCE, element);
        if (result.error().isPresent()) {
            throw new IOException("Failed to parse pack metadata section '" + metadataType.name()
                    + "': " + result.error().get().message());
        }
        return result.result().orElse(null);
    }

    @Override
    public void close() {
    }

    @Override
    public String packId() {
        return locationInfo.id();
    }
}