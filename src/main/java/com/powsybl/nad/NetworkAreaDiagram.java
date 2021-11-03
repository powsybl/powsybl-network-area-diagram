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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class NetworkAreaDiagram {

    private final Network network;

    public NetworkAreaDiagram(Network network) {
        this.network = Objects.requireNonNull(network);
    }

    public void draw(Path svgFile) {
        draw(svgFile, new SvgParameters());
    }

    public void draw(Path svgFile, SvgParameters svgParameters) {
        draw(svgFile, svgParameters, new LayoutParameters());
    }

    public void draw(Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters) {
        draw(svgFile, svgParameters, layoutParameters, new DefaultStyleProvider());
    }

    public void draw(Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters,
                                   StyleProvider styleProvider) {
        draw(svgFile, svgParameters, layoutParameters, styleProvider, new BasicForceLayoutFactory());
    }

    public void draw(Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters,
                                   StyleProvider styleProvider, LayoutFactory layoutFactory) {
        draw(svgFile, svgParameters, layoutParameters, styleProvider, layoutFactory, new IntIdProvider());
    }

    public void draw(Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters,
                                   StyleProvider styleProvider, LayoutFactory layoutFactory, IdProvider idProvider) {
        Objects.requireNonNull(svgFile);
        Objects.requireNonNull(layoutParameters);
        Objects.requireNonNull(svgParameters);
        Objects.requireNonNull(styleProvider);
        Objects.requireNonNull(layoutFactory);
        Objects.requireNonNull(idProvider);

        Graph graph = new NetworkGraphBuilder(network, idProvider).buildGraph();
        layoutFactory.create().run(graph, layoutParameters);
        new SvgWriter(svgParameters, styleProvider).writeSvg(graph, svgFile);
    }

    public void draw(OutputStream os) {
        draw(os, new SvgParameters());
    }

    public void draw(OutputStream os, SvgParameters svgParameters) {
        draw(os, svgParameters, new LayoutParameters());
    }

    public void draw(OutputStream os, SvgParameters svgParameters, LayoutParameters layoutParameters) {
        draw(os, svgParameters, layoutParameters, new DefaultStyleProvider());
    }

    public void draw(OutputStream os, SvgParameters svgParameters, LayoutParameters layoutParameters,
                     StyleProvider styleProvider) {
        draw(os, svgParameters, layoutParameters, styleProvider, new BasicForceLayoutFactory());
    }

    public void draw(OutputStream os, SvgParameters svgParameters, LayoutParameters layoutParameters,
                     StyleProvider styleProvider, LayoutFactory layoutFactory) {
        draw(os, svgParameters, layoutParameters, styleProvider, layoutFactory, new IntIdProvider());
    }

    public void draw(OutputStream os, SvgParameters svgParameters, LayoutParameters layoutParameters,
                     StyleProvider styleProvider, LayoutFactory layoutFactory, IdProvider idProvider) {
        Graph graph = new NetworkGraphBuilder(network, idProvider).buildGraph();
        layoutFactory.create().run(graph, layoutParameters);
        new SvgWriter(svgParameters, styleProvider).writeSvg(graph, os);
    }

    public String drawToString(SvgParameters svgParameters) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            draw(os, svgParameters);
            os.flush();
            return os.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
