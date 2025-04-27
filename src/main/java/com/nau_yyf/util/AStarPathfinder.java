package com.nau_yyf.util;

import java.util.*;

public class AStarPathfinder {
    // 节点类，表示网格中的一个位置
    public static class Node implements Comparable<Node> {
        private int x, y;
        private int g; // 起点到当前节点的实际距离
        private int h; // 当前节点到目标的估计距离
        private Node parent; // 父节点，用于回溯路径

        public Node(int x, int y) {
            this.x = x;
            this.y = y;
            this.g = 0;
            this.h = 0;
            this.parent = null;
        }

        // 添加getter方法
        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getF() {
            return g + h;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.getF(), other.getF());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Node node = (Node) obj;
            return x == node.x && y == node.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    // 四个方向：上、右、下、左
    private static final int[][] DIRECTIONS = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

    // 查找从起点到目标的路径
    public static List<Node> findPath(boolean[][] grid, int startX, int startY, int targetX, int targetY) {
        int rows = grid.length;
        int cols = grid[0].length;

        // 验证起点和终点是否合法
        if (startX < 0 || startX >= cols || startY < 0 || startY >= rows ||
                targetX < 0 || targetX >= cols || targetY < 0 || targetY >= rows ||
                grid[startY][startX] || grid[targetY][targetX]) {
            return Collections.emptyList(); // 起点或终点不可行走
        }

        PriorityQueue<Node> openList = new PriorityQueue<>();
        Set<Node> closedSet = new HashSet<>();

        // 初始化起点
        Node startNode = new Node(startX, startY);
        startNode.h = calculateHeuristic(startX, startY, targetX, targetY);
        openList.add(startNode);

        while (!openList.isEmpty()) {
            // 获取F值最小的节点
            Node current = openList.poll();

            // 如果到达目标，构建路径并返回
            if (current.x == targetX && current.y == targetY) {
                return buildPath(current);
            }

            // 将当前节点加入关闭列表
            closedSet.add(current);

            // 处理相邻节点
            for (int[] dir : DIRECTIONS) {
                int newX = current.x + dir[0];
                int newY = current.y + dir[1];

                // 检查新位置是否有效且可行走
                if (newX < 0 || newX >= cols || newY < 0 || newY >= rows ||
                        grid[newY][newX]) {
                    continue; // 位置无效或不可行走
                }

                Node neighbor = new Node(newX, newY);

                // 如果已经在关闭列表中，跳过
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                // 计算从起点到邻居的新G值
                int newG = current.g + 1; // 假设每步代价为1

                // 检查是否已经在开放列表中
                boolean inOpenList = false;
                for (Node node : openList) {
                    if (node.equals(neighbor)) {
                        inOpenList = true;
                        // 如果新路径更好，更新路径
                        if (newG < node.g) {
                            node.g = newG;
                            node.parent = current;
                        }
                        break;
                    }
                }

                // 如果不在开放列表中，添加到开放列表
                if (!inOpenList) {
                    neighbor.g = newG;
                    neighbor.h = calculateHeuristic(newX, newY, targetX, targetY);
                    neighbor.parent = current;
                    openList.add(neighbor);
                }
            }
        }

        // 如果开放列表为空但没有找到路径，返回空列表
        return Collections.emptyList();
    }

    // 计算启发式值 (曼哈顿距离)
    private static int calculateHeuristic(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    // 从目标节点回溯构建路径
    private static List<Node> buildPath(Node targetNode) {
        List<Node> path = new ArrayList<>();
        Node current = targetNode;

        while (current != null) {
            path.add(current);
            current = current.parent;
        }

        // 反转路径，从起点到终点
        Collections.reverse(path);
        return path;
    }
} 