/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.nad.model.Point;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class LayoutParameters {
    private boolean textNodesForceLayout = false;
    private double springRepulsionFactorForceLayout = 0.0;
    private Map<String, Point> initialPositions = Collections.emptyMap();

    public LayoutParameters() {
    }

    public LayoutParameters(LayoutParameters other) {
        this.textNodesForceLayout = other.textNodesForceLayout;
        this.springRepulsionFactorForceLayout = other.springRepulsionFactorForceLayout;
        this.initialPositions = new HashMap<>(initialPositions);
    }

    public Map<String, Point> getInitialPositions() {
        return Collections.unmodifiableMap(initialPositions);
    }

    public void setInitialPositions(Map<String, Point> initialPositions) {
        this.initialPositions = new HashMap<>(initialPositions);
    }

    public boolean isTextNodesForceLayout() {
        return textNodesForceLayout;
    }

    public LayoutParameters setTextNodesForceLayout(boolean textNodesForceLayout) {
        this.textNodesForceLayout = textNodesForceLayout;
        return this;
    }

    public LayoutParameters setSpringRepulsionFactorForceLayout(double springRepulsionFactorForceLayout) {
        this.springRepulsionFactorForceLayout = springRepulsionFactorForceLayout;
        return this;
    }

    public double getSpringRepulsionFactorForceLayout() {
        return springRepulsionFactorForceLayout;
    }
}
