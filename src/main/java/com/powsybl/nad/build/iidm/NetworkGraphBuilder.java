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
            VoltageLevelNode vlNode = new VoltageLevelNode(idProvider.createId(vl), vl.getId(), vl.getNameOrId());
            TextNode textNode = new TextNode(vlNode.getDiagramId() + "_text", vl.getNameOrId());
            vl.getBusView().getBusStream()
                    .map(bus -> new BusInnerNode(idProvider.createId(bus), bus.getId()))
                    .forEach(vlNode::addBusNode);
            graph.addNode(vlNode);
            graph.addNode(textNode);
            graph.addEdge(vlNode, textNode, new TextEdge(textNode.getDiagramId() + "_edge"));
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

            addEdge(line, side, BranchEdge.LINE_EDGE);
        }

        @Override
        public void visitTwoWindingsTransformer(TwoWindingsTransformer twt, Branch.Side side) {
            // check if the edge was not already added (at the other side of the transformer)
            if (graph.containsEdge(twt.getId())) {
                return;
            }

            addEdge(twt, side, BranchEdge.TWO_WT_EDGE);
        }

        @Override
        public void visitThreeWindingsTransformer(ThreeWindingsTransformer twt, ThreeWindingsTransformer.Side side) {
            // check if the transformer was not already added (at the other sides of the transformer)
            if (graph.containsNode(twt.getId())) {
                return;
            }

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

            Terminal terminalA = twt.getTerminal(side);
            Terminal terminalB = twt.getTerminal(otherSide1);
            Terminal terminalC = twt.getTerminal(otherSide2);

            VoltageLevelNode vlNodeA = graph.getVoltageLevelNode(terminalA.getVoltageLevel().getId())
                    .orElseThrow(() -> new PowsyblException("Cannot add three-windings transformer, its voltage level is unknown"));
            VoltageLevelNode vlNodeB = getOrCreateVoltageLevelNode(terminalB);
            VoltageLevelNode vlNodeC = getOrCreateVoltageLevelNode(terminalC);

            BusInnerNode busNodeA = vlNodeA.getBusInnerNode(terminalA.getBusView().getBus().getId());
            BusInnerNode busNodeB = vlNodeB.getBusInnerNode(terminalB.getBusView().getBus().getId());
            BusInnerNode busNodeC = vlNodeB.getBusInnerNode(terminalB.getBusView().getBus().getId());

            String twtId = twt.getId();
            String twtName = twt.getNameOrId();
            ThreeWtNode tn = new ThreeWtNode(idProvider.createId(twt), twtId, twtName);
            graph.addNode(tn);
            graph.addEdge(vlNodeA, busNodeA, tn, new ThreeWtEdge(idProvider.createId(get3wtLeg(twt, side)), twtId, twtName, iidmSideToSide(side), vlNodeA.isVisible()));
            graph.addEdge(vlNodeB, busNodeB, tn, new ThreeWtEdge(idProvider.createId(get3wtLeg(twt, otherSide1)), twtId, twtName, iidmSideToSide(otherSide1), vlNodeB.isVisible()));
            graph.addEdge(vlNodeC, busNodeC, tn, new ThreeWtEdge(idProvider.createId(get3wtLeg(twt, otherSide2)), twtId, twtName, iidmSideToSide(otherSide2), vlNodeC.isVisible()));
        }

        @Override
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

            addEdge(terminal, otherSideTerminal, hvdcLine, BranchEdge.HVDC_LINE_EDGE, otherSide == HvdcLine.Side.ONE);
        }

        private void addEdge(Branch<?> branch, Branch.Side side, String edgeType) {
            Terminal terminalA = branch.getTerminal(side);
            Terminal terminalB = branch.getTerminal(side == Branch.Side.ONE ? Branch.Side.TWO : Branch.Side.ONE);

            addEdge(terminalA, terminalB, branch, edgeType, side == Branch.Side.TWO);
        }

        private void addEdge(Terminal terminalA, Terminal terminalB, Identifiable<?> identifiable, String edgeType, boolean terminalsInReversedOrder) {
            VoltageLevelNode vlNodeA = graph.getVoltageLevelNode(terminalA.getVoltageLevel().getId())
                    .orElseThrow(() -> new PowsyblException("Cannot add edge, corresponding voltage level is unknown: '" + terminalA.getVoltageLevel().getId() + "'"));
            VoltageLevelNode vlNodeB = getOrCreateVoltageLevelNode(terminalB);

            BusInnerNode busNodeA = terminalA.isConnected() ? vlNodeA.getBusInnerNode(terminalA.getBusView().getBus().getId()) : null;
            BusInnerNode busNodeB = terminalB.isConnected() ? vlNodeB.getBusInnerNode(terminalB.getBusView().getBus().getId()) : null;

            BranchEdge edge = new BranchEdge(idProvider.createId(identifiable), identifiable.getId(), identifiable.getNameOrId(), edgeType);
            if (!terminalsInReversedOrder) {
                graph.addEdge(vlNodeA, busNodeA, vlNodeB, busNodeB, edge);
            } else {
                graph.addEdge(vlNodeB, busNodeB, vlNodeA, busNodeA, edge);
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
            VoltageLevelNode invisibleVlNode = new VoltageLevelNode(idProvider.createId(vl), vl.getId(), vl.getNameOrId(), false);
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
