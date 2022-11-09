/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad;

import com.powsybl.nad.model.Point;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
class PointTest {
    @Test
    void testPoint() {
        assertEquals(new Point(0, 0), new Point(0, 0));
        assertNotEquals(new Point(0, 0), new Point(0, 1));
    }
}
