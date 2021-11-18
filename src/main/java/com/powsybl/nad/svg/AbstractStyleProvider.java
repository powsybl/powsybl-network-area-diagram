/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.nad.model.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractStyleProvider implements StyleProvider {

    private static final String CLASSES_PREFIX = "nad-";
    private static final String VOLTAGE_LEVEL_NODES_CLASS = CLASSES_PREFIX + "vl-nodes";
    private static final String DISCONNECTED_SIDE_EDGE_CLASS = CLASSES_PREFIX + "disconnected";
    private static final String EDGES_CLASS = CLASSES_PREFIX + "edges";

    private final BaseVoltagesConfig baseVoltagesConfig;

    protected AbstractStyleProvider() {
        this(BaseVoltagesConfig.fromPlatformConfig());
    }

    protected AbstractStyleProvider(BaseVoltagesConfig baseVoltagesConfig) {
        this.baseVoltagesConfig = Objects.requireNonNull(baseVoltagesConfig);
    }

    @Override
    public String getStyleDefs() {
        StringBuilder styleSheetBuilder = new StringBuilder("\n");
        for (URL cssUrl : getCssUrls()) {
            try {
                styleSheetBuilder.append(new String(IOUtils.toByteArray(cssUrl), StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new UncheckedIOException("Can't read css file " + cssUrl.getPath(), e);
            }
        }
        return styleSheetBuilder.toString()
                .replace("\r\n", "\n") // workaround for https://bugs.openjdk.java.net/browse/JDK-8133452
                .replace("\r", "\n");
    }

    protected List<URL> getCssUrls() {
        return getCssFilenames().stream()
                .map(n -> getClass().getResource("/" + n))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getNodeStyleClasses(Node node) {
        List<String> styles = new ArrayList<>();
        if (node instanceof VoltageLevelNode) {
            double nominalV = ((VoltageLevelNode) node).getNominalV();
            getBaseVoltageStyle(nominalV).ifPresent(styles::add);
        }
        return styles;
    }

    @Override
    public String getEdgesStyle() {
        return EDGES_CLASS;
    }

    @Override
    public String getVoltageLevelNodeStyle() {
        return VOLTAGE_LEVEL_NODES_CLASS;
    }

    @Override
    public List<String> getEdgeStyleClasses(Edge edge) {
        if (edge instanceof LineEdge) {
            return getBaseVoltageStyle(((LineEdge) edge).getNominalV())
                    .map(Collections::singletonList).orElse(Collections.emptyList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getSideEdgeStyleClasses(Edge edge, Edge.Side side) {
        Objects.requireNonNull(side);
        List<String> result = new ArrayList<>();
        if (edge instanceof AbstractBranchEdge && !((AbstractBranchEdge) edge).isConnected(side)) {
            result.add(DISCONNECTED_SIDE_EDGE_CLASS);
        }
        if (edge instanceof TwoWtEdge) {
            getBaseVoltageStyle(((TwoWtEdge) edge).getNominalV(side)).ifPresent(result::add);
        }
        return result;
    }

    private Optional<String> getBaseVoltageStyle(double nominalV) {
        return baseVoltagesConfig.getBaseVoltageName(nominalV, baseVoltagesConfig.getDefaultProfile());
    }

}
