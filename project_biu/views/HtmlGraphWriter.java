package views;

import graph.Graph;
import graph.Node;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;


public final class HtmlGraphWriter {
    /*Public API*/

    // Write a full <html> document
    public static void write(Graph graph, Writer out) throws IOException {
        Map<Node, Point> coords = LayoutEngine.compute(graph);

        out.write("""
                  <!DOCTYPE html>
                  <html lang="en">
                  <head>
                    <meta charset="UTF-8">
                    <title>Graph View</title>
                  """);
        out.write(BASE_CSS);
        out.write("""
                  </head><body>
                  <h2>Computation Graph</h2>
                  """);

        final int W = 900, H = 420;
        out.write("<svg width=\"" + W + "\" height=\"" + H + "\" viewBox=\"0 0 "
                  + W + " " + H + "\">");
        out.write(ARROW_DEF);

        // ---------- Draw nodes ----------
        for (Node n : graph) {
            Point p = coords.get(n);
            if (isTopic(n)) {
                circle(out, p.x, p.y, 28, "topic");
            } else { // agent
                rect(out, p.x - 45, p.y - 30, 90, 60, "agent");
            }
            label(out, p.x, p.y, n.getName());
        }

        // ---------- Draw edges ----------
        for (Node src : graph) {
            for (Node dst : src.getEdges()) {  // <-- Node keeps its own list
                Point s = coords.get(src);
                Point t = coords.get(dst);
                line(out, s.x, s.y, t.x, t.y);
            }
        }

        out.write("</svg></body></html>");
    }

    /*Helpers*/

    private static boolean isTopic(Node n) {
        // our convention: topics start with “T”, agents with “A”
        return n.getName().startsWith("T");
    }

    private static void circle(Writer w,double cx,double cy,int r,String cls) throws IOException {
        w.write("<circle class=\"" + cls + "\" cx=\"" + cx + "\" cy=\"" + cy + "\" r=\"" + r + "\"/>");
    }
    private static void rect(Writer w,double x,double y,int wth,int h,String cls) throws IOException {
        w.write("<rect class=\"" + cls + "\" x=\"" + x + "\" y=\"" + y +
                "\" width=\"" + wth + "\" height=\"" + h + "\" rx=\"10\" ry=\"10\"/>");
    }
    private static void label(Writer w,double x,double y,String txt) throws IOException {
        w.write("<text class=\"label\" x=\"" + x + "\" y=\"" + y + "\">" +
                escape(txt) + "</text>");
    }
    private static void line(Writer w,double x1,double y1,double x2,double y2) throws IOException {
        w.write("<line class=\"edge\" x1=\"" + x1 + "\" y1=\"" + y1 +
                "\" x2=\"" + x2 + "\" y2=\"" + y2 + "\"/>");
    }
    private static String escape(String s){
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    /*Styling & arrow definition*/

    private static final String ARROW_DEF =
        "<defs><marker id=\"arrow\" markerWidth=\"10\" markerHeight=\"10\" refX=\"10\" refY=\"5\" orient=\"auto\" markerUnits=\"strokeWidth\">" +
        "<path d=\"M0,0 L10,5 L0,10 Z\" fill=\"#555\"/></marker></defs>";

    private static final String BASE_CSS =
        """
        <style>
          body{margin:0;font-family:'Segoe UI',sans-serif;background:#fafafa;text-align:center}
          svg{border:1px solid #ccc;box-shadow:0 2px 6px rgba(0,0,0,.1)}
          .topic{fill:#c8e6c9;stroke:#2e7d32;stroke-width:2}
          .agent{fill:#bbdefb;stroke:#1565c0;stroke-width:2}
          .label{text-anchor:middle;dominant-baseline:middle;font-size:14px;pointer-events:none}
          .edge{stroke:#555;stroke-width:2;marker-end:url(#arrow)}
        </style>
        """;

    /*layout engine*/
    private record Point(double x,double y){}

    private static final class LayoutEngine {
        static Map<Node, Point> compute(Graph g) {
            double topicX = 70, agentX = 225;
            int tCnt = 0, aCnt = 0;
            Map<Node, Point> m = new HashMap<>();
            for (Node n : g) {
                if (isTopic(n)) {
                    m.put(n, new Point(topicX, 100 + 160 * tCnt++));
                } else {
                    m.put(n, new Point(agentX, 70 + 160 * aCnt++));
                }
            }
            return m;
        }
    }
}
