package net.reumann.sf.service;
 
import net.reumann.sf.mapper.UtilitiesMapper; 
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*; 
import java.util.ArrayList;
import java.util.Collections;
import java.util.List; 
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Service
public class UtilitiesService {
 
    private UtilitiesMapper utilitiesMapper;

    @Autowired
    public UtilitiesService(UtilitiesMapper utilitiesMapper) {
        this.utilitiesMapper = utilitiesMapper; 
    }
      
    public void copyTableIntoStage(TableToStageData tableToStageData) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        utilitiesMapper.copyTableIntoStage(tableToStageData.query, tableToStageData.stage, tableToStageData.path,
                tableToStageData.stageFileName, tableToStageData.stageInfo);
        stopWatch.stop();
        log.info("Total time to copy table {} into stage {}, {}", tableToStageData.query, tableToStageData.stage, stopWatch);
    }

    public void getFileFromStage(TableToStageData tableToStageData) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        utilitiesMapper.getFileFromStage(tableToStageData.stage, tableToStageData.path, tableToStageData.stageFileName, tableToStageData.downloadDir);
        stopWatch.stop();
        log.info("Total time to getFileFromStage {}", stopWatch);
    }

    public int mergeFilesIntoAnotherFile(String dirWithFiles,  String fileOutputFileFullName, String delimiter) throws Exception {
        log.info("started mergeFilesIntoAnotherFile to produce {}", fileOutputFileFullName);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int rowsProcessed = 0;
        List<String> orderedFileNames = getSortedFileNames(dirWithFiles);

        //attempt to make the dir if it doesn't exist
        File file = new File(fileOutputFileFullName);
        if (!file.exists()) {
            if (file.getParent() != null) {
                File parent = new File(file.getParent());
                parent.mkdirs();
            }
        }

        try (
               FileOutputStream fos = new FileOutputStream(fileOutputFileFullName);
               GZIPOutputStream gzos = new GZIPOutputStream(fos);
               OutputStreamWriter osw = new OutputStreamWriter(gzos, "UTF-8");
               BufferedWriter writer = new BufferedWriter(osw);
               )
       {
           boolean wroteHeader = false;
           for (String orderedFileName : orderedFileNames) {
               int fileRowCount = 0;

               StopWatch stopWatch2 = new StopWatch();
               stopWatch2.start();
               try (
                   FileInputStream fis = new FileInputStream(orderedFileName);
                   GZIPInputStream gzipIS = new GZIPInputStream(fis);
                   BufferedReader reader = new BufferedReader(new InputStreamReader(gzipIS, "UTF-8"));

                 ) {
                   String line;
                   // Read each line and write to output
                   while ((line = reader.readLine()) != null) {
                       if (fileRowCount != 0 || !wroteHeader) { 
                           writer.write(line);
                           writer.newLine();
                           if (!wroteHeader) wroteHeader = true; 
                       }
                       rowsProcessed++;
                       fileRowCount++;
                   }
                   writer.flush();
               }
               stopWatch2.stop();
               log.info("processed file {}, in {}", orderedFileName, stopWatch2);
           }
       }
        stopWatch.stop();
        log.info("Total time to process all files {}", stopWatch);
        return rowsProcessed;
    }

    public int exportTableDataToFileUsingStage(TableToStageData tableToStageData, boolean padHeaderColsWithQuotes) throws Exception {
        String stage = tableToStageData.stage;
        String stageFound = utilitiesMapper.checkStageName(stage.toUpperCase());
        if (stageFound == null) {
            utilitiesMapper.createStage(stage, tableToStageData.stageInfo);
        }
        copyTableIntoStage(tableToStageData);
        getFileFromStage(tableToStageData);
        int rows = mergeFilesIntoAnotherFile(tableToStageData.downloadDir, tableToStageData.outputFileFullName, tableToStageData.stageInfo.fileDelimiter);
        utilitiesMapper.removeFilesFromStage(tableToStageData.stage, tableToStageData.path);
        return  rows;
    }


    public static List<String> getSortedFileNames(String directoryPath) {

        List<String> fileNames = new ArrayList<>();
        File directory = new File(directoryPath);

        // Check if directory exists and is actually a directory
        if (!directory.exists() || !directory.isDirectory()) {
            log.error("Invalid directory path: {}", directoryPath);
            return fileNames;
        }

        // Get all files in the directory
        File[] files = directory.listFiles();

        // If directory is empty or inaccessible
        if (files == null) {
            log.error("Unable to access directory contents or directory is empty");
            return fileNames;
        }

        // Add each file name to the list
        for (File file : files) {
            if (file.isFile()) { // Only include files, not subdirectories, and skips hidden files
                if (file.getName().startsWith(".")) {
                    continue;
                }
                fileNames.add(file.getAbsolutePath());
            }
        }

        /*
        a basic natural sort won't work since the files are named with underscores
        and _15_12_0 would be before _15_2_0, so need this comparator to
        handle each number between underscores
         */
        Collections.sort(fileNames, new StageFileUnderscoreNumberComparator());

        return fileNames;
    }

    @Builder
    public static class OutputTableWriterInfo {
        public String query;
        public String outputFileFullName;
        public String warehouse;
        @Builder.Default
        public String columnDelimiter = ",";
        @NonNull
        public Integer fetchSize;
        public boolean debug;
        public boolean createHeaderRow; 
    }

    @Builder
    public static class StageInfo {
        public String fileFormatType; //CSV, etc
        public String fileDelimiter; //','
        public String fileCompression; //NONE
        public String overwrite; //TRUE
        public String single; //FALSE
        public String maxFileSize; //5368709120 5GB 5_368_709_120
        public String createHeader; //used in COPY INTO
        public int skipHeaderInCreateStage; //0 or 1
    }

    @Builder
    public static class TableToStageData {
        public String query;
        public String stage;
        public String path;
        public String stageFileName;
        public String downloadDir;
        public String outputFileFullName;
        public StageInfo stageInfo;
    }
}
