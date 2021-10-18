/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import java.util.Collections;
import java.util.List;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TextNode extends AbstractNode {

    private final String text;
    private TextEdge edge;

    protected TextNode(String diagramId, String text) {
        super(diagramId, null);
        this.text = text;
    }

    @Override
    public List<TextEdge> getAdjacentEdges() {
        return Collections.singletonList(edge);
    }

    public void setEdge(TextEdge edge) {
        this.edge =  edge;
    }

    public String getText() {
        return text;
    }
}
