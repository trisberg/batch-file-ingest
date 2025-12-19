package com.example.batch;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(args = {"localFilePath=file:test/name-list.csv"})
class BatchFileIngestApplicationTests {

	@Test
	void contextLoads() {
	}

}
