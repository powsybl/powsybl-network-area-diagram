/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.gd.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class Graph {

    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final Map<String, Edge> edges = new LinkedHashMap<>();

    public void addNode(Node node) {
        nodes.put(node.getDiagramId(), node);
    }

    public void addEdge(Edge edge) {
        edges.put(edge.getDiagramId(), edge);
    }

    public Stream<Node> getNodesStream() {
        return nodes.values().stream();
    }

    public Stream<Edge> getEdgesStream() {
        return edges.values().stream();
    }

    public Optional<Node> getNode(String diagramId) {
        return Optional.ofNullable(nodes.get(diagramId));
    }

    public Optional<Edge> getEdge(String diagramId) {
        return Optional.ofNullable(edges.get(diagramId));
    }

}
