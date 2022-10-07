/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.iidm.network.Network;
import com.powsybl.nad.NetworkAreaDiagram;
import com.powsybl.nad.build.iidm.IntIdProvider;
import com.powsybl.nad.build.iidm.NetworkGraphBuilder;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Node;
import com.powsybl.nad.model.VoltageLevelNode;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.SvgWriter;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Luma Zamarreno <zamarrenolm at aia.es>
 */
class ForceLayoutWithFixedPositionsTest {

    static final Path OUTPUT = Paths.get("/Users/zamarrenolm/work/temp/nad/fixed-positions/output");
    static final double SPRING_REPULSION_FACTOR = 0.2;

    @Test
    void testDiamond() {
        draw(SimpleNetworkFactory.createDiamond());
    }

    private static void draw(Network network) {
        LayoutParameters layoutParameters = layoutParameters();
        SvgParameters svgParameters = svgParameters();

        new NetworkAreaDiagram(network).draw(
                OUTPUT.resolve("all"),
                svgParameters,
                layoutParameters);

        // FIXME(Luma) The following lines should be used like:
        //  Map<String, Point> backbonePoints = new NetworkAreaDiagram(network).layout(layoutParameters);
        Graph graph = new NetworkGraphBuilder(network, vl -> vl.getNominalV() >= 100, new IntIdProvider()).buildGraph();
        new BasicForceLayoutFactory().create().run(graph, layoutParameters);
        writeSvg(graph, network, "backbone", svgParameters);
        printPositions(graph);

        // FIXME(Luma) Then these lines like:
        //  layoutParameters.setFixedPoints(backbonePoints);
        Graph graph2 = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER, new IntIdProvider()).buildGraph();
        for (VoltageLevelNode vlFixedNode : graph.getVoltageLevelNodesStream().filter(VoltageLevelNode::isVisible).collect(Collectors.toList())) {
            Node vlFixedNode2 = graph2.getNode(vlFixedNode.getEquipmentId()).orElseThrow();
            vlFixedNode2.setPosition(vlFixedNode.getPosition());
            vlFixedNode2.setFixedPosition(true);
        }
        new BasicForceLayoutFactory().create().run(graph2, layoutParameters);
        writeSvg(graph2, network, "all-with-fixed-backbone", svgParameters);
        printPositions(graph2);

        // Check all positions from graph have been preserved in graph2
        checkFixedPositions(graph, graph2);
    }

    private static void writeSvg(Graph graph, Network network, String subset, SvgParameters svgParameters) {
        Path svgFile = OUTPUT.resolve(network.getNameOrId() + "-" + subset + ".svg");
        new SvgWriter(svgParameters, styleProvider(network), labelProvider(network, svgParameters)).writeSvg(graph, svgFile);
    }

    private static void checkFixedPositions(Graph expected, Graph actual) {
        for (VoltageLevelNode vlNodeExpected : expected.getVoltageLevelNodesStream().filter(VoltageLevelNode::isVisible).collect(Collectors.toList())) {
            String equipmentId = vlNodeExpected.getEquipmentId();
            Node vlNodeActual = actual.getNode(equipmentId).orElseThrow();
            assertEquals(vlNodeExpected.getPosition().getX(), vlNodeActual.getPosition().getX());
            assertEquals(vlNodeExpected.getPosition().getY(), vlNodeActual.getPosition().getY());
        }
    }

    private static void printPositions(Graph graph) {
        for (VoltageLevelNode vlNode : graph.getVoltageLevelNodesStream().filter(VoltageLevelNode::isVisible).collect(Collectors.toList())) {
            System.out.printf("%10.4f %10.4f %-32s %s%n", vlNode.getPosition().getX(), vlNode.getPosition().getY(), vlNode.getEquipmentId(), vlNode.getName().orElse(""));
        }
    }

    private static SvgParameters svgParameters() {
        return new SvgParameters()
                .setInsertNameDesc(false)
                .setSvgWidthAndHeightAdded(false);
    }

    private static StyleProvider styleProvider(Network network) {
        return new NominalVoltageStyleProvider(network);
    }

    private static LabelProvider labelProvider(Network network, SvgParameters svgParameters) {
        return new DefaultLabelProvider(network, svgParameters);
    }

    private static LayoutParameters layoutParameters() {
        return new LayoutParameters()
            .setSpringRepulsionFactorForceLayout(SPRING_REPULSION_FACTOR);
    }
}
