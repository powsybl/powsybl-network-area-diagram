/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.gd.model;

public abstract class AbstractNode implements Node {

    private final String diagramId;
    private int width;
    private int height;
    private Point position;

    protected AbstractNode(String diagramId) {
        this.diagramId = diagramId;
        position = new Point();
        width = 0;
        height = 0;
    }

    @Override
    public String getDiagramId() {
        return diagramId;
    }

    @Override
    public void setPosition(double x, double y) {
        position = new Point(x, y);
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
