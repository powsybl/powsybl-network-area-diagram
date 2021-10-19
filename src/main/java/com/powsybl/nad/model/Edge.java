/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import java.util.*;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class Edge {

    private final String diagramId;
    private final String equipmentId;
    private final String name;
    private final Node node1;
    private final Node node2;
    private List<Point> polyline = new ArrayList<>();

    public Edge(String diagramId, String equipmentId, String nameOrId, Node node1, Node node2) {
        this.diagramId = diagramId;
        this.equipmentId = equipmentId;
        this.name = nameOrId;
        this.node1 = node1;
        this.node2 = node2;
    }

    public List<Node> getAdjacentNodes() {
        return Arrays.asList(node1, node2);
    }

    public Node getNode1() {
        return node1;
    }

    public Node getNode2() {
        return node2;
    }

    public List<Point> getPolyline() {
        return Collections.unmodifiableList(polyline);
    }

    public void setPolyline(List<Point> polyline) {
        this.polyline = new ArrayList<>(polyline);
    }

    public String getDiagramId() {
        return diagramId;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }
}
