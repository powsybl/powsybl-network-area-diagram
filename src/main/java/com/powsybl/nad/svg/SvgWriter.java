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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
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
    private static final double CIRCLE_RADIUS = 0.6;
    private static final double TRANSFORMER_CIRCLE_RADIUS = 0.2;

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
        try {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, INDENT, svgOs);
            addSvgRoot(graph, writer);
            addStyle(writer);
            addMetadata(writer);
            drawBranchEdges(graph, writer);
            drawThreeWtEdges(graph, writer);
            drawVoltageLevelNodes(graph, writer);
            drawTextEdges(graph, writer);
            drawTextNodes(graph, writer);
            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
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

            writer.writeEndElement();
        }
        writer.writeEndElement();
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
        List<Point> half = edge.getLine(side);
        if (edge.isVisible(side)) {
            String lineFormatted = half.stream()
                .map(point -> getFormattedValue(point.getX()) + "," + getFormattedValue(point.getY()))
                .collect(Collectors.joining(" "));
            writer.writeAttribute("points", lineFormatted);
            drawEdgeInfo(writer, edge, side, labelProvider.getEdgeInfos(graph, edge, side));
        }
        if (edge.getType().equals(BranchEdge.TWO_WT_EDGE)) {
            drawTransformer(writer, half);
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
        writer.writeAttribute("points", lineFormatted);
//            drawEdgeInfo(writer, edge, side, labelProvider.getEdgeInfos(graph, edge, side)); TODO: !!
        writer.writeEndElement();
    }

    private void drawEdgeInfo(XMLStreamWriter writer, BranchEdge edge, BranchEdge.Side side, List<EdgeInfo> edgeInfos) throws XMLStreamException {
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.EDGE_INFOS_CLASS);
        writer.writeAttribute(TRANSFORM_ATTRIBUTE, getTranslateString(getArrowCenter(edge, side)));
        double angle = getEdgeYAxisAngle(edge, side);
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

    private Point getArrowCenter(BranchEdge edge, BranchEdge.Side side) {
        List<Point> line = edge.getLine(side);
        if (line.size() > 2) {
            return line.get(1).atDistance(svgParameters.getArrowShift(), line.get(2));
        } else {
            return line.get(0).atDistance(svgParameters.getArrowShift() + CIRCLE_RADIUS, line.get(1));
        }
    }

    private double getEdgeYAxisAngle(BranchEdge edge, BranchEdge.Side side) {
        List<Point> line = edge.getLine(side);
        Point point1 = line.get(line.size() - 1);
        Point point0 = line.get(line.size() - 2);
        return Math.atan2(point1.getX() - point0.getX(), -(point1.getY() - point0.getY()));
    }

    private void drawTransformer(XMLStreamWriter writer, List<Point> half) throws XMLStreamException {
        writer.writeEmptyElement(CIRCLE_ELEMENT_NAME);
        Point point1 = half.get(half.size() - 1); // point in the middle
        Point point2 = half.get(half.size() - 2); // point before
        Point circleCenter = point1.atDistance(TRANSFORMER_CIRCLE_RADIUS / 2, point2);
        writer.writeAttribute("cx", getFormattedValue(circleCenter.getX()));
        writer.writeAttribute("cy", getFormattedValue(circleCenter.getY()));
        writer.writeAttribute(CIRCLE_RADIUS_ATTRIBUTE, getFormattedValue(TRANSFORMER_CIRCLE_RADIUS));
    }

    private void drawVoltageLevelNodes(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.VOLTAGE_LEVEL_NODES_CLASS);
        for (VoltageLevelNode vlNode : graph.getVoltageLevelNodesStream().filter(VoltageLevelNode::isVisible).collect(Collectors.toList())) {
            writer.writeStartElement(GROUP_ELEMENT_NAME);
            writer.writeAttribute(TRANSFORM_ATTRIBUTE, getTranslateString(vlNode));
            drawNodeCircle(writer, vlNode);
            writeNbBuses(writer, vlNode);
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

    private void drawNodeCircle(XMLStreamWriter writer, VoltageLevelNode vlNode) throws XMLStreamException {
        writer.writeEmptyElement(CIRCLE_ELEMENT_NAME);
        writer.writeAttribute(ID_ATTRIBUTE, vlNode.getDiagramId());
        addStylesIfAny(writer, styleProvider.getNodeStyleClasses(vlNode));
        insertName(writer, vlNode::getName);
        writer.writeAttribute(CIRCLE_RADIUS_ATTRIBUTE, getFormattedValue(CIRCLE_RADIUS));
    }

    private void writeNbBuses(XMLStreamWriter writer, VoltageLevelNode vlNode) throws XMLStreamException {
        writer.writeStartElement(TEXT_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.BUSES_TEXT_CLASS);
        writer.writeAttribute(STYLE_ELEMENT_NAME, "text-anchor:middle;dominant-baseline:middle");
        writer.writeCharacters(String.valueOf(vlNode.getBusNodesCount()));
        writer.writeEndElement();
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
        writer.writeAttribute("points", lineFormatted1);
    }

    private void addStylesIfAny(XMLStreamWriter writer, List<String> edgeStyleClasses) throws XMLStreamException {
        if (!edgeStyleClasses.isEmpty()) {
            writer.writeAttribute(CLASS_ATTRIBUTE, String.join(" ", edgeStyleClasses));
        }
    }

    private void shiftEdgeStart(List<Point> points) {
        Point point0 = points.get(0).atDistance(CIRCLE_RADIUS, points.get(1));
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
