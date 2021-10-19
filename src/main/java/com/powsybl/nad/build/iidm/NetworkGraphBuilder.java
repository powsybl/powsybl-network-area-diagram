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
        this.network = network;
        this.idProvider = idProvider;
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
                    .map(bus -> new BusNode(idProvider.createId(bus), bus.getId()))
                    .forEach(vlNode::addBusNode);
        }
    }

    private void addGraphEdges(Graph graph) {
        for (VoltageLevel voltageLevel : network.getVoltageLevels()) {
            voltageLevel.visitEquipments(new VisitorBuilder(graph));
        }
    }

    private class VisitorBuilder implements TopologyVisitor {
        private final Graph graph;

        public VisitorBuilder(Graph graph) {
            this.graph = graph;
        }

        @Override
        public void visitBusbarSection(BusbarSection busbarSection) {
        }

        @Override
        public void visitLine(Line line, Branch.Side side) {
            Optional<VoltageLevelNode> vlNode1 = graph.getVoltageLevelNode(line.getTerminal1().getVoltageLevel().getId());
            Optional<VoltageLevelNode> vlNode2 = graph.getVoltageLevelNode(line.getTerminal2().getVoltageLevel().getId());
            if (vlNode1.isPresent() && vlNode2.isPresent()) {
                graph.addEdge(new Edge(idProvider.createId(line), line.getId(), line.getNameOrId(), vlNode1.get(), vlNode2.get()));
            } else {
                throw new PowsyblException("Cannot add line, voltage level unknown");
            }
        }

        @Override
        public void visitTwoWindingsTransformer(TwoWindingsTransformer twoWindingsTransformer, Branch.Side side) {
            Optional<VoltageLevelNode> vlNode1 = graph.getVoltageLevelNode(twoWindingsTransformer.getTerminal1().getVoltageLevel().getId());
            Optional<VoltageLevelNode> vlNode2 = graph.getVoltageLevelNode(twoWindingsTransformer.getTerminal2().getVoltageLevel().getId());
            if (vlNode1.isPresent() && vlNode2.isPresent()) {
                Edge edge = new Edge(idProvider.createId(twoWindingsTransformer), twoWindingsTransformer.getId(),
                        twoWindingsTransformer.getNameOrId(), vlNode1.get(), vlNode2.get());
                graph.addEdge(edge);
            } else {
                throw new PowsyblException("Cannot add line, one voltage level unknown");
            }
        }

        @Override
        public void visitThreeWindingsTransformer(ThreeWindingsTransformer threeWindingsTransformer, ThreeWindingsTransformer.Side side) {
            List<? extends Terminal> terminals = threeWindingsTransformer.getTerminals();
            Optional<VoltageLevelNode> vlNode1 = graph.getVoltageLevelNode(terminals.get(0).getVoltageLevel().getId());
            Optional<VoltageLevelNode> vlNode2 = graph.getVoltageLevelNode(terminals.get(1).getVoltageLevel().getId());
            Optional<VoltageLevelNode> vlNode3 = graph.getVoltageLevelNode(terminals.get(2).getVoltageLevel().getId());
            if (vlNode1.isPresent() && vlNode2.isPresent() && vlNode3.isPresent()) {
                String transformerId = threeWindingsTransformer.getId();
                String transformerName = threeWindingsTransformer.getNameOrId();
                TransformerNode tn = new TransformerNode(idProvider.createId(threeWindingsTransformer), transformerId, threeWindingsTransformer.getNameOrId());
                graph.addEdge(new Edge(idProvider.createId(threeWindingsTransformer.getLeg1()), transformerId + LEG1_SUFFIX, transformerName, vlNode1.get(), tn));
                graph.addEdge(new Edge(idProvider.createId(threeWindingsTransformer.getLeg2()), transformerId + LEG2_SUFFIX, transformerName, vlNode2.get(), tn));
                graph.addEdge(new Edge(idProvider.createId(threeWindingsTransformer.getLeg3()), transformerId + LEG3_SUFFIX, transformerName, vlNode3.get(), tn));
            } else {
                throw new PowsyblException("Cannot add three-windings transformer, one voltage level unknown");
            }
        }

        @Override
        public void visitGenerator(Generator generator) {
        }

        @Override
        public void visitLoad(Load load) {
        }

        @Override
        public void visitShuntCompensator(ShuntCompensator shuntCompensator) {
        }

        @Override
        public void visitDanglingLine(DanglingLine danglingLine) {
        }

        @Override
        public void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator) {
        }
    }
}
