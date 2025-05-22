package com.rex.textgen.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import java.util.*;

public class TextGenerator {
        private final Plugin plugin;
        private final Map<Character, boolean[][]> characterPatterns;
        private final Map<UUID, List<Block>> lastGenerated;
        private static final int MAX_DISTANCE = 50;

        public TextGenerator(Plugin plugin) {
                this.plugin = plugin;
                this.characterPatterns = initializePatterns();
                this.lastGenerated = new HashMap<>();
        }

        public boolean generateText(Player player, String text, Material blockType, int size, int spacing, int width,
                        String style, Material outlineType) {
                Location centerLoc;

                // Try to get block player is looking at
                RayTraceResult rayTrace = player.rayTraceBlocks(MAX_DISTANCE);
                if (rayTrace != null && rayTrace.getHitBlock() != null) {
                        // If looking at a block, use that location
                        centerLoc = rayTrace.getHitBlock().getLocation().clone().add(0, 1, 0);
                } else {
                        // If not looking at a block, generate in air 24 blocks away
                        centerLoc = player.getEyeLocation()
                                        .add(player.getLocation().getDirection().multiply(MAX_DISTANCE));
                }

                // Get player's direction vector
                Vector direction = player.getLocation().getDirection();
                double angle = Math.atan2(direction.getZ(), direction.getX());
                boolean alongX = Math.abs(Math.cos(angle)) > Math.abs(Math.sin(angle));

                // Calculate size of each cell and total width
                String upperText = text.toUpperCase();
                int cellSize = size;
                double totalWidth = 0;
                for (int i = 0; i < upperText.length(); i++) {
                        if (characterPatterns.containsKey(upperText.charAt(i))) {
                                totalWidth += cellSize * 5; // Each character is 5 cells wide
                                if (i < upperText.length() - 1) {
                                        totalWidth += spacing;
                                }
                        }
                }

                // Calculate start position based on player's direction
                Location startLoc = centerLoc.clone();
                if (alongX) {
                        // Player looking east/west
                        startLoc.add(0, 0, -(totalWidth / 2.0));
                } else {
                        // Player looking north/south
                        startLoc.add(-(totalWidth / 2.0), 0, 0);
                }

                List<Block> generatedBlocks = new ArrayList<>();

                // Generate outline first if specified
                if (outlineType != null) {
                        // Generate each character's outline
                        double currentOffset = 0;
                        for (char c : upperText.toCharArray()) {
                                boolean[][] pattern = characterPatterns.get(c);
                                if (pattern != null) {
                                        Location charLoc = startLoc.clone();
                                        if (alongX) {
                                                charLoc.add(0, 0, currentOffset);
                                        } else {
                                                charLoc.add(currentOffset, 0, 0);
                                        }
                                        placeOutline(charLoc, pattern, outlineType, generatedBlocks, size, width,
                                                        alongX);
                                        currentOffset += cellSize * 5 + spacing;
                                }
                        }
                }

                // Generate main text
                double currentOffset = 0;
                for (char c : upperText.toCharArray()) {
                        boolean[][] pattern = characterPatterns.get(c);
                        if (pattern != null) {
                                Location charLoc = startLoc.clone();
                                if (alongX) {
                                        charLoc.add(0, 0, currentOffset);
                                } else {
                                        charLoc.add(currentOffset, 0, 0);
                                }
                                placeCharacter(charLoc, pattern, blockType, generatedBlocks, size, width, alongX);
                                currentOffset += cellSize * 5 + spacing;
                        }
                }

                // Store the generated blocks
                lastGenerated.put(player.getUniqueId(), generatedBlocks);
                return true;
        }

        public boolean undoLastGeneration(Player player) {
                return clearLastGeneration(player);
        }

        private boolean clearLastGeneration(Player player) {
                List<Block> blocks = lastGenerated.remove(player.getUniqueId());
                if (blocks != null && !blocks.isEmpty()) {
                        blocks.forEach(block -> block.setType(Material.AIR));
                        return true;
                }
                return false;
        }

