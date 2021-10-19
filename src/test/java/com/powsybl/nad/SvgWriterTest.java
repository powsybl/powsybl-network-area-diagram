/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad;

import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class SvgWriterTest extends AbstractTest {

    @Test
    void testIEEE30() {
        URL url = Objects.requireNonNull(getClass().getResource("/IEEE_30_bus.xiidm"));
        Network network = Importers.loadNetwork(url.getFile());
        assertEquals(toString("/IEEE_30_bus.svg"), generateSvgString(network));
    }

}
