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
public abstract class AbstractBranchEdge extends AbstractEdge implements BranchEdge {
    private final boolean side1Connected;
    private final boolean side2Connected;
    private List<Point> line1 = Collections.emptyList();
    private List<Point> line2 = Collections.emptyList();

    protected AbstractBranchEdge(String diagramId, String equipmentId, String nameOrId, boolean side1Connected, boolean side2Connected) {
        super(diagramId, equipmentId, nameOrId);
        this.side1Connected = side1Connected;
        this.side2Connected = side2Connected;
    }

    public List<Point> getLine(Side side) {
        return side == Side.ONE ? getSide1() : getSide2();
    }

    @Override
    public List<Point> getSide1() {
        return Collections.unmodifiableList(line1);
    }

    @Override
    public List<Point> getSide2() {
        return Collections.unmodifiableList(line2);
    }

    @Override
    public void setSide1(Point... points) {
        Arrays.stream(points).forEach(Objects::requireNonNull);
        this.line1 = Arrays.asList(points);
    }

    @Override
    public void setSide2(Point... points) {
        Arrays.stream(points).forEach(Objects::requireNonNull);
        this.line2 = Arrays.asList(points);
    }

    public boolean isConnected(Side side) {
        return side == Side.ONE ? isSide1Connected() : isSide2Connected();
    }

    public boolean isSide1Connected() {
        return side1Connected;
    }

    public boolean isSide2Connected() {
        return side2Connected;
    }
}
