/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class LayoutParameters {
    private boolean textNodesForceLayout = false;
    private boolean springRepulsionForceLayout = false;

    public LayoutParameters() {
    }

    public LayoutParameters(LayoutParameters other) {
        this.textNodesForceLayout = other.textNodesForceLayout;
        this.springRepulsionForceLayout = other.springRepulsionForceLayout;
    }

    public boolean isTextNodesForceLayout() {
        return textNodesForceLayout;
    }

    public LayoutParameters setTextNodesForceLayout(boolean textNodesForceLayout) {
        this.textNodesForceLayout = textNodesForceLayout;
        return this;
    }

    public boolean isSpringRepulsionForceLayout() {
        return springRepulsionForceLayout;
    }

    public LayoutParameters setSpringRepulsionForceLayout(boolean springRepulsionForceLayout) {
        this.springRepulsionForceLayout = springRepulsionForceLayout;
        return this;
    }
}
