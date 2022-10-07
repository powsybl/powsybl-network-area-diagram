/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.iidm.network.Network;
import com.powsybl.nad.NetworkAreaDiagram;
import com.powsybl.nad.svg.SvgParameters;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Luma Zamarreno <zamarrenolm at aia.es>
 */
class ForceLayoutIdOrderBadDrawingTest {

    static final Path OUTPUT = Paths.get("/Users/zamarrenolm/work/temp/nad/check-vl-order/output");
    static final double SPRING_REPULSION_FACTOR = 2.0;

    @Test
    void testIdOrderBadDrawing() {
        draw(SimpleNetworkFactory.createIdOrderBadDrawing(true), "bad");
        draw(SimpleNetworkFactory.createIdOrderBadDrawing(false), "nice");
        fail("check bad and nice .svg files. bad drawing has crossings: A-D link crosses over E-F");
    }

    private static void draw(Network network, String filename) {
        LayoutParameters layoutParameters = layoutParameters();
        SvgParameters svgParameters = svgParameters();

        new NetworkAreaDiagram(network).draw(
                OUTPUT.resolve(filename),
                svgParameters,
                layoutParameters);
    }

    private static SvgParameters svgParameters() {
        return new SvgParameters()
                .setInsertNameDesc(false)
                .setSvgWidthAndHeightAdded(false);
    }

    private static LayoutParameters layoutParameters() {
        return new LayoutParameters()
                .setSpringRepulsionFactorForceLayout(SPRING_REPULSION_FACTOR);
    }
}
