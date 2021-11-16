/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad;

import com.powsybl.ieeecdf.converter.IeeeCdfNetworkFactory;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.DefaultStyleProvider;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.svg.SvgParameters;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
class SvgWriterTest extends AbstractTest {

    @Override
    protected LayoutParameters getLayoutParameters() {
        return new LayoutParameters();
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

    @Test
    void testIEEE30() {
        Network network = IeeeCdfNetworkFactory.create30();
        assertEquals(toString("/IEEE_30_bus.svg"), generateSvgString(network, "/IEEE_30_bus.svg"));
    }

    @Test
    void testIEEE14() {
        Network network = IeeeCdfNetworkFactory.create14();
        assertEquals(toString("/IEEE_14_bus.svg"), generateSvgString(network, "/IEEE_14_bus.svg"));
    }

    @Test
    void testDisconnection() {
        Network network = IeeeCdfNetworkFactory.create14();
        network.getLine("L3-4-1").getTerminal1().disconnect();
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

}
