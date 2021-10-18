/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

public abstract class AbstractEdge implements Edge {

    private final String id;

    protected AbstractEdge(String id) {
        this.id = id;
    }

    @Override
    public String getDiagramId() {
        return id;
    }
}
