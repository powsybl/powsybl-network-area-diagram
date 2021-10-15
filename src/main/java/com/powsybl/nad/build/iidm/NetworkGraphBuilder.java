/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.build.iidm;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.nad.build.GraphBuilder;
import com.powsybl.nad.model.*;

import java.util.Optional;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class NetworkGraphBuilder implements GraphBuilder {
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
                    .map(bus -> new BusNode())
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
                graph.addEdge(new LineEdge(idProvider.createId(line), line.getId(), vlNode1.get(), vlNode2.get()));
            } else {
                throw new PowsyblException("Cannot add line, voltage level unknown");
            }
        }

        @Override
        public void visitTwoWindingsTransformer(TwoWindingsTransformer twoWindingsTransformer, Branch.Side side) {
            Optional<VoltageLevelNode> vlNode1 = graph.getVoltageLevelNode(twoWindingsTransformer.getTerminal1().getVoltageLevel().getId());
            Optional<VoltageLevelNode> vlNode2 = graph.getVoltageLevelNode(twoWindingsTransformer.getTerminal2().getVoltageLevel().getId());
            if (vlNode1.isPresent() && vlNode2.isPresent()) {
                graph.addEdge(new TwoWindingsTransformerEdge(idProvider.createId(twoWindingsTransformer), twoWindingsTransformer.getId(), vlNode1.get(), vlNode2.get()));
            } else {
                throw new PowsyblException("Cannot add line, one voltage level unknown");
            }
        }

        @Override
        public void visitThreeWindingsTransformer(ThreeWindingsTransformer threeWindingsTransformer, ThreeWindingsTransformer.Side side) {
            Optional<VoltageLevelNode> vlNode1 = graph.getVoltageLevelNode(threeWindingsTransformer.getTerminals().get(0).getVoltageLevel().getId());
            Optional<VoltageLevelNode> vlNode2 = graph.getVoltageLevelNode(threeWindingsTransformer.getTerminals().get(1).getVoltageLevel().getId());
            Optional<VoltageLevelNode> vlNode3 = graph.getVoltageLevelNode(threeWindingsTransformer.getTerminals().get(2).getVoltageLevel().getId());
            if (vlNode1.isPresent() && vlNode2.isPresent() && vlNode3.isPresent()) {
                graph.addEdge(new ThreeWindingsTransformerEdge(idProvider.createId(threeWindingsTransformer), threeWindingsTransformer.getId(), vlNode1.get(), vlNode2.get(), vlNode3.get()));
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
