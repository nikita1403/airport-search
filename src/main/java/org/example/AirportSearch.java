package org.example;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.stream.Collectors;

public class AirportSearch {
    private static final String DELIMITER = ";";
    private static final Map<String, Integer> indexingCSV;
    private static final Scanner scanner = new Scanner(System.in);
    private static String pathToCSVFile;

    static
    {
        indexingCSV = new HashMap<>();
        boolean flagIfFileExist = true;
        System.out.println("Enter the path to the CSV file containing the airport data");
        while (flagIfFileExist)
        {
            pathToCSVFile = scanner.nextLine();
            try(RandomAccessFile randomAccessFile = new RandomAccessFile(pathToCSVFile, "r")) {
                flagIfFileExist = false;
                String line;
                while ((line = randomAccessFile.readLine()) != null) {
                    String[] values = line.split(DELIMITER);
                    if (values.length > 1 && !values[1].equals("Column2")) {
                        String nameAirport = values[1].toLowerCase();
                        int position = Math.toIntExact(randomAccessFile.getFilePointer() - line.length() - 2);
                        indexingCSV.put(nameAirport, position);
                    }
                }
            } catch (IOException e) {
                System.out.println("This file does not exist, please try again");
            }
        }
    }
    private static void fileSearch(String airportName, String stringFilter)
    {
        int countRows = 0;
        try {
            Filter filter = new Filter(stringFilter);
            long start = System.currentTimeMillis();
            List<Integer> positions = indexingCSV.entrySet()
                    .stream()
                    .filter(e -> e.getKey().startsWith(airportName))
                    .map(Map.Entry::getValue)
                    .sorted()
                    .collect(Collectors.toList());
            try(RandomAccessFile randomAccessFile = new RandomAccessFile(pathToCSVFile, "r")) {
                for (long position : positions) {
                    randomAccessFile.seek(position);
                    String line1 = randomAccessFile.readLine();
                    String[] tokens = line1.split(DELIMITER);
                    if(tokens[1].toLowerCase().startsWith(airportName) && filter.matches(tokens))
                    {
                        System.out.println(tokens[1] + " " + Arrays.toString(tokens));
                        countRows++;
                    }
                }
                long end = System.currentTimeMillis();
                long elapsed = end - start;
                System.out.println("Time spent searching and printing results, ms:" + elapsed);
                System.out.println("Total lines found: " + countRows);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        catch (RuntimeException e)
        {
            System.out.println(e.getMessage());
        }
    }
    public static void main(String[] args) {

        String airportName, filter;
        boolean flag = true;
        do {
            Runtime runtime = Runtime.getRuntime();
            System.out.println("Enter a search filter in the format column[<column number with 1>]<comparison operation><comparison value>. To exit, type !quit");
            filter = scanner.nextLine();
            switch (filter)
            {
                case "!quit":
                {
                    flag = false;
                    break;
                }
                default:
                    System.out.println("Enter the beginning of the airport name");
                    airportName = scanner.nextLine();
                    fileSearch(airportName.toLowerCase(),filter);
                    break;
            }
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            System.out.println("Total memory: " + totalMemory / (1024 * 1024) + " MB");
            System.out.println("Free memory: " + freeMemory / (1024 * 1024) + " MB");
            System.out.println("Used memory: " + usedMemory / (1024 * 1024) + " MB");
        } while (flag);
    }
}