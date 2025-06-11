package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.example.GraphTextAnalyzer;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class GraphTextAnalyzerTest {

    private GraphTextAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new GraphTextAnalyzer();
    }

    @Test
    void testRandomWalk() {
        // Add some nodes and edges to the graph
        analyzer.addNode("A");
        analyzer.addNode("B");
        analyzer.addNode("C");
        analyzer.addEdge("A", "B", 1);
        analyzer.addEdge("B", "C", 1);
        analyzer.addEdge("C", "A", 1);

        // Redirect System.out to capture output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        analyzer.randomWalk();

        // Restore System.out
        System.setOut(System.out);

        String output = outContent.toString().trim();
        assertFalse(output.isEmpty(), "Output should not be empty");
        assertTrue(output.contains(" -> "), "Output should contain path separator");

        // Test with an empty graph
        analyzer = new GraphTextAnalyzer(); // Reset analyzer
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        analyzer.randomWalk();
        System.setOut(System.out);
        assertEquals("图为空，无法游走。", outContent.toString().trim(), "Should print message for empty graph");
    }

    @Test
    void testRandomWalkWithSpecificGraph1() {
        analyzer.addNode("analyzed");
        analyzer.addNode("the");
        analyzer.addNode("data");
        analyzer.addEdge("analyzed", "the", 1);
        analyzer.addEdge("the", "data", 1);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        analyzer.randomWalk();

        System.setOut(System.out);

        String output = outContent.toString().trim();
        assertTrue(output.matches("(analyzed -> the -> data|the -> data|data)"), "Output should be a valid path");
    }

    @Test
    void testRandomWalkWithSpecificGraph2() {
        analyzer.addNode("analyzed");
        analyzer.addNode("the");
        analyzer.addNode("data");
        analyzer.addEdge("analyzed", "the", 1);
        analyzer.addEdge("the", "data", 1);
        analyzer.addEdge("analyzed", "data", 1);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        analyzer.randomWalk();

        System.setOut(System.out);

        String output = outContent.toString().trim();
        assertTrue(output.matches("(analyzed -> the -> data|analyzed -> data|the -> data|data)"), "Output should be a valid path");
    }

    @Test
    void testRandomWalkWithEmptyGraph() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        analyzer.randomWalk();
        System.setOut(System.out);
        assertEquals("图为空，无法游走。", outContent.toString().trim(), "Should print message for empty graph");
    }

    @Test
    void testRandomWalkWithChineseCharacters() {
        analyzer.addNode("分析");
        analyzer.addNode("数据");
        analyzer.addEdge("分析", "数据", 1);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        analyzer.randomWalk();

        System.setOut(System.out);

        String output = outContent.toString().trim();
        assertTrue(output.matches("(分析 -> 数据|数据)"), "Output should be a valid path with Chinese characters");
    }

    @Test
    void testRandomWalkWithSingleNode() {
        analyzer.addNode("singleNode");

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        analyzer.randomWalk();

        System.setOut(System.out);

        String output = outContent.toString().trim();
        assertEquals("singleNode", output, "Output should be the single node itself");
    }

    @Test
    void testRandomWalkComplexGraph() {
        // 构建一个更复杂的图
        analyzer.addNode("A");
        analyzer.addNode("B");
        analyzer.addNode("C");
        analyzer.addNode("D");
        analyzer.addNode("E");
        analyzer.addEdge("A", "B", 1);
        analyzer.addEdge("A", "C", 1);
        analyzer.addEdge("B", "D", 1);
        analyzer.addEdge("C", "E", 1);
        analyzer.addEdge("D", "A", 1);
        analyzer.addEdge("E", "B", 1);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        analyzer.randomWalk();

        System.setOut(originalOut);

        String output = outContent.toString().trim();
        assertFalse(output.isEmpty(), "Output should not be empty");
        assertTrue(output.contains(" -> "), "Output should contain path separator");

        // 验证输出路径的有效性，例如，路径中的每个节点都必须是图中的节点
        String[] pathNodes = output.split(" -> ");
        for (String node : pathNodes) {
            assertTrue(analyzer.getNodes().contains(node), "Path contains invalid node: " + node);
        }

        // 进一步验证路径的合理性，例如，路径中的每一步都必须是图中存在的边
        for (int i = 0; i < pathNodes.length - 1; i++) {
            String fromNode = pathNodes[i];
            String toNode = pathNodes[i+1];
            assertTrue(analyzer.getGraph().containsKey(fromNode) && analyzer.getGraph().get(fromNode).containsKey(toNode),
                    "Invalid edge in path: " + fromNode + " -> " + toNode);
        }
    }
    
    @Test
    void testQueryBridgeWords() {
        // 测试没有桥接词的情况
        analyzer.addNode("A");
        analyzer.addNode("B");
        analyzer.addEdge("A", "B", 1);
        
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        analyzer.queryBridgeWords("A", "B");
        
        System.setOut(System.out);
        assertEquals("No bridge words from \"A\" to \"B\"!", outContent.toString().trim());
        
        // 测试有桥接词的情况
        analyzer.addNode("C");
        analyzer.addEdge("A", "C", 1);
        analyzer.addEdge("C", "B", 1);
        
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        analyzer.queryBridgeWords("A", "B");
        
        System.setOut(System.out);
        assertEquals("The bridge words from \"A\" to \"B\" are: \"C\"", outContent.toString().trim());
        
        // 测试节点不存在的情况
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        analyzer.queryBridgeWords("X", "Y");
        
        System.setOut(System.out);
        assertEquals("No X or Y in the graph!", outContent.toString().trim());
    }
}