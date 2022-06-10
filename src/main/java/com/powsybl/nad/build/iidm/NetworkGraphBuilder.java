/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.build.iidm;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.nad.model.ThreeWtNode;
import com.powsybl.iidm.network.*;
import com.powsybl.nad.build.GraphBuilder;
import com.powsybl.nad.model.*;
import com.powsybl.nad.utils.iidm.IidmUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class NetworkGraphBuilder implements GraphBuilder {

    private final Graph graph;
    private final IdProvider idProvider;
    private final Iterable<VoltageLevel> voltageLevels;

    public NetworkGraphBuilder(Iterable<VoltageLevel> voltageLevels) {
        this(voltageLevels, new IntIdProvider());
    }

    public NetworkGraphBuilder(Iterable<VoltageLevel> voltageLevels, IdProvider idProvider) {
        this(voltageLevels, idProvider, new Graph());
    }

    public NetworkGraphBuilder(Iterable<VoltageLevel> voltageLevels, IdProvider idProvider, Graph existingGraph) {
        this.graph = Objects.requireNonNull(existingGraph);
        this.voltageLevels = Objects.requireNonNull(voltageLevels);
        this.idProvider = Objects.requireNonNull(idProvider);
    }

    @Override
    public Graph buildGraph() {
        addGraphNodes(graph);
        addGraphEdges(graph);
        return graph;
    }

    private void addGraphNodes(Graph graph) {
        voltageLevels.forEach(vl -> createVoltageLevelNode(vl, graph, true));
    }

    private void addGraphEdges(Graph graph) {
        voltageLevels.forEach(vl -> vl.visitEquipments(new VisitorBuilder(graph)));
    }

    private VoltageLevelNode createVoltageLevelNode(VoltageLevel vl, Graph graph, boolean visible) {
        VoltageLevelNode vlNode;
        Optional<VoltageLevelNode> oVl = graph.getVoltageLevelNode(vl.getId());
        if (oVl.isPresent()) {
            vlNode = oVl.get();
            if (!vlNode.isVisible() && visible) {
                vlNode.setVisible(true);
                graph.addTextNode(vlNode);
            }
        } else {
            vlNode = new VoltageLevelNode(idProvider.createId(vl), vl.getId(), vl.getNameOrId(), vl.isFictitious(), visible);
            vl.getBusView().getBusStream()
                    .map(bus -> new BusNode(idProvider.createId(bus), bus.getId()))
                    .forEach(vlNode::addBusNode);
            graph.addNode(vlNode);
            if (visible) {
                graph.addTextNode(vlNode);
            }
        }
        return vlNode;
    }

    private class VisitorBuilder extends DefaultTopologyVisitor {
        private final Graph graph;

        public VisitorBuilder(Graph graph) {
            this.graph = graph;
        }

        @Override
        public void visitLine(Line line, Branch.Side side) {
            addEdge(line, side, BranchEdge.LINE_EDGE);
        }

        @Override
        public void visitTwoWindingsTransformer(TwoWindingsTransformer twt, Branch.Side side) {
            addEdge(twt, side, BranchEdge.TWO_WT_EDGE);
        }

        @Override
        public void visitThreeWindingsTransformer(ThreeWindingsTransformer twt, ThreeWindingsTransformer.Side side) {
            // check if the transformer was not already added (at the other sides of the transformer)
            if (graph.containsNode(twt.getId())) {
                return;
            }

            ThreeWtNode tn = new ThreeWtNode(idProvider.createId(twt), twt.getId(), twt.getNameOrId());
            graph.addNode(tn);

            for (ThreeWindingsTransformer.Side s : getSidesArray(side)) {
                addThreeWtEdge(twt, tn, s);
            }
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
            // check if the edge was not already added (at the other side of the transformer)
            if (graph.containsEdge(branch.getId())) {
                return;
            }

            Terminal terminalA = branch.getTerminal(side);
            Terminal terminalB = branch.getTerminal(IidmUtils.getOpposite(side));

            addEdge(terminalA, terminalB, branch, edgeType, side == Branch.Side.TWO);
        }

        private void addEdge(Terminal terminalA, Terminal terminalB, Identifiable<?> identifiable, String edgeType, boolean terminalsInReversedOrder) {
            VoltageLevelNode vlNodeA = graph.getVoltageLevelNode(terminalA.getVoltageLevel().getId())
                    .orElseThrow(() -> new PowsyblException("Cannot add edge, corresponding voltage level is unknown: '" + terminalA.getVoltageLevel().getId() + "'"));
            VoltageLevelNode vlNodeB = getOrCreateInvisibleVoltageLevelNode(terminalB);

            BusNode busNodeA = getBusNode(terminalA);
            BusNode busNodeB = getBusNode(terminalB);

            BranchEdge edge = new BranchEdge(idProvider.createId(identifiable), identifiable.getId(), identifiable.getNameOrId(), edgeType);
            if (!terminalsInReversedOrder) {
                graph.addEdge(vlNodeA, busNodeA, vlNodeB, busNodeB, edge);
            } else {
                graph.addEdge(vlNodeB, busNodeB, vlNodeA, busNodeA, edge);
            }
        }

        private void addThreeWtEdge(ThreeWindingsTransformer twt, ThreeWtNode tn, ThreeWindingsTransformer.Side side) {
            Terminal terminal = twt.getTerminal(side);
            VoltageLevelNode vlNode = getOrCreateInvisibleVoltageLevelNode(terminal);
            ThreeWtEdge edge = new ThreeWtEdge(idProvider.createId(IidmUtils.get3wtLeg(twt, side)),
                    twt.getId(), twt.getNameOrId(), IidmUtils.getThreeWtEdgeSideFromIidmSide(side), vlNode.isVisible());
            graph.addEdge(vlNode, getBusNode(terminal), tn, edge);
        }

        private ThreeWindingsTransformer.Side[] getSidesArray(ThreeWindingsTransformer.Side sideA) {
            ThreeWindingsTransformer.Side sideB;
            ThreeWindingsTransformer.Side sideC;
            if (sideA == ThreeWindingsTransformer.Side.ONE) {
                sideB = ThreeWindingsTransformer.Side.TWO;
                sideC = ThreeWindingsTransformer.Side.THREE;
            } else if (sideA == ThreeWindingsTransformer.Side.TWO) {
                sideB = ThreeWindingsTransformer.Side.ONE;
                sideC = ThreeWindingsTransformer.Side.THREE;
            } else {
                sideB = ThreeWindingsTransformer.Side.ONE;
                sideC = ThreeWindingsTransformer.Side.TWO;
            }
            return new ThreeWindingsTransformer.Side[] {sideA, sideB, sideC};
        }

        private BusNode getBusNode(Terminal terminal) {
            Bus connectableBusA = terminal.getBusView().getConnectableBus();
            if (connectableBusA == null) {
                graph.getVoltageLevelNode(terminal.getVoltageLevel().getId()).ifPresent(vlNode -> vlNode.setHasUnknownBusNode(true));
                return BusNode.UNKNOWN;
            }
            return graph.getBusNode(connectableBusA.getId());
        }

        private VoltageLevelNode getOrCreateInvisibleVoltageLevelNode(Terminal terminal) {
            VoltageLevel vl = terminal.getVoltageLevel();
            return graph.getVoltageLevelNode(vl.getId()).orElseGet(() -> createVoltageLevelNode(vl, graph, false));
        }
    }
}
