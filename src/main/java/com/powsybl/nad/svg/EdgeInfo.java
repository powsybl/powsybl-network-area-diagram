/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import java.util.Optional;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class EdgeInfo {
    static final String ACTIVE_POWER = "ActivePower";
    static final String REACTIVE_POWER = "ReactivePower";

    private final String infoType;
    private final Direction arrowDirection;
    private final String leftLabel;
    private final String rightLabel;

    public EdgeInfo(String infoType, Direction arrowDirection, String leftLabel, String rightLabel) {
        this.infoType = infoType;
        this.arrowDirection = arrowDirection;
        this.leftLabel = leftLabel;
        this.rightLabel = rightLabel;
    }

    public EdgeInfo(String infoType, double value) {
        this(infoType, value < 0 ? Direction.IN : Direction.OUT, null, String.valueOf(Math.round(value)));
    }

    public String getInfoType() {
        return infoType;
    }

    public Optional<Direction> getDirection() {
        return Optional.ofNullable(arrowDirection);
    }

    public Optional<String> getLeftLabel() {
        return Optional.ofNullable(leftLabel);
    }

    public Optional<String> getRightLabel() {
        return Optional.ofNullable(rightLabel);
    }

    public enum Direction {
        IN, OUT
    }
}
