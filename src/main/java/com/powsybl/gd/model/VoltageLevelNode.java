/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.gd.model;

import java.util.ArrayList;
import java.util.List;

public class VoltageLevelNode extends AbstractNode {

    private final List<Edge> adjacentEdges = new ArrayList<>();
    private final String equipmentId;
    private final String nameOrId;
    private final double nominalV;
    private final List<BusNode> busNodes = new ArrayList<>();

    public VoltageLevelNode(String diagramId, String equipmentId, String nameOrId, double nominalV) {
        super(diagramId);
        this.equipmentId = equipmentId;
        this.nameOrId = nameOrId;
        this.nominalV = nominalV;
    }

    @Override
    public List<Edge> getAdjacentEdges() {
        return adjacentEdges;
    }

    public double getNominalV() {
        return nominalV;
    }

    public void addEdge(Edge edge) {
        adjacentEdges.add(edge);
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public String getNameOrId() {
        return nameOrId;
    }

    public void addBusNode(BusNode busNode) {
        busNodes.add(busNode);
    }

    public int getBusNodesCount() {
        return busNodes.size();
    }
}
