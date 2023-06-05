package org.example;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class Filter {
    private final List<String[]> andConditions = new ArrayList<>();
    public Filter(String filter) {
        if(!filter.isEmpty())
        {
            String[] orConditions = filter.split("\\|\\|");
            for (String orCondition : orConditions) {
                orCondition = orCondition.trim();
                andConditions.add(orCondition.split("&"));
            }
            for (String[] andCondition : andConditions) {
                if (Arrays.stream(andCondition).anyMatch(x -> !x.matches("column\\[\\d+\\](<|=|>|<>)(\\d+(\\.\\d+)?|.\\w+)"))) {
                    throw new RuntimeException("Invalid filter input");
                }
            }
        }
    }

    public boolean matches(String[] row) {
        if(andConditions.isEmpty()) return true;
        for (String[] andCondition : andConditions) {
            boolean andResult = true;
            for (String andCond : andCondition) {
                andCond = andCond.trim();
                int index = Integer.parseInt(andCond.substring(andCond.indexOf("[") + 1, andCond.indexOf("]"))) - 1;
                if (index >= 0 && index < row.length) {
                    String columnValue = row[index];
                    String operator = andCond.replaceAll("[^><=!]+", "");
                    String value = andCond.substring(andCond.contains(">") ? andCond.indexOf(">") + 1 : andCond.indexOf("]") + 2).trim();
                    value = value.replace("\"", "");
                    try {
                        double numberValue = Double.parseDouble(value);
                        if (columnValue.matches("\\d+(\\.\\d+)?")) {
                            double numberColumnValue = Double.parseDouble(columnValue);
                            switch (operator) {
                                case ">":
                                    andResult = andResult && (numberColumnValue > numberValue);
                                    break;
                                case "<":
                                    andResult = andResult && (numberColumnValue < numberValue);
                                    break;
                                case "=":
                                    andResult = andResult && (numberColumnValue == numberValue);
                                    break;
                                case "<>":
                                    andResult = andResult && (numberColumnValue != numberValue);
                                    break;
                                default:
                                    andResult = false;
                            }
                        } else {
                            andResult = false;

                        }
                    } catch (NumberFormatException e) {
                        switch (operator) {
                            case "=":
                                andResult = andResult && (columnValue.equals(value));
                                break;
                            case "<>":
                                andResult = andResult && !(columnValue.equals(value));
                                break;
                            default:
                                andResult = false;
                        }
                    }
                }
                else
                {
                    andResult = false;
                }
            }

            if (andResult) {
                return true;
            }
        }
        return false;
    }
}