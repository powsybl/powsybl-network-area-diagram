/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class VoltageLevelNode extends AbstractNode {

    private final List<BusInnerNode> busInnerNodes = new ArrayList<>();
    private final boolean visible;

    public VoltageLevelNode(String diagramId, String equipmentId, String nameOrId) {
        this(diagramId, equipmentId, nameOrId, true);
    }

    public VoltageLevelNode(String diagramId, String equipmentId, String nameOrId, boolean visible) {
        super(diagramId, equipmentId, nameOrId);
        this.visible = visible;
    }

    public void addBusNode(BusInnerNode busInnerNode) {
        Objects.requireNonNull(busInnerNode);
        busInnerNodes.add(busInnerNode);
    }

    public List<BusInnerNode> getBusNodes() {
        return Collections.unmodifiableList(busInnerNodes);
    }

    public Stream<BusInnerNode> getBusNodeStream() {
        return busInnerNodes.stream();
    }

    public boolean isVisible() {
        return visible;
    }

    public void sortBusInnerNodes(Comparator<? super BusInnerNode> c) {
        busInnerNodes.sort(c);
    }
}
