/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import org.jgrapht.graph.Pseudograph;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class Graph {

    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final Map<String, Edge> edges = new LinkedHashMap<>();
    private double minX = 0;
    private double minY = 0;
    private double maxX = 0;
    private double maxY = 0;

    public void addNode(Node node) {
        nodes.put(node.getEquipmentId(), node);
    }

    public void addEdge(Edge edge) {
        edges.put(edge.getEquipmentId(), edge);
    }

    public Stream<Node> getNodesStream() {
        return nodes.values().stream();
    }

    public Stream<VoltageLevelNode> getVoltageLevelNodesStream() {
        return nodes.values().stream().filter(VoltageLevelNode.class::isInstance).map(VoltageLevelNode.class::cast);
    }

    public Stream<TransformerNode> getTransformerNodesStream() {
        return nodes.values().stream().filter(TransformerNode.class::isInstance).map(TransformerNode.class::cast);
    }

    public Stream<TextNode> getTextNodesStream() {
        return nodes.values().stream().filter(TextNode.class::isInstance).map(TextNode.class::cast);
    }

    public Stream<Edge> getEdgesStream() {
        return edges.values().stream();
    }

    public Optional<Node> getNode(String equipmentId) {
        return Optional.ofNullable(nodes.get(equipmentId));
    }

    public Optional<VoltageLevelNode> getVoltageLevelNode(String voltageLevelId) {
        return getNode(voltageLevelId).filter(VoltageLevelNode.class::isInstance).map(VoltageLevelNode.class::cast);
    }

    public Optional<Edge> getEdge(String diagramId) {
        return Optional.ofNullable(edges.get(diagramId));
    }

    public org.jgrapht.Graph<Node, Edge> toJgrapht() {
        org.jgrapht.Graph<Node, Edge> graph = new Pseudograph<>(Edge.class);
        nodes.values().forEach(graph::addVertex);
        edges.values().forEach(e -> graph.addEdge(e.getNode1(), e.getNode2(), e));
        return graph;
    }

    public double getWidth() {
        return maxX - minX;
    }

    public double getHeight() {
        return maxY - minY;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public void setDimensions(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }
}
