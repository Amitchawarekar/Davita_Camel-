package com.citiustech.restprojects.route;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;

import com.citiustech.restprojects.processor.BasicAuthProcessor;

public class PatientCrudRoute extends RouteBuilder {
	
	public String getAllPatient;
	public String patientById;
	public String addPatient;
	public String deletePatient;
	public String updatePatient;
	public String updatePatientAddress;
	

	public String getAddPatient() {
		return addPatient;
	}


	public void setAddPatient(String addPatient) {
		this.addPatient = addPatient;
	}


	public String getDeletePatient() {
		return deletePatient;
	}


	public void setDeletePatient(String deletePatient) {
		this.deletePatient = deletePatient;
	}


	public String getUpdatePatient() {
		return updatePatient;
	}


	public void setUpdatePatient(String updatePatient) {
		this.updatePatient = updatePatient;
	}


	public String getUpdatePatientAddress() {
		return updatePatientAddress;
	}


	public void setUpdatePatientAddress(String updatePatientAddress) {
		this.updatePatientAddress = updatePatientAddress;
	}


	public String getPatientById() {
		return patientById;
	}


	public void setPatientById(String patientById) {
		this.patientById = patientById;
	}


	public String getGetAllPatient() {
		return getAllPatient;
	}


	public void setGetAllPatient(String getAllPatient) {
		this.getAllPatient = getAllPatient;
	}


	@Override
	public void configure() throws Exception {
		// TODO Auto-generated method stub
		
		
		restConfiguration() // starts server and configure the server
				.component("spark-rest").port(8080)
				.bindingMode(RestBindingMode.json);

		rest("/PatientData")
				.get("/all").to("direct:getAllPatients")

				.get("/{id}").to("direct:getPatientById")

				.post().to("direct:addPatient")

				.patch("/{id}").to("direct:updatePatientAddress")

				.delete("/{id}").to("direct:deletePatient")

				.put("/{id}").to("direct:putPatient");
		
		
		
		//get All patients using JDBC Component
		from("direct:getAllPatients").log("${header.Authorization}")
		.process(new BasicAuthProcessor())
		.choice()
		.when(header("SuccessfulAuthorization").isEqualTo(true))
		.setBody(simple(getGetAllPatient()))
		.to("jdbc:mysqlDataSource")
//		.when(header("SuccessfulAuthorization").isEqualTo(false))
		.otherwise()
		.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(401))
		.setBody(simple("Invalid Login"));
		
		
		
		//get PatientByID using SQL component
		from("direct:getPatientById")
		.process(new BasicAuthProcessor())
		.choice()
		.when(header("SuccessfulAuthorization").isEqualTo(true))
		.setHeader("id", simple("${header.id}")).
		to(getPatientById())
		.otherwise()
		.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(401))
		.setBody(simple("Invalid Login"));
		
		
		//add patient 
		from("direct:addPatient")
		.process(new BasicAuthProcessor())
		.choice()
		.when(header("SuccessfulAuthorization").isEqualTo(true))
		.to(getAddPatient())
		.otherwise()
		.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(401))
		.setBody(simple("Invalid Login"));
		
		//delete patient
		from("direct:deletePatient")
		.process(new BasicAuthProcessor())
		.choice()
		.when(header("SuccessfulAuthorization").isEqualTo(true))
		.setHeader("id", simple("${header.id}"))
		.to(getDeletePatient())
		.otherwise()
		.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(401))
		.setBody(simple("Invalid Login"));
		
		
		//put patient
		from("direct:putPatient")
		.process(new BasicAuthProcessor())
		.choice()
		.when(header("SuccessfulAuthorization").isEqualTo(true))
		 .log("Update patient data : ${body}")
		   .to(getUpdatePatient())
		   .otherwise()
			.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(401))
			.setBody(simple("Invalid Login"));
		
		// patch patient
		from("direct:updatePatientAddress")
		.process(new BasicAuthProcessor())
		.choice()
		.when(header("SuccessfulAuthorization").isEqualTo(true))
		   .log("Update patient data : ${body}")
		   .to(getUpdatePatientAddress())
		   .otherwise()
			.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(401))
			.setBody(simple("Invalid Login"));

	}

}
