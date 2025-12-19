package com.example.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class PersonItemProcessor implements ItemProcessor<Person, Person> {
	private static final Logger LOGGER = LoggerFactory.getLogger(PersonItemProcessor.class);

    @Override
	public Person process(Person person) throws Exception  {
		String firstName = person.firstName().toUpperCase();
		String lastName = person.lastName().toUpperCase();

		Person processedPerson = new Person(firstName, lastName);

		LOGGER.info("Processed: " + person + " into: " + processedPerson);

		return processedPerson;
	}
}
