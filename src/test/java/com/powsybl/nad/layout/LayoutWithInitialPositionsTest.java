/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.nad.NetworkAreaDiagram;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.model.Point;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreno <zamarrenolm at aia.es>
 */
class LayoutWithInitialPositionsTest {

    @Test
    void testDiamond() {
        checkLayoutWithInitialPositions(LayoutNetworkFactory.createDiamond());
    }

    private static void checkLayoutWithInitialPositions(Network network) {
        Predicate<VoltageLevel> filter = vl -> vl.getNominalV() >= 100;

        // Perform an initial layout with only a few voltage levels of the network
        NetworkAreaDiagram initialDiagram = new NetworkAreaDiagram(network, filter);
        Map<String, Point> initialPositions = initialDiagram.getLayout().run(initialDiagram.buildGraph(), getLayoutParameters());

        // Check initial points contains an entry for all voltage levels filtered
        network.getVoltageLevelStream().filter(filter).forEach(vl -> assertTrue(initialPositions.containsKey(vl.getId())));
        // Check we have voltage levels in the network that are not filtered and thus will not have an initial positions
        assertTrue(network.getVoltageLevelStream().anyMatch(filter.negate()));
        network.getVoltageLevelStream().filter(filter.negate()).forEach(vl -> assertFalse(initialPositions.containsKey(vl.getId())));

        checkAllInitialPositionsFixed(network, initialPositions);
        checkOnlySomeInitialPositionsFixed(network, initialPositions);
    }

    private static void checkAllInitialPositionsFixed(Network network, Map<String, Point> initialPositions) {
        // Perform a global layout with all the voltage levels in the network,
        // giving fixed positions for some equipment
        NetworkAreaDiagram completeNetworkDiagram = new NetworkAreaDiagram(network, VoltageLevelFilter.NO_FILTER);
        Layout layout = completeNetworkDiagram.getLayout();
        layout.setFixedNodePositions(initialPositions);
        Map<String, Point> allPositions = layout.run(completeNetworkDiagram.buildGraph(), getLayoutParameters());

        // Check positions of initial layout have been preserved in global layout
        for (Map.Entry<String, Point> l : initialPositions.entrySet()) {
            String equipmentId = l.getKey();
            Point expected = l.getValue();
            Point actual = allPositions.get(equipmentId);
            assertNotNull(actual);
            assertEquals(expected.getX(), actual.getX());
            assertEquals(expected.getY(), actual.getY());
        }
    }

    private static void checkOnlySomeInitialPositionsFixed(Network network, Map<String, Point> initialPositions) {
        // Perform a global layout with all the voltage levels in the network,
        // giving initial positions for some equipment,
        // and fixing the position for only some equipment
        NetworkAreaDiagram completeNetworkDiagram = new NetworkAreaDiagram(network, VoltageLevelFilter.NO_FILTER);
        Layout layout = completeNetworkDiagram.getLayout();
        layout.setInitialNodePositions(initialPositions);
        // Only consider fixed the first one in the initial layout
        Set<String> fixedNodes = Set.of(initialPositions.keySet().iterator().next());
        layout.setNodesWithFixedPosition(fixedNodes);
        Map<String, Point> allPositions1 = layout.run(completeNetworkDiagram.buildGraph(), getLayoutParameters());

        // Check positions of initial layout have been preserved in global layout
        for (Map.Entry<String, Point> l : initialPositions.entrySet()) {
            String equipmentId = l.getKey();
            Point expected = l.getValue();
            Point actual = allPositions1.get(equipmentId);
            assertNotNull(actual);
            if (fixedNodes.contains(equipmentId)) {
                assertEquals(expected.getX(), actual.getX());
                assertEquals(expected.getY(), actual.getY());
            } else {
                // We expect that the nodes with initial position but that have not been fixed have been moved
                assertTrue(expected.getX() != actual.getX() || expected.getY() != actual.getY());
            }
        }
    }

    private static LayoutParameters getLayoutParameters() {
        return new LayoutParameters()
                .setSpringRepulsionFactorForceLayout(0.2);
    }
}
