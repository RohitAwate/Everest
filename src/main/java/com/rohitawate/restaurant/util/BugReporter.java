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

package com.rohitawate.restaurant.util;

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
        System.out.println("RESTaurant Bug Reporting Service");
        System.out.println();
        System.out.println("Please describe the issue with as much detail and clarity as possible: ");
        String userMessage = scanner.nextLine();
        StringBuilder builder = new StringBuilder();
        builder.append("\nThank you for your input! The issue was recorded.\n\n");
        builder.append("With your permission, this service can collect some anonymous, non-personal information about your system.\n");
        builder.append("This information will help us to better reproduce the issue and fix it quickly.\n");
        builder.append("This includes:\n");
        builder.append(" - Operating system details.\n");
        builder.append(" - Details about your Java Runtime Environment.\n\n");
        builder.append("Allow? (Y/N)\n>> ");
        System.out.print(builder.toString());
        String allowSystemData = scanner.nextLine();

        allowSystemData = allowSystemData.toLowerCase();

        StringBuilder report = new StringBuilder();
        report.append("Log date: ");
        report.append(LocalDateTime.now());
        report.append("\n\n");
        if (allowSystemData.equals("y") || allowSystemData.equals("yes")) {
            report.append(generateSystemDetails());
            System.out.println("\nThat's great! We will include your system details in with the bug report.");
        } else {
            System.out.println("\nAlrighty! We will only include RESTaurant's log files in the report.");
        }

        scanner.close();
        report.append("User Message:\n");
        report.append(userMessage);
        generateReport(report.toString());
        generateZipFile();

        System.out.println("\nYour issue was successfully reported and will be fixed soon.");
        System.out.println("Thank you! :)");
    }

    private static String generateSystemDetails() {
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
        builder.append("\n");
        builder.append("Java VM: ");
        builder.append(System.getProperty("java.vm.name"));
        builder.append(" ");
        builder.append(System.getProperty("java.version"));
        builder.append("\nJava VM Vendor: ");
        builder.append(System.getProperty("java.vendor"));
        builder.append("\n\n");

        return builder.toString();
    }

    private static void generateReport(String reportContents) {
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
            ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream("Logs.zip"), Charset.forName("UTF-8"));
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
        String line = "";

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
