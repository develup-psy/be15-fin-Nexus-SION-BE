package com.nexus.sion.feature.project.command.application.util;

public class FPScoreUtils {

    public static String classifyComplexity(String type, int det, int ftrOrRet) {
        return switch (type) {
            case "EI", "EO", "EQ" -> {
                if (ftrOrRet <= 1) yield (det <= 4) ? "SIMPLE" : (det <= 15) ? "SIMPLE" : "MEDIUM";
                else if (ftrOrRet == 2) yield (det <= 4) ? "SIMPLE" : (det <= 15) ? "MEDIUM" : "COMPLEX";
                else yield (det <= 4) ? "MEDIUM" : "COMPLEX";
            }
            case "ILF", "EIF" -> {
                if (ftrOrRet == 1) yield (det <= 19) ? "SIMPLE" : (det <= 50) ? "SIMPLE" : "MEDIUM";
                else if (ftrOrRet <= 5) yield (det <= 19) ? "SIMPLE" : (det <= 50) ? "MEDIUM" : "COMPLEX";
                else yield (det <= 19) ? "MEDIUM" : "COMPLEX";
            }
            default -> "Unknown";
        };
    }

    public static int getFpScore(String type, String complexity) {
        return switch (type) {
            case "EI" -> switch (complexity) {
                case "SIMPLE" -> 3;
                case "MEDIUM" -> 4;
                case "COMPLEX" -> 6;
                default -> 0;
            };
            case "EO" -> switch (complexity) {
                case "SIMPLE" -> 4;
                case "MEDIUM" -> 5;
                case "COMPLEX" -> 7;
                default -> 0;
            };
            case "EQ" -> switch (complexity) {
                case "SIMPLE" -> 3;
                case "MEDIUM" -> 4;
                case "COMPLEX" -> 6;
                default -> 0;
            };
            case "ILF" -> switch (complexity) {
                case "SIMPLE" -> 7;
                case "MEDIUM" -> 10;
                case "COMPLEX" -> 15;
                default -> 0;
            };
            case "EIF" -> switch (complexity) {
                case "SIMPLE" -> 5;
                case "MEDIUM" -> 7;
                case "COMPLEX" -> 10;
                default -> 0;
            };
            default -> 0;
        };
    }
}

