/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import java.util.Arrays;
import java.util.List;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TextEdge implements Edge {

    private final String id;

    private VoltageLevelNode vlNode;
    private TextNode textNode;

    public TextEdge(String id) {
        this.id = id;
    }

    @Override
    public String getDiagramId() {
        return id;
    }

    @Override
    public List<Node> getAdjacentNodes() {
        return Arrays.asList(vlNode, textNode);
    }

    public VoltageLevelNode getVoltageLevelNode() {
        return vlNode;
    }

    public TextNode getTextNode() {
        return textNode;
    }

}
