/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class BranchEdge extends AbstractEdge {

    public enum Side {
        ONE, TWO
    }

    public static final String TWO_WT_EDGE = "TwoWtEdge";
    public static final String LINE_EDGE = "LineEdge";

    private List<Point> line1 = Collections.emptyList();
    private List<Point> line2 = Collections.emptyList();
    private final boolean[] visible = new boolean[] {true, true};
    private final String type;

    public BranchEdge(String diagramId, String equipmentId, String nameOrId, String type) {
        super(diagramId, equipmentId, nameOrId);
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public List<Point> getLine(Side side) {
        Objects.requireNonNull(side);
        return side == Side.ONE ? getSide1() : getSide2();
    }

    public List<Point> getSide1() {
        return Collections.unmodifiableList(line1);
    }

    public List<Point> getSide2() {
        return Collections.unmodifiableList(line2);
    }

    public void setSide1(Point... points) {
        Arrays.stream(points).forEach(Objects::requireNonNull);
        this.line1 = Arrays.asList(points);
    }

    public void setSide2(Point... points) {
        Arrays.stream(points).forEach(Objects::requireNonNull);
        this.line2 = Arrays.asList(points);
    }

    public boolean isVisible(Side side) {
        Objects.requireNonNull(side);
        return visible[side.ordinal()];
    }

    public void setVisible(Side side, boolean visible) {
        Objects.requireNonNull(side);
        this.visible[side.ordinal()] = visible;
    }
}
