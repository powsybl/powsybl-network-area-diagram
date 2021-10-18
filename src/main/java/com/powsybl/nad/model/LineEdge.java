/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LineEdge extends AbstractEdge implements BranchEdge {
    private final List<VoltageLevelNode> adjacentNodes = new LinkedList<>();
    private List<Point> polyline = new ArrayList<>();

    public LineEdge(String id, VoltageLevelNode node1, VoltageLevelNode node2) {
        super(id);
        adjacentNodes.add(node1);
        adjacentNodes.add(node2);
    }

    @Override
    public List<VoltageLevelNode> getAdjacentNodes() {
        return Collections.unmodifiableList(adjacentNodes);
    }

    public List<Point> getPolyline() {
        return Collections.unmodifiableList(polyline);
    }

    public void setPolyline(List<Point> polyline) {
        this.polyline = new ArrayList<>(polyline);
    }

}
