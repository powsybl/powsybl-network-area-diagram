package com.powsybl.nad.layout;

import com.powsybl.nad.model.*;

import java.util.Objects;
import java.util.Set;

public abstract class AbstractLayout implements Layout {

    protected void edgeLayout(Graph graph, LayoutParameters layoutParameters) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(layoutParameters);
        graph.getNonMultiBranchEdgesStream().forEach(edge -> singleBranchEdgeLayout(graph.getNode1(edge), graph.getNode2(edge), edge));
        graph.getMultiBranchEdgesStream().forEach(edges -> multiBranchEdgesLayout(graph, edges, layoutParameters));
        graph.getThreeWtEdgesStream().forEach(edge -> threeWtEdgeLayout(graph.getNode1(edge), graph.getNode2(edge), edge));
        graph.getTextEdgesMap().forEach((edge, nodes) -> textEdgeLayout(nodes.getFirst(), nodes.getSecond(), edge));
    }

    protected void textEdgeLayout(Node node1, Node node2, TextEdge edge) {
        Point point1 = new Point(node1.getX(), node1.getY());
        Point point2 = new Point(node2.getX(), node2.getY());
        edge.setPoints(point1, point2);
    }

    private void singleBranchEdgeLayout(Node node1, Node node2, BranchEdge edge) {
        Point point1 = new Point(node1.getX(), node1.getY());
        Point point2 = new Point(node2.getX(), node2.getY());
        Point middle = Point.createMiddlePoint(point1, point2);
        edge.setSide1(point1, middle);
        edge.setSide2(point2, middle);
        if (node1 instanceof VoltageLevelNode && !((VoltageLevelNode) node1).isVisible()) {
            edge.setVisible(BranchEdge.Side.ONE, false);
        }
        if (node2 instanceof VoltageLevelNode && !((VoltageLevelNode) node2).isVisible()) {
            edge.setVisible(BranchEdge.Side.TWO, false);
        }
    }

    private void multiBranchEdgesLayout(Graph graph, Set<Edge> edges, LayoutParameters layoutParameters) {
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
            if (!(edge instanceof BranchEdge)) {
                continue;
            }
            BranchEdge branchEdge = (BranchEdge) edge;
            if (2 * i + 1 == nbForks) { // in the middle, hence alpha = 0
                singleBranchEdgeLayout(node1, node2, branchEdge);
            } else {
                double alpha = -forkAperture / 2 + i * angleStep;
                double angleFork1 = angle - alpha;
                double angleFork2 = angle + Math.PI + alpha;
                Point fork1 = pointA.shift(forkLength * Math.cos(angleFork1), forkLength * Math.sin(angleFork1));
                Point fork2 = pointB.shift(forkLength * Math.cos(angleFork2), forkLength * Math.sin(angleFork2));

                Point middle = Point.createMiddlePoint(fork1, fork2);
                branchEdge.setSide1(pointA, fork1, middle);
                branchEdge.setSide2(pointB, fork2, middle);
                if (node2 instanceof VoltageLevelNode && !((VoltageLevelNode) node2).isVisible()) {
                    branchEdge.setVisible(BranchEdge.Side.TWO, false);
                }
            }
            i++;
        }
    }

    private void threeWtEdgeLayout(Node node1, Node node2, ThreeWtEdge edge) {
        Point point1 = new Point(node1.getX(), node1.getY());
        Point point2 = new Point(node2.getX(), node2.getY());
        edge.setPoints(point1, point2);
    }
}
