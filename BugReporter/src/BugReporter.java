/*
 * Copyright 2018 Rohit Awate.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.*;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BugReporter {
    public static void main(String args[]) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Everest Bug Reporting Service");
        System.out.println("-----------------------------\n");
        System.out.println("Please describe the issue with as much detail and clarity as possible (no newline): ");
        String userMessage = scanner.nextLine();
        scanner.close();

        generateReportFile(generateReport(userMessage));
        generateZipFile();

        System.out.println("\nYour report was submitted successfully reported and will be evaluated soon.");
        System.out.println("Thank you! :)");
    }

    private static String generateReport(String userMessage) {
        StringBuilder report = new StringBuilder();
        report.append("Report date: ");
        report.append(LocalDateTime.now());
        report.append("\n\n");
        report.append(getSystemDetails());
        report.append("User Message:\n");
        report.append(userMessage);
        return report.toString();
    }

    private static String getSystemDetails() {
        StringBuilder builder = new StringBuilder();
        String OS = System.getProperty("os.name");
        if (OS.equals("Linux")) {
            builder.append(getLinuxDetails());
        }
        builder.append("OS: ");
        builder.append(OS);
        builder.append(" ");
        builder.append(System.getProperty("os.arch"));
        builder.append(" ");
        builder.append(System.getProperty("os.version"));
        builder.append("\nJava VM: ");
        builder.append(System.getProperty("java.vm.name"));
        builder.append("\nVM Version: ");
        builder.append(System.getProperty("java.version"));
        builder.append("\nVM Vendor: ");
        builder.append(System.getProperty("java.vendor"));
        builder.append("\n\n");

        return builder.toString();
    }

    private static void generateReportFile(String reportContents) {
        String reportFileName = "Report - " + LocalDate.now() + ".txt";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("logs/" + reportFileName));
            writer.write(reportContents);
            writer.close();
        } catch (IOException IOE) {
            IOE.printStackTrace();
        }
    }

    private static void generateZipFile() {
        try {
            Scanner scanner;
            FileInputStream fileInputStream;
            ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream("BugReport-" + LocalDate.now() + ".zip"), Charset.forName("UTF-8"));
            File sourceDir = new File("logs/");
            String[] logFiles = sourceDir.list();

            for (String logFile : logFiles) {
                zipStream.putNextEntry(new ZipEntry(logFile));

                fileInputStream = new FileInputStream("logs/" + logFile);
                scanner = new Scanner(fileInputStream);

                while (scanner.hasNext()) {
                    zipStream.flush();
                    zipStream.write(scanner.nextLine().getBytes());
                    zipStream.write('\n');
                }

                zipStream.closeEntry();
                scanner.close();
                fileInputStream.close();
            }

            zipStream.close();
        } catch (IOException IOE) {
            IOE.printStackTrace();
        }
    }

    private static String getLinuxDetails() {
        String line;

        try {
            File etcDir = new File("/etc/");
            String releaseFile = "";
            for (String file : etcDir.list()) {
                if (file.endsWith("-release")) {
                    releaseFile = file;
                    break;
                }
            }
            BufferedReader reader = new BufferedReader(new FileReader("/etc/" + releaseFile));

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("PRETTY_NAME"))
                    break;
            }
            reader.close();
            if (!line.equals("")) {
                line = "Distribution: " + line.substring(13, line.length() - 1) + "\n";
            }
        } catch (IOException IOE) {
            line = "";
            Scanner scanner = new Scanner(System.in);
            System.out.println("We couldn't fetch information about your Linux distribution. Please fill in the following details:");
            System.out.println("Distribution name: ");
            line += "Distribution: " + scanner.nextLine() + "\n";
            System.out.println("Version: ");
            line += "Version: " + scanner.nextLine() + "\n";
            scanner.close();
        }
        return line;
    }
}
