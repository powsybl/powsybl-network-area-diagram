/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad;

import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.DefaultStyleProvider;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.svg.SvgParameters;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class SvgWriterTest extends AbstractTest {

    @Override
    protected LayoutParameters getLayoutParameters() {
        return new LayoutParameters();
    }

    @Override
    protected SvgParameters getSvgParameters() {
        return new SvgParameters().setInsertName(true);
    }

    @Override
    protected StyleProvider getStyleProvider() {
        return new DefaultStyleProvider();
    }

    @Test
    void testIEEE30() {
        URL url = Objects.requireNonNull(getClass().getResource("/IEEE_30_bus.xiidm"));
        Network network = Importers.loadNetwork(url.getFile());
        assertEquals(toString("/IEEE_30_bus.svg"), generateSvgString(network, "/IEEE_30_bus.svg"));
    }

    @Test
    void testIEEE14() {
        URL url = Objects.requireNonNull(getClass().getResource("/IEEE_14_bus.xiidm"));
        Network network = Importers.loadNetwork(url.getFile());
        assertEquals(toString("/IEEE_14_bus.svg"), generateSvgString(network, "/IEEE_14_bus.svg"));
    }

    @Test
    void testIEEE24() {
        URL url = Objects.requireNonNull(getClass().getResource("/IEEE_24_bus.xiidm"));
        Network network = Importers.loadNetwork(url.getFile());
        assertEquals(toString("/IEEE_24_bus.svg"), generateSvgString(network, "/IEEE_24_bus.svg"));
    }

    @Test
    void testIEEE57() {
        URL url = Objects.requireNonNull(getClass().getResource("/IEEE_57_bus.xiidm"));
        Network network = Importers.loadNetwork(url.getFile());
        assertEquals(toString("/IEEE_57_bus.svg"), generateSvgString(network, "/IEEE_57_bus.svg"));
    }

    @Test
    void testIEEE118() {
        URL url = Objects.requireNonNull(getClass().getResource("/IEEE_118_bus.xiidm"));
        Network network = Importers.loadNetwork(url.getFile());
        assertEquals(toString("/IEEE_118_bus.svg"), generateSvgString(network, "/IEEE_118_bus.svg"));
    }

}
