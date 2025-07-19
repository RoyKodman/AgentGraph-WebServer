package views;

import graph.Graph;
import graph.Node;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * HtmlGraphWriter generates interactive HTML visualizations of computation graphs.
 * <p>
 * It provides static methods to write a full HTML document with SVG and JavaScript for visualizing nodes and edges.
 * </p>
 * <h2>Usage</h2>
 * <pre>{@code
 * HtmlGraphWriter.write(graph, writer);
 * }</pre>
 */
public final class HtmlGraphWriter {
    /*Public API*/

    // Write a full <html> document
    public static void write(Graph graph, Writer out) throws IOException {
        Map<Node, Point> coords = LayoutEngine.compute(graph);
        System.out.println("Graph nodes: " + graph.size());
        out.write("""
                  <!DOCTYPE html>
                  <html lang="en">
                  <head>
                    <meta charset="UTF-8">
                    <title>Interactive Computation Graph</title>
                  """);
        out.write(BASE_CSS);
        out.write("""
                  </head><body>
                  <div class="header">
                    <h2>Interactive Computation Graph</h2>
                    <div class="controls">
                      <button id="resetBtn" class="btn">Reset Layout</button>
                      <button id="centerBtn" class="btn">Center View</button>
                      <span class="info">Drag nodes to rearrange • Scroll to zoom</span>
                    </div>
                  </div>
                  <div class="graph-container">
                  """);

        final int W = 900, H = 420;
        out.write("<svg id=\"graphSvg\" width=\"" + W + "\" height=\"" + H + "\" viewBox=\"0 0 "
                  + W + " " + H + "\">");
        out.write(ARROW_DEF);

        // Generate edges data for JavaScript
        out.write("<g id=\"edges\">");
        for (Node src : graph) {
            for (Node dst : src.getEdges()) {
                out.write("<line class=\"edge\" data-src=\"" + escape(src.getName()) + 
                         "\" data-dst=\"" + escape(dst.getName()) + "\"/>");
            }
        }
        out.write("</g>");

        // Generate nodes
        out.write("<g id=\"nodes\">");
        for (Node n : graph) {
            Point p = coords.get(n);
            String nodeId = "node-" + escape(n.getName());
            
            if (isTopic(n)) {
                out.write("<g class=\"node-group\" data-node=\"" + escape(n.getName()) + 
                         "\" transform=\"translate(" + p.x + "," + p.y + ")\">");
                out.write("<circle class=\"topic node-shape\" r=\"28\" data-id=\"" + nodeId + "\"/>");
                out.write("<text class=\"label\">" + escape(getDisplayName(n.getName())) + "</text>");
                out.write("</g>");
            } else {
                out.write("<g class=\"node-group\" data-node=\"" + escape(n.getName()) + 
                         "\" transform=\"translate(" + p.x + "," + p.y + ")\">");
                out.write("<rect class=\"agent node-shape\" x=\"-45\" y=\"-30\" width=\"90\" height=\"60\" rx=\"10\" ry=\"10\" data-id=\"" + nodeId + "\"/>");
                out.write("<text class=\"label\">" + escape(getDisplayName(n.getName())) + "</text>");
                out.write("</g>");
            }
        }
        out.write("</g>");

        out.write("</svg>");
        out.write("</div>");
        
        // Add JavaScript for interactivity
        out.write(JAVASCRIPT_CODE);
        
        // Add a note about the dynamic graph
        out.write("""
                  <div class="footer">
                    <p class="note">
                      <strong>Interactive Features:</strong> Drag nodes to rearrange, scroll to zoom, use controls above
                      <br>Green circles = topics, Blue rectangles = agents
                    </p>
                  </div>
                  </body></html>
                  """);
    }

    /*Helpers*/

    private static boolean isTopic(Node n) {
        // our convention: topics start with "T", agents with "A"
        return n.getName().startsWith("T");
    }

    private static String getDisplayName(String nodeName) {
        if (nodeName.startsWith("T") || nodeName.startsWith("A")) {
            // Strip prefix and any suffix after an underscore
            int start = 1;
            int underscore = nodeName.indexOf('_', start);
            return underscore == -1
                  ? nodeName.substring(start)
                  : nodeName.substring(start, underscore);
        }
        return nodeName;
    }

