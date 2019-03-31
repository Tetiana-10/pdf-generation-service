package main.java.hello.Dto;

import java.sql.Date;

public class UserDto {
	   private String firstName;
	   private String lastName;
	   private Date dateOfCreation;

	   public String getFirstName() {
	      return firstName;
	   }

	   public void setFirstName(String firstName) {
	      this.firstName = firstName;
	   }

	   public String getLastName() {
	      return lastName;
	   }

	   public void setCountry(String lastName) {
	      this.lastName = lastName;
	   }
	   
	   public Date getDateOfCreation () {
		   return dateOfCreation;
	   }
	   public void setDateOfCreation (Date dateOfCreation) {
		   this.dateOfCreation = dateOfCreation;
	   }   
}
