/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.nad.model.Edge;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Node;
import com.powsybl.nad.model.Point;
import com.powsybl.sld.force.layout.ForceLayout;
import com.powsybl.sld.force.layout.Vector;

import java.util.Set;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class BasicForceLayout implements Layout {

    @Override
    public void run(Graph graph, LayoutParameters layoutParameters) {
        ForceLayout<Node, Edge> forceLayout = new ForceLayout<>(graph.getJgraphtGraph())
                .setMaxSpeed(1e3);
        forceLayout.execute();

        double[] dims = new double[4];
        graph.getNodesStream().forEach(node -> {
            Vector p = forceLayout.getStablePosition(node);
            node.setPosition(p.getX(), p.getY());
            dims[0] = Math.min(dims[0], p.getX());
            dims[1] = Math.max(dims[1], p.getX());
            dims[2] = Math.min(dims[2], p.getY());
            dims[3] = Math.max(dims[3], p.getY());
        });
        graph.setDimensions(dims[0], dims[1], dims[2], dims[3]);

        graph.getNonMultiEdgesStream().forEach(edge -> drawStraightEdge(graph.getNode1(edge), graph.getNode2(edge), edge));
        graph.getMultiEdgesStream().forEach(edges -> drawForkEdges(graph, edges, layoutParameters));
    }

    private void drawStraightEdge(Node node1, Node node2, Edge edge) {
        Point point1 = new Point(node1.getX(), node1.getY());
        Point point2 = new Point(node2.getX(), node2.getY());
        Point middle = Point.createMiddlePoint(point1, point2);
        edge.setSide1(point1, middle);
        edge.setSide2(point2, middle);
    }

    private void drawForkEdges(Graph graph, Set<Edge> edges, LayoutParameters layoutParameters) {
        Edge firstEdge = edges.iterator().next();
        Node node1 = graph.getNode1(firstEdge);
        Node node2 = graph.getNode2(firstEdge);
        Point pointA = new Point(node1.getX(), node1.getY());
        Point pointB = new Point(node2.getX(), node2.getY());

        double dx = pointB.getX() - pointA.getX();
        double dy = pointB.getY() - pointA.getY();
        double angle = Math.atan2(dy, dx);

        int nbForks = edges.size();
        double forkAperture = layoutParameters.getEdgesForkAperture();
        double forkLength = layoutParameters.getEdgesForkLength();
        double angleStep = forkAperture / (nbForks - 1);

        int i = 0;
        for (Edge edge : edges) {
            if (2 * i + 1 == nbForks) { // in the middle, hence alpha = 0
                drawStraightEdge(node1, node2, edge);
            } else {
                double alpha = -forkAperture / 2 + i * angleStep;
                double angleFork1 = angle - alpha;
                double angleFork2 = angle + Math.PI + alpha;
                Point fork1 = new Point(forkLength * Math.cos(angleFork1), forkLength * Math.sin(angleFork1)).shift(pointA);
                Point fork2 = new Point(forkLength * Math.cos(angleFork2), forkLength * Math.sin(angleFork2)).shift(pointB);

                Point middle = Point.createMiddlePoint(fork1, fork2);
                edge.setSide1(pointA, fork1, middle);
                edge.setSide2(pointB, fork2, middle);
            }
            i++;
        }
    }
}
