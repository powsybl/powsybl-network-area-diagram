package com.powsybl.nad.layout;

import com.powsybl.nad.model.*;

import java.util.Objects;
import java.util.stream.Stream;

public abstract class AbstractLayout implements Layout {

    @Override
    public void run(Graph graph, LayoutParameters layoutParameters) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(layoutParameters);

        nodesLayout(graph, layoutParameters);
        busNodesLayout(graph, layoutParameters);
        edgesLayout(graph, layoutParameters);

        computeSize(graph);
    }

    protected abstract void nodesLayout(Graph graph, LayoutParameters layoutParameters);

    protected abstract void busNodesLayout(Graph graph, LayoutParameters layoutParameters);

    protected void edgesLayout(Graph graph, LayoutParameters layoutParameters) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(layoutParameters);
        graph.getBranchEdgeStream().forEach(edge -> {
            setEdgeVisibility(graph.getNode1(edge), edge, BranchEdge.Side.ONE);
            setEdgeVisibility(graph.getNode2(edge), edge, BranchEdge.Side.TWO);
        });
    }

    private void setEdgeVisibility(Node node, BranchEdge branchEdge, BranchEdge.Side side) {
        if (node instanceof VoltageLevelNode) {
            branchEdge.setVisible(side, ((VoltageLevelNode) node).isVisible());
        }
    }

    private void computeSize(Graph graph) {
        double[] dims = new double[4];
        Stream.concat(graph.getTextNodesStream(), graph.getNodesStream()).forEach(node -> {
            dims[0] = Math.min(dims[0], node.getX());
            dims[1] = Math.max(dims[1], node.getX());
            dims[2] = Math.min(dims[2], node.getY());
            dims[3] = Math.max(dims[3], node.getY());
        });
        graph.setDimensions(dims[0], dims[1], dims[2], dims[3]);
    }
}