    private static String escape(String s){
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"", "&quot;");
    }

    /*Styling & arrow definition*/

    private static final String ARROW_DEF =
        "<defs>" +
        "<marker id=\"arrow\" markerWidth=\"10\" markerHeight=\"10\" refX=\"10\" refY=\"5\" orient=\"auto\" markerUnits=\"strokeWidth\">" +
        "<path d=\"M0,0 L10,5 L0,10 Z\" fill=\"#555\"/>" +
        "</marker>" +
        "<filter id=\"glow\" x=\"-50%\" y=\"-50%\" width=\"200%\" height=\"200%\">" +
        "<feGaussianBlur stdDeviation=\"3\" result=\"coloredBlur\"/>" +
        "<feMerge><feMergeNode in=\"coloredBlur\"/><feMergeNode in=\"SourceGraphic\"/></feMerge>" +
        "</filter>" +
        "<filter id=\"shadow\" x=\"-50%\" y=\"-50%\" width=\"200%\" height=\"200%\">" +
        "<feDropShadow dx=\"2\" dy=\"2\" stdDeviation=\"2\" flood-opacity=\"0.3\"/>" +
        "</filter>" +
        "</defs>";

    private static final String BASE_CSS =
        """
        <style>
          * { box-sizing: border-box; }
          body {
            margin: 0;
            font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #f8f5f0 0%, #ede7e0 50%, #f0ede8 100%);
            min-height: 100vh;
            color: #333;
          }
          .header {
            background: rgba(255,255,255,0.95);
            backdrop-filter: blur(10px);
            padding: 1rem 2rem;
            box-shadow: 0 2px 20px rgba(0,0,0,0.1);
            border-bottom: 1px solid rgba(255,255,255,0.2);
          }
          .header h2 {
            margin: 0 0 0.5rem 0;
            color: #2c3e50;
            font-weight: 600;
            font-size: 1.5rem;
          }
          .controls {
            display: flex;
            align-items: center;
            gap: 1rem;
            flex-wrap: wrap;
          }
          .btn {
            background: linear-gradient(45deg, #4CAF50, #45a049);
            color: white;
            border: none;
            padding: 0.5rem 1rem;
            border-radius: 25px;
            cursor: pointer;
            font-size: 0.85rem;
            font-weight: 500;
            transition: all 0.3s ease;
            box-shadow: 0 2px 8px rgba(76,175,80,0.3);
          }
          .btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 15px rgba(76,175,80,0.4);
          }
          .btn:active {
            transform: translateY(0);
          }
          .info {
            color: #666;
            font-size: 0.85rem;
            font-style: italic;
          }
          .graph-container {
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 2rem;
            min-height: calc(100vh - 140px);
          }
          #graphSvg {
            background: rgba(255,255,255,0.95);
            border-radius: 15px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
            backdrop-filter: blur(10px);
            border: 1px solid rgba(255,255,255,0.3);
            transition: transform 0.3s ease;
          }
          #graphSvg:hover {
            transform: scale(1.02);
          }
          .topic {
            fill: #4CAF50;
            stroke: #2E7D32;
            stroke-width: 3;
            filter: drop-shadow(2px 2px 4px rgba(0,0,0,0.2));
            transition: all 0.3s ease;
            cursor: grab;
          }
          .topic:hover {
            fill: #66BB6A;
            filter: drop-shadow(3px 3px 8px rgba(0,0,0,0.3)) url(#glow);
            transform: scale(1.1);
          }
          .topic.dragging {
            cursor: grabbing;
            filter: url(#glow);
          }
          .agent {
            fill: #2196F3;
            stroke: #1565C0;
            stroke-width: 3;
            filter: drop-shadow(2px 2px 4px rgba(0,0,0,0.2));
            transition: all 0.3s ease;
            cursor: grab;
          }
          .agent:hover {
            fill: #42A5F5;
            filter: drop-shadow(3px 3px 8px rgba(0,0,0,0.3)) url(#glow);
            transform: scale(1.05);
          }
          .agent.dragging {
            cursor: grabbing;
            filter: url(#glow);
          }
          .label {
            text-anchor: middle;
            dominant-baseline: middle;
            font-size: 13px;
            font-weight: 600;
            pointer-events: none;
            fill: #2c3e50;
            text-shadow: 1px 1px 2px rgba(255,255,255,0.8);
          }
          .edge {
            stroke: #34495e;
            stroke-width: 2.5;
            marker-end: url(#arrow);
            opacity: 0.8;
            transition: all 0.3s ease;
          }
          .edge:hover {
            stroke: #e74c3c;
            stroke-width: 3;
            opacity: 1;
          }
          .edge.highlight {
            stroke: #e74c3c;
            stroke-width: 3;
            opacity: 1;
            animation: pulse 1.5s infinite;
          }
          @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.6; }
          }
          .footer {
            background: rgba(255,255,255,0.95);
            backdrop-filter: blur(10px);
            padding: 1rem 2rem;
            text-align: center;
            border-top: 1px solid rgba(255,255,255,0.2);
          }
          .note {
            margin: 0;
            font-size: 0.9rem;
            color: #555;
            line-height: 1.5;
          }
          .physics-enabled .node-group {
            animation: float 3s ease-in-out infinite;
          }
          .physics-enabled .node-group:nth-child(even) {
            animation-delay: -1.5s;
          }
          @keyframes float {
            0%, 100% { transform: translateY(0px); }
            50% { transform: translateY(-5px); }
          }
        </style>
        """;

    private static final String JAVASCRIPT_CODE = """
        <script>
        (function() {
            const svg = document.getElementById('graphSvg');
            const nodesGroup = document.getElementById('nodes');
            const edgesGroup = document.getElementById('edges');
            let isDragging = false;
            let dragNode = null;
            let dragOffset = { x: 0, y: 0 };
            let physicsEnabled = false;
            let originalPositions = new Map();
            
            // Store original positions
            document.querySelectorAll('.node-group').forEach(node => {
                const transform = node.getAttribute('transform');
                const match = transform.match(/translate\\(([^,]+),([^)]+)\\)/);
                if (match) {
                    originalPositions.set(node.dataset.node, {
                        x: parseFloat(match[1]),
                        y: parseFloat(match[2])
                    });
                }
            });
            
            // Drag functionality
            nodesGroup.addEventListener('mousedown', startDrag);
            document.addEventListener('mousemove', drag);
            document.addEventListener('mouseup', endDrag);
            
            // Touch events for mobile
            nodesGroup.addEventListener('touchstart', handleTouch);
            document.addEventListener('touchmove', handleTouch);
            document.addEventListener('touchend', endDrag);
            
            function startDrag(e) {
                e.preventDefault();
                const target = e.target.closest('.node-group');
                if (!target) return;
                
                isDragging = true;
                dragNode = target;
                target.querySelector('.node-shape').classList.add('dragging');
                
                const rect = svg.getBoundingClientRect();
                const transform = target.getAttribute('transform');
                const match = transform.match(/translate\\(([^,]+),([^)]+)\\)/);
                const nodeX = parseFloat(match[1]);
                const nodeY = parseFloat(match[2]);
                
                const svgX = (e.clientX - rect.left) * (900 / rect.width);
                const svgY = (e.clientY - rect.top) * (420 / rect.height);
                
                dragOffset.x = svgX - nodeX;
                dragOffset.y = svgY - nodeY;
                
                highlightConnectedEdges(target.dataset.node);
            }
            
            function drag(e) {
                if (!isDragging || !dragNode) return;
                e.preventDefault();
                
                const rect = svg.getBoundingClientRect();
                const svgX = (e.clientX - rect.left) * (900 / rect.width);
                const svgY = (e.clientY - rect.top) * (420 / rect.height);
                
                const newX = Math.max(50, Math.min(850, svgX - dragOffset.x));
                const newY = Math.max(50, Math.min(370, svgY - dragOffset.y));
                
                dragNode.setAttribute('transform', `translate(${newX},${newY})`);
                updateEdges();
            }
            
            function endDrag() {
                if (dragNode) {
                    dragNode.querySelector('.node-shape').classList.remove('dragging');
                    clearHighlightedEdges();
                }
                isDragging = false;
                dragNode = null;
            }
            
            function handleTouch(e) {
                if (e.touches.length !== 1) return;
                const touch = e.touches[0];
                const mouseEvent = new MouseEvent(e.type === 'touchstart' ? 'mousedown' : 
                                                e.type === 'touchmove' ? 'mousemove' : 'mouseup', {
                    clientX: touch.clientX,
                    clientY: touch.clientY
                });
                e.target.dispatchEvent(mouseEvent);
            }
            
            function updateEdges() {
                document.querySelectorAll('.edge').forEach(edge => {
                    const srcName = edge.dataset.src;
                    const dstName = edge.dataset.dst;
                    const srcNode = document.querySelector(`[data-node="${srcName}"]`);
                    const dstNode = document.querySelector(`[data-node="${dstName}"]`);
                    
                    if (srcNode && dstNode) {
                        const srcPos = getNodePosition(srcNode);
                        const dstPos = getNodePosition(dstNode);
                        const srcIsTopic = srcNode.querySelector('.topic');
                        const dstIsTopic = dstNode.querySelector('.topic');
                        
                        const edgeStart = srcIsTopic ? 
                            getCircleEdge(srcPos, dstPos, 28) : 
                            getRectEdge(srcPos, dstPos, 90, 60);
                        const edgeEnd = dstIsTopic ? 
                            getCircleEdge(dstPos, srcPos, 28) : 
                            getRectEdge(dstPos, srcPos, 90, 60);
                        
                        edge.setAttribute('x1', edgeStart.x);
                        edge.setAttribute('y1', edgeStart.y);
                        edge.setAttribute('x2', edgeEnd.x);
                        edge.setAttribute('y2', edgeEnd.y);
                    }
                });
            }
            
            function getNodePosition(node) {
                const transform = node.getAttribute('transform');
                const match = transform.match(/translate\\(([^,]+),([^)]+)\\)/);
                return {
                    x: parseFloat(match[1]),
                    y: parseFloat(match[2])
                };
            }
            
            function getCircleEdge(center, target, radius) {
                const dx = target.x - center.x;
                const dy = target.y - center.y;
                const distance = Math.sqrt(dx * dx + dy * dy);
                if (distance === 0) return center;
                return {
                    x: center.x + (dx / distance) * radius,
                    y: center.y + (dy / distance) * radius
                };
            }
            
            function getRectEdge(center, target, width, height) {
                const dx = target.x - center.x;
                const dy = target.y - center.y;
                const halfWidth = width / 2;
                const halfHeight = height / 2;
                
                if (Math.abs(dx) / halfWidth > Math.abs(dy) / halfHeight) {
                    return {
                        x: center.x + (dx > 0 ? halfWidth : -halfWidth),
                        y: center.y + dy * (halfWidth / Math.abs(dx))
                    };
                } else {
                    return {
                        x: center.x + dx * (halfHeight / Math.abs(dy)),
                        y: center.y + (dy > 0 ? halfHeight : -halfHeight)
                    };
                }
            }
            
            function highlightConnectedEdges(nodeName) {
                document.querySelectorAll('.edge').forEach(edge => {
                    if (edge.dataset.src === nodeName || edge.dataset.dst === nodeName) {
                        edge.classList.add('highlight');
                    }
                });
            }
            
            function clearHighlightedEdges() {
                document.querySelectorAll('.edge').forEach(edge => {
                    edge.classList.remove('highlight');
                });
            }
            
            // Control buttons
            document.getElementById('resetBtn').addEventListener('click', () => {
                document.querySelectorAll('.node-group').forEach(node => {
                    const original = originalPositions.get(node.dataset.node);
                    if (original) {
                        node.setAttribute('transform', `translate(${original.x},${original.y})`);
                    }
                });
                updateEdges();
            });
            
            document.getElementById('centerBtn').addEventListener('click', () => {
                svg.style.transform = 'scale(1)';
                svg.scrollIntoView({ behavior: 'smooth', block: 'center' });
            });
            
            // Zoom functionality
            svg.addEventListener('wheel', (e) => {
                e.preventDefault();
                const rect = svg.getBoundingClientRect();
                const scale = e.deltaY > 0 ? 0.9 : 1.1;
                const currentTransform = svg.style.transform || 'scale(1)';
                const currentScale = parseFloat(currentTransform.match(/scale\\(([^)]+)\\)/)?.[1] || 1);
                const newScale = Math.max(0.5, Math.min(3, currentScale * scale));
                svg.style.transform = `scale(${newScale})`;
            });
            
            // Initialize edges
            updateEdges();
        })();
        </script>
        """;

    /*layout engine*/
    private record Point(double x,double y){}

    private static final class LayoutEngine {
        static Map<Node, Point> compute(Graph g) {
            // Separate topics and agents
            List<Node> topics = new ArrayList<>();
            List<Node> agents = new ArrayList<>();
            
            for (Node n : g) {
                if (isTopic(n)) {
                    topics.add(n);
                } else {
                    agents.add(n);
                }
            }
            
            Map<Node, Point> m = new HashMap<>();
            
            // Layout parameters
            final double SVG_WIDTH = 900;
            final double SVG_HEIGHT = 420;
            final double MARGIN = 60; // Reduced margin
            final double MIN_SPACING = 80; // Reduced minimum spacing
            
            // Calculate available space
            double availableHeight = SVG_HEIGHT - 2 * MARGIN;
            double availableWidth = SVG_WIDTH - 2 * MARGIN;
            
            // Layout topics on the left
            if (!topics.isEmpty()) {
                double topicSpacing = topics.size() > 1 ? 
                    Math.min(120, availableHeight / (topics.size() - 1)) : 0;
                double startY = topics.size() == 1 ? 
                    SVG_HEIGHT / 2 : 
                    MARGIN + (availableHeight - (topics.size() - 1) * topicSpacing) / 2;
                
                // Ensure we don't go outside bounds
                if (startY + (topics.size() - 1) * topicSpacing > SVG_HEIGHT - MARGIN) {
                    topicSpacing = (availableHeight) / Math.max(1, topics.size() - 1);
                    startY = MARGIN;
                }
                
                for (int i = 0; i < topics.size(); i++) {
                    double y = startY + i * topicSpacing;
                    y = Math.max(MARGIN, Math.min(SVG_HEIGHT - MARGIN, y)); // Clamp to bounds
                    m.put(topics.get(i), new Point(100, y));
                }
            }
            
            // Layout agents - distribute across available width if needed
            if (!agents.isEmpty()) {
                // Calculate how many columns we need
                int maxAgentsPerColumn = Math.max(1, (int) (availableHeight / MIN_SPACING));
                int columns = (int) Math.ceil((double) agents.size() / maxAgentsPerColumn);
                double columnWidth = columns > 1 ? availableWidth / (columns + 1) : availableWidth / 2;
                
                for (int i = 0; i < agents.size(); i++) {
                    int column = i / maxAgentsPerColumn;
                    int row = i % maxAgentsPerColumn;
                    
                    // Calculate x position - distribute columns evenly
                    double x = SVG_WIDTH - MARGIN - availableWidth + columnWidth * (column + 1);
                    x = Math.max(200, Math.min(SVG_WIDTH - MARGIN, x)); // Ensure minimum distance from topics
                    
                    // Calculate y position within the column
                    int agentsInThisColumn = Math.min(maxAgentsPerColumn, agents.size() - column * maxAgentsPerColumn);
                    double agentSpacing = agentsInThisColumn > 1 ? 
                        Math.min(120, availableHeight / (agentsInThisColumn - 1)) : 0;
                    double startY = agentsInThisColumn == 1 ? 
                        SVG_HEIGHT / 2 : 
                        MARGIN + (availableHeight - (agentsInThisColumn - 1) * agentSpacing) / 2;
                    
                    // Ensure we don't go outside bounds
                    if (startY + (agentsInThisColumn - 1) * agentSpacing > SVG_HEIGHT - MARGIN) {
                        agentSpacing = availableHeight / Math.max(1, agentsInThisColumn - 1);
                        startY = MARGIN;
                    }
                    
                    double y = startY + row * agentSpacing;
                    y = Math.max(MARGIN, Math.min(SVG_HEIGHT - MARGIN, y)); // Clamp to bounds
                    
                    m.put(agents.get(i), new Point(x, y));
                }
            }
            
            return m;
        }
    }
}