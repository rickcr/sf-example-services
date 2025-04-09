package net.reumann.sf;

import net.reumann.sf.BaseIT;
import net.reumann.sf.mapper.UtilitiesMapper;
import net.reumann.sf.service.UtilitiesService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class UtilitiesServiceIT extends BaseIT {


    @Autowired
    private UtilitiesMapper utilitiesMapper;

    @Autowired
    private UtilitiesService utilitiesService;
  
 
    @Test
    public void create_and_use_stage() throws Exception {
        String stage = "my_stage"; //intenral SF stage
        long maxFileSize = 5368709120L; //max 5gb
        String path = "1234"; //unique path in the stage to have table contents dump to

        UtilitiesService.StageInfo stageInfo = UtilitiesService.StageInfo.builder().fileDelimiter(",").fileCompression("gzip").fileFormatType("CSV")
                .maxFileSize(String.valueOf(maxFileSize)).overwrite("TRUE").single("FALSE").createHeader("TRUE").skipHeaderInCreateStage(0).build();

         
        if (utilitiesMapper.checkStageName(stage.toUpperCase()) == null) {
            log.debug("stage not found, so creating it");
            utilitiesMapper.createStage(stage, stageInfo);
        } 

        String query = "select * from YOUR_TABLE order by 1";
        String stageFileName = "my-ouptut-filename"; //what you want the files to be called that get downloaded from GET
        String downloadDir = "/Users/rickreumann/Downloads/stagecontents/"; //where you locally want the files to download to
        String outputFile = "/Users/rickreumann/Downloads/results.csv.gz"; //name of your final output file

        UtilitiesService.TableToStageData tableToStageData = UtilitiesService.TableToStageData.builder()
                .query(query)
                .stage(stage)
                .path(path)
                .stageFileName(stageFileName)
                .stageInfo(stageInfo)
                .downloadDir(downloadDir)
                .build();

        utilitiesService.copyTableIntoStage(tableToStageData);
        utilitiesService.getFileFromStage(tableToStageData);
        utilitiesService.mergeFilesIntoAnotherFile(tableToStageData.downloadDir,
                outputFile, ",");
        utilitiesMapper.removeFilesFromStage(stage, path);
    }
}
