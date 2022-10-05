package com.powsybl.nad.layout;

import com.powsybl.ieeecdf.converter.IeeeCdfNetworkFactory;
import com.powsybl.iidm.network.Network;
import com.powsybl.nad.NetworkAreaDiagram;
import com.powsybl.nad.svg.SvgParameters;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ForceLayoutSpringRepulsionMultipleFactorsTest {

    // Check "A new force-directed graph drawing method based on edge–edge repulsion"
    // https://www.sciencedirect.com/science/article/abs/pii/S1045926X11000802
    // Downloaded from https://www.researchgate.net/publication/4175452_A_new_force-directed_graph_drawing_method_based_on_edge-edge_repulsion
    // Stackoverflow https://stackoverflow.com/questions/53415160/how-to-make-a-force-directed-layout-with-no-node-edge-overlapping

    // Check "A Force-Directed Algorithm that Preserves Edge Crossing Properties" François Bertault 1999

    static final Path OUTPUT = Paths.get("/Users/zamarrenolm/work/temp/nad/spring-repulsion/output");
    static final double[][] FACTORS = {
            {0.0, 0.25},
            //{0.1, 0.5, 0.75},
            //{1.0, 2.0, 5.0}
    };
    static final String STYLES = "\n" +
            "h1 { color:steelblue; }\n" +
            "table.factors { width:100%; border: 1px solid lightgray; margin: 1em 0; border-collapse: collapse; }\n" +
            "th.factor { border:1px solid lightgray; background-color:whitesmoke; text-align:center; font-family:monospace; }\n" +
            "td.graph { border:1px solid lightgray; }\n";

    @Test
    void testIEEE9() throws IOException {
        draw(IeeeCdfNetworkFactory.create9(), FACTORS);
    }

    @Test
    void testIEEE14() throws IOException {
        draw(IeeeCdfNetworkFactory.create14(), FACTORS);
    }

    @Test
    void testIEEE30() throws IOException {
        draw(IeeeCdfNetworkFactory.create30(), FACTORS);
    }

    @Test
    void testIEEE33() throws IOException {
        draw(IeeeCdfNetworkFactory.create33(), FACTORS);
    }

    @Test
    void testIEEE57() throws IOException {
        draw(IeeeCdfNetworkFactory.create57(), FACTORS);
    }

    @Test
    void testIEEE69() throws IOException {
        draw(IeeeCdfNetworkFactory.create69(), FACTORS);
    }

    @Test
    void testIEEE118() throws IOException {
        draw(IeeeCdfNetworkFactory.create118(), FACTORS);
    }

    @Test
    @Disabled("do not test big cases")
    void testIEEE300() throws IOException {
        draw(IeeeCdfNetworkFactory.create300(), FACTORS);
    }

    @Test
    void testTriangleWithCenter() throws IOException {
        draw(SimpleNetworkFactory.createTriangleWithCenter(), FACTORS);
    }

    @Test
    void testDiamond() throws IOException {
        draw(SimpleNetworkFactory.createDiamond(), FACTORS);
    }

    private static void draw(Network network, double[][] factors) throws IOException {
        Path html = OUTPUT.resolve(String.format("index-%s.html", network.getId()));
        String title = String.format("Compare output of different spring repulsion factor values on network %s", network.getNameOrId());

        String[][] drawings = buildDrawings(network, factors);
        assertNotNull(drawings);

        try (OutputStream pout = Files.newOutputStream(html);
             PrintWriter pw = new PrintWriter(pout)) {
            printHeader(pw, title);
            for (int kr = 0; kr < drawings.length; kr++) {
                printFactors(pw, FACTORS[kr], drawings[kr]);
            }
            printFooter(pw);
        }
    }

    private static String[][] buildDrawings(Network network, double[][] factors) {
        String[][] drawings = new String[factors.length][];
        for (int k = 0; k < factors.length; k++) {
            drawings[k] = new String[factors[k].length];
            for (int kf = 0; kf < factors[k].length; kf++) {
                drawings[k][kf] = draw(network, factors[k][kf]);
            }
        }
        return drawings;
    }

    private static void printHeader(PrintWriter pw, String title) {
        pw.printf("<html>%n<head>%n<title>%s</title>%n<style>%s</style>%n</head>%n<body>%n", title, STYLES);
        pw.printf("<h1>%s</h1>", title);
    }

    private static void printFooter(PrintWriter pw) {
        pw.println("</body>\n</html>");
    }

    private static void printFactors(PrintWriter p, double[] factors, String[] drawings) {
        p.println("<table class='factors'>");
        p.println("<thead><tr>");
        for (int k = 0; k < drawings.length; k++) {
            p.print("<th class='factor grid'>");
            p.printf("%.2f", factors[k]);
            p.println("</th>");
        }
        p.println("</tr></thead>");
        p.println("<tbody><tr>");
        for (String drawing : drawings) {
            p.print("<td class='graph grid'>");
            // An image can be resized by the browser and an object not?
            //p.printf("<object data=\"%s\"></object>", drawings[k]);
            p.printf("<img src=\"%s\" />", drawing);
            p.println("</td>");
        }
        p.println("</tr></tobdy>");
        p.println("</table>");
    }

    private static String draw(Network network, double factor) {
        LayoutParameters layoutParameters = new LayoutParameters()
                .setSpringRepulsionFactorForceLayout(factor);

        SvgParameters svgParameters = new SvgParameters()
                .setInsertNameDesc(false)
                .setSvgWidthAndHeightAdded(false);

        String filename1 = network.getNameOrId() + "-repulsion-factor-" + factor + ".svg";
        new NetworkAreaDiagram(network).draw(
                OUTPUT.resolve(filename1),
                svgParameters,
                layoutParameters);

        return filename1;
    }

}
