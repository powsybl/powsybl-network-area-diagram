/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.forcedlayout.ForceLayout;
import com.powsybl.forcedlayout.Vector;
import com.powsybl.nad.model.*;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class BasicForceLayout extends AbstractLayout {

    @Override
    public void run(Graph graph, LayoutParameters layoutParameters) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(layoutParameters);

        if (layoutParameters.isTextNodesForceLayout()) {
            graph.getVoltageLevelNodesStream().collect(Collectors.toList()).forEach(vlNode -> {
                TextNode textNode = vlNode.getTextNode();
                graph.addNode(textNode);
                graph.addEdge(vlNode, textNode, new TextEdge(textNode.getDiagramId() + "_edge"));
            });
        }

        ForceLayout<Node, Edge> forceLayout = new ForceLayout<>(graph.getJgraphtGraph())
                .setMaxSpeed(1e3);
        forceLayout.execute();

        graph.getNodesStream().forEach(node -> {
            Vector p = forceLayout.getStablePosition(node);
            node.setPosition(p.getX(), p.getY());
        });

        edgeLayout(graph, layoutParameters);

        double[] dims = new double[4];
        graph.getNodesStream().forEach(node -> {
            dims[0] = Math.min(dims[0], node.getX());
            dims[1] = Math.max(dims[1], node.getX());
            dims[2] = Math.min(dims[2], node.getY());
            dims[3] = Math.max(dims[3], node.getY());
        });
        graph.setDimensions(dims[0], dims[1], dims[2], dims[3]);

    }

}
