package org.popcraft.chunkyborder.integration;

import com.flowpowered.math.vector.Vector2d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.platform.util.Vector2;
import org.popcraft.chunky.shape.AbstractEllipse;
import org.popcraft.chunky.shape.AbstractPolygon;
import org.popcraft.chunky.shape.Shape;

import java.util.ArrayList;
import java.util.List;

public class BlueMapIntegration extends AbstractMapIntegration {
    private static final String MARKER_SET_ID = "chunky";
    private final List<Runnable> pendingMarkers = new ArrayList<>();
    private BlueMapAPI blueMapAPI;

    public BlueMapIntegration() {
        BlueMapAPI.onEnable(blueMap -> {
            this.blueMapAPI = blueMap;
            pendingMarkers.forEach(Runnable::run);
            pendingMarkers.clear();
        });
        BlueMapAPI.onDisable(blueMap -> this.blueMapAPI = null);
    }

    @Override
    public void addShapeMarker(final World world, final Shape shape) {
        if (blueMapAPI == null) {
            this.pendingMarkers.add(() -> this.addShapeMarker(world, shape));
            return;
        }
        final MarkerSet markerSet = new MarkerSet(MARKER_SET_ID);
        markerSet.setLabel(this.label);
        final de.bluecolored.bluemap.api.math.Shape blueShape;
        if (shape instanceof final AbstractPolygon polygon) {
            final List<Vector2> polygonPoints = polygon.points();
            final int size = polygonPoints.size();
            final Vector2d[] points = new Vector2d[size];
            for (int i = 0; i < size; ++i) {
                final Vector2 p = polygonPoints.get(i);
                points[i] = Vector2d.from(p.getX(), p.getZ());
            }
            blueShape = new de.bluecolored.bluemap.api.math.Shape(points);
        } else if (shape instanceof final AbstractEllipse ellipse) {
            final Vector2 center = ellipse.center();
            final Vector2 radii = ellipse.radii();
            final Vector2d centerPos = Vector2d.from(center.getX(), center.getZ());
            blueShape = de.bluecolored.bluemap.api.math.Shape.createEllipse(centerPos, radii.getX(), radii.getZ(), 100);
        } else {
            return;
        }
        final ShapeMarker marker = new ShapeMarker(this.label, blueShape, world.getSeaLevel());
        marker.setColors(new Color(0xFF000000 | this.color), new Color(0));
        marker.setLineWidth(this.weight);
        marker.setDepthTestEnabled(false);
        markerSet.getMarkers().put(MARKER_SET_ID, marker);
        blueMapAPI.getWorld(world.getName())
                .map(BlueMapWorld::getMaps)
                .ifPresent(maps -> maps.forEach(map -> map.getMarkerSets().put(MARKER_SET_ID, markerSet)));
    }

    @Override
    public void removeShapeMarker(final World world) {
        if (blueMapAPI == null) {
            return;
        }
        blueMapAPI.getWorld(world.getName())
                .map(BlueMapWorld::getMaps)
                .ifPresent(maps -> maps.forEach(map -> map.getMarkerSets().remove(MARKER_SET_ID)));
    }

    @Override
    public void removeAllShapeMarkers() {
        if (blueMapAPI == null) {
            return;
        }
        blueMapAPI.getMaps().forEach(map -> map.getMarkerSets().remove(MARKER_SET_ID));
    }
}
