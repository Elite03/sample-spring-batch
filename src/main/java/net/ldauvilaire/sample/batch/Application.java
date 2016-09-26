package net.ldauvilaire.sample.batch;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import net.ldauvilaire.sample.batch.job.JobConstants;

public class Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	private static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	public static void main(String args[]) {

		CommandLineParser parser = new DefaultParser();

		Options options = new Options();
		options.addOption(Option.builder("init")
				.argName("initFile")
				.hasArg()
				.desc("use given file for initialisation")
				.build());

		try {
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("init")) {
				String param = line.getOptionValue("init");

				File initFile = new File(param);
				if (initFile.exists()) {
					Application application = new Application();
					application.init(initFile);
				} else {
					LOGGER.error("File [{}] does not exists", initFile);
					HelpFormatter formatter = new HelpFormatter();
					formatter.printHelp("sample-batch", options);
				}

			} else {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("sample-batch", options);
			}

		} catch (ParseException ex) {
			LOGGER.error("A ParseException has occurred", ex);
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("sample-batch", options);
		}
	}

	public void init(File inputFile) {

		LOGGER.debug("Starting Application ...");

		AbstractApplicationContext context = new AnnotationConfigApplicationContext(BatchConfiguration.class);

		String inputFilePath = inputFile.getPath();
		LOGGER.debug("Application Input Path = [{}].", inputFilePath);

		String dirName = null;
		String fileName = null;
		try {
			dirName = inputFile.getParentFile().getPath();
			fileName = inputFile.getName();
		} catch (Exception ex) {
			LOGGER.error("An Exception has occurred", ex);
		}

		LOGGER.info("dirName = [{}].", dirName);
		LOGGER.info("fileName = [{}].", fileName);

		JobLauncher jobLauncher = (JobLauncher) context.getBean(JobLauncher.class);
		Job initJob = (Job) context.getBean(JobConstants.FIRST_JOB_ID);

		for (int i=0; i<2; i++) {

			JobParametersBuilder jpBuilder = new JobParametersBuilder();
			{
				jpBuilder.addString("time", DF.format(new Date()));
				jpBuilder.addString("dirName", dirName);
				jpBuilder.addString("fileName", fileName);
			}
			JobParameters jobParameters = jpBuilder.toJobParameters();

			try {
				LOGGER.info("-- Run {} - Debut -----------------------------------------------", i+1);
				jobLauncher.run(initJob, jobParameters);
				LOGGER.info("-- Run {} - Fin -------------------------------------------------", i+1);
			} catch (JobExecutionAlreadyRunningException ex) {
				LOGGER.error("A JobExecutionAlreadyRunningException has occurred", ex);
			} catch (JobRestartException ex) {
				LOGGER.error("A JobRestartException has occurred", ex);
			} catch (JobInstanceAlreadyCompleteException ex) {
				LOGGER.error("A JobInstanceAlreadyCompleteException has occurred", ex);
			} catch (JobParametersInvalidException ex) {
				LOGGER.error("A JobParametersInvalidException has occurred", ex);
			} catch (Exception ex) {
				LOGGER.error("An Exception has occurred", ex);
			}
		}

		context.close();

		LOGGER.debug("Ending Application.");
	}
}
