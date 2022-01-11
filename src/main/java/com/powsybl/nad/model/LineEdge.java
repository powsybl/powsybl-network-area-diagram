package com.powsybl.nad.model;

public class LineEdge extends AbstractBranchEdge {

    private final double nominalV;

    public LineEdge(String diagramId, String equipmentId, String nameOrId, double nominalV) {
        super(diagramId, equipmentId, nameOrId);
        this.nominalV = nominalV;
    }

    public double getNominalV() {
        return nominalV;
    }
}
