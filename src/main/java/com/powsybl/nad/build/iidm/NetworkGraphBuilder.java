/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.build.iidm;

import com.powsybl.commons.PowsyblException;
import com.powsybl.nad.model.ThreeWtNode;
import com.powsybl.iidm.network.*;
import com.powsybl.nad.build.GraphBuilder;
import com.powsybl.nad.model.*;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class NetworkGraphBuilder implements GraphBuilder {

    private final Network network;
    private final IdProvider idProvider;
    private final Predicate<VoltageLevel> voltageLevelFilter;

    public NetworkGraphBuilder(Network network, Predicate<VoltageLevel> voltageLevelFilter, IdProvider idProvider) {
        this.network = Objects.requireNonNull(network);
        this.voltageLevelFilter = voltageLevelFilter;
        this.idProvider = Objects.requireNonNull(idProvider);
    }

    public NetworkGraphBuilder(Network network, Predicate<VoltageLevel> voltageLevelFilter) {
        this(network, voltageLevelFilter, new IntIdProvider());
    }

    public NetworkGraphBuilder(Network network) {
        this(network, VoltageLevelFilter.NO_FILTER, new IntIdProvider());
    }

    @Override
    public Graph buildGraph() {
        Graph graph = new Graph();
        addGraphNodes(graph);
        addGraphEdges(graph);
        return graph;
    }

    private void addGraphNodes(Graph graph) {
        network.getVoltageLevelStream().filter(voltageLevelFilter).forEach(vl -> {
            VoltageLevelNode vlNode = new VoltageLevelNode(idProvider.createId(vl),
                    vl.getId(), vl.getNameOrId(), vl.getNominalV());
            TextNode textNode = new TextNode(vlNode.getDiagramId() + "_text", vl.getNameOrId());
            graph.addNode(vlNode);
            graph.addNode(textNode);
            graph.addEdge(vlNode, textNode, new TextEdge(textNode.getDiagramId() + "_edge"));
            vl.getBusView().getBusStream()
                    .map(bus -> new BusInnerNode(idProvider.createId(bus), bus.getId()))
                    .forEach(vlNode::addBusNode);
        });
    }

    private void addGraphEdges(Graph graph) {
        network.getVoltageLevelStream()
                .filter(voltageLevelFilter)
                .forEach(vl -> vl.visitEquipments(new VisitorBuilder(graph)));
    }

    private class VisitorBuilder extends DefaultTopologyVisitor {
        private final Graph graph;

        public VisitorBuilder(Graph graph) {
            this.graph = graph;
        }

        @Override
        public void visitLine(Line line, Branch.Side side) {
            // check if the edge was not already added (at the other side of the line)
            if (graph.containsEdge(line.getId())) {
                return;
            }

            Branch.Side otherSide = side == Branch.Side.ONE ? Branch.Side.TWO : Branch.Side.ONE;
            VoltageLevelNode vlNode = graph.getVoltageLevelNode(line.getTerminal(side).getVoltageLevel().getId())
                    .orElseThrow(() -> new PowsyblException("Cannot add line, its voltage level is unknown"));
            VoltageLevelNode vlOtherNode = getOrCreateVoltageLevelNode(line.getTerminal(otherSide));

            BranchEdge edge = new BranchEdge(idProvider.createId(line), line.getId(), line.getNameOrId(), BranchEdge.LINE_EDGE);
            if (side == Branch.Side.ONE) {
                graph.addEdge(vlNode, vlOtherNode, edge);
            } else {
                graph.addEdge(vlOtherNode, vlNode, edge);
            }
        }

        @Override
        public void visitTwoWindingsTransformer(TwoWindingsTransformer twt, Branch.Side side) {
            // check if the edge was not already added (at the other side of the transformer)
            if (graph.containsEdge(twt.getId())) {
                return;
            }

            Branch.Side otherSide = side == Branch.Side.ONE ? Branch.Side.TWO : Branch.Side.ONE;
            VoltageLevelNode vlNode = graph.getVoltageLevelNode(twt.getTerminal(side).getVoltageLevel().getId())
                    .orElseThrow(() -> new PowsyblException("Cannot add two-windings transformer, its voltage level is unknown"));
            VoltageLevelNode vlOtherNode = getOrCreateVoltageLevelNode(twt.getTerminal(otherSide));

            BranchEdge edge = new BranchEdge(idProvider.createId(twt), twt.getId(), twt.getNameOrId(), BranchEdge.TWO_WT_EDGE);
            if (side == Branch.Side.ONE) {
                graph.addEdge(vlNode, vlOtherNode, edge);
            } else {
                graph.addEdge(vlOtherNode, vlNode, edge);
            }
        }

        @Override
        public void visitThreeWindingsTransformer(ThreeWindingsTransformer twt, ThreeWindingsTransformer.Side side) {
            // check if the transformer was not already added (at the other sides of the transformer)
            if (graph.containsNode(twt.getId())) {
                return;
            }

            VoltageLevelNode vlNode = graph.getVoltageLevelNode(twt.getTerminal(side).getVoltageLevel().getId())
                    .orElseThrow(() -> new PowsyblException("Cannot add three-windings transformer, its voltage level is unknown"));
            ThreeWindingsTransformer.Side otherSide1;
            ThreeWindingsTransformer.Side otherSide2;
            if (side == ThreeWindingsTransformer.Side.ONE) {
                otherSide1 = ThreeWindingsTransformer.Side.TWO;
                otherSide2 = ThreeWindingsTransformer.Side.THREE;
            } else if (side == ThreeWindingsTransformer.Side.TWO) {
                otherSide1 = ThreeWindingsTransformer.Side.ONE;
                otherSide2 = ThreeWindingsTransformer.Side.THREE;
            } else {
                otherSide1 = ThreeWindingsTransformer.Side.ONE;
                otherSide2 = ThreeWindingsTransformer.Side.TWO;
            }
            VoltageLevelNode vlOtherNode1 = getOrCreateVoltageLevelNode(twt.getTerminal(otherSide1));
            VoltageLevelNode vlOtherNode2 = getOrCreateVoltageLevelNode(twt.getTerminal(otherSide2));

            String twtId = twt.getId();
            String twtName = twt.getNameOrId();
            ThreeWtNode tn = new ThreeWtNode(idProvider.createId(twt), twtId, twtName);
            graph.addNode(tn);
            graph.addEdge(vlNode, tn, new ThreeWtEdge(idProvider.createId(get3wtLeg(twt, side)), twtId, twtName, iidmSideToSide(side), vlNode.isVisible()));
            graph.addEdge(vlOtherNode1, tn, new ThreeWtEdge(idProvider.createId(get3wtLeg(twt, otherSide1)), twtId, twtName, iidmSideToSide(otherSide1), vlOtherNode1.isVisible()));
            graph.addEdge(vlOtherNode2, tn, new ThreeWtEdge(idProvider.createId(get3wtLeg(twt, otherSide2)), twtId, twtName, iidmSideToSide(otherSide2), vlOtherNode2.isVisible()));
        }

        public void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
            // check if the hvdc line was not already added (at the other side of the line)
            HvdcLine hvdcLine = converterStation.getHvdcLine();
            if (graph.containsEdge(hvdcLine.getId())) {
                return;
            }

            HvdcLine.Side otherSide = (hvdcLine.getConverterStation1().getId().equals(converterStation.getId()))
                    ? HvdcLine.Side.TWO : HvdcLine.Side.ONE;

            Terminal terminal = converterStation.getTerminal();
            Terminal otherSideTerminal = hvdcLine.getConverterStation(otherSide).getTerminal();
            VoltageLevelNode vlNode = graph.getVoltageLevelNode(terminal.getVoltageLevel().getId())
                    .orElseThrow(() -> new PowsyblException("Cannot add hvdc line, its voltage level is unknown"));
            VoltageLevelNode vlOtherNode = getOrCreateVoltageLevelNode(otherSideTerminal);

            BranchEdge edge = new BranchEdge(idProvider.createId(hvdcLine), hvdcLine.getId(), hvdcLine.getNameOrId(), BranchEdge.HVDC_LINE_EDGE);
            if (otherSide == HvdcLine.Side.TWO) {
                graph.addEdge(vlNode, vlOtherNode, edge);
            } else {
                graph.addEdge(vlOtherNode, vlNode, edge);
            }
        }

        private ThreeWindingsTransformer.Leg get3wtLeg(ThreeWindingsTransformer twt, ThreeWindingsTransformer.Side side) {
            if (side == ThreeWindingsTransformer.Side.ONE) {
                return twt.getLeg1();
            } else if (side == ThreeWindingsTransformer.Side.TWO) {
                return twt.getLeg2();
            } else {
                return twt.getLeg3();
            }
        }

        private VoltageLevelNode getOrCreateVoltageLevelNode(Terminal terminal) {
            VoltageLevel vl = terminal.getVoltageLevel();
            return graph.getVoltageLevelNode(vl.getId()).orElseGet(() -> createInvisibleVoltageLevelNode(vl));
        }

        private VoltageLevelNode createInvisibleVoltageLevelNode(VoltageLevel vl) {
            VoltageLevelNode invisibleVlNode = new VoltageLevelNode(idProvider.createId(vl), vl.getId(), vl.getNameOrId(), vl.getNominalV(), false);
            graph.addNode(invisibleVlNode);
            return invisibleVlNode;
        }

        private ThreeWtEdge.Side iidmSideToSide(ThreeWindingsTransformer.Side side) {
            switch (Objects.requireNonNull(side)) {
                case ONE:
                    return ThreeWtEdge.Side.ONE;
                case TWO:
                    return ThreeWtEdge.Side.TWO;
                case THREE:
                    return ThreeWtEdge.Side.THREE;
            }
            return null;
        }
    }
}
