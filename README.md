# akka-m800
akka assignment

Build with maven, on success you will find log-processor.jar in target folder. Run below command.

java -jar log-processor.jar <directory-path-with-files>


FileScanner Actor is created initially. Iterates through files in the provided directory. For each file a FileParser actor is created. Each FileParser creates an Aggregator actor and sends signals to it. 
