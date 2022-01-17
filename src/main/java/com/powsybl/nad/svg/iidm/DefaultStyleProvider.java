/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg.iidm;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.iidm.network.*;
import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.model.Edge;
import com.powsybl.nad.model.ThreeWtEdge;
import com.powsybl.nad.model.ThreeWtNode;
import com.powsybl.nad.svg.AbstractStyleProvider;
import com.powsybl.nad.svg.EdgeInfo;

import java.util.*;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class DefaultStyleProvider extends AbstractStyleProvider {

    private final Network network;

    public DefaultStyleProvider(Network network) {
        super();
        this.network = network;
    }

    public DefaultStyleProvider(Network network, BaseVoltagesConfig baseVoltageStyle) {
        super(baseVoltageStyle);
        this.network = network;
    }

    @Override
    public List<String> getCssFilenames() {
        return Collections.singletonList("defaultStyle.css");
    }

    @Override
    public List<String> getEdgeInfoStyles(EdgeInfo info) {
        List<String> styles = new LinkedList<>();
        if (info.getInfoType().equals(EdgeInfo.ACTIVE_POWER)) {
            styles.add(CLASSES_PREFIX + "active");
        } else if (info.getInfoType().equals(EdgeInfo.REACTIVE_POWER)) {
            styles.add(CLASSES_PREFIX + "reactive");
        }
        info.getDirection().ifPresent(direction -> styles.add(
                CLASSES_PREFIX + (direction == EdgeInfo.Direction.IN ? "state-in" : "state-out")));
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
        return terminal != null
                ? getBaseVoltageStyle(terminal.getVoltageLevel().getNominalV())
                : Optional.empty();
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
                if (branch.getTerminal1() != null) {
                    terminal = branch.getTerminal1();
                } else if (branch.getTerminal2() != null) {
                    terminal = branch.getTerminal2();
                }
            } else if (branchEdge.getType().equals(BranchEdge.HVDC_LINE_EDGE)) {
                return Optional.of(HVDC_EDGE_CLASS);
            }
        } else if (edge instanceof ThreeWtEdge) {
            terminal = network.getThreeWindingsTransformer(edge.getEquipmentId())
                    .getTerminal(IidmUtils.getIidmSideFromThreeWtEdgeSide(((ThreeWtEdge) edge).getSide()));
        }

        return terminal != null
                ? getBaseVoltageStyle(terminal.getVoltageLevel().getNominalV())
                : Optional.empty();
    }

    @Override
    protected Optional<String> getBaseVoltageStyle(BranchEdge edge, BranchEdge.Side side) {
        if (edge.getType().equals(BranchEdge.TWO_WT_EDGE) || edge.getType().equals(BranchEdge.HVDC_LINE_EDGE)) {
            Terminal terminal = IidmUtils.getTerminalFromEdge(network, edge, side);
            double nominalVoltage = terminal == null ? 0 : terminal.getVoltageLevel().getNominalV();
            return getBaseVoltageStyle(nominalVoltage);
        }
        return Optional.empty();
    }
}
