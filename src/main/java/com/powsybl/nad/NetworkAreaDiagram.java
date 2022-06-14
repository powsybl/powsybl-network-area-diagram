/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.nad.build.iidm.IdProvider;
import com.powsybl.nad.build.iidm.IntIdProvider;
import com.powsybl.nad.build.iidm.NetworkGraphBuilder;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.layout.BasicForceLayoutFactory;
import com.powsybl.nad.layout.LayoutFactory;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.svg.*;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.TopologicalStyleProvider;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class NetworkAreaDiagram {

    private final Network network;
    private Predicate<VoltageLevel> voltageLevelFilter;

    private Graph graph = new Graph();

    private final SvgParameters svgParameters;

    private final LayoutParameters layoutParameters;

    private final StyleProvider styleProvider;

    private final LabelProvider labelProvider;

    private final LayoutFactory layoutFactory;
    private final IdProvider idProvider;

    public NetworkAreaDiagram(Network network) {
        this(network, VoltageLevelFilter.NO_FILTER);
    }

    public NetworkAreaDiagram(Network network, String voltageLevelId, int depth) {
        this(network, VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public NetworkAreaDiagram(Network network, List<String> voltageLevelIds) {
        this(network, VoltageLevelFilter.createVoltageLevelsFilter(network, voltageLevelIds));
    }

    public NetworkAreaDiagram(Network network, List<String> voltageLevelIds, int depth) {
        this(network, VoltageLevelFilter.createVoltageLevelsDepthFilter(network, voltageLevelIds, depth));
    }

    public NetworkAreaDiagram(Network network, Predicate<VoltageLevel> voltageLevelFilter) {
        this(network, voltageLevelFilter, new SvgParameters());
    }

    public NetworkAreaDiagram(Network network, Predicate<VoltageLevel> voltageLevelFilter,
                              SvgParameters svgParameters) {
        this(network, voltageLevelFilter, svgParameters, new LayoutParameters());
    }

    public NetworkAreaDiagram(Network network, Predicate<VoltageLevel> voltageLevelFilter,
                              SvgParameters svgParameters, LayoutParameters layoutParameters) {
        this(network, voltageLevelFilter, svgParameters, layoutParameters, new TopologicalStyleProvider(network));
    }

    public NetworkAreaDiagram(Network network, Predicate<VoltageLevel> voltageLevelFilter,
                              SvgParameters svgParameters, LayoutParameters layoutParameters,
                              StyleProvider styleProvider) {
        this(network, voltageLevelFilter, svgParameters, layoutParameters, styleProvider, new DefaultLabelProvider(network));
    }

    public NetworkAreaDiagram(Network network, Predicate<VoltageLevel> voltageLevelFilter,
                              SvgParameters svgParameters, LayoutParameters layoutParameters,
                              StyleProvider styleProvider, LabelProvider labelProvider) {
        this(network, voltageLevelFilter, svgParameters, layoutParameters, styleProvider, labelProvider, new BasicForceLayoutFactory());
    }

    public NetworkAreaDiagram(Network network, Predicate<VoltageLevel> voltageLevelFilter,
                              SvgParameters svgParameters, LayoutParameters layoutParameters,
                              StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory) {
        this(network, voltageLevelFilter, svgParameters, layoutParameters, styleProvider, labelProvider, layoutFactory, new IntIdProvider());
    }

    public NetworkAreaDiagram(Network network, Predicate<VoltageLevel> voltageLevelFilter,
                              SvgParameters svgParameters, LayoutParameters layoutParameters,
                              StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory,
                              IdProvider idProvider) {
        this.network = Objects.requireNonNull(network);
        this.voltageLevelFilter = Objects.requireNonNull(voltageLevelFilter);
        this.layoutParameters = Objects.requireNonNull(layoutParameters);
        this.svgParameters = Objects.requireNonNull(svgParameters);
        this.styleProvider = Objects.requireNonNull(styleProvider);
        this.labelProvider = Objects.requireNonNull(labelProvider);
        this.layoutFactory = Objects.requireNonNull(layoutFactory);
        this.idProvider = Objects.requireNonNull(idProvider);
    }

    public NetworkAreaDiagram addVoltageLevels(String voltageLevelId, int depth) {
        return addVoltageLevels(VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public NetworkAreaDiagram addVoltageLevels(List<String> voltageLevelIds, int depth) {
        return addVoltageLevels(VoltageLevelFilter.createVoltageLevelsDepthFilter(network, voltageLevelIds, depth));
    }

    public NetworkAreaDiagram addVoltageLevels(Predicate<VoltageLevel> voltageLevelFilter) {
        this.voltageLevelFilter = this.voltageLevelFilter.or(Objects.requireNonNull(voltageLevelFilter));
        return this;
    }

    public void draw(String svgFile) {
        draw(Path.of(svgFile));
    }

    public void draw(Path svgFile) {
        Objects.requireNonNull(svgFile);

        List<VoltageLevel> voltageLevels = network.getVoltageLevelStream().filter(voltageLevelFilter).collect(Collectors.toList());
        graph = new NetworkGraphBuilder(voltageLevels, idProvider, graph).buildGraph();
        layoutFactory.create().run(graph, layoutParameters);
        new SvgWriter(svgParameters, styleProvider, labelProvider).writeSvg(graph, svgFile);
    }

    public void draw(Writer writer) {
        List<VoltageLevel> voltageLevels = network.getVoltageLevelStream().filter(voltageLevelFilter).collect(Collectors.toList());
        graph = new NetworkGraphBuilder(voltageLevels, idProvider, graph).buildGraph();
        layoutFactory.create().run(graph, layoutParameters);
        new SvgWriter(svgParameters, styleProvider, labelProvider).writeSvg(graph, writer);
    }

    public String drawToString() {
        try (StringWriter writer = new StringWriter()) {
            draw(writer);
            return writer.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
