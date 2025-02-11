package org.popcraft.chunkyborder;

import org.popcraft.chunkyborder.integration.BlueMapIntegration;
import org.popcraft.chunkyborder.integration.DynmapCommonAPIProvider;
import org.popcraft.chunkyborder.integration.DynmapIntegration;
import org.popcraft.chunkyborder.integration.MapIntegration;
import org.popcraft.chunkyborder.integration.SquaremapIntegration;
import org.popcraft.chunkyborder.platform.MapIntegrationLoader;
import xyz.jpenilla.squaremap.api.SquaremapProvider;

import java.util.Optional;

public class FabricMapIntegrationLoader implements MapIntegrationLoader {
    @Override
    public Optional<MapIntegration> loadBlueMap() {
        try {
            Class.forName("de.bluecolored.bluemap.api.BlueMapAPI");
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
        return Optional.of(new BlueMapIntegration());
    }

    @Override
    public Optional<MapIntegration> loadDynmap() {
        try {
            Class.forName("org.dynmap.DynmapCommonAPI");
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
        return DynmapCommonAPIProvider.get().map(DynmapIntegration::new);
    }

    @Override
    public Optional<MapIntegration> loadSquaremap() {
        try {
            Class.forName("xyz.jpenilla.squaremap.api.SquaremapProvider");
            return Optional.of(new SquaremapIntegration(SquaremapProvider.get()));
        } catch (ClassNotFoundException | IllegalStateException e) {
            return Optional.empty();
        }
    }
}
