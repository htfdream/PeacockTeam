package org.lekrikon;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;


public class Main {

    public static void main(String[] args) throws IOException {


        long startTime = System.currentTimeMillis();
        Path path;
        if (args.length > 0 && !args[0].isEmpty())
            path = Path.of(args[0]);
        else {
            System.out.println("Need set path in args");
            return;
        }

        Set<String> setUniqueLines = new HashSet<>();
        int cnt = 0;
        try(BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while (reader.ready()){
                line = reader.readLine();
                setUniqueLines.add(line);
                cnt++;
            }
        }

        List<UniqueLine> uniqueLines = new ArrayList<>();
        for (String entry : setUniqueLines) {
            String[] splitLine = entry.split(";",-1);
            if (!Arrays.stream(splitLine).anyMatch(s -> !s.matches("\"\\d*\"")))
                uniqueLines.add(new UniqueLine(entry, splitLine));

        }
        long stage1Time = System.currentTimeMillis() - startTime;


        int n = uniqueLines.size();
        System.out.println("Total lines: " + cnt);
        System.out.println("Total unique lines: " + n);

        if (n == 0) {
            System.out.println("0");
            return;
        }
        DSU dsu = new DSU(n);


        int maxColumns = uniqueLines.stream()
                .mapToInt(ul -> ul.parts.length)
                .max()
                .orElse(0);

        for (int col = 0; col < maxColumns; col++) {
            Map<String, Integer> valueToRow = new HashMap<>();
            for (int row = 0; row < n; row++) {
                String[] parts = uniqueLines.get(row).parts;
                if (col >= parts.length) continue;
                String val = parts[col];
                if (val == null || val.isEmpty() || val.equals("\"\"")) continue;

                Integer prev = valueToRow.put(val, row);
                if (prev != null ) {
                    dsu.union(prev, row);
                }
            }
        }

        long stage2Time = System.currentTimeMillis() - startTime - stage1Time;

        Map<Integer, List<Integer>> groups = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int root = dsu.find(i);
            groups.computeIfAbsent(root, k -> new ArrayList<>()).add(i);
        }


        List<List<Integer>> filteredGroups = groups.values().stream()
                .filter(list -> list.size() > 1)
                .sorted((a, b) -> Integer.compare(b.size(), a.size()))
                .collect(Collectors.toList());

        long stage3Time = System.currentTimeMillis() - startTime - stage1Time - stage2Time;

        long elapsed = System.currentTimeMillis() - startTime;


        System.out.println(filteredGroups.size());

        int groupNum = 1;
        for (List<Integer> group : filteredGroups) {
            System.out.println("Группа " + groupNum);
            for (int idx : group) {
                System.out.println(uniqueLines.get(idx).originalLine);
            }
            groupNum++;
        }

        System.err.println("Execution time: " + elapsed + " ms");
        System.err.println("Unique lines count: " + n);
        System.err.println("Duplicate lines skipped: " + (cnt - n));


        System.err.println("Stage 1 (Read file): " + stage1Time + " ms");
        System.err.println("Stage 2 (DSU): " + stage2Time + " ms");
        System.err.println("Stage 3 (Group): " + stage3Time + " ms");

        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        System.err.println("Memory: " + usedMemory / (1024 * 1024));
    }


    static class UniqueLine {
        String originalLine;
        String[] parts;

        UniqueLine(String line, String[] parts) {
            this.originalLine = line;
            this.parts = parts;
        }
    }

    static class DSU {
        int[] parent;
        int[] size;
        DSU(int n) {
            parent = new int[n];
            size = new int[n];
            for (int i = 0; i < n; i++) parent[i] = i;
            Arrays.fill(size, 1);
        }
        int find(int n) {
            if (parent[n] != n) parent[n] = find(parent[n]);
            return parent[n];
        }
        void union(int a, int b) {
            int rootA = find(a);
            int rootB = find(b);
            if (rootA == rootB) return;
            if (size[rootA] < size[rootB]) {
                parent[rootA] = rootB;
                size[rootB] += size[rootA];
            } else {
                parent[rootB] = rootA;
                size[rootA] += size[rootB];
            }
        }
    }
}