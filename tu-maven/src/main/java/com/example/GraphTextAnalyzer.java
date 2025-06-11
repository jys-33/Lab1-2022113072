package com.example;

import java.io.IOException;
import java.util.*;
import java .io.*;

public class GraphTextAnalyzer {
    private Map<String, Map<String, Integer>> graph = new HashMap<>();
    private Set<String> nodes = new HashSet<>();
    private Random random = new Random();

    public static void main(String[] args) throws IOException {
        GraphTextAnalyzer analyzer = new GraphTextAnalyzer();
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入文本文件路径：");
        String filePath = scanner.nextLine();
        analyzer.buildGraphFromFile(filePath);
        while (true) {
            System.out.println("请选择功能：\n1. 展示有向图\n2. 查询桥接词\n3. 根据桥接词生成新文本\n4. 计算最短路径\n5. 计算PageRank\n6. 随机游走\n7. 导出有向图图片\n0. 退出");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    analyzer.showDirectedGraph();
                    break;
                case "2":
                    System.out.println("请输入两个单词（用空格分隔）：");
                    String[] words = scanner.nextLine().toLowerCase().split("\\s+");
                    if (words.length != 2) {
                        System.out.println("输入格式错误！");
                        break;
                    }
                    analyzer.queryBridgeWords(words[0], words[1]);
                    break;
                case "3":
                    System.out.println("请输入新文本：");
                    String inputText = scanner.nextLine();
                    String newText = analyzer.generateNewText(inputText);
                    System.out.println("生成的新文本：\n" + newText);
                    break;
                case "4":
                    System.out.println("请输入两个单词（用空格分隔）：");
                    String[] sp = scanner.nextLine().toLowerCase().split("\\s+");
                    if (sp.length != 2) {
                        System.out.println("输入格式错误！");
                        break;
                    }
                    analyzer.calcShortestPath(sp[0], sp[1]);
                    break;
                case "5":
                    System.out.println("请输入要查询的单词：");
                    String prWord = scanner.nextLine().toLowerCase();
                    analyzer.calPageRank(prWord);
                    break;
                case "6":
                    analyzer.randomWalk();
                    break;
                case "7":
                    System.out.println("请输入DOT文件保存路径：");
                    String dotPath = scanner.nextLine();
                    System.out.println("请输入图片文件保存路径：");
                    String imgPath = scanner.nextLine();
                    analyzer.exportGraphImage(dotPath, imgPath);
                    break;
                case "0":
                    System.out.println("程序结束。");
                    return;
                default:
                    System.out.println("无效选择，请重新输入。");
            }
        }
    }

    // 功能1：读入文本并生成有向图
    public void buildGraphFromFile(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append(" ");
        }
        br.close();
        String text = sb.toString().toLowerCase();
        text = text.replaceAll("[\r\n]+", " ");
        text = text.replaceAll("[.,!?;:\\-\\(\\)\"']", " ");
        text = text.replaceAll("[^a-z ]", "");
        String[] words = text.trim().split("\\s+");
        nodes.clear();
        graph.clear();
        for (int i = 0; i < words.length; i++) {
            if (words[i].isEmpty()) continue;
            nodes.add(words[i]);
        }
        for (int i = 0; i < words.length - 1; i++) {
            String from = words[i];
            String to = words[i + 1];
            if (from.isEmpty() || to.isEmpty()) continue;
            graph.putIfAbsent(from, new HashMap<>());
            Map<String, Integer> edges = graph.get(from);
            edges.put(to, edges.getOrDefault(to, 0) + 1);
        }
        System.out.println("有向图已生成，节点数：" + nodes.size());
    }

    // 功能2：展示有向图
    public void addNode(String node) {
        nodes.add(node);
    }

    public void addEdge(String from, String to, int weight) {
        graph.putIfAbsent(from, new HashMap<>());
        graph.get(from).put(to, weight);
    }

    public Map<String, Map<String, Integer>> getGraph() {
        return graph;
    }

    public Set<String> getNodes() {
        return nodes;
    }

    public void showDirectedGraph() {
        System.out.println("有向图边列表：");
        for (String from : graph.keySet()) {
            for (Map.Entry<String, Integer> entry : graph.get(from).entrySet()) {
                System.out.println(from + " -> " + entry.getKey() + " (weight: " + entry.getValue() + ")");
            }
        }
    }

    // 功能3：查询桥接词
    public void queryBridgeWords(String word1, String word2) {
        if (!nodes.contains(word1) || !nodes.contains(word2)) {
            System.out.println("No " + word1 + " or " + word2 + " in the graph!");
            return;
        }
        Set<String> bridges = new HashSet<>();
        Map<String, Integer> next = graph.get(word1);
        if (next != null) {
            for (String mid : next.keySet()) {
                Map<String, Integer> next2 = graph.get(mid);
                if (next2 != null && next2.containsKey(word2)) {
                    bridges.add(mid);
                }
            }
        }
        if (bridges.isEmpty()) {
            System.out.println("No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!");
        } else {
            System.out.print("The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are: ");
            System.out.println("\"" + String.join("\", \"", bridges) + "\"");
        }
    }

    // 功能4：根据桥接词生成新文本
    public String generateNewText(String inputText) {
        String text = inputText.toLowerCase();
        text = text.replaceAll("[\r\n]+", " ");
        text = text.replaceAll("[.,!?;:\\-\\(\\)\"']", " ");
        text = text.replaceAll("[^a-z ]", "");
        String[] words = text.trim().split("\\s+");
        if (words.length < 2) return inputText;
        List<String> result = new ArrayList<>();
        for (int i = 0; i < words.length - 1; i++) {
            result.add(words[i]);
            Set<String> bridges = new HashSet<>();
            Map<String, Integer> next = graph.get(words[i]);
            if (next != null) {
                for (String mid : next.keySet()) {
                    Map<String, Integer> next2 = graph.get(mid);
                    if (next2 != null && next2.containsKey(words[i + 1])) {
                        bridges.add(mid);
                    }
                }
            }
            if (!bridges.isEmpty()) {
                List<String> bridgeList = new ArrayList<>(bridges);
                String bridge = bridgeList.get(random.nextInt(bridgeList.size()));
                result.add(bridge);
            }
        }
        result.add(words[words.length - 1]);
        return String.join(" ", result);
    }

    // 功能5：计算最短路径
    public void calcShortestPath(String word1, String word2) {
        if (!nodes.contains(word1) || !nodes.contains(word2)) {
            System.out.println("No " + word1 + " or " + word2 + " in the graph!");
            return;
        }
        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        Set<String> visited = new HashSet<>();
        for (String node : nodes) {
            dist.put(node, Integer.MAX_VALUE);
        }
        dist.put(word1, 0);
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));
        pq.add(word1);
        while (!pq.isEmpty()) {
            String u = pq.poll();
            if (visited.contains(u)) continue;
            visited.add(u);
            Map<String, Integer> edges = graph.get(u);
            if (edges == null) continue;
            for (String v : edges.keySet()) {
                int weight = edges.get(v);
                if (dist.get(u) != Integer.MAX_VALUE && dist.get(u) + weight < dist.get(v)) {
                    dist.put(v, dist.get(u) + weight);
                    prev.put(v, u);
                    pq.add(v);
                }
            }
        }
        if (dist.get(word2) == Integer.MAX_VALUE) {
            System.out.println("No path from \"" + word1 + "\" to \"" + word2 + "\"!");
            return;
        }
        List<String> path = new ArrayList<>();
        String curr = word2;
        while (curr != null) {
            path.add(curr);
            curr = prev.get(curr);
        }
        Collections.reverse(path);
        System.out.println("Shortest path from \"" + word1 + "\" to \"" + word2 + "\" is: " + String.join(" -> ", path) + " (Length: " + (path.size() - 1) + ")");
    }

    // 功能6：计算PageRank
    public void calPageRank(String word) {
        final double d = 0.85;
        final int maxIter = 100;
        final double tol = 1e-6;
        Map<String, Double> pr = new HashMap<>();
        int N = nodes.size();
        for (String node : nodes) {
            pr.put(node, 1.0 / N);
        }
        for (int iter = 0; iter < maxIter; iter++) {
            Map<String, Double> newPr = new HashMap<>();
            for (String node : nodes) {
                double sum = 0.0;
                for (String u : nodes) {
                    Map<String, Integer> edges = graph.get(u);
                    if (edges != null && edges.containsKey(node)) {
                        int out = edges.values().stream().mapToInt(x -> x).sum();
                        if (out > 0) {
                            sum += pr.get(u) / out;
                        }
                    }
                }
                newPr.put(node, (1 - d) / N + d * sum);
            }
            double diff = 0.0;
            for (String node : nodes) {
                diff += Math.abs(newPr.get(node) - pr.get(node));
            }
            pr = newPr;
            if (diff < tol) break;
        }
        if (!pr.containsKey(word)) {
            System.out.println("No " + word + " in the graph!");
        } else {
            System.out.printf("PageRank of \"%s\" is: %.4f\n", word, pr.get(word));
        }
    }

    // 功能7：随机游走
    public void randomWalk() {
        if (nodes.isEmpty()) {
            System.out.println("图为空，无法游走。");
            return;
        }
        List<String> nodeList = new ArrayList<>(nodes);
        String curr = nodeList.get(random.nextInt(nodeList.size()));
        Set<String> visitedEdges = new HashSet<>();
        List<String> path = new ArrayList<>();
        path.add(curr);
        while (true) {
            Map<String, Integer> edges = graph.get(curr);
            if (edges == null || edges.isEmpty()) break;
            List<String> nexts = new ArrayList<>(edges.keySet());
            String next = nexts.get(random.nextInt(nexts.size()));
            String edge = curr + "->" + next;
            if (visitedEdges.contains(edge)) break;
            visitedEdges.add(edge);
            path.add(next);
            curr = next;
        }
        System.out.println(String.join(" -> ", path));
    }

    // 导出有向图为DOT格式并保存为图片
    public void exportGraphImage(String dotFilePath, String imgFilePath) {
        try {
            // 1. 生成DOT文件
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dotFilePath), "UTF-8"));
            writer.write("digraph G {\n");
            for (String from : graph.keySet()) {
                for (Map.Entry<String, Integer> entry : graph.get(from).entrySet()) {
                    String to = entry.getKey();
                    int weight = entry.getValue();
                    writer.write(String.format("    \"%s\" -> \"%s\" [label=\"%d\"];\n", from, to, weight));
                }
            }
            writer.write("}\n");
            writer.close();
            // 2. 调用Graphviz生成图片（需本地安装dot命令）
            Process process = Runtime.getRuntime().exec(new String[]{"dot", "-Tpng", dotFilePath, "-o", imgFilePath});
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("图片已成功生成并保存到：" + imgFilePath);
            } else {
                System.out.println("调用Graphviz失败，请确保已安装dot命令。");
            }
        } catch (Exception e) {
            System.out.println("导出图片失败：" + e.getMessage());
        }
    }
}

