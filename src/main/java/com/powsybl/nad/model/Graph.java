/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.Pseudograph;
import org.jgrapht.graph.WeightedPseudograph;

import java.util.*;
import java.util.stream.Collectors;
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

    private final org.jgrapht.Graph<Node, Edge> voltageLevelGraph = new WeightedPseudograph<>(Edge.class);
    private final org.jgrapht.Graph<Node, Edge> busGraph = new Pseudograph<>(Edge.class);
    private final List<TextNode> textNodes = new ArrayList<>();
    private final Map<TextEdge, Pair<VoltageLevelNode, TextNode>> textEdges = new LinkedHashMap<>();

    public void addNode(Node node) {
        Objects.requireNonNull(node);
        if (!(node instanceof TextNode)) {
            nodes.put(node.getEquipmentId(), node);
            voltageLevelGraph.addVertex(node);
            if (node instanceof VoltageLevelNode) {
                ((VoltageLevelNode) node).getBusNodeStream().forEach(busGraph::addVertex);
            }
            if (node instanceof ThreeWtNode) {
                busGraph.addVertex(node);
            }
        } else {
            textNodes.add((TextNode) node);
        }
    }

    public void addEdge(VoltageLevelNode node1, BusInnerNode busInnerNode1,
                        VoltageLevelNode node2, BusInnerNode busInnerNode2, BranchEdge edge) {
        addVoltageLevelsEdge(node1, node2, edge);
        addBusesEdge(busInnerNode1, busInnerNode2, edge);
    }

    public void addEdge(VoltageLevelNode vlNode, BusInnerNode busNode, ThreeWtNode tNode, ThreeWtEdge edge) {
        addVoltageLevelsEdge(vlNode, tNode, edge);
        addBusesEdge(busNode, tNode, edge);
    }

    public void addEdge(VoltageLevelNode vlNode, TextNode textNode, TextEdge edge) {
        Objects.requireNonNull(vlNode);
        Objects.requireNonNull(textNode);
        Objects.requireNonNull(edge);
        textEdges.put(edge, Pair.of(vlNode, textNode));
    }

    private void addVoltageLevelsEdge(Node node1, Node node2, Edge edge) {
        Objects.requireNonNull(node1);
        Objects.requireNonNull(node2);
        Objects.requireNonNull(edge);
        edges.put(edge.getEquipmentId(), edge);
        voltageLevelGraph.addEdge(node1, node2, edge);
    }

    private void addBusesEdge(BusInnerNode node1, Node node2, Edge edge) {
        Objects.requireNonNull(edge);
        if (node1 != null && node2 != null) {
            busGraph.addEdge(node1, node2, edge);
        }
    }

    public Stream<Node> getNodesStream() {
        return voltageLevelGraph.vertexSet().stream();
    }

    public Stream<VoltageLevelNode> getVoltageLevelNodesStream() {
        return nodes.values().stream().filter(VoltageLevelNode.class::isInstance).map(VoltageLevelNode.class::cast);
    }

    public Stream<ThreeWtNode> getThreeWtNodesStream() {
        return nodes.values().stream().filter(ThreeWtNode.class::isInstance).map(ThreeWtNode.class::cast);
    }

    public Stream<TextNode> getTextNodesStream() {
        return textNodes.stream();
    }

    public List<TextNode> getTextNodes() {
        return Collections.unmodifiableList(textNodes);
    }

    public Stream<Edge> getEdgesStream() {
        return edges.values().stream();
    }

    public Collection<Edge> getEdges() {
        return Collections.unmodifiableCollection(voltageLevelGraph.edgeSet());
    }

    public Stream<Edge> getEdgeStream(Node node) {
        return voltageLevelGraph.edgesOf(node).stream();
    }

    public Collection<Edge> getBusEdges(BusInnerNode busNode) {
        return busGraph.edgesOf(busNode);
    }

    public List<BranchEdge> getBranchEdges() {
        return voltageLevelGraph.edgeSet().stream()
                .filter(BranchEdge.class::isInstance)
                .map(BranchEdge.class::cast)
                .collect(Collectors.toList());
    }

    public Stream<TextEdge> getTextEdgesStream() {
        return textEdges.keySet().stream();
    }

    public List<TextEdge> getTextEdges() {
        return getTextEdgesStream().collect(Collectors.toList());
    }

    public Map<TextEdge, Pair<VoltageLevelNode, TextNode>> getTextEdgesMap() {
        return Collections.unmodifiableMap(textEdges);
    }

    public Stream<BranchEdge> getNonMultiBranchEdgesStream() {
        return voltageLevelGraph.edgeSet().stream()
                .filter(BranchEdge.class::isInstance)
                .map(BranchEdge.class::cast)
                .filter(e -> voltageLevelGraph.getAllEdges(voltageLevelGraph.getEdgeSource(e), voltageLevelGraph.getEdgeTarget(e)).size() == 1);
    }

    public Stream<Set<Edge>> getMultiBranchEdgesStream() {
        return voltageLevelGraph.edgeSet().stream()
                .map(e -> voltageLevelGraph.getAllEdges(voltageLevelGraph.getEdgeSource(e), voltageLevelGraph.getEdgeTarget(e)))
                .filter(e -> e.size() > 1)
                .distinct();
    }

    public Stream<ThreeWtEdge> getThreeWtEdgesStream() {
        return voltageLevelGraph.edgeSet().stream()
                .filter(ThreeWtEdge.class::isInstance)
                .map(ThreeWtEdge.class::cast);
    }

    public List<ThreeWtEdge> getThreeWtEdges() {
        return getThreeWtEdgesStream().collect(Collectors.toList());
    }

    public Optional<Node> getNode(String equipmentId) {
        return Optional.ofNullable(nodes.get(equipmentId));
    }

    public Optional<VoltageLevelNode> getVoltageLevelNode(String voltageLevelId) {
        return getNode(voltageLevelId).filter(VoltageLevelNode.class::isInstance).map(VoltageLevelNode.class::cast);
    }

    public org.jgrapht.Graph<Node, Edge> getJgraphtGraph(boolean includeTextNodes) {
        if (includeTextNodes) {
            org.jgrapht.Graph<Node, Edge> graphWithTextNodes = new WeightedPseudograph<>(Edge.class);
            voltageLevelGraph.vertexSet().forEach(graphWithTextNodes::addVertex);
            voltageLevelGraph.edgeSet().forEach(e -> graphWithTextNodes.addEdge(voltageLevelGraph.getEdgeSource(e), voltageLevelGraph.getEdgeTarget(e), e));
            textNodes.forEach(graphWithTextNodes::addVertex);
            textEdges.forEach((edge, nodePair) -> {
                graphWithTextNodes.addEdge(nodePair.getFirst(), nodePair.getSecond(), edge);
                graphWithTextNodes.setEdgeWeight(edge, 1);
            });
            return graphWithTextNodes;
        } else {
            return voltageLevelGraph;
        }
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

    public Node getNode1(Edge edge) {
        return voltageLevelGraph.getEdgeSource(edge);
    }

    public Node getNode2(Edge edge) {
        return voltageLevelGraph.getEdgeTarget(edge);
    }

    public boolean containsEdge(String equipmentId) {
        return edges.containsKey(equipmentId);
    }

    public boolean containsNode(String equipmentId) {
        return nodes.containsKey(equipmentId);
    }
}
