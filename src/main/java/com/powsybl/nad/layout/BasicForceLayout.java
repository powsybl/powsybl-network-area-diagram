/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.forcelayout.ForceLayout;
import com.powsybl.forcelayout.Vector;
import com.powsybl.nad.model.*;
import org.jgrapht.alg.util.Pair;

import java.util.Objects;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class BasicForceLayout extends AbstractLayout {

    @Override
    public void run(Graph graph, LayoutParameters layoutParameters) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(layoutParameters);

        org.jgrapht.Graph<Node, Edge> jgraphtGraph = graph.getJgraphtGraph(layoutParameters.isTextNodesForceLayout());
        ForceLayout<Node, Edge> forceLayout = new ForceLayout<>(jgraphtGraph);
        forceLayout.execute();

        jgraphtGraph.vertexSet().forEach(node -> {
            Vector p = forceLayout.getStablePosition(node);
            node.setPosition(p.getX(), p.getY());
        });

        if (!layoutParameters.isTextNodesForceLayout()) {
            graph.getTextEdgesMap().forEach(this::fixedTextNodeLayout);
        }

        edgeLayout(graph, layoutParameters);

        double[] dims = new double[4];
        jgraphtGraph.vertexSet().forEach(node -> {
            dims[0] = Math.min(dims[0], node.getX());
            dims[1] = Math.max(dims[1], node.getX());
            dims[2] = Math.min(dims[2], node.getY());
            dims[3] = Math.max(dims[3], node.getY());
        });
        graph.setDimensions(dims[0], dims[1], dims[2], dims[3]);

    }

    private void fixedTextNodeLayout(TextEdge textEdge, Pair<VoltageLevelNode, TextNode> nodes) {
        VoltageLevelNode vlNode = nodes.getFirst();
        Point fixedShift = getTextNodeFixedShift();
        nodes.getSecond().setPosition(vlNode.getX() + fixedShift.getX(), vlNode.getY() + fixedShift.getY());
    }

    protected Point getTextNodeFixedShift() {
        return new Point(1, 0);
    }
}
