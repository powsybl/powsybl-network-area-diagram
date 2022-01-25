/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg.iidm;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.nad.model.*;
import com.powsybl.nad.svg.AbstractStyleProvider;
import com.powsybl.nad.utils.iidm.IidmUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class NominalVoltageStyleProvider extends AbstractStyleProvider {

    protected final Network network;

    public NominalVoltageStyleProvider(Network network) {
        super();
        this.network = network;
    }

    public NominalVoltageStyleProvider(Network network, BaseVoltagesConfig baseVoltageStyle) {
        super(baseVoltageStyle);
        this.network = network;
    }

    @Override
    public List<String> getCssFilenames() {
        return Collections.singletonList("nominalStyle.css");
    }

    @Override
    public List<String> getNodeStyleClasses(Node node) {
        List<String> styles = new ArrayList<>();
        if (node instanceof VoltageLevelNode) {
            double nominalV = network.getVoltageLevel(node.getEquipmentId()).getNominalV();
            getBaseVoltageStyle(nominalV).ifPresent(styles::add);
        }
        return styles;
    }

    @Override
    public Optional<String> getThreeWtNodeBackgroundStyle(ThreeWtNode threeWtNode) {
        return Optional.of(CLASSES_PREFIX + "3wt-bg");
    }

    @Override
    public Optional<String> getThreeWtNodeStyle(ThreeWtNode threeWtNode, ThreeWtEdge.Side side) {
        Terminal terminal = network.getThreeWindingsTransformer(threeWtNode.getEquipmentId())
                .getTerminal(IidmUtils.getIidmSideFromThreeWtEdgeSide(side));
        return getBaseVoltageStyle(terminal);
    }

    @Override
    protected boolean isDisconnectedBranch(BranchEdge edge, BranchEdge.Side side) {
        Terminal terminal = IidmUtils.getTerminalFromEdge(network, edge, side);
        return terminal == null || !terminal.isConnected();
    }

    @Override
    protected Optional<String> getBaseVoltageStyle(Edge edge) {
        Terminal terminal = null;
        if (edge instanceof BranchEdge) {
            BranchEdge branchEdge = (BranchEdge) edge;
            if (branchEdge.getType().equals(BranchEdge.LINE_EDGE)) {
                Branch<?> branch = network.getBranch(edge.getEquipmentId());
                Terminal terminal1 = branch.getTerminal1();
                Terminal terminal2 = branch.getTerminal2();
                if (terminal1 != null && terminal1.isConnected()) {
                    terminal = terminal1;
                } else if (terminal2 != null && terminal2.isConnected()) {
                    terminal = branch.getTerminal2();
                }
            } else if (branchEdge.getType().equals(BranchEdge.HVDC_LINE_EDGE)) {
                return Optional.of(HVDC_EDGE_CLASS);
            }
        } else if (edge instanceof ThreeWtEdge) {
            terminal = network.getThreeWindingsTransformer(edge.getEquipmentId())
                    .getTerminal(IidmUtils.getIidmSideFromThreeWtEdgeSide(((ThreeWtEdge) edge).getSide()));
        }

        return getBaseVoltageStyle(terminal);
    }

    @Override
    protected Optional<String> getBaseVoltageStyle(BranchEdge edge, BranchEdge.Side side) {
        if (edge.getType().equals(BranchEdge.TWO_WT_EDGE) || edge.getType().equals(BranchEdge.HVDC_LINE_EDGE)) {
            Terminal terminal = IidmUtils.getTerminalFromEdge(network, edge, side);
            return getBaseVoltageStyle(terminal);
        }
        return Optional.empty();
    }

    protected Optional<String> getBaseVoltageStyle(Terminal terminal) {
        if (terminal == null) {
            return Optional.empty();
        }
        return terminal.isConnected()
                ? getBaseVoltageStyle(terminal.getVoltageLevel().getNominalV())
                : Optional.of(DISCONNECTED_CLASS);
    }
}
