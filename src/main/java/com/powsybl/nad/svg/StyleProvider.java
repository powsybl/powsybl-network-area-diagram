/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.nad.model.*;

import java.util.List;
import java.util.Optional;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public interface StyleProvider {

    String CLASSES_PREFIX = "nad-";
    String VOLTAGE_LEVEL_NODES_CLASS = CLASSES_PREFIX + "vl-nodes";
    String TEXT_NODES_CLASS = CLASSES_PREFIX + "text-nodes";
    String THREE_WT_NODES_CLASS = CLASSES_PREFIX + "3wt-nodes";
    String BUSES_TEXT_CLASS = CLASSES_PREFIX + "text-buses";
    String DISCONNECTED_SIDE_EDGE_CLASS = CLASSES_PREFIX + "disconnected";
    String BRANCH_EDGES_CLASS = CLASSES_PREFIX + "branch-edges";
    String HVDC_EDGE_CLASS = CLASSES_PREFIX + "hvdc-edge";
    String THREE_WT_EDGES_CLASS = CLASSES_PREFIX + "3wt-edges";
    String TEXT_EDGES_CLASS = CLASSES_PREFIX + "text-edges";
    String EDGE_INFOS_CLASS = CLASSES_PREFIX + "edge-infos";
    String ARROW_IN_CLASS = CLASSES_PREFIX + "arrow-in";
    String ARROW_OUT_CLASS = CLASSES_PREFIX + "arrow-out";

    List<String> getCssFilenames();

    String getStyleDefs();

    List<String> getNodeStyleClasses(Node node);

    List<String> getEdgeStyleClasses(Edge edge);

    List<String> getSideEdgeStyleClasses(BranchEdge edge, BranchEdge.Side side);

    List<String> getEdgeInfoStyles(EdgeInfo info);

    Optional<String> getThreeWtNodeBackgroundStyle(ThreeWtNode threeWtNode);

    Optional<String> getThreeWtNodeStyle(ThreeWtNode threeWtNode, ThreeWtEdge.Side one);
}
