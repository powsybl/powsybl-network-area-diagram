/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import java.util.*;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractEdge implements Edge {

    private final String diagramId;
    private final String equipmentId;
    private final String name;
    private List<Point> line1 = Collections.emptyList();
    private List<Point> line2 = Collections.emptyList();

    protected AbstractEdge(String diagramId, String equipmentId, String nameOrId) {
        this.diagramId = diagramId;
        this.equipmentId = equipmentId;
        this.name = nameOrId;
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
        this.line1 = Arrays.asList(points);
    }

    @Override
    public void setSide2(Point... points) {
        this.line2 = Arrays.asList(points);
    }

    @Override
    public String getDiagramId() {
        return diagramId;
    }

    @Override
    public String getEquipmentId() {
        return equipmentId;
    }

    @Override
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }
}
