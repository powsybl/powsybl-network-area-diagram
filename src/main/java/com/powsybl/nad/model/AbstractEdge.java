/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import java.util.*;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractEdge implements Edge {

    private final String diagramId;
    private final String equipmentId;
    private final String name;
    private List<Point> polyline = new ArrayList<>();

    protected AbstractEdge(String diagramId, String equipmentId, String nameOrId) {
        this.diagramId = diagramId;
        this.equipmentId = equipmentId;
        this.name = nameOrId;
    }

    @Override
    public List<Point> getPolyline() {
        return Collections.unmodifiableList(polyline);
    }

    @Override
    public void setPolyline(List<Point> polyline) {
        this.polyline = new ArrayList<>(polyline);
    }

    @Override
    public String getDiagramId() {
        return diagramId;
    }

    @Override
    public String getEquipmentId() {
        return equipmentId;
    }

    @Override
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }
}
