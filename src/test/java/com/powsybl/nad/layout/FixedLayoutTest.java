/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.iidm.network.Network;
import com.powsybl.nad.build.iidm.NetworkGraphBuilder;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Point;
import com.powsybl.nad.svg.NetworkTestFactory;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Luma Zamarreno <zamarrenolm at aia.es>
 */
class FixedLayoutTest {

    @Test
    void testPointsAreDifferent() {
        Point p0 = new Point(1, 0);
        Point p1 = new Point(2, 0);
        assertNotEquals(p0, p1);
        assertNotEquals(p0.hashCode(), p1.hashCode());
    }

    @Test
    void testCurrentLimits() {
        Network network = NetworkTestFactory.createTwoVoltageLevels();

        Map<String, Point> expected = Map.of(
                "vl1", new Point(1, 0),
                "vl2", new Point(2, 1));
        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER).buildGraph();
        Layout fixedLayout = new BasicFixedLayoutFactory().create();
        fixedLayout.setInitialNodePositions(expected);
        Map<String, Point> actual = fixedLayout.run(graph, new LayoutParameters());
        assertEquals(expected, actual);
    }
}
