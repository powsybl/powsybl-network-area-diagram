/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad;

import com.powsybl.ieeecdf.converter.IeeeCdfNetworkFactory;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
class SvgWriterTest extends AbstractTest {

    private LayoutParameters layoutParameters;

    @BeforeEach
    public void setup() {
        this.layoutParameters = new LayoutParameters();
    }

    @Override
    protected LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    @Override
    protected SvgParameters getSvgParameters() {
        return new SvgParameters()
                .setInsertName(true)
                .setSvgWidthAndHeightAdded(true)
                .setFixedWidth(800);
    }

    @Override
    protected StyleProvider getStyleProvider() {
        return new DefaultStyleProvider();
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return new DefaultLabelProvider(network);
    }

    @Test
    void testIEEE30() {
        Network network = IeeeCdfNetworkFactory.create30();
        assertEquals(toString("/IEEE_30_bus.svg"), generateSvgString(network, "/IEEE_30_bus.svg"));
    }

    @Test
    void testIEEE14() {
        Network network = IeeeCdfNetworkFactory.create14();
        LoadFlow.run(network);
        assertEquals(toString("/IEEE_14_bus.svg"), generateSvgString(network, "/IEEE_14_bus.svg"));
    }

    @Test
    void testIEEE14ForceLayoutWithTextNodes() {
        Network network = IeeeCdfNetworkFactory.create14();
        getLayoutParameters().setTextNodesForceLayout(true);
        assertEquals(toString("/IEEE_14_bus_text_nodes.svg"), generateSvgString(network, "/IEEE_14_bus_text_nodes.svg"));
    }

    @Test
    void testDisconnection() {
        Network network = IeeeCdfNetworkFactory.create14();
        network.getLine("L3-4-1").getTerminal1().disconnect();
        network.getTwoWindingsTransformer("T4-7-1").getTerminal1().disconnect();
        assertEquals(toString("/IEEE_14_bus_disconnection.svg"), generateSvgString(network, "/IEEE_14_bus_disconnection.svg"));
    }

    @Test
    void testIEEE24() {
        Network network = NetworkXml.read(getClass().getResourceAsStream("/IEEE_24_bus.xiidm"));
        assertEquals(toString("/IEEE_24_bus.svg"), generateSvgString(network, "/IEEE_24_bus.svg"));
    }

    @Test
    void testIEEE57() {
        Network network = IeeeCdfNetworkFactory.create57();
        assertEquals(toString("/IEEE_57_bus.svg"), generateSvgString(network, "/IEEE_57_bus.svg"));
    }

    @Test
    void testIEEE118() {
        Network network = IeeeCdfNetworkFactory.create118();
        assertEquals(toString("/IEEE_118_bus.svg"), generateSvgString(network, "/IEEE_118_bus.svg"));
    }

    @Test
    void testIEEE118PartialGraph() {
        Network network = IeeeCdfNetworkFactory.create118();
        VoltageLevelFilter vlDepthFilter = VoltageLevelFilter.createVoltageLevelDepthFilter(network, "VL92", 4);
        assertEquals(toString("/IEEE_118_bus.svg"), generateSvgString(network, vlDepthFilter, "/IEEE_118_bus_partial.svg"));
    }

    @Test
    void testEurope() {
        Network network = Importers.loadNetwork("simple-eu.uct", getClass().getResourceAsStream("/simple-eu.uct"));
        assertEquals(toString("/simple-eu.svg"), generateSvgString(network, "/simple-eu.svg"));
    }

}
