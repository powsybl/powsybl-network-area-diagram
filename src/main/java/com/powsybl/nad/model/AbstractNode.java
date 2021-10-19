/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import java.util.Optional;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractNode implements Node {

    private final String diagramId;
    private final String equipmentId;
    private final String name;
    private int width;
    private int height;
    private Point position;

    protected AbstractNode(String diagramId, String equipmentId, String name) {
        this.diagramId = diagramId;
        this.equipmentId = equipmentId;
        this.name = name;
        position = new Point();
        width = 0;
        height = 0;
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

    @Override
    public void setPosition(double x, double y) {
        position = new Point(x, y);
    }

    @Override
    public double getX() {
        return position.getX();
    }

    @Override
    public double getY() {
        return position.getY();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
