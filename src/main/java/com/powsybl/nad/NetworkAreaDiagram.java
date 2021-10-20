/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad;

import com.powsybl.iidm.network.Network;
import com.powsybl.nad.build.iidm.IdProvider;
import com.powsybl.nad.build.iidm.IntIdProvider;
import com.powsybl.nad.build.iidm.NetworkGraphBuilder;
import com.powsybl.nad.layout.BasicForceLayoutFactory;
import com.powsybl.nad.layout.LayoutFactory;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.svg.DefaultStyleProvider;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.SvgWriter;

import java.nio.file.Path;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class NetworkAreaDiagram {

    private final Network network;

    public NetworkAreaDiagram(Network network) {
        this.network = network;
    }

    public void draw(Path svgFile) {
        draw(svgFile, new LayoutParameters());
    }

    public void draw(Path svgFile, LayoutParameters layoutParameters) {
        draw(svgFile, layoutParameters, new SvgParameters());
    }

    public void draw(Path svgFile, LayoutParameters layoutParameters, SvgParameters svgParameters) {
        draw(svgFile, layoutParameters, svgParameters, new DefaultStyleProvider());
    }

    public void draw(Path svgFile, LayoutParameters layoutParameters, SvgParameters svgParameters,
                                   StyleProvider styleProvider) {
        draw(svgFile, layoutParameters, svgParameters, styleProvider, new BasicForceLayoutFactory());
    }

    public void draw(Path svgFile, LayoutParameters layoutParameters, SvgParameters svgParameters,
                                   StyleProvider styleProvider, LayoutFactory layoutFactory) {
        draw(svgFile, layoutParameters, svgParameters, styleProvider, layoutFactory, new IntIdProvider());
    }

    public void draw(Path svgFile, LayoutParameters layoutParameters, SvgParameters svgParameters,
                                   StyleProvider styleProvider, LayoutFactory layoutFactory, IdProvider idProvider) {
        Graph graph = new NetworkGraphBuilder(network, idProvider).buildGraph();
        layoutFactory.create().run(graph, layoutParameters);
        new SvgWriter(svgParameters, styleProvider).writeSvg(graph, svgFile);
    }
}
