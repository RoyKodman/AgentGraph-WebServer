# AgentGraph-WebServer

## Background

AgentGraph-WebServer is a Java-based web server and visualization tool for agent-based computation graphs. It is designed for advanced programming coursework and demonstrates concepts such as:

- Custom multi-threaded HTTP server implementation
- Dynamic servlet registration and request handling
- Uploading and parsing configuration files to define agent graphs
- Real-time publishing and display of messages to topics
- Visualization of computation graphs as HTML

The project is organized into several main modules:
- **Server**: A lightweight, extensible HTTP server (`MyHTTPServer`) supporting servlet registration for different HTTP methods and URI prefixes.
- **Servlets**: Handlers for specific HTTP endpoints, including:
  - `ConfLoader`: Handles configuration file uploads and graph creation.
  - `TopicDisplayer`: Allows publishing messages to topics and displays the latest topic values.
  - `HtmlLoader`: Serves static HTML files from the `html_files` directory.
- **Graph**: Core logic for representing agents, topics, and their relationships as a computation graph.
- **Views**: HTML rendering utilities for graph visualization.
- **Configs**: Classes for loading and managing configuration files that define the agent graph structure.

## Installation

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- (Optional) A modern web browser for viewing the HTML visualizations

### Directory Structure

- `project_biu/`: Main Java source code
  - `server/`: HTTP server implementation
  - `servlets/`: HTTP endpoint handlers
  - `graph/`: Agent, topic, and graph logic
  - `views/`: HTML graph rendering
  - `configs/`: Configuration file management
- `config_files/`: Example and uploaded configuration files
- `html_files/`: HTML templates and static files
- `project.log`: Server log output

### Build

1. **Compile the Java sources:**
   ```sh
   javac -d out/ -sourcepath . project_biu/Main.java
   ```

2. **(Optional) Compile all sources for IDE support:**
   ```sh
   javac -d out/ -sourcepath . project_biu/**/*.java
   ```

## Running the Server

1. **Start the server:**
   ```sh
   java -cp out/ Main
   ```

   - The server listens on port `8080` by default.
   - All output (including errors) is redirected to `project.log`.

2. **Access the web interface:**
   - Open your browser and navigate to: [http://localhost:8080/app/index.html](http://localhost:8080/app/index.html)
   - You can also access other HTML files in the `html_files/` directory via `/app/{filename}`.

## Usage

### Uploading a Configuration

- Use the provided HTML form (e.g., `form.html`) to upload a configuration file (`.conf`) to `/upload` (handled by `ConfLoader`).
- The server will parse the configuration, update the agent graph, and return an HTML visualization.

### Publishing Messages

- Send a GET request to `/publish?topic={topicName}&message={value}` to publish a message to a topic.
- The latest values for all topics are displayed in a styled HTML table.

### Viewing the Graph

- After uploading a configuration, the server generates and displays the computation graph using the `HtmlGraphWriter`.

## Project Structure

```
project_biu/
  Main.java                # Entry point, starts the HTTP server and registers servlets
  server/
    MyHTTPServer.java      # Multi-threaded HTTP server with servlet registration
    HTTPServer.java        # HTTP server interface
    RequestParser.java     # HTTP request parsing utilities
  servlets/
    ConfLoader.java        # Handles config uploads and graph creation
    TopicDisplayer.java    # Publishes messages and displays topic values
    HtmlLoader.java        # Serves static HTML files
  graph/
    Graph.java, Node.java, Agent.java, ... # Core graph and agent logic
  views/
    HtmlGraphWriter.java   # Renders the graph as HTML
  configs/
    GenericConfig.java     # Loads and manages configuration files
config_files/
  *.conf                   # Example and uploaded configuration files
html_files/
  *.html                   # HTML templates and static files
```

## Example Commands

- **Compile:**  
  `javac -d out/ -sourcepath . project_biu/Main.java`
- **Run:**  
  `java -cp out/ Main`
- **Access Web UI:**  
  Open [http://localhost:8080/app/index.html](http://localhost:8080/app/index.html) in your browser.

---

**Note:**  
- All server logs are written to `project.log` in the project root.
---
