/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Point;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public interface Layout {
    Map<String, Point> run(Graph graph, LayoutParameters layoutParameters);

    default void setInitialNodePositions(Map<String, Point> initialNodePositions) {
    }

    default void setNodesWithFixedPosition(Set<String> nodesWithFixedPosition) {
    }

    default void setFixedNodePositions(Map<String, Point> fixedNodePositions) {
        setInitialNodePositions(fixedNodePositions);
        setNodesWithFixedPosition(fixedNodePositions.keySet());
    }

    default Map<String, Point> getInitialNodePositions() {
        return Collections.emptyMap();
    }

    default Set<String> getNodesWithFixedPosition() {
        return Collections.emptySet();
    }
}
