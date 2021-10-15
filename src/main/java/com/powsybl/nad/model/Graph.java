/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

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

    public void addNode(Node node) {
        nodes.put(node.getEquipmentId(), node);
    }

    public void addEdge(Edge edge) {
        edges.put(edge.getEquipmentId(), edge);
    }

    public Stream<Node> getNodesStream() {
        return nodes.values().stream();
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

}
