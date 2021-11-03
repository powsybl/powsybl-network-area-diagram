package com.powsybl.nad.layout;

import com.powsybl.nad.model.Edge;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Node;
import com.powsybl.nad.model.Point;

import java.util.Set;

public abstract class AbstractLayout implements Layout {

    protected void edgeLayout(Graph graph, LayoutParameters layoutParameters) {
        graph.getNonMultiEdgesStream().forEach(edge -> singleEdgeLayout(graph.getNode1(edge), graph.getNode2(edge), edge));
        graph.getMultiEdgesStream().forEach(edges -> multiEdgesLayout(graph, edges, layoutParameters));
    }

    private void singleEdgeLayout(Node node1, Node node2, Edge edge) {
        Point point1 = new Point(node1.getX(), node1.getY());
        Point point2 = new Point(node2.getX(), node2.getY());
        Point middle = Point.createMiddlePoint(point1, point2);
        edge.setSide1(point1, middle);
        edge.setSide2(point2, middle);
    }

    private void multiEdgesLayout(Graph graph, Set<Edge> edges, LayoutParameters layoutParameters) {
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
                singleEdgeLayout(node1, node2, edge);
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
