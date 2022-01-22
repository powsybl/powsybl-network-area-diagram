/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg.iidm;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.iidm.network.*;
import com.powsybl.nad.model.BusNode;
import com.powsybl.nad.model.Node;
import com.powsybl.nad.utils.iidm.IidmUtils;

import java.util.*;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TopologicalStyleProvider extends NominalVoltageStyleProvider {

    private final Map<String, String> styleMap = new HashMap<>();
    private final Map<String, Integer> baseVoltagesCounter = new HashMap<>();

    public TopologicalStyleProvider(Network network) {
        super(network);
    }

    public TopologicalStyleProvider(Network network, BaseVoltagesConfig baseVoltageStyle) {
        super(network, baseVoltageStyle);
    }

    @Override
    public List<String> getCssFilenames() {
        return Collections.singletonList("topologicalStyle.css");
    }

    @Override
    public List<String> getNodeStyleClasses(Node node) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getNodeStyleClasses(BusNode busNode) {
        Bus b = network.getBusView().getBus(busNode.getEquipmentId());
        List<String> styles = new ArrayList<>();
        getNodeTopologicalStyle(b).ifPresent(styles::add);
        return styles;
    }

    private Optional<String> getNodeTopologicalStyle(Bus b) {
        if (styleMap.containsKey(b.getId())) {
            return Optional.ofNullable(styleMap.get(b.getId()));
        }
        return getBaseVoltageStyle(b.getVoltageLevel().getNominalV())
                .map(baseVoltageStyle -> fillStyleMap(baseVoltageStyle, b));
    }

    private String fillStyleMap(String style, Bus bus) {
        Collection<Bus> connectedBuses = getConnectedBuses(bus);
        Integer baseVoltageIndex = baseVoltagesCounter.compute(style, (k, v) -> v == null ? 0 : v + 1);
        String topologicalStyle = style + "-" + baseVoltageIndex;
        connectedBuses.forEach(b -> styleMap.put(b.getId(), topologicalStyle));
        return topologicalStyle;
    }

    private Collection<Bus> getConnectedBuses(Bus bus) {
        Set<Bus> visitedBuses = new HashSet<>();
        findConnectedBuses(bus, visitedBuses);
        return visitedBuses;
    }

    private void findConnectedBuses(Bus bus, Set<Bus> visitedBus) {
        if (visitedBus.contains(bus)) {
            return;
        }
        visitedBus.add(bus);
        bus.visitConnectedEquipments(new DefaultTopologyVisitor() {
            @Override
            public void visitLine(Line line, Branch.Side side) {
                Terminal t = line.getTerminal(IidmUtils.getOpposite(side));
                Bus otherBus = t.getBusView().getBus();
                if (otherBus != null && !visitedBus.contains(otherBus)) {
                    findConnectedBuses(otherBus, visitedBus);
                }
            }
        });
    }

    @Override
    protected Optional<String> getBaseVoltageStyle(Terminal terminal) {
        if (terminal == null) {
            return Optional.empty();
        }
        return terminal.isConnected()
                ? getNodeTopologicalStyle(terminal.getBusView().getBus())
                : Optional.of(DISCONNECTED_CLASS);
    }
}
