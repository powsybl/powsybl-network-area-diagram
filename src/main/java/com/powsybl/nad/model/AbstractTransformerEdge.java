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
public abstract class AbstractTransformerEdge extends AbstractEdge implements BranchEdge {

    private final List<VoltageLevelNode> adjacentNodes = new ArrayList<>();
    private final Point transformerPosition = new Point();
    private final List<List<Point>> polylines = new ArrayList<>();

    protected AbstractTransformerEdge(String diagramId, String id, VoltageLevelNode node1, VoltageLevelNode node2) {
        super(diagramId, id);
        adjacentNodes.add(node1);
        adjacentNodes.add(node2);
    }

    protected AbstractTransformerEdge(String diagramId, String id, VoltageLevelNode node1, VoltageLevelNode node2, VoltageLevelNode node3) {
        super(diagramId, id);
        adjacentNodes.add(node1);
        adjacentNodes.add(node2);
        adjacentNodes.add(node3);
    }

    public List<VoltageLevelNode> getAdjacentNodes() {
        return Collections.unmodifiableList(adjacentNodes);
    }

    public Point getTransformerPosition() {
        return transformerPosition;
    }

    public List<List<Point>> getEdgePolylines() {
        return Collections.unmodifiableList(polylines);
    }

    public void addPolyline(List<Point> polyline) {
        polylines.add(polyline);
    }
}
