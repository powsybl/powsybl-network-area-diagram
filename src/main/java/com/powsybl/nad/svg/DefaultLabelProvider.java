/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.nad.model.*;

import java.util.Arrays;
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
        Terminal terminal = branch.getTerminal(getSideFromNadSide(side));
        return Arrays.asList(new EdgeInfo(EdgeInfo.ACTIVE_POWER, terminal.getP()),
                new EdgeInfo(EdgeInfo.REACTIVE_POWER, terminal.getQ()));
    }

    private static Branch.Side getSideFromNadSide(BranchEdge.Side side) {
        return side == BranchEdge.Side.ONE ? Branch.Side.ONE : Branch.Side.TWO;
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
