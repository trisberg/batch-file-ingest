package com.example.batch;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
@EnableTask
public class BatchConfiguration {

    @Bean
	@StepScope
	public ItemStreamReader<Person> reader(@Value("#{jobParameters['localFilePath']}") String filePath, ResourceLoader resourceLoader) {

		if (filePath == null || !filePath.matches("[a-z]+:.*")) {
			throw new IllegalArgumentException(String.format("Invalid file path: %s; a scheme like 'file:' or 'https:' must be provided", filePath));
		}

		return new FlatFileItemReaderBuilder<Person>()
			.name("reader")
			.resource(resourceLoader.getResource(filePath))
			.delimited()
			.names(new String[] { "firstName", "lastName" })
			.fieldSetMapper(new PersonFieldSetMapper())
			.build();
    }

    @Bean
	public ItemProcessor<Person, Person> processor() {
		return new PersonItemProcessor();
	}

	@Bean
	public ItemWriter<Person> writer(DataSource dataSource) {
		return new JdbcBatchItemWriterBuilder<Person>()
			.beanMapped()
			.dataSource(dataSource)
			.sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)")
			.build();
	}

    @Bean
	public Job ingestJob(JobRepository jobRepository, Step step1) {
		return new JobBuilder("ingestJob", jobRepository)
			.incrementer(new RunIdIncrementer())
			.flow(step1)
			.end()
			.build();
	}

	@Bean
	public Step step1(JobRepository jobRepository, DataSourceTransactionManager transactionManager, ItemReader<Person> reader, ItemProcessor<Person, Person> processor, ItemWriter<Person> writer) {
		return new StepBuilder("ingest", jobRepository)
			.<Person, Person>chunk(10, transactionManager)
			.reader(reader)
			.processor(processor())
			.writer(writer)
			.build();
	}

    public static class PersonFieldSetMapper implements FieldSetMapper<Person> {
        @Override
        public Person mapFieldSet(FieldSet fieldSet) {
            String firstName = fieldSet.readString(0);
            String lastName = fieldSet.readString(1);
            return new Person(firstName, lastName);
        }
    }
}
