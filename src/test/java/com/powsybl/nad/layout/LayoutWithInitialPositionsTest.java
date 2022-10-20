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
        LayoutParameters layoutParameters = new LayoutParameters()
                .setSpringRepulsionFactorForceLayout(0.2);

        // Perform an initial layout with only a few voltage levels of the network
        NetworkAreaDiagram initialDiagram = new NetworkAreaDiagram(network, filter);
        Map<String, Point> initialPositions = initialDiagram.layout(layoutParameters);

        // Check initial points contains an entry for all voltage levels filtered
        network.getVoltageLevelStream().filter(filter).forEach(vl -> assertTrue(initialPositions.containsKey(vl.getId())));
        // Check we have voltage levels in the network that are not filtered and thus will not have an initial positions
        assertTrue(network.getVoltageLevelStream().anyMatch(filter.negate()));
        network.getVoltageLevelStream().filter(filter.negate()).forEach(vl -> assertFalse(initialPositions.containsKey(vl.getId())));

        // Perform a global layout with all the voltage levels in the network,
        // giving initial (fixed) positions for some equipment
        layoutParameters.setInitialPositions(initialPositions);
        NetworkAreaDiagram completeNetworkDiagram = new NetworkAreaDiagram(network, VoltageLevelFilter.NO_FILTER);
        Map<String, Point> allPositions = completeNetworkDiagram.layout(layoutParameters);

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
}
