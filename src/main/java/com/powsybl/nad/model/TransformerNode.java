/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TransformerNode extends AbstractNode {

    private final List<Edge> adjacentEdges = new ArrayList<>();

    public TransformerNode(String diagramId, String equipmentId, String nameOrId) {
        super(diagramId, equipmentId, nameOrId);
    }

    public void addEdge(Edge edge) {
        adjacentEdges.add(edge);
    }

    @Override
    public List<Edge> getAdjacentEdges() {
        return Collections.unmodifiableList(adjacentEdges);
    }
}
