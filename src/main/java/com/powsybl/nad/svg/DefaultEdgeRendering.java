/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.nad.model.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class DefaultEdgeRendering implements EdgeRendering {

    @Override
    public void run(Graph graph, SvgParameters svgParameters) {
        graph.getNonMultiBranchEdgesStream().forEach(edge -> computeSingleBranchEdgeCoordinates(graph, edge, svgParameters));
        graph.getMultiBranchEdgesStream().forEach(edges -> computeMultiBranchEdgesCoordinates(graph, edges, svgParameters));
        graph.getLoopBranchEdgesMap().forEach((node, edges) -> loopEdgesLayout(graph, node, edges, svgParameters));
        graph.getThreeWtEdgesStream().forEach(edge -> computeThreeWtEdgeCoordinates(graph, edge, svgParameters));
        graph.getTextEdgesMap().forEach((edge, nodes) -> computeTextEdgeLayoutCoordinates(nodes.getFirst(), nodes.getSecond(), edge));
    }

    private void computeTextEdgeLayoutCoordinates(Node node1, Node node2, TextEdge edge) {
        edge.setPoints(node1.getPosition(), node2.getPosition());
    }

    private void computeSingleBranchEdgeCoordinates(Graph graph, BranchEdge edge, SvgParameters svgParameters) {
        Node node1 = graph.getBusGraphNode1(edge);
        Node node2 = graph.getBusGraphNode2(edge);

        Point direction1 = getDirection(node2, () -> graph.getNode2(edge));
        Point edgeStart1 = computeEdgeStart(node1, direction1, graph.getVoltageLevelNode1(edge), svgParameters);

        Point direction2 = getDirection(node1, () -> graph.getNode1(edge));
        Point edgeStart2 = computeEdgeStart(node2, direction2, graph.getVoltageLevelNode2(edge), svgParameters);

        Point middle = Point.createMiddlePoint(edgeStart1, edgeStart2);
        if (edge.getType().equals(BranchEdge.TWO_WT_EDGE)) {
            double radius = svgParameters.getTransformerCircleRadius();
            edge.setPoints1(edgeStart1, middle.atDistance(1.5 * radius, direction2));
            edge.setPoints2(edgeStart2, middle.atDistance(1.5 * radius, direction1));
        } else {
            edge.setPoints1(edgeStart1, middle);
            edge.setPoints2(edgeStart2, middle);
        }
    }

    private Point getDirection(Node directionBusGraphNode, Supplier<Node> vlNodeSupplier) {
        if (directionBusGraphNode == BusNode.UNKNOWN) {
            return vlNodeSupplier.get().getPosition();
        }
        return directionBusGraphNode.getPosition();
    }

    private Point computeEdgeStart(Node node, Point direction, VoltageLevelNode vlNode, SvgParameters svgParameters) {
        // If edge not connected to a bus node on that side, we use corresponding voltage level with specific extra radius
        if (node == BusNode.UNKNOWN && vlNode != null) {
            double unknownBusRadius = SvgWriter.getVoltageLevelCircleRadius(vlNode, svgParameters) + svgParameters.getUnknownBusNodeExtraRadius();
            return vlNode.getPosition().atDistance(unknownBusRadius, direction);
        }

        Point edgeStart = node.getPosition();
        if (node instanceof BusNode && vlNode != null) {
            double busAnnulusOuterRadius = SvgWriter.getBusAnnulusOuterRadius((BusNode) node, vlNode, svgParameters);
            edgeStart = edgeStart.atDistance(busAnnulusOuterRadius - svgParameters.getEdgeStartShift(), direction);
        }
        return edgeStart;
    }

    private void computeMultiBranchEdgesCoordinates(Graph graph, List<BranchEdge> edges, SvgParameters svgParameters) {
        BranchEdge firstEdge = edges.iterator().next();
        VoltageLevelNode nodeA = graph.getVoltageLevelNode1(firstEdge);
        VoltageLevelNode nodeB = graph.getVoltageLevelNode2(firstEdge);
        Point pointA = nodeA.getPosition();
        Point pointB = nodeB.getPosition();

        double dx = pointB.getX() - pointA.getX();
        double dy = pointB.getY() - pointA.getY();
        double angle = Math.atan2(dy, dx);

        int nbForks = edges.size();
        double forkAperture = svgParameters.getEdgesForkAperture();
        double forkLength = svgParameters.getEdgesForkLength();
        double angleStep = forkAperture / (nbForks - 1);

        int i = 0;
        for (BranchEdge edge : edges) {
            if (2 * i + 1 == nbForks) { // in the middle, hence alpha = 0
                computeSingleBranchEdgeCoordinates(graph, edge, svgParameters);
            } else {
                double alpha = -forkAperture / 2 + i * angleStep;
                double angleForkA = angle - alpha;
                double angleForkB = angle + Math.PI + alpha;
                Point forkA = pointA.shift(forkLength * Math.cos(angleForkA), forkLength * Math.sin(angleForkA));
                Point forkB = pointB.shift(forkLength * Math.cos(angleForkB), forkLength * Math.sin(angleForkB));
                Point middle = Point.createMiddlePoint(forkA, forkB);

                BranchEdge.Side sideA = graph.getNode1(edge) == nodeA ? BranchEdge.Side.ONE : BranchEdge.Side.TWO;
                BranchEdge.Side sideB = sideA.getOpposite();

                Node busNodeA = sideA == BranchEdge.Side.ONE ? graph.getBusGraphNode1(edge) : graph.getBusGraphNode2(edge);
                Node busNodeB = sideA == BranchEdge.Side.ONE ? graph.getBusGraphNode2(edge) : graph.getBusGraphNode1(edge);

                Point edgeStartA = computeEdgeStart(busNodeA, forkA, nodeA, svgParameters);
                edge.setPoints(sideA, edgeStartA, forkA, middle);

                Point edgeStartB = computeEdgeStart(busNodeB, forkB, nodeB, svgParameters);
                edge.setPoints(sideB, edgeStartB, forkB, middle);
            }
            i++;
        }
    }

    private void loopEdgesLayout(Graph graph, VoltageLevelNode node, List<BranchEdge> loopEdges, SvgParameters svgParameters) {
        List<Double> angles = computeLoopAngles(graph, loopEdges, node, svgParameters);

        int i = 0;
        Point nodePoint = node.getPosition();
        for (BranchEdge edge : loopEdges) {
            double angle = angles.get(i++);
            Point middle = nodePoint.atDistance(svgParameters.getLoopDistance(), angle);
            Point fork1 = nodePoint.atDistance(svgParameters.getEdgesForkLength(), angle - svgParameters.getLoopEdgesAperture() / 2);
            Point fork2 = nodePoint.atDistance(svgParameters.getEdgesForkLength(), angle + svgParameters.getLoopEdgesAperture() / 2);

            Node busNode1 = graph.getBusGraphNode1(edge);
            Node busNode2 = graph.getBusGraphNode2(edge);

            Point edgeStart1 = computeEdgeStart(busNode1, fork1, node, svgParameters);
            edge.setPoints(BranchEdge.Side.ONE, edgeStart1, fork1, middle);

            Point edgeStart2 = computeEdgeStart(busNode2, fork2, node, svgParameters);
            edge.setPoints(BranchEdge.Side.TWO, edgeStart2, fork2, middle);
        }
    }

    private List<Double> computeLoopAngles(Graph graph, List<BranchEdge> loopEdges, Node node, SvgParameters svgParameters) {
        int nbLoops = loopEdges.size();

        List<Double> anglesOtherEdges = graph.getBranchEdgeStream(node)
                .filter(e -> !loopEdges.contains(e))
                .mapToDouble(e -> getAngle(e, graph, node))
                .sorted().boxed().collect(Collectors.toList());

        List<Double> loopAngles = new ArrayList<>();
        if (anglesOtherEdges.size() > 0) {
            anglesOtherEdges.add(anglesOtherEdges.get(0) + 2 * Math.PI);
            double apertureWithMargin = svgParameters.getLoopEdgesAperture() * 1.2;

            double[] deltaAngles = new double[anglesOtherEdges.size() - 1];
            int nbSeparatedSlots = 0;
            int nbSharedSlots = 0;
            for (int i = 0; i < anglesOtherEdges.size() - 1; i++) {
                deltaAngles[i] = anglesOtherEdges.get(i + 1) - anglesOtherEdges.get(i);
                nbSeparatedSlots += deltaAngles[i] > apertureWithMargin ? 1 : 0;
                nbSharedSlots += Math.floor(deltaAngles[i] / apertureWithMargin);
            }

            List<Integer> sortedIndices = IntStream.range(0, deltaAngles.length)
                    .boxed().sorted(Comparator.comparingDouble(i -> deltaAngles[i]))
                    .collect(Collectors.toList());

            if (nbLoops <= nbSeparatedSlots) {
                // Place loops in "slots" separated by non-loop edges
                for (int i = sortedIndices.size() - nbLoops; i < sortedIndices.size(); i++) {
                    int iSorted = sortedIndices.get(i);
                    loopAngles.add((anglesOtherEdges.get(iSorted) + anglesOtherEdges.get(iSorted + 1)) / 2);
                }
            } else if (nbLoops <= nbSharedSlots) {
                // Place the maximum of loops in "slots" separated by non-loop edges, and put the excessive ones in the bigger "slots"
                int nbExcessiveRemaining = nbLoops - nbSeparatedSlots;
                for (int i = sortedIndices.size() - 1; i >= 0; i--) {
                    int iSorted = sortedIndices.get(i);
                    int nbAvailableSlots = (int) Math.floor(deltaAngles[iSorted] / apertureWithMargin);
                    if (nbAvailableSlots == 0) {
                        break;
                    }
                    int nbLoopsInDelta = Math.min(nbAvailableSlots, nbExcessiveRemaining + 1);
                    double extraSpace = deltaAngles[iSorted] - svgParameters.getLoopEdgesAperture() * nbLoopsInDelta; // extra space without margins
                    double intraSpace = extraSpace / (nbLoopsInDelta + 1); // space between two loops and between non-loop edges and first/last loop
                    double angleStep = (anglesOtherEdges.get(iSorted + 1) - anglesOtherEdges.get(iSorted) - intraSpace) / nbLoopsInDelta;
                    double startAngle = anglesOtherEdges.get(iSorted) + intraSpace / 2 + angleStep / 2;
                    IntStream.range(0, nbLoopsInDelta).mapToDouble(iLoop -> startAngle + iLoop * angleStep).forEach(loopAngles::add);
                    nbExcessiveRemaining -= nbLoopsInDelta - 1;
                }
            } else {
                // Not enough place in the slots: dividing the circle in nbLoops, starting in the middle of the biggest slot
                int iMaxDelta = sortedIndices.get(sortedIndices.size() - 1);
                double startAngle = (anglesOtherEdges.get(iMaxDelta) + anglesOtherEdges.get(iMaxDelta + 1)) / 2;
                IntStream.range(0, nbLoops).mapToDouble(i -> startAngle + i * 2 * Math.PI / nbLoops).forEach(loopAngles::add);
            }

        } else {
            // No other edges: dividing the circle in nbLoops
            IntStream.range(0, nbLoops).mapToDouble(i -> i * 2 * Math.PI / nbLoops).forEach(loopAngles::add);
        }

        return loopAngles;
    }

    private double getAngle(BranchEdge edge, Graph graph, Node node) {
        BranchEdge.Side side = graph.getNode1(edge) == node ? BranchEdge.Side.ONE : BranchEdge.Side.TWO;
        return edge.getEdgeStartAngle(side);
    }

    private void computeThreeWtEdgeCoordinates(Graph graph, ThreeWtEdge edge, SvgParameters svgParameters) {
        Node node1 = graph.getBusGraphNode1(edge);
        Node node2 = graph.getBusGraphNode2(edge);

        Point direction1 = getDirection(node2, () -> graph.getNode2(edge));
        Point edgeStart1 = computeEdgeStart(node1, direction1, graph.getVoltageLevelNode(edge), svgParameters);

        Point direction2 = getDirection(node1, () -> graph.getNode1(edge));
        Point edgeStart2 = computeEdgeStart(node2, direction2, null, svgParameters);

        edge.setPoints(edgeStart1, edgeStart2);
    }
}
