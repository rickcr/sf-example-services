package net.reumann.sf.mapper;

import net.reumann.sf.service.UtilitiesService;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.session.ResultHandler;

import java.util.List;

@Mapper
public interface UtilitiesMapper {

    @Update("""
            CREATE STAGE ${stageName}  
            FILE_FORMAT = (TYPE = 'CSV'  FIELD_DELIMITER = '${stageInfo.fileDelimiter}' SKIP_HEADER = ${stageInfo.skipHeaderInCreateStage})""")
    void createStage(String stageName, UtilitiesService.StageInfo stageInfo);

    @Update("""
          COPY INTO @${stage}/${path}/${stageFileName} from (${query})
          FILE_FORMAT = (
                          TYPE = '${stageInfo.fileFormatType}'
                          FIELD_DELIMITER = '${stageInfo.fileDelimiter}'
                          COMPRESSION = '${stageInfo.fileCompression}'
                      )
         HEADER = ${stageInfo.createHeader}
         OVERWRITE = ${stageInfo.overwrite}
         SINGLE = ${stageInfo.single}
         MAX_FILE_SIZE = ${stageInfo.maxFileSize}
       """)
    void copyTableIntoStage(String query, String stage, String path, String stageFileName, UtilitiesService.StageInfo stageInfo);

    @Update("GET @${stage}/${path}/${stageFileName} file://${filePath}")
    void getFileFromStage(String stage, String path, String stageFileName, String filePath);

    @Update("REMOVE @${stage}/${path}")
    void removeFilesFromStage(String stage, String path);

    @Select("""
            SELECT STAGE_NAME FROM INFORMATION_SCHEMA.STAGES
            WHERE STAGE_NAME = '${stageName}' AND STAGE_SCHEMA = 'PUBLIC'
    """)
    String checkStageName(String stageName);
}
