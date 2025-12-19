package com.example.batch;

public record Person(String firstName, String lastName) {

	@Override
	public String toString() {
		return "Person[" + firstName + "," + lastName + "]";
	}

}
