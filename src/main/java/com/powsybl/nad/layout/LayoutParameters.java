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
    private double edgesForkAperture = Math.toRadians(60);
    private double edgesForkLength = 0.8;

    public double getEdgesForkAperture() {
        return edgesForkAperture;
    }

    public LayoutParameters setEdgesForkAperture(double edgesForkApertureDegrees) {
        this.edgesForkAperture = Math.toRadians(edgesForkApertureDegrees);
        return this;
    }

    public double getEdgesForkLength() {
        return edgesForkLength;
    }

    public LayoutParameters setEdgesForkLength(double edgesForkLength) {
        this.edgesForkLength = edgesForkLength;
        return this;
    }
}
