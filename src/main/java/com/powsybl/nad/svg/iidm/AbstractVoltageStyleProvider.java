/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg.iidm;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.model.Edge;
import com.powsybl.nad.model.ThreeWtEdge;
import com.powsybl.nad.model.ThreeWtNode;
import com.powsybl.nad.svg.AbstractStyleProvider;
import com.powsybl.nad.utils.iidm.IidmUtils;

import java.util.Optional;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractVoltageStyleProvider extends AbstractStyleProvider {

    protected final Network network;

    protected AbstractVoltageStyleProvider(Network network) {
        this.network = network;
    }

    public AbstractVoltageStyleProvider(Network network, BaseVoltagesConfig baseVoltageStyle) {
        super(baseVoltageStyle);
        this.network = network;
    }

    @Override
    public Optional<String> getThreeWtNodeBackgroundStyle(ThreeWtNode threeWtNode) {
        return Optional.of(CLASSES_PREFIX + "3wt-bg");
    }

    @Override
    protected Optional<String> getBaseVoltageStyle(ThreeWtNode threeWtNode, ThreeWtEdge.Side side) {
        Terminal terminal = network.getThreeWindingsTransformer(threeWtNode.getEquipmentId())
                .getTerminal(IidmUtils.getIidmSideFromThreeWtEdgeSide(side));
        return getBaseVoltageStyle(terminal);
    }

    @Override
    protected boolean isDisconnected(Edge edge) {
        if (edge instanceof ThreeWtEdge) {
            ThreeWtEdge twtEdge = (ThreeWtEdge) edge;
            Terminal terminal = network.getThreeWindingsTransformer(twtEdge.getEquipmentId())
                    .getTerminal(IidmUtils.getIidmSideFromThreeWtEdgeSide(twtEdge.getSide()));
            return terminal == null || !terminal.isConnected();
        }
        if (edge instanceof BranchEdge) {
            return isDisconnected((BranchEdge) edge, BranchEdge.Side.ONE) && isDisconnected((BranchEdge) edge, BranchEdge.Side.TWO);
        }
        return false;
    }

    @Override
    protected boolean isDisconnected(BranchEdge edge, BranchEdge.Side side) {
        Terminal terminal = IidmUtils.getTerminalFromEdge(network, edge, side);
        return terminal == null || !terminal.isConnected();
    }

    @Override
    protected boolean isDisconnected(ThreeWtNode threeWtNode, ThreeWtEdge.Side side) {
        Terminal terminal = network.getThreeWindingsTransformer(threeWtNode.getEquipmentId())
                .getTerminal(IidmUtils.getIidmSideFromThreeWtEdgeSide(side));
        return terminal == null || !terminal.isConnected();
    }

    @Override
    protected Optional<String> getBaseVoltageStyle(Edge edge) {
        if (edge instanceof BranchEdge) {
            String branchType = ((BranchEdge) edge).getType();
            if (branchType.equals(BranchEdge.LINE_EDGE)) {
                return getLineEdgeBaseVoltageStyle(edge);
            } else if (branchType.equals(BranchEdge.HVDC_LINE_EDGE)) {
                return Optional.of(HVDC_EDGE_CLASS);
            }
        } else if (edge instanceof ThreeWtEdge) {
            Terminal terminal = network.getThreeWindingsTransformer(edge.getEquipmentId())
                    .getTerminal(IidmUtils.getIidmSideFromThreeWtEdgeSide(((ThreeWtEdge) edge).getSide()));
            return getBaseVoltageStyle(terminal);
        }

        return Optional.empty();
    }

    @Override
    protected Optional<String> getBaseVoltageStyle(BranchEdge edge, BranchEdge.Side side) {
        if (edge.getType().equals(BranchEdge.TWO_WT_EDGE) || edge.getType().equals(BranchEdge.HVDC_LINE_EDGE)) {
            Terminal terminal = IidmUtils.getTerminalFromEdge(network, edge, side);
            return getBaseVoltageStyle(terminal);
        }
        return Optional.empty();
    }

    protected Optional<String> getLineEdgeBaseVoltageStyle(Edge edge) {
        Branch<?> branch = network.getBranch(edge.getEquipmentId());
        if (branch.getTerminal1() != null && branch.getTerminal1().isConnected()) {
            return getBaseVoltageStyle(branch.getTerminal1());
        }
        if (branch.getTerminal2() != null && branch.getTerminal2().isConnected()) {
            return getBaseVoltageStyle(branch.getTerminal2());
        }
        return Optional.empty();
    }

    protected abstract Optional<String> getBaseVoltageStyle(Terminal terminal);
}
