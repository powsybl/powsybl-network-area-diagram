/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg.iidm;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.nad.model.*;
import com.powsybl.nad.svg.EdgeInfo;
import com.powsybl.nad.svg.LabelProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class DefaultLabelProvider implements LabelProvider {
    private final Network network;

    public DefaultLabelProvider(Network network) {
        this.network = network;
    }

    @Override
    public List<EdgeInfo> getEdgeInfos(Graph graph, BranchEdge edge, BranchEdge.Side side) {
        Branch<?> branch = network.getBranch(edge.getEquipmentId());
        if (branch == null) {
            throw new PowsyblException("Unknown branch '" + edge.getEquipmentId() + "'");
        }
        Terminal terminal = branch.getTerminal(SideUtils.getIidmSideFromBranchEdgeSide(side));
        return getEdgeInfos(terminal);
    }

    @Override
    public List<EdgeInfo> getEdgeInfos(Graph graph, ThreeWtEdge edge) {
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(edge.getEquipmentId());
        if (transformer == null) {
            throw new PowsyblException("Unknown three windings transformer '" + edge.getEquipmentId() + "'");
        }
        Terminal terminal = transformer.getTerminal(SideUtils.getIidmSideFromThreeWtEdgeSide(edge.getSide()));
        return getEdgeInfos(terminal);
    }

    private List<EdgeInfo> getEdgeInfos(Terminal terminal) {
        if (terminal == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(new EdgeInfo(EdgeInfo.ACTIVE_POWER, terminal.getP()),
                new EdgeInfo(EdgeInfo.REACTIVE_POWER, terminal.getQ()));
    }

    @Override
    public String getArrowPathDIn() {
        return "M-0.1 -0.1 H0.1 L0 0.1z";
    }

    @Override
    public String getArrowPathDOut() {
        return "M-0.1 0.1 H0.1 L0 -0.1z";
    }
}
