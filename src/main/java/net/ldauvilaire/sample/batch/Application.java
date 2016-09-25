package net.ldauvilaire.sample.batch;

import java.io.File;

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

	public static void main(String args[]) {

		LOGGER.debug("Starting Application ...");

		AbstractApplicationContext context = new AnnotationConfigApplicationContext(BatchConfiguration.class);

		String inputFilePath = (((args == null) || (args.length < 1)) ? null : args[0]);
		LOGGER.debug("Application Input Path = [{}].", inputFilePath);

		String dirName = null;
		String fileName = null;
		try {
			File inputFile = new File(inputFilePath);
			dirName = inputFile.getParentFile().getPath();
			fileName = inputFile.getName();
		} catch (Exception ex) {
			LOGGER.error("An Exception has occurred", ex);
		}

		LOGGER.info("dirName = [{}].", dirName);
		LOGGER.info("fileName = [{}].", fileName);

		JobParametersBuilder jpBuilder = new JobParametersBuilder();
		{
			jpBuilder.addString("dirName", dirName);
			jpBuilder.addString("fileName", fileName);
		}
		JobParameters jobParameters = jpBuilder.toJobParameters();

		JobLauncher jobLauncher = (JobLauncher) context.getBean(JobLauncher.class);
		Job initJob = (Job) context.getBean(JobConstants.FIRST_JOB_ID);

		try {
			jobLauncher.run(initJob, jobParameters);
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

		context.close();

		LOGGER.debug("Ending Application.");
	}
}
