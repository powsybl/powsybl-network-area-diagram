/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
class LayoutParametersTest {

    @Test
    void test() {
        LayoutParameters layoutParameters0 = new LayoutParameters()
                .setTextNodesForceLayout(true);

        LayoutParameters layoutParameters1 = new LayoutParameters(layoutParameters0);

        assertEquals(layoutParameters0.isTextNodesForceLayout(), layoutParameters1.isTextNodesForceLayout());
    }
}
