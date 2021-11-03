/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class VoltageLevelNode extends AbstractNode {

    private final double nominalV;
    private final List<BusInnerNode> busInnerNodes = new ArrayList<>();

    public VoltageLevelNode(String diagramId, String equipmentId, String nameOrId, double nominalV) {
        super(diagramId, equipmentId, nameOrId);
        this.nominalV = nominalV;
    }

    public double getNominalV() {
        return nominalV;
    }

    public void addBusNode(BusInnerNode busInnerNode) {
        busInnerNodes.add(busInnerNode);
    }

    public int getBusNodesCount() {
        return busInnerNodes.size();
    }
}
