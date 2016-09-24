package net.ldauvilaire.sample.batch.job;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import net.ldauvilaire.sample.batch.domain.dto.PersonDTO;
import net.ldauvilaire.sample.batch.domain.model.Person;

@Configuration
public class FirstJobConfiguration {

	private static final String PROPERTY_NAME_FIRST_CHUNCK_SIZE = "first.chunck.size";

	@Autowired
	protected JobBuilderFactory jobBuilderFactory;

	@Autowired
	protected StepBuilderFactory stepBuilderFactory;

	@Autowired
	protected EntityManagerFactory entityManagerFactory;

	@Autowired
	protected JobRepository jobRepository;

	@Resource(name=JobConstants.FIRST_JOB_ITEM_READER_ID)
	protected ItemReader<PersonDTO> reader;

	@Resource(name=JobConstants.FIRST_JOB_ITEM_PROCESSOR_ID)
	protected ItemProcessor<PersonDTO, Person> processor;

	@Resource(name=JobConstants.FIRST_JOB_ITEM_WRITER_ID)
	protected ItemWriter<Person> writer;

	@Resource(name=JobConstants.FIRST_JOB_EXECUTION_LISTENER_ID)
	protected JobExecutionListener listener;

	@Bean
	public Job firstJob(Environment env) throws Exception {
		return jobBuilderFactory.get(JobConstants.FIRST_JOB_ID)
				.repository(jobRepository)
				.incrementer(new RunIdIncrementer())
				.listener(listener)
				.flow(firstStep1(env))
				.end()
				.build();
	}

	@Bean
	public Step firstStep1(Environment env) throws Exception {
		Integer chunkSize = env.getRequiredProperty(PROPERTY_NAME_FIRST_CHUNCK_SIZE, Integer.class);
		return stepBuilderFactory.get(JobConstants.FIRST_JOB_STEP_ID)
				.repository(jobRepository)
				.<PersonDTO, Person> chunk(chunkSize)
				.reader(reader)
				.processor(processor)
				.writer(writer)
				.build();
	}
}
