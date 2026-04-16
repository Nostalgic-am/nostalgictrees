package com.nostalgictrees.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class InMemoryPackResources implements PackResources {

    private static final Gson GSON = new GsonBuilder().create();
    private final PackLocationInfo locationInfo;
    private final Map<PackType, Map<ResourceLocation, byte[]>> resources = new EnumMap<>(PackType.class);
    private final JsonObject packMeta;

    public InMemoryPackResources(PackLocationInfo locationInfo) {
        this.locationInfo = locationInfo;
        resources.put(PackType.CLIENT_RESOURCES, new HashMap<>());
        resources.put(PackType.SERVER_DATA, new HashMap<>());

        // Build pack.mcmeta JSON
        packMeta = new JsonObject();
        JsonObject pack = new JsonObject();
        pack.addProperty("description", "Nostalgic Trees Generated Resources");
        pack.addProperty("pack_format", 34);
        packMeta.add("pack", pack);
    }

    public void putResource(PackType type, ResourceLocation location, byte[] data) {
        resources.get(type).put(location, data);
    }

    public void putJson(PackType type, ResourceLocation location, String json) {
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
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        byte[] data = resources.get(type).get(location);
        if (data != null) {
            return () -> new ByteArrayInputStream(data);
        }
        return null;
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
        Map<ResourceLocation, byte[]> typeResources = resources.get(type);
        for (Map.Entry<ResourceLocation, byte[]> entry : typeResources.entrySet()) {
            ResourceLocation loc = entry.getKey();
            if (loc.getNamespace().equals(namespace) && loc.getPath().startsWith(path)) {
                byte[] data = entry.getValue();
                output.accept(loc, () -> new ByteArrayInputStream(data));
            }
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        Set<String> namespaces = new HashSet<>();
        for (ResourceLocation loc : resources.get(type).keySet()) {
            namespaces.add(loc.getNamespace());
        }
        return namespaces;
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) throws IOException {
        JsonObject meta = packMeta.getAsJsonObject(serializer.getMetadataSectionName());
        if (meta != null) {
            return serializer.fromJson(meta);
        }
        return null;
    }

    @Override
    public void close() {
    }

    @Override
    public String packId() {
        return locationInfo.id();
    }
}
