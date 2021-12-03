/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import java.util.List;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public interface BranchEdge extends Edge {
    enum Side {
        ONE, TWO
    }

    List<Point> getLine(Side side);

    List<Point> getSide1();

    List<Point> getSide2();

    void setSide1(Point... points);

    void setSide2(Point... points);

    boolean isVisible(Side side);

    void setVisible(Side side, boolean visible);
}
