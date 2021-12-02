/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.build.iidm;

import com.powsybl.iidm.network.*;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class VoltageLevelFilter implements Predicate<VoltageLevel> {

    public static Predicate<VoltageLevel> NO_FILTER = voltageLevel -> true;

    private final Set<VoltageLevel> voltageLevels;

    public VoltageLevelFilter(Set<VoltageLevel> voltageLevels) {
        this.voltageLevels = voltageLevels;
    }

    @Override
    public boolean test(VoltageLevel voltageLevel) {
        return voltageLevels.contains(voltageLevel);
    }

    public static VoltageLevelFilter createVoltageLevelDepthFilter(Network network, String voltageLevelId, int depth) {
        Set<VoltageLevel> voltageLevels = new HashSet<>();
        VoltageLevel vl = network.getVoltageLevel(voltageLevelId);
        traverseVoltageLevels(vl, depth, voltageLevels);
        return new VoltageLevelFilter(voltageLevels);
    }

    private static void traverseVoltageLevels(VoltageLevel vl, int depth, Set<VoltageLevel> visitedVoltageLevels) {
        if (visitedVoltageLevels.contains(vl) || depth <= 0) {
            return;
        }
        visitedVoltageLevels.add(vl);
        vl.visitEquipments(new VlVisitor(depth - 1, visitedVoltageLevels));
    }

    private static class VlVisitor extends DefaultTopologyVisitor {
        private final int depth;
        private final Set<VoltageLevel> visitedVoltageLevels;

        public VlVisitor(int depth, Set<VoltageLevel> visitedVoltageLevels) {
            this.depth = depth;
            this.visitedVoltageLevels = visitedVoltageLevels;
        }

        @Override
        public void visitLine(Line line, Branch.Side side) {
            visitBranch(line, side);
        }

        @Override
        public void visitTwoWindingsTransformer(TwoWindingsTransformer twt, Branch.Side side) {
            visitBranch(twt, side);
        }

        @Override
        public void visitThreeWindingsTransformer(ThreeWindingsTransformer twt, ThreeWindingsTransformer.Side side) {
            if (side == ThreeWindingsTransformer.Side.ONE) {
                visitTerminal(twt.getTerminal(ThreeWindingsTransformer.Side.TWO));
                visitTerminal(twt.getTerminal(ThreeWindingsTransformer.Side.THREE));
            } else if (side == ThreeWindingsTransformer.Side.TWO) {
                visitTerminal(twt.getTerminal(ThreeWindingsTransformer.Side.ONE));
                visitTerminal(twt.getTerminal(ThreeWindingsTransformer.Side.THREE));
            } else {
                visitTerminal(twt.getTerminal(ThreeWindingsTransformer.Side.ONE));
                visitTerminal(twt.getTerminal(ThreeWindingsTransformer.Side.TWO));
            }
        }

        private void visitBranch(Branch<?> branch, Branch.Side side) {
            Branch.Side otherSide = side == Branch.Side.ONE ? Branch.Side.TWO : Branch.Side.ONE;
            visitTerminal(branch.getTerminal(otherSide));
        }

        private void visitTerminal(Terminal terminal) {
            traverseVoltageLevels(terminal.getVoltageLevel(), depth, visitedVoltageLevels);
        }
    }

}
