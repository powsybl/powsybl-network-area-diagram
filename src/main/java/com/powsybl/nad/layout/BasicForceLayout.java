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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class BasicForceLayout extends AbstractLayout {

    @Override
    protected void nodesLayout(Graph graph, LayoutParameters layoutParameters) {
        org.jgrapht.Graph<Node, Edge> jgraphtGraph = graph.getJgraphtGraph(layoutParameters.isTextNodesForceLayout());
        ForceLayout<Node, Edge> forceLayout = new ForceLayout<>(jgraphtGraph);
        forceLayout.setSpringRepulsionFactor(layoutParameters.getSpringRepulsionFactorForceLayout());

        setInitialPositions(forceLayout, graph);
        forceLayout.setFixedNodes(getNodesWithFixedPosition().stream()
                .map(graph::getNode)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet()));

        forceLayout.execute();

        jgraphtGraph.vertexSet().forEach(node -> {
            Vector p = forceLayout.getStablePosition(node);
            node.setPosition(p.getX(), p.getY());
        });

        if (!layoutParameters.isTextNodesForceLayout()) {
            graph.getTextEdgesMap().forEach(this::fixedTextNodeLayout);
        }
    }

    private void setInitialPositions(ForceLayout<Node, Edge> forceLayout, Graph graph) {
        Map<Node, com.powsybl.forcelayout.Point> initialPoints = getInitialNodePositions().entrySet().stream()
                // Only accept positions for nodes in the graph
                .filter(nodePosition -> graph.getNode(nodePosition.getKey()).isPresent())
                .collect(Collectors.toMap(
                        nodePosition -> graph.getNode(nodePosition.getKey()).orElseThrow(),
                        nodePosition -> new com.powsybl.forcelayout.Point(nodePosition.getValue().getX(), nodePosition.getValue().getY()),
                        // If same node has two points, keep the first one considered
                        (point1, point2) -> point1
                ));
        forceLayout.setInitialPoints(initialPoints);
    }

    @Override
    protected void busNodesLayout(Graph graph, LayoutParameters layoutParameters) {
        Comparator<BusNode> c = Comparator.comparing(bn -> graph.getBusEdges(bn).size());
        graph.getVoltageLevelNodesStream().forEach(n -> {
            n.sortBusNodes(c);
            List<BusNode> sortedNodes = n.getBusNodes();
            for (int i = 0; i < sortedNodes.size(); i++) {
                BusNode busNode = sortedNodes.get(i);
                busNode.setIndex(i);
                busNode.setNbNeighbouringBusNodes(sortedNodes.size() - 1);
                busNode.setPosition(n.getPosition());
            }
        });
    }

    private void fixedTextNodeLayout(TextEdge textEdge, Pair<VoltageLevelNode, TextNode> nodes) {
        Point fixedShift = getTextNodeFixedShift();
        Point textPos = nodes.getFirst().getPosition().shift(fixedShift.getX(), fixedShift.getY());
        nodes.getSecond().setPosition(textPos);
    }

    protected Point getTextNodeFixedShift() {
        return new Point(1, 0);
    }
}
