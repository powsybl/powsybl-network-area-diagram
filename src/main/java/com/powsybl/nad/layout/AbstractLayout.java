package com.powsybl.nad.layout;

import com.powsybl.nad.model.*;

import java.util.Objects;
import java.util.Set;

public abstract class AbstractLayout implements Layout {

    protected void edgeLayout(Graph graph, LayoutParameters layoutParameters) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(layoutParameters);
        graph.getNonMultiBranchEdgesStream().forEach(edge -> singleBranchEdgeLayout(graph, edge));
        graph.getMultiBranchEdgesStream().forEach(edges -> multiBranchEdgesLayout(graph, edges, layoutParameters));
        graph.getThreeWtEdgesStream().forEach(edge -> threeWtEdgeLayout(graph.getNode1(edge), graph.getNode2(edge), edge));
        graph.getTextEdgesMap().forEach((edge, nodes) -> textEdgeLayout(nodes.getFirst(), nodes.getSecond(), edge));
    }

    protected void textEdgeLayout(Node node1, Node node2, TextEdge edge) {
        Point point1 = new Point(node1.getX(), node1.getY());
        Point point2 = new Point(node2.getX(), node2.getY());
        edge.setPoints(point1, point2);
    }

    private void singleBranchEdgeLayout(Graph graph, BranchEdge edge) {
        Node node1 = graph.getNode1(edge);
        Node node2 = graph.getNode2(edge);
        Point point1 = new Point(node1.getX(), node1.getY());
        Point point2 = new Point(node2.getX(), node2.getY());
        Point middle = Point.createMiddlePoint(point1, point2);
        edge.setPoints1(point1, middle);
        edge.setPoints2(point2, middle);
        setEdgeVisibility(node1, edge, BranchEdge.Side.ONE);
        setEdgeVisibility(node2, edge, BranchEdge.Side.TWO);
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
                singleBranchEdgeLayout(graph, branchEdge);
            } else {
                double alpha = -forkAperture / 2 + i * angleStep;
                double angleFork1 = angle - alpha;
                double angleFork2 = angle + Math.PI + alpha;
                Point fork1 = pointA.shift(forkLength * Math.cos(angleFork1), forkLength * Math.sin(angleFork1));
                Point fork2 = pointB.shift(forkLength * Math.cos(angleFork2), forkLength * Math.sin(angleFork2));

                Point middle = Point.createMiddlePoint(fork1, fork2);
                BranchEdge.Side side = graph.getNode1(edge) == node1 ? BranchEdge.Side.ONE : BranchEdge.Side.TWO;
                branchEdge.setPoints(side, pointA, fork1, middle);
                branchEdge.setPoints(side.getOpposite(), pointB, fork2, middle);
                setEdgeVisibility(node1, branchEdge, BranchEdge.Side.ONE);
                setEdgeVisibility(node2, branchEdge, BranchEdge.Side.TWO);
            }
            i++;
        }
    }

    private void setEdgeVisibility(Node node, BranchEdge branchEdge, BranchEdge.Side side) {
        if (node instanceof VoltageLevelNode && !((VoltageLevelNode) node).isVisible()) {
            branchEdge.setVisible(side, false);
        }
    }

    private void threeWtEdgeLayout(Node node1, Node node2, ThreeWtEdge edge) {
        Point point1 = new Point(node1.getX(), node1.getY());
        Point point2 = new Point(node2.getX(), node2.getY());
        edge.setPoints(point1, point2);
    }
}
