/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.nad.model.Edge;
import com.powsybl.nad.model.Node;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import java.io.InputStream;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class GraphMetadata {

    private static final String METADATA_NAMESPACE_URI = "http://www.powsybl.org/schema/nad-metadata/1_0";
    private static final String METADATA_PREFIX = "nad";
    private static final String METADATA_ELEMENT_NAME = "metadata";
    private static final String METADATA_NODES_ELEMENT_NAME = "nodes";
    private static final String METADATA_EDGES_ELEMENT_NAME = "edges";
    private static final String METADATA_NODE_ELEMENT_NAME = "node";
    private static final String METADATA_EDGE_ELEMENT_NAME = "edge";
    private static final String DIAGRAM_ID_ATTRIBUTE = "diagramId";
    private static final String EQUIPMENT_ID_ATTRIBUTE = "equipmentId";

    private final Map<String, String> nodeIdByDiagramId = new LinkedHashMap<>();

    private final Map<String, String> edgeIdByDiagramId = new LinkedHashMap<>();

    private final Optional<UnaryOperator<String>> diagramIdFunction;

    public GraphMetadata() {
        this(Collections.emptyList(), Collections.emptyList(), null);
    }

    public GraphMetadata(Stream<Node> nodes,
                         Stream<Edge> edges,
                         UnaryOperator<String> diagramIdFunction) {
        this(nodes.collect(Collectors.toUnmodifiableList()), edges.collect(Collectors.toUnmodifiableList()), diagramIdFunction);
    }

    public GraphMetadata(List<Node> nodes,
                         List<Edge> edges,
                         UnaryOperator<String> diagramIdFunction) {

        this.diagramIdFunction = Optional.ofNullable(diagramIdFunction);
        nodes.forEach(this::addNode);
        edges.forEach(this::addEdge);
    }

    public static GraphMetadata parseXml(InputStream inputStream) throws XMLStreamException {
        return parseXml(XMLInputFactory.newDefaultFactory().createXMLStreamReader(inputStream));
    }

    public static GraphMetadata parseXml(XMLStreamReader reader) throws XMLStreamException {
        GraphMetadata metadata = new GraphMetadata();

        XmlUtil.readUntilEndElement(METADATA_ELEMENT_NAME, reader, () -> {
            String token = reader.getLocalName();
            switch (token) {
                case METADATA_NODES_ELEMENT_NAME:
                    XmlUtil.readUntilEndElement(token, reader, () -> {
                        if (reader.getLocalName().equals(METADATA_NODE_ELEMENT_NAME)) {
                            parseId(metadata.nodeIdByDiagramId, reader);
                        }
                    });
                    break;
                case METADATA_EDGES_ELEMENT_NAME:
                    XmlUtil.readUntilEndElement(token, reader, () -> {
                        if (reader.getLocalName().equals(METADATA_EDGE_ELEMENT_NAME)) {
                            parseId(metadata.edgeIdByDiagramId, reader);
                        }
                    });
                    break;
                default:
                    // Not managed
            }
        });
        return metadata;
    }

    private static void parseId(Map<String, String> ids, XMLStreamReader reader) {
        String diagramId = reader.getAttributeValue(null, DIAGRAM_ID_ATTRIBUTE);
        String equipmentId = reader.getAttributeValue(null, EQUIPMENT_ID_ATTRIBUTE);
        ids.put(diagramId, equipmentId);
    }

    public void writeXml(XMLStreamWriter writer) throws XMLStreamException {
        // Root element
        writer.writeStartElement(METADATA_ELEMENT_NAME);
        writer.writeNamespace(METADATA_PREFIX, METADATA_NAMESPACE_URI);
        // Nodes
        writeIdMapping(METADATA_NODES_ELEMENT_NAME, METADATA_NODE_ELEMENT_NAME, nodeIdByDiagramId, writer);
        // Edges
        writeIdMapping(METADATA_EDGES_ELEMENT_NAME, METADATA_EDGE_ELEMENT_NAME, edgeIdByDiagramId, writer);
        // End root element
        writer.writeEndElement();
    }

    private void writeIdMapping(String rootElementName, String tagElementName, Map<String, String> ids, XMLStreamWriter writer) throws XMLStreamException {
        if (ids.entrySet().isEmpty()) {
            writer.writeEmptyElement(METADATA_PREFIX, rootElementName, METADATA_NAMESPACE_URI);
        } else {
            writer.writeStartElement(METADATA_PREFIX, rootElementName, METADATA_NAMESPACE_URI);
            for (Map.Entry<String, String> entry : ids.entrySet()) {
                writer.writeEmptyElement(METADATA_PREFIX, tagElementName, METADATA_NAMESPACE_URI);
                writer.writeAttribute(DIAGRAM_ID_ATTRIBUTE, entry.getKey());
                writer.writeAttribute(EQUIPMENT_ID_ATTRIBUTE, entry.getValue());
            }
            writer.writeEndElement();
        }
    }

    public void addNode(Node node) {
        Objects.requireNonNull(node);
        String svgId = node.getDiagramId();
        if (diagramIdFunction.isPresent()) {
            svgId = diagramIdFunction.get().apply(svgId);
        }
        nodeIdByDiagramId.put(svgId, node.getEquipmentId());
    }

    public void addEdge(Edge edge) {
        Objects.requireNonNull(edge);
        String svgId = edge.getDiagramId();
        if (diagramIdFunction.isPresent()) {
            svgId = diagramIdFunction.get().apply(svgId);
        }
        edgeIdByDiagramId.put(svgId, edge.getEquipmentId());
    }
}
