/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.sld.styles.BaseVoltageStyle;

import java.util.Collections;
import java.util.List;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class DefaultStyleProvider extends AbstractStyleProvider {

    public DefaultStyleProvider() {
        super();
    }

    public DefaultStyleProvider(BaseVoltageStyle baseVoltageStyle) {
        super(baseVoltageStyle);
    }

    @Override
    public List<String> getCssFilenames() {
        return Collections.singletonList("defaultStyle.css");
    }
}
