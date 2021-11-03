/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.build.iidm;

import com.powsybl.commons.PowsyblException;
import com.powsybl.nad.model.TransformerNode;
import com.powsybl.iidm.network.*;
import com.powsybl.nad.build.GraphBuilder;
import com.powsybl.nad.model.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class NetworkGraphBuilder implements GraphBuilder {

    private static final String LEG1_SUFFIX = "_leg1";
    private static final String LEG2_SUFFIX = "_leg2";
    private static final String LEG3_SUFFIX = "_leg3";

    private final Network network;
    private final IdProvider idProvider;

    public NetworkGraphBuilder(Network network, IdProvider idProvider) {
        this.network = Objects.requireNonNull(network);
        this.idProvider = Objects.requireNonNull(idProvider);
    }

    public NetworkGraphBuilder(Network network) {
        this(network, new IntIdProvider());
    }

    @Override
    public Graph buildGraph() {
        Graph graph = new Graph();
        addGraphNodes(graph);
        addGraphEdges(graph);
        return graph;
    }

    private void addGraphNodes(Graph graph) {
        for (VoltageLevel voltageLevel : network.getVoltageLevels()) {
            VoltageLevelNode vlNode = new VoltageLevelNode(idProvider.createId(voltageLevel),
                    voltageLevel.getId(), voltageLevel.getNameOrId(), voltageLevel.getNominalV());
            graph.addNode(vlNode);
            voltageLevel.getBusView().getBusStream()
                    .map(bus -> new BusInnerNode(idProvider.createId(bus), bus.getId()))
                    .forEach(vlNode::addBusNode);
        }
    }

    private void addGraphEdges(Graph graph) {
        for (VoltageLevel voltageLevel : network.getVoltageLevels()) {
            voltageLevel.visitEquipments(new VisitorBuilder(graph));
        }
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
            Optional<VoltageLevelNode> vlNode1 = graph.getVoltageLevelNode(line.getTerminal1().getVoltageLevel().getId());
            Optional<VoltageLevelNode> vlNode2 = graph.getVoltageLevelNode(line.getTerminal2().getVoltageLevel().getId());
            if (vlNode1.isEmpty() || vlNode2.isEmpty()) {
                throw new PowsyblException("Cannot add line, voltage level unknown");
            }
            graph.addEdge(vlNode1.get(), vlNode2.get(),
                    new BranchEdge(idProvider.createId(line), line.getId(), line.getNameOrId(),
                            line.getTerminal1().isConnected(), line.getTerminal2().isConnected()));
        }

        @Override
        public void visitTwoWindingsTransformer(TwoWindingsTransformer twt, Branch.Side side) {
            // check if the edge was not already added (at the other side of the transformer)
            if (graph.containsEdge(twt.getId())) {
                return;
            }
            Optional<VoltageLevelNode> vlNode1 = graph.getVoltageLevelNode(twt.getTerminal1().getVoltageLevel().getId());
            Optional<VoltageLevelNode> vlNode2 = graph.getVoltageLevelNode(twt.getTerminal2().getVoltageLevel().getId());
            if (vlNode1.isEmpty() || vlNode2.isEmpty()) {
                throw new PowsyblException("Cannot add line, one voltage level unknown");
            }
            BranchEdge edge = new BranchEdge(idProvider.createId(twt), twt.getId(), twt.getNameOrId(),
                    twt.getTerminal1().isConnected(), twt.getTerminal2().isConnected());
            graph.addEdge(vlNode1.get(), vlNode2.get(), edge);
        }

        @Override
        public void visitThreeWindingsTransformer(ThreeWindingsTransformer twt, ThreeWindingsTransformer.Side side) {
            // check if the transformer was not already added (at the other sides of the transformer)
            if (graph.containsNode(twt.getId())) {
                return;
            }
            List<? extends Terminal> terminals = twt.getTerminals();
            Optional<VoltageLevelNode> vlNode1 = graph.getVoltageLevelNode(terminals.get(0).getVoltageLevel().getId());
            Optional<VoltageLevelNode> vlNode2 = graph.getVoltageLevelNode(terminals.get(1).getVoltageLevel().getId());
            Optional<VoltageLevelNode> vlNode3 = graph.getVoltageLevelNode(terminals.get(2).getVoltageLevel().getId());
            if (vlNode1.isEmpty() || vlNode2.isEmpty() || vlNode3.isEmpty()) {
                throw new PowsyblException("Cannot add three-windings transformer, one voltage level unknown");
            }
            String twtId = twt.getId();
            String twtName = twt.getNameOrId();
            TransformerNode tn = new TransformerNode(idProvider.createId(twt), twtId, twtName);
            graph.addEdge(vlNode1.get(), tn, new TransformerEdge(idProvider.createId(twt.getLeg1()), twtId + LEG1_SUFFIX, twtName, twt.getLeg1().getTerminal().isConnected()));
            graph.addEdge(vlNode2.get(), tn, new TransformerEdge(idProvider.createId(twt.getLeg2()), twtId + LEG2_SUFFIX, twtName, twt.getLeg2().getTerminal().isConnected()));
            graph.addEdge(vlNode3.get(), tn, new TransformerEdge(idProvider.createId(twt.getLeg3()), twtId + LEG3_SUFFIX, twtName, twt.getLeg3().getTerminal().isConnected()));
        }
    }
}
