package com.powsybl.nad.model;

public class LineEdge extends AbstractBranchEdge {

    private final double nominalV;

    public LineEdge(String diagramId, String equipmentId, String nameOrId, boolean side1Connected, boolean side2Connected, double nominalV) {
        super(diagramId, equipmentId, nameOrId, side1Connected, side2Connected);
        this.nominalV = nominalV;
    }

    public double getNominalV() {
        return nominalV;
    }
}
