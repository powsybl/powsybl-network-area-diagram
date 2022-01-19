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

    private final List<BusInnerNode> busInnerNodes = new ArrayList<>();
    private final boolean visible;

    public VoltageLevelNode(String diagramId, String equipmentId, String nameOrId, double nominalV) {
        this(diagramId, equipmentId, nameOrId, nominalV, true);
    }

    public VoltageLevelNode(String diagramId, String equipmentId, String nameOrId, double nominalV, boolean visible) {
        super(diagramId, equipmentId, nameOrId);
        this.visible = visible;
    }

    public void addBusNode(BusInnerNode busInnerNode) {
        busInnerNodes.add(busInnerNode);
    }

    public int getBusNodesCount() {
        return busInnerNodes.size();
    }

    public boolean isVisible() {
        return visible;
    }
}
