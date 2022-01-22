/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.nad.model.*;
import org.apache.commons.io.output.WriterOutputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class SvgWriter {

    private static final String INDENT = "    ";
    public static final String NAMESPACE_URI = "http://www.w3.org/2000/svg";
    private static final String SVG_ROOT_ELEMENT_NAME = "svg";
    private static final String STYLE_ELEMENT_NAME = "style";
    private static final String METADATA_ELEMENT_NAME = "metadata";
    private static final String GROUP_ELEMENT_NAME = "g";
    private static final String POLYLINE_ELEMENT_NAME = "polyline";
    private static final String PATH_ELEMENT_NAME = "path";
    private static final String CIRCLE_ELEMENT_NAME = "circle";
    private static final String TEXT_ELEMENT_NAME = "text";
    private static final String ID_ATTRIBUTE = "id";
    private static final String WIDTH_ATTRIBUTE = "width";
    private static final String HEIGHT_ATTRIBUTE = "height";
    private static final String VIEW_BOX_ATTRIBUTE = "viewBox";
    private static final String TITLE_ATTRIBUTE = "title";
    private static final String CLASS_ATTRIBUTE = "class";
    private static final String TRANSFORM_ATTRIBUTE = "transform";
    private static final String CIRCLE_RADIUS_ATTRIBUTE = "r";
    private static final String PATH_D_ATTRIBUTE = "d";
    private static final String X_ATTRIBUTE = "x";
    private static final String Y_ATTRIBUTE = "y";
    private static final String POINTS_ATTRIBUTE = "points";

    private final SvgParameters svgParameters;
    private final StyleProvider styleProvider;
    private final LabelProvider labelProvider;

    public SvgWriter(SvgParameters svgParameters, StyleProvider styleProvider, LabelProvider labelProvider) {
        this.svgParameters = Objects.requireNonNull(svgParameters);
        this.styleProvider = Objects.requireNonNull(styleProvider);
        this.labelProvider = Objects.requireNonNull(labelProvider);
    }

    public void writeSvg(Graph graph, Path svgFile) {
        Objects.requireNonNull(svgFile);
        Path dir = svgFile.toAbsolutePath().getParent();
        String svgFileName = svgFile.getFileName().toString();
        if (!svgFileName.endsWith(".svg")) {
            svgFileName = svgFileName + ".svg";
        }
        try (OutputStream svgOs = new BufferedOutputStream(Files.newOutputStream(dir.resolve(svgFileName)))) {
            writeSvg(graph, svgOs);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeSvg(Graph graph, Writer svgWriter) {
        try (WriterOutputStream svgOs = new WriterOutputStream(svgWriter, StandardCharsets.UTF_8)) {
            writeSvg(graph, svgOs);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeSvg(Graph graph, OutputStream svgOs) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(svgOs);

        // Edge coordinates need to be computed first, based on svg parameters
        computeBranchEdgesCoordinates(graph);

        try {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, INDENT, svgOs);
            addSvgRoot(graph, writer);
            addStyle(writer);
            addMetadata(writer);
            drawBranchEdges(graph, writer);
            drawThreeWtEdges(graph, writer);
            drawVoltageLevelNodes(graph, writer);
            drawThreeWtNodes(graph, writer);
            drawTextEdges(graph, writer);
            drawTextNodes(graph, writer);
            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private void computeBranchEdgesCoordinates(Graph graph) {
        graph.getNonMultiBranchEdgesStream().forEach(edge -> computeSingleBranchEdgeCoordinates(graph, edge));
        graph.getMultiBranchEdgesStream().forEach(edges -> computeMultiBranchEdgesCoordinates(graph, edges));
        graph.getThreeWtEdgesStream().forEach(edge -> computeThreeWtEdgeCoordinates(graph.getNode1(edge), graph.getNode2(edge), edge));
        graph.getTextEdgesMap().forEach((edge, nodes) -> computeTextEdgeLayoutCoordinates(nodes.getFirst(), nodes.getSecond(), edge));
    }

    private void computeTextEdgeLayoutCoordinates(Node node1, Node node2, TextEdge edge) {
        Point point1 = new Point(node1.getX(), node1.getY());
        Point point2 = new Point(node2.getX(), node2.getY());
        edge.setPoints(point1, point2);
    }

    private void computeSingleBranchEdgeCoordinates(Graph graph, BranchEdge edge) {
        Node node1 = graph.getNode1(edge);
        Node node2 = graph.getNode2(edge);
        Point point1 = new Point(node1.getX(), node1.getY());
        Point point2 = new Point(node2.getX(), node2.getY());
        Point middle = Point.createMiddlePoint(point1, point2);
        edge.setPoints1(point1, middle);
        edge.setPoints2(point2, middle);
    }

    private void computeMultiBranchEdgesCoordinates(Graph graph, Set<Edge> edges) {
        Edge firstEdge = edges.iterator().next();
        Node node1 = graph.getNode1(firstEdge);
        Node node2 = graph.getNode2(firstEdge);
        Point pointA = new Point(node1.getX(), node1.getY());
        Point pointB = new Point(node2.getX(), node2.getY());

        double dx = pointB.getX() - pointA.getX();
        double dy = pointB.getY() - pointA.getY();
        double angle = Math.atan2(dy, dx);

        int nbForks = edges.size();
        double forkAperture = svgParameters.getEdgesForkAperture();
        double forkLength = svgParameters.getEdgesForkLength();
        double angleStep = forkAperture / (nbForks - 1);

        int i = 0;
        for (Edge edge : edges) {
            if (!(edge instanceof BranchEdge)) {
                continue;
            }
            BranchEdge branchEdge = (BranchEdge) edge;
            if (2 * i + 1 == nbForks) { // in the middle, hence alpha = 0
                computeSingleBranchEdgeCoordinates(graph, branchEdge);
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
            }
            i++;
        }
    }

    private void computeThreeWtEdgeCoordinates(Node node1, Node node2, ThreeWtEdge edge) {
        Point point1 = new Point(node1.getX(), node1.getY());
        Point point2 = new Point(node2.getX(), node2.getY());
        edge.setPoints(point1, point2);
    }

    private void drawBranchEdges(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.BRANCH_EDGES_CLASS);
        for (BranchEdge edge : graph.getBranchEdges()) {
            writer.writeStartElement(GROUP_ELEMENT_NAME);
            writer.writeAttribute(ID_ATTRIBUTE, edge.getDiagramId());
            addStylesIfAny(writer, styleProvider.getEdgeStyleClasses(edge));
            insertName(writer, edge::getName);

            drawHalfEdge(graph, writer, edge, BranchEdge.Side.ONE);
            drawHalfEdge(graph, writer, edge, BranchEdge.Side.TWO);

            if (edge.getType().equals(BranchEdge.HVDC_LINE_EDGE)) {
                drawConverterStation(writer, edge);
            }

            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void drawConverterStation(XMLStreamWriter writer, BranchEdge edge) throws XMLStreamException {
        writer.writeEmptyElement(POLYLINE_ELEMENT_NAME);
        List<Point> line1 = edge.getPoints(BranchEdge.Side.ONE);
        List<Point> line2 = edge.getPoints(BranchEdge.Side.TWO);
        List<Point> points = new ArrayList<>(2);
        double halfWidth = svgParameters.getConverterStationWidth() / 2;
        if (line1.size() > 2) {
            points.add(line1.get(2).atDistance(halfWidth, line1.get(1)));
            points.add(line2.get(2).atDistance(halfWidth, line2.get(1)));
        } else {
            points.add(line1.get(1).atDistance(halfWidth, line1.get(0)));
            points.add(line2.get(1).atDistance(halfWidth, line2.get(0)));
        }
        String lineFormatted = points.stream()
                .map(point -> getFormattedValue(point.getX()) + "," + getFormattedValue(point.getY()))
                .collect(Collectors.joining(" "));
        writer.writeAttribute(POINTS_ATTRIBUTE, lineFormatted);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.HVDC_CLASS);
    }

    private void drawThreeWtEdges(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        List<ThreeWtEdge> threeWtEdges = graph.getThreeWtEdges();
        if (threeWtEdges.isEmpty()) {
            return;
        }

        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.THREE_WT_EDGES_CLASS);
        for (ThreeWtEdge edge : threeWtEdges) {
            if (!edge.isVisible()) {
                continue;
            }
            drawThreeWtEdge(graph, writer, edge);
        }
        writer.writeEndElement();
    }

    private void drawHalfEdge(Graph graph, XMLStreamWriter writer, BranchEdge edge, BranchEdge.Side side) throws XMLStreamException {
        // the half edge is only drawn if visible, but if the edge is a TwoWtEdge, the transformer is still drawn
        if (!edge.isVisible(side) && !(edge.getType().equals(BranchEdge.TWO_WT_EDGE))) {
            return;
        }
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        addStylesIfAny(writer, styleProvider.getSideEdgeStyleClasses(edge, side));
        writer.writeEmptyElement(POLYLINE_ELEMENT_NAME);
        List<Point> half = edge.getPoints(side);
        if (edge.isVisible(side)) {
            String lineFormatted = half.stream()
                .map(point -> getFormattedValue(point.getX()) + "," + getFormattedValue(point.getY()))
                .collect(Collectors.joining(" "));
            writer.writeAttribute(POINTS_ATTRIBUTE, lineFormatted);
            drawEdgeInfo(writer, half, labelProvider.getEdgeInfos(graph, edge, side));
        }
        if (edge.getType().equals(BranchEdge.TWO_WT_EDGE)) {
            draw2WtWinding(writer, half);
        }
        writer.writeEndElement();
    }

    private void drawThreeWtEdge(Graph graph, XMLStreamWriter writer, ThreeWtEdge edge) throws XMLStreamException {
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(ID_ATTRIBUTE, edge.getDiagramId());
        addStylesIfAny(writer, styleProvider.getEdgeStyleClasses(edge));
        insertName(writer, edge::getName);
        writer.writeEmptyElement(POLYLINE_ELEMENT_NAME);
        List<Point> points = edge.getPoints();
        String lineFormatted = points.stream()
                .map(point -> getFormattedValue(point.getX()) + "," + getFormattedValue(point.getY()))
                .collect(Collectors.joining(" "));
        writer.writeAttribute(POINTS_ATTRIBUTE, lineFormatted);
        drawEdgeInfo(writer, points, labelProvider.getEdgeInfos(graph, edge));
        writer.writeEndElement();
    }

    private void drawThreeWtNodes(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        List<ThreeWtNode> threeWtNodes = graph.getThreeWtNodesStream().collect(Collectors.toList());
        if (threeWtNodes.isEmpty()) {
            return;
        }

        double dNodeCenter = svgParameters.getTransformerCircleRadius() * 0.6;
        Point point1 = Point.createPointFromRhoTheta(dNodeCenter, 90);
        Point point2 = Point.createPointFromRhoTheta(dNodeCenter, 210);
        Point point3 = Point.createPointFromRhoTheta(dNodeCenter, 330);

        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.THREE_WT_NODES_CLASS);
        for (ThreeWtNode threeWtNode : threeWtNodes) {
            writer.writeStartElement(GROUP_ELEMENT_NAME);
            writer.writeAttribute(TRANSFORM_ATTRIBUTE, getTranslateString(threeWtNode));
            addStylesIfAny(writer, styleProvider.getNodeStyleClasses(threeWtNode));
            Optional<String> backgroundStyle = styleProvider.getThreeWtNodeBackgroundStyle(threeWtNode);
            if (backgroundStyle.isPresent()) {
                writer.writeStartElement(GROUP_ELEMENT_NAME);
                writer.writeAttribute(CLASS_ATTRIBUTE, backgroundStyle.get());
                draw3WtWinding(point1, null, writer);
                draw3WtWinding(point2, null, writer);
                draw3WtWinding(point3, null, writer);
                writer.writeEndElement();
            }
            draw3WtWinding(point1, styleProvider.getThreeWtNodeStyle(threeWtNode, ThreeWtEdge.Side.ONE).orElse(null), writer);
            draw3WtWinding(point2, styleProvider.getThreeWtNodeStyle(threeWtNode, ThreeWtEdge.Side.TWO).orElse(null), writer);
            draw3WtWinding(point3, styleProvider.getThreeWtNodeStyle(threeWtNode, ThreeWtEdge.Side.THREE).orElse(null), writer);
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void draw3WtWinding(Point circleCenter, String style, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeEmptyElement(CIRCLE_ELEMENT_NAME);
        if (style != null) {
            writer.writeAttribute(CLASS_ATTRIBUTE, style);
        }
        writer.writeAttribute("cx", getFormattedValue(circleCenter.getX()));
        writer.writeAttribute("cy", getFormattedValue(circleCenter.getY()));
        writer.writeAttribute(CIRCLE_RADIUS_ATTRIBUTE, getFormattedValue(svgParameters.getTransformerCircleRadius()));
    }

    private void drawEdgeInfo(XMLStreamWriter writer, List<Point> line, List<EdgeInfo> edgeInfos) throws XMLStreamException {
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.EDGE_INFOS_CLASS);
        writer.writeAttribute(TRANSFORM_ATTRIBUTE, getTranslateString(getArrowCenter(line)));
        double angle = getEdgeEndYAxisAngle(line);
        double textAngle = Math.abs(angle) > Math.PI / 2 ? angle - Math.signum(angle) * Math.PI : angle;
        for (EdgeInfo info : edgeInfos) {
            writer.writeStartElement(GROUP_ELEMENT_NAME);
            addStylesIfAny(writer, styleProvider.getEdgeInfoStyles(info));
            drawInAndOutArrows(writer, angle);
            Optional<String> rightLabel = info.getRightLabel();
            if (rightLabel.isPresent()) {
                drawLabel(writer, rightLabel.get(), svgParameters.getArrowLabelShift(), textAngle, "dominant-baseline:middle");
            }
            Optional<String> leftLabel = info.getLeftLabel();
            if (leftLabel.isPresent()) {
                drawLabel(writer, leftLabel.get(), -svgParameters.getArrowLabelShift(), textAngle, "dominant-baseline:middle; text-anchor:end");
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void drawInAndOutArrows(XMLStreamWriter writer, double angle) throws XMLStreamException {
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(TRANSFORM_ATTRIBUTE, getRotateString(angle));
        writer.writeEmptyElement(PATH_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.ARROW_IN_CLASS);
        writer.writeAttribute(PATH_D_ATTRIBUTE, labelProvider.getArrowPathDIn());
        writer.writeEmptyElement(PATH_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.ARROW_OUT_CLASS);
        writer.writeAttribute(PATH_D_ATTRIBUTE, labelProvider.getArrowPathDOut());
        writer.writeEndElement();
    }

    private void drawLabel(XMLStreamWriter writer, String label, double labelShiftX, double angle, String style) throws XMLStreamException {
        writer.writeStartElement(TEXT_ELEMENT_NAME);
        writer.writeAttribute(TRANSFORM_ATTRIBUTE, getRotateString(angle));
        writer.writeAttribute(X_ATTRIBUTE, getFormattedValue(labelShiftX));
        writer.writeAttribute(STYLE_ELEMENT_NAME, style);
        writer.writeCharacters(label);
        writer.writeEndElement();
    }

    private String getRotateString(double angleRad) {
        return "rotate(" + getFormattedValue(Math.toDegrees(angleRad)) + ")";
    }

    private Point getArrowCenter(List<Point> line) {
        if (line.size() > 2) {
            return line.get(1).atDistance(svgParameters.getArrowShift(), line.get(2));
        } else {
            return line.get(0).atDistance(svgParameters.getArrowShift() + svgParameters.getVoltageLevelCircleRadius(), line.get(1));
        }
    }

    private double getEdgeEndYAxisAngle(List<Point> line) {
        Point point1 = line.get(line.size() - 1);
        Point point0 = line.get(line.size() - 2);
        return Math.atan2(point1.getX() - point0.getX(), -(point1.getY() - point0.getY()));
    }

    private double getEdgeStartAngle(List<Point> line) {
        Point point1 = line.get(1);
        Point point0 = line.get(0);
        return Math.atan2(point1.getY() - point0.getY(), point1.getX() - point0.getX());
    }

    private void draw2WtWinding(XMLStreamWriter writer, List<Point> half) throws XMLStreamException {
        writer.writeEmptyElement(CIRCLE_ELEMENT_NAME);
        Point point1 = half.get(half.size() - 1); // point in the middle
        Point point2 = half.get(half.size() - 2); // point before
        double radius = svgParameters.getTransformerCircleRadius();
        Point circleCenter = point1.atDistance(radius / 2, point2);
        writer.writeAttribute("cx", getFormattedValue(circleCenter.getX()));
        writer.writeAttribute("cy", getFormattedValue(circleCenter.getY()));
        writer.writeAttribute(CIRCLE_RADIUS_ATTRIBUTE, getFormattedValue(radius));
    }

    private void drawVoltageLevelNodes(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.VOLTAGE_LEVEL_NODES_CLASS);
        for (VoltageLevelNode vlNode : graph.getVoltageLevelNodesStream().filter(VoltageLevelNode::isVisible).collect(Collectors.toList())) {
            writer.writeStartElement(GROUP_ELEMENT_NAME);
            writer.writeAttribute(TRANSFORM_ATTRIBUTE, getTranslateString(vlNode));
            drawNode(graph, writer, vlNode);
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void drawTextNodes(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.TEXT_NODES_CLASS);
        for (TextNode tn : graph.getTextNodes()) {
            writeTextNode(writer, tn);
        }
        writer.writeEndElement();
    }

    private String getTranslateString(Node node) {
        return getTranslateString(node.getX(), node.getY());
    }

    private String getTranslateString(Point point) {
        return getTranslateString(point.getX(), point.getY());
    }

    private String getTranslateString(double x, double y) {
        return "translate(" + getFormattedValue(x) + "," + getFormattedValue(y) + ")";
    }

    private void writeTextNode(XMLStreamWriter writer, TextNode textNode) throws XMLStreamException {
        if (textNode == null) {
            return;
        }
        writer.writeStartElement(TEXT_ELEMENT_NAME);
        writer.writeAttribute(X_ATTRIBUTE, getFormattedValue(textNode.getX()));
        writer.writeAttribute(Y_ATTRIBUTE, getFormattedValue(textNode.getY()));
        writer.writeAttribute(STYLE_ELEMENT_NAME, "dominant-baseline:middle");
        writer.writeCharacters(textNode.getText());
        writer.writeEndElement();
    }

    private void drawNode(Graph graph, XMLStreamWriter writer, VoltageLevelNode vlNode) throws XMLStreamException {
        writer.writeAttribute(ID_ATTRIBUTE, vlNode.getDiagramId());
        addStylesIfAny(writer, styleProvider.getNodeStyleClasses(vlNode));
        insertName(writer, vlNode::getName);

        int nbBuses = vlNode.getBusNodes().size();
        double nodeOuterRadius = svgParameters.getVoltageLevelCircleRadius();
        double busOuterRadius = nodeOuterRadius;

        List<Edge> traversingBusEdges = graph.getEdgeStream(vlNode).filter(edge -> !(edge instanceof TextEdge)).collect(Collectors.toList());

        for (BusInnerNode busNode : vlNode.getBusNodes()) {
            double busInnerRadius = busOuterRadius - nodeOuterRadius / nbBuses;
            if (busInnerRadius == 0) {
                writer.writeEmptyElement(CIRCLE_ELEMENT_NAME);
                writer.writeAttribute(CIRCLE_RADIUS_ATTRIBUTE, getFormattedValue(busOuterRadius));
            } else {
                Collection<Edge> busEdges = graph.getBusEdges(busNode);
                traversingBusEdges.removeAll(busEdges);
                writer.writeEmptyElement(PATH_ELEMENT_NAME);
                writer.writeAttribute(PATH_D_ATTRIBUTE, getFragmentedAnnulusPath(busOuterRadius, busInnerRadius, traversingBusEdges, graph, vlNode));
            }
            writer.writeAttribute(ID_ATTRIBUTE, busNode.getDiagramId());
            addStylesIfAny(writer, styleProvider.getNodeStyleClasses(busNode));
            busOuterRadius = busInnerRadius;
        }
    }

    private String getFragmentedAnnulusPath(double outerRadius, double innerRadius, List<Edge> traversingBusEdges, Graph graph, VoltageLevelNode vlNode) {
        if (traversingBusEdges.isEmpty()) {
            String path = "M" + getCirclePath(outerRadius, 0, Math.PI, true)
                    + " M" + getCirclePath(outerRadius, Math.PI, 0, true);
            if (innerRadius > 0) { // going the other way around (counter-clockwise) to subtract the inner circle
                path += "M" + getCirclePath(innerRadius, 0, Math.PI, false)
                        + "M" + getCirclePath(innerRadius, Math.PI, 0, false);
            }
            return path;
        }

        List<Double> angles = traversingBusEdges.stream()
                .map(edge -> getLine(edge, graph, vlNode))
                .filter(list -> !list.isEmpty())
                .mapToDouble(this::getEdgeStartAngle)
                .sorted().boxed().collect(Collectors.toList());
        if (!angles.isEmpty()) {
            // adding first angle to close the circle annulus, and adding 360° to keep the list ordered
            angles.add(angles.get(0) + 2 * Math.PI);
        }

        double halfWidth = svgParameters.getNodeHollowWidth() / 2;
        double deltaAngle0 = halfWidth / outerRadius;
        double deltaAngle1 = halfWidth / innerRadius;

        StringBuilder path = new StringBuilder();
        for (int i = 0; i < angles.size() - 1; i++) {
            double outerArcStart = angles.get(i) + deltaAngle0;
            double outerArcEnd = angles.get(i + 1) - deltaAngle0;
            double innerArcStart = angles.get(i + 1) - deltaAngle1;
            double innerArcEnd = angles.get(i) + deltaAngle1;
            if (outerArcEnd > outerArcStart && innerArcEnd < innerArcStart) {
                path.append("M").append(getCirclePath(outerRadius, outerArcStart, outerArcEnd, true))
                        .append(" L").append(getCirclePath(innerRadius, innerArcStart, innerArcEnd, false))
                        .append(" Z ");
            }
        }

        return path.toString();
    }

    private String getCirclePath(double radius, double angleStart, double angleEnd, boolean clockWise) {
        double arcAngle = angleEnd - angleStart;
        double xStart = radius * Math.cos(angleStart);
        double yStart = radius * Math.sin(angleStart);
        double xEnd = radius * Math.cos(angleEnd);
        double yEnd = radius * Math.sin(angleEnd);
        int largeArc = Math.abs(arcAngle) > Math.PI ? 1 : 0;
        return String.format(Locale.US, "%.3f,%.3f A%.3f,%.3f %.3f %d %d %.3f,%.3f",
                xStart, yStart, radius, radius, Math.toDegrees(arcAngle), largeArc, clockWise ? 1 : 0, xEnd, yEnd);
    }

    private List<Point> getLine(Edge edge, Graph graph, VoltageLevelNode vlNode) {
        if (edge instanceof ThreeWtEdge) {
            return ((ThreeWtEdge) edge).getPoints();
        } else if (edge instanceof BranchEdge) {
            BranchEdge.Side side = graph.getNode1(edge) == vlNode ? BranchEdge.Side.ONE : BranchEdge.Side.TWO;
            return ((BranchEdge) edge).getPoints(side);
        }
        return Collections.emptyList();
    }

    private void insertName(XMLStreamWriter writer, Supplier<Optional<String>> getName) throws XMLStreamException {
        if (svgParameters.isInsertName()) {
            Optional<String> nodeName = getName.get();
            if (nodeName.isPresent()) {
                writer.writeAttribute(TITLE_ATTRIBUTE, nodeName.get());
            }
        }
    }

    private void drawTextEdges(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.TEXT_EDGES_CLASS);
        for (TextEdge edge : graph.getTextEdges()) {
            drawTextEdge(writer, edge);
        }
        writer.writeEndElement();
    }

    private void drawTextEdge(XMLStreamWriter writer, TextEdge edge) throws XMLStreamException {
        writer.writeEmptyElement(POLYLINE_ELEMENT_NAME);
        writer.writeAttribute(ID_ATTRIBUTE, edge.getDiagramId());
        addStylesIfAny(writer, styleProvider.getEdgeStyleClasses(edge));
        List<Point> points = edge.getPoints();
        shiftEdgeStart(points);
        String lineFormatted1 = points.stream()
                .map(point -> getFormattedValue(point.getX()) + "," + getFormattedValue(point.getY()))
                .collect(Collectors.joining(" "));
        writer.writeAttribute(POINTS_ATTRIBUTE, lineFormatted1);
    }

    private void addStylesIfAny(XMLStreamWriter writer, List<String> edgeStyleClasses) throws XMLStreamException {
        if (!edgeStyleClasses.isEmpty()) {
            writer.writeAttribute(CLASS_ATTRIBUTE, String.join(" ", edgeStyleClasses));
        }
    }

    private void shiftEdgeStart(List<Point> points) {
        Point point0 = points.get(0).atDistance(svgParameters.getVoltageLevelCircleRadius(), points.get(1));
        points.get(0).setX(point0.getX());
        points.get(0).setY(point0.getY());
    }

    private void addSvgRoot(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("", SVG_ROOT_ELEMENT_NAME, NAMESPACE_URI);
        if (svgParameters.isSvgWidthAndHeightAdded()) {
            double[] diagramDimension = getDiagramDimensions(graph);
            writer.writeAttribute(WIDTH_ATTRIBUTE, getFormattedValue(diagramDimension[0]));
            writer.writeAttribute(HEIGHT_ATTRIBUTE, getFormattedValue(diagramDimension[1]));
        }
        writer.writeAttribute(VIEW_BOX_ATTRIBUTE, getViewBoxValue(graph));
        writer.writeDefaultNamespace(NAMESPACE_URI);
    }

    private double[] getDiagramDimensions(Graph graph) {
        double width = getDiagramWidth(graph);
        double height = getDiagramHeight(graph);
        double scale;
        switch (svgParameters.getSizeConstraint()) {
            case FIXED_WIDTH:
                scale = svgParameters.getFixedWidth() / width;
                break;
            case FIXED_HEIGHT:
                scale = svgParameters.getFixedHeight() / height;
                break;
            default:
                scale = 20;
                break;
        }
        return new double[] {width * scale, height * scale};
    }

    private double getDiagramHeight(Graph graph) {
        Padding diagramPadding = svgParameters.getDiagramPadding();
        return graph.getHeight() + diagramPadding.getTop() + diagramPadding.getBottom();
    }

    private double getDiagramWidth(Graph graph) {
        Padding diagramPadding = svgParameters.getDiagramPadding();
        return graph.getWidth() + diagramPadding.getLeft() + diagramPadding.getRight();
    }

    private String getViewBoxValue(Graph graph) {
        Padding diagramPadding = svgParameters.getDiagramPadding();
        return getFormattedValue(graph.getMinX() - diagramPadding.getLeft()) + " "
                + getFormattedValue(graph.getMinY() - diagramPadding.getTop()) + " "
                + getFormattedValue(getDiagramWidth(graph)) + " " + getFormattedValue(getDiagramHeight(graph));
    }

    private void addStyle(XMLStreamWriter writer) throws XMLStreamException {
        switch (svgParameters.getCssLocation()) {
            case INSERTED_IN_SVG:
                writer.writeStartElement(STYLE_ELEMENT_NAME);
                writer.writeCData(styleProvider.getStyleDefs());
                writer.writeEndElement();
                break;
            case EXTERNAL_IMPORTED:
                writer.writeStartElement(STYLE_ELEMENT_NAME);
                for (String cssFilename : styleProvider.getCssFilenames()) {
                    writer.writeCharacters("@import url(" + cssFilename + ");");
                }
                writer.writeEndElement();
                break;
            case EXTERNAL_NO_IMPORT:
                // nothing to do
                break;
        }
    }

    private void addMetadata(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(METADATA_ELEMENT_NAME);
        writer.writeEndElement();
    }

    private static String getFormattedValue(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

}
