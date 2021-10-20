/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.nad.model.Edge;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Node;
import com.powsybl.nad.model.Point;
import com.powsybl.sld.force.layout.ForceLayout;
import com.powsybl.sld.force.layout.Vector;

import java.util.Arrays;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class BasicForceLayout implements Layout {

    @Override
    public void run(Graph graph, LayoutParameters layoutParameters) {
        ForceLayout<Node, Edge> forceLayout = new ForceLayout<>(graph.toJgrapht())
                .setMaxSpeed(1e3);
        forceLayout.execute();

        double[] dims = new double[4];
        graph.getNodesStream().forEach(node -> {
            Vector p = forceLayout.getStablePosition(node);
            node.setPosition(p.getX(), p.getY());
            dims[0] = Math.min(dims[0], p.getX());
            dims[1] = Math.max(dims[1], p.getX());
            dims[2] = Math.min(dims[2], p.getY());
            dims[3] = Math.max(dims[3], p.getY());
        });
        graph.setDimensions(dims[0], dims[1], dims[2], dims[3]);

        graph.getEdgesStream().forEach(edge ->
                edge.setPolyline(Arrays.asList(
                        new Point(edge.getNode1().getX(), edge.getNode1().getY()),
                        new Point(edge.getNode2().getX(), edge.getNode2().getY())))
        );

    }
}
