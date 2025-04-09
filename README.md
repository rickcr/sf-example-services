# sf-example-services
This main purpose of this project is to demonstrate how you could dump a large table into a Snowflake stage and then retrieve the multiple files (representing that table) Snowflake produces using GET, and then combining them into a single file. Currently, Snowflake only supports a max file size of 5GB, so if you need to dump a table into a file that will end up larger than 5GB, you'll need to gather up all the small files it produces and create one large from that. **NOTE:**, the files Snowflake produces will not sort correctly using a natural sort, so there is a Comparator in this project that will enable them to be sorted correctly from your local file system.

The project also illustrates how to create a stage, use COPY INTO, and of course using GET to retrieve the files from the Stage.

The project uses Spring and Mybatis for the db work, but you can see the queries being run in the UtilitiesMapper and ajdust to whatever your using (Spring JDBC, Hibernate, etc)

To run the unit test illustrating the work:

1) Modify your jdbc information in test/resources/application-test.yml
2) In the UtilitiesServicesIT class modify:
   
        String stage = "my_stage"; //intenral SF stage
        String query = "select * from YOUR_TABLE order by 1";
        String stageFileName = "my-ouptut-filename"; //what you want the files to be called that get downloaded from GET
        String downloadDir = "/Users/rickreumann/Downloads/stagecontents/"; //where you locally want the files to download to
        String outputFile = "/Users/rickreumann/Downloads/results.csv.gz"; //name of your final output file
