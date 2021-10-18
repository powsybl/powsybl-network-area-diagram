/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class BusNode {
    private final String diagramId;
    private final String id;

    public BusNode(String diagramId, String id) {
        this.diagramId = diagramId;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getDiagramId() {
        return diagramId;
    }
}
