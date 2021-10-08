/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.gd.model;

public class TwoWindingsTransformerEdge extends AbstractTransformerEdge {
    protected TwoWindingsTransformerEdge(String id, VoltageLevelNode node1, VoltageLevelNode node2) {
        super(id, node1, node2);
    }
}
