/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.ThreeWtEdge;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
class EdgeInfoTest extends AbstractTest {

    private SvgParameters svgParameters;

    private LayoutParameters layoutParameters;

    @BeforeEach
    public void setup() {
        this.layoutParameters = new LayoutParameters();
        this.svgParameters = new SvgParameters()
                .setSvgWidthAndHeightAdded(true)
                .setFixedWidth(800);
    }

    @Override
    protected LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    @Override
    protected SvgParameters getSvgParameters() {
        return svgParameters;
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return new NominalVoltageStyleProvider(network);
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return new LabelProvider() {
            @Override
            public List<EdgeInfo> getEdgeInfos(Graph graph, BranchEdge edge, BranchEdge.Side side) {
                return Collections.singletonList(new EdgeInfo("test", EdgeInfo.Direction.OUT, "int.", "ext."));
            }

            @Override
            public List<EdgeInfo> getEdgeInfos(Graph graph, ThreeWtEdge edge) {
                return Collections.singletonList(new EdgeInfo("test", EdgeInfo.Direction.IN, "int.", "ext."));
            }

            @Override
            public String getArrowPathDIn() { // larger arrow
                return "M-0.2 -0.1 H0.2 L0 0.1z";
            }

            @Override
            public String getArrowPathDOut() { // thinner arrow
                return "M-0.05 0.1 H0.05 L0 -0.1z";
            }
        };
    }

    @Test
    public void testPerpendicularLabels() {
        Network network = NetworkTestFactory.createTwoVoltageLevels();
        getSvgParameters().setEdgeInfoAlongEdge(false)
                .setArrowShift(0.5)
                .setArrowLabelShift(0.25);
        assertEquals(toString("/perpendicular_labels.svg"), generateSvgString(network, "/perpendicular_labels.svg"));
    }

    @Test
    public void testParallelLabels() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        getSvgParameters().setArrowShift(0.6)
                .setArrowLabelShift(0.2);
        assertEquals(toString("/double_labels.svg"), generateSvgString(network, "/double_labels.svg"));
    }
}
