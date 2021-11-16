/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractBranchEdge extends AbstractEdge {
    private final boolean side1Connected;
    private final boolean side2Connected;

    public AbstractBranchEdge(String diagramId, String equipmentId, String nameOrId, boolean side1Connected, boolean side2Connected) {
        super(diagramId, equipmentId, nameOrId);
        this.side1Connected = side1Connected;
        this.side2Connected = side2Connected;
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
