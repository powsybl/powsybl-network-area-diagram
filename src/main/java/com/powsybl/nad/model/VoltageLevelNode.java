/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class VoltageLevelNode extends AbstractNode {

    private final Map<String, BusInnerNode> busInnerNodes = new LinkedHashMap<>();
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
        busInnerNodes.put(busInnerNode.getEquipmentId(), busInnerNode);
    }

    public Collection<BusInnerNode> getBusNodes() {
        return Collections.unmodifiableCollection(busInnerNodes.values());
    }

    public Stream<BusInnerNode> getBusNodeStream() {
        return busInnerNodes.values().stream();
    }

    public boolean isVisible() {
        return visible;
    }

    public BusInnerNode getBusInnerNode(String id) {
        return busInnerNodes.get(id);
    }

    public void sortBusInnerNodes(Comparator<? super BusInnerNode> c) {
        List<BusInnerNode> sortedNodes = busInnerNodes.values().stream().sorted(c).collect(Collectors.toList());
        busInnerNodes.clear();
        sortedNodes.forEach(node -> busInnerNodes.put(node.getEquipmentId(), node));
    }
}
