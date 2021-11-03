/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.nad.model.Edge;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.VoltageLevelNode;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    private static final String CIRCLE_ELEMENT_NAME = "circle";
    private static final String TEXT_ELEMENT_NAME = "text";
    private static final String ID_ATTRIBUTE = "id";
    private static final String WIDTH_ATTRIBUTE = "width";
    private static final String HEIGHT_ATTRIBUTE = "height";
    private static final String VIEW_BOX_ATTRIBUTE = "viewBox";
    private static final String TITLE_ATTRIBUTE = "title";
    private static final String CLASS_ATTRIBUTE = "class";
    private static final String TRANSFORM_ATTRIBUTE = "transform";
    private static final double CIRCLE_RADIUS = 0.6;
    private static final double LINE_WIDTH = 0.2;

    private final SvgParameters svgParameters;
    private final StyleProvider styleProvider;

    public SvgWriter(SvgParameters svgParameters, StyleProvider styleProvider) {
        this.svgParameters = Objects.requireNonNull(svgParameters);
        this.styleProvider = Objects.requireNonNull(styleProvider);
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
            e.printStackTrace();
        }
    }

    public void writeSvg(Graph graph, OutputStream svgWriter) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(svgWriter);
        try {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, INDENT, svgWriter);
            addSvgRoot(graph, writer);
            addStyle(writer);
            addMetadata(writer);
            drawEdges(graph, writer);
            drawNodes(graph, writer);
            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private void drawEdges(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        for (Edge edge : graph.getEdgesStream().collect(Collectors.toList())) {
            writer.writeStartElement(GROUP_ELEMENT_NAME);
            writer.writeAttribute(ID_ATTRIBUTE, edge.getDiagramId());
            List<String> edgeStyleClasses = styleProvider.getEdgeStyleClasses(edge);
            if (!edgeStyleClasses.isEmpty()) {
                writer.writeAttribute(CLASS_ATTRIBUTE, String.join(" ", edgeStyleClasses));
            }
            insertName(writer, edge::getName);

            drawSideLine(writer, edge, Edge.Side.ONE);
            drawSideLine(writer, edge, Edge.Side.TWO);

            writer.writeEndElement();
        }
    }

    private void drawSideLine(XMLStreamWriter writer, Edge edge, Edge.Side side) throws XMLStreamException {
        writer.writeEmptyElement(POLYLINE_ELEMENT_NAME);
        List<String> edgeSideStyleClasses = styleProvider.getSideEdgeStyleClasses(edge, side);
        if (!edgeSideStyleClasses.isEmpty()) {
            writer.writeAttribute(CLASS_ATTRIBUTE, String.join(" ", edgeSideStyleClasses));
        }
        String lineFormatted1 = edge.getLine(side).stream()
                .map(point -> getFormattedValue(point.getX()) + "," + getFormattedValue(point.getY()))
                .collect(Collectors.joining(" "));
        writer.writeAttribute("points", lineFormatted1);
    }

    private void drawNodes(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, styleProvider.getVoltageLevelNodeStyle());
        for (VoltageLevelNode vlNode : graph.getVoltageLevelNodesStream().collect(Collectors.toList())) {
            writer.writeStartElement(GROUP_ELEMENT_NAME);
            writer.writeAttribute(TRANSFORM_ATTRIBUTE, "translate(" +
                    getFormattedValue(vlNode.getX()) + "," + getFormattedValue(vlNode.getY()) + ")");
            drawCircle(writer, vlNode);
            writeNbBuses(writer, vlNode);
            writer.writeEndElement();
        }
        writer.writeEndElement();

    }

    private void drawCircle(XMLStreamWriter writer, VoltageLevelNode vlNode) throws XMLStreamException {
        writer.writeEmptyElement(CIRCLE_ELEMENT_NAME);
        writer.writeAttribute(ID_ATTRIBUTE, vlNode.getDiagramId());
        writer.writeAttribute(CLASS_ATTRIBUTE, String.join(" ", styleProvider.getNodeStyleClasses(vlNode)));
        insertName(writer, vlNode::getName);
        writer.writeAttribute("r", getFormattedValue(CIRCLE_RADIUS));
    }

    private void writeNbBuses(XMLStreamWriter writer, VoltageLevelNode vlNode) throws XMLStreamException {
        writer.writeStartElement(TEXT_ELEMENT_NAME);
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

    private void addSvgRoot(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("", SVG_ROOT_ELEMENT_NAME, NAMESPACE_URI);
        if (svgParameters.isSvgWidthAndHeightAdded()) {
            writer.writeAttribute(WIDTH_ATTRIBUTE, getFormattedValue(getDiagramWidth(graph)));
            writer.writeAttribute(HEIGHT_ATTRIBUTE, getFormattedValue(getDiagramHeight(graph)));
        }
        writer.writeAttribute(VIEW_BOX_ATTRIBUTE, getViewBoxValue(graph));
        writer.writeDefaultNamespace(NAMESPACE_URI);
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
        // TODO: add the graph metadata in the SVG
        writer.writeEndElement();
    }

    private static String getFormattedValue(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

}