        private void placeOutline(Location start, boolean[][] pattern, Material blockType, List<Block> generatedBlocks,
                        int size, int width, boolean alongX) {
                int cellSize = size;
                if (cellSize < 1)
                        cellSize = 1;

                for (int y = 0; y < pattern.length; y++) {
                        for (int x = 0; x < pattern[y].length; x++) {
                                if (pattern[y][x]) {
                                        // Fill each cell with blocks plus one block extra on each side
                                        for (int dy = -1; dy <= cellSize; dy++) {
                                                for (int dx = -1; dx <= cellSize; dx++) {
                                                        // Place outline block one block behind
                                                        Location blockLoc = start.clone();
                                                        if (alongX) {
                                                                blockLoc.add(
                                                                                -1, // One block behind
                                                                                (pattern.length - y - 1) * cellSize
                                                                                                + dy, // height
                                                                                x * cellSize + dx // length
                                                                );
                                                        } else {
                                                                blockLoc.add(
                                                                                x * cellSize + dx, // length
                                                                                (pattern.length - y - 1) * cellSize
                                                                                                + dy, // height
                                                                                -1 // One block behind
                                                                );
                                                        }
                                                        Block block = blockLoc.getBlock();
                                                        // Only place if air to avoid overwriting existing blocks
                                                        if (block.getType() == Material.AIR) {
                                                                block.setType(blockType);
                                                                generatedBlocks.add(block);
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }

        private void placeCharacter(Location start, boolean[][] pattern, Material blockType,
                        List<Block> generatedBlocks, int size, int width, boolean alongX) {
                int cellSize = size;
                if (cellSize < 1)
                        cellSize = 1;

                for (int y = 0; y < pattern.length; y++) {
                        for (int x = 0; x < pattern[y].length; x++) {
                                if (pattern[y][x]) {
                                        // Fill each cell with blocks
                                        for (int dy = 0; dy < cellSize; dy++) {
                                                for (int dx = 0; dx < cellSize; dx++) {
                                                        // Add blocks for thickness (width)
                                                        for (int w = 0; w < width; w++) {
                                                                Location blockLoc = start.clone();
                                                                if (alongX) {
                                                                        blockLoc.add(
                                                                                        w, // width
                                                                                        (pattern.length - y - 1)
                                                                                                        * cellSize + dy, // height
                                                                                        x * cellSize + dx // length
                                                                        );
                                                                } else {
                                                                        blockLoc.add(
                                                                                        x * cellSize + dx, // length
                                                                                        (pattern.length - y - 1)
                                                                                                        * cellSize + dy, // height
                                                                                        w // width
                                                                        );
                                                                }
                                                                Block block = blockLoc.getBlock();
                                                                block.setType(blockType);
                                                                generatedBlocks.add(block);
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }

        private Map<Character, boolean[][]> initializePatterns() {
                Map<Character, boolean[][]> patterns = new HashMap<>();

                // Define patterns for bold letters (5x5 grid)
                patterns.put('A', new boolean[][] {
                                { false, true, true, true, false },
                                { true, false, false, false, true },
                                { true, true, true, true, true },
                                { true, false, false, false, true },
                                { true, false, false, false, true }
                });

                patterns.put('B', new boolean[][] {
                                { true, true, true, true, false },
                                { true, false, false, false, true },
                                { true, true, true, true, false },
                                { true, false, false, false, true },
                                { true, true, true, true, false }
                });

                patterns.put('C', new boolean[][] {
                                { false, true, true, true, true },
                                { true, false, false, false, false },
                                { true, false, false, false, false },
                                { true, false, false, false, false },
                                { false, true, true, true, true }
                });

                patterns.put('D', new boolean[][] {
                                { true, true, true, true, false },
                                { true, false, false, false, true },
                                { true, false, false, false, true },
                                { true, false, false, false, true },
                                { true, true, true, true, false }
                });

                patterns.put('E', new boolean[][] {
                                { true, true, true, true, true },
                                { true, false, false, false, false },
                                { true, true, true, true, false },
                                { true, false, false, false, false },
                                { true, true, true, true, true }
                });

                patterns.put('F', new boolean[][] {
                                { true, true, true, true, true },
                                { true, false, false, false, false },
                                { true, true, true, true, false },
                                { true, false, false, false, false },
                                { true, false, false, false, false }
                });

                patterns.put('G', new boolean[][] {
                                { false, true, true, true, true },
                                { true, false, false, false, false },
                                { true, false, true, true, true },
                                { true, false, false, false, true },
                                { false, true, true, true, true }
                });

                patterns.put('H', new boolean[][] {
                                { true, false, false, false, true },
                                { true, false, false, false, true },
                                { true, true, true, true, true },
                                { true, false, false, false, true },
                                { true, false, false, false, true }
                });

                patterns.put('I', new boolean[][] {
                                { true, true, true, true, true },
                                { false, false, true, false, false },
                                { false, false, true, false, false },
                                { false, false, true, false, false },
                                { true, true, true, true, true }
                });

                patterns.put('J', new boolean[][] {
                                { true, true, true, true, true },
                                { false, false, false, true, false },
                                { false, false, false, true, false },
                                { true, false, false, true, false },
                                { false, true, true, false, false }
                });

                patterns.put('K', new boolean[][] {
                                { true, false, false, false, true },
                                { true, false, false, true, false },
                                { true, true, true, false, false },
                                { true, false, false, true, false },
                                { true, false, false, false, true }
                });

                patterns.put('L', new boolean[][] {
                                { true, false, false, false, false },
                                { true, false, false, false, false },
                                { true, false, false, false, false },
                                { true, false, false, false, false },
                                { true, true, true, true, true }
                });

                patterns.put('M', new boolean[][] {
                                { true, false, false, false, true },
                                { true, true, false, true, true },
                                { true, false, true, false, true },
                                { true, false, false, false, true },
                                { true, false, false, false, true }
                });

                patterns.put('N', new boolean[][] {
                                { true, false, false, false, true },
                                { true, true, false, false, true },
                                { true, false, true, false, true },
                                { true, false, false, true, true },
                                { true, false, false, false, true }
                });

                patterns.put('O', new boolean[][] {
                                { false, true, true, true, false },
                                { true, false, false, false, true },
                                { true, false, false, false, true },
                                { true, false, false, false, true },
                                { false, true, true, true, false }
                });

                patterns.put('P', new boolean[][] {
                                { true, true, true, true, false },
                                { true, false, false, false, true },
                                { true, true, true, true, false },
                                { true, false, false, false, false },
                                { true, false, false, false, false }
                });

                patterns.put('Q', new boolean[][] {
                                { false, true, true, true, false },
                                { true, false, false, false, true },
                                { true, false, false, false, true },
                                { true, false, false, true, false },
                                { false, true, true, false, true }
                });

                patterns.put('R', new boolean[][] {
                                { true, true, true, true, false },
                                { true, false, false, false, true },
                                { true, true, true, true, false },
                                { true, false, false, true, false },
                                { true, false, false, false, true }
                });

                patterns.put('S', new boolean[][] {
                                { false, true, true, true, true },
                                { true, false, false, false, false },
                                { false, true, true, true, false },
                                { false, false, false, false, true },
                                { true, true, true, true, false }
                });

                patterns.put('T', new boolean[][] {
                                { true, true, true, true, true },
                                { false, false, true, false, false },
                                { false, false, true, false, false },
                                { false, false, true, false, false },
                                { false, false, true, false, false }
                });

                patterns.put('U', new boolean[][] {
                                { true, false, false, false, true },
                                { true, false, false, false, true },
                                { true, false, false, false, true },
                                { true, false, false, false, true },
                                { false, true, true, true, false }
                });

                patterns.put('V', new boolean[][] {
                                { true, false, false, false, true },
                                { true, false, false, false, true },
                                { true, false, false, false, true },
                                { false, true, false, true, false },
                                { false, false, true, false, false }
                });

                patterns.put('W', new boolean[][] {
                                { true, false, false, false, true },
                                { true, false, false, false, true },
                                { true, false, true, false, true },
                                { true, true, false, true, true },
                                { true, false, false, false, true }
                });

                patterns.put('X', new boolean[][] {
                                { true, false, false, false, true },
                                { false, true, false, true, false },
                                { false, false, true, false, false },
                                { false, true, false, true, false },
                                { true, false, false, false, true }
                });

                patterns.put('Y', new boolean[][] {
                                { true, false, false, false, true },
                                { false, true, false, true, false },
                                { false, false, true, false, false },
                                { false, false, true, false, false },
                                { false, false, true, false, false }
                });

                patterns.put('Z', new boolean[][] {
                                { true, true, true, true, true },
                                { false, false, false, true, false },
                                { false, false, true, false, false },
                                { false, true, false, false, false },
                                { true, true, true, true, true }
                });

                return patterns;
        }
}