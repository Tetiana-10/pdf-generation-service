package main.java.hello;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.aws.context.config.annotation.EnableContextInstanceData;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.metrics.AwsSdkMetrics;

import io.micrometer.core.instrument.MeterRegistry;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JsonDataSource;

@RestController
@EnableScheduling
@Configuration
@EnableContextInstanceData
public class GreetingController {
	Logger logger = LoggerFactory.getLogger(GreetingController.class);
	GreetingController(MeterRegistry meterRegistry) {
	       meterRegistry.gauge("users.current", (int)(Math.random()));
	    }
	 
	@PostMapping("/")
	public ResponseEntity<byte[]> generatePdf(@RequestBody String users) throws JRException, IOException {
		File file = new File("Jasper.pdf");

		logger.info("A TRACE Message");
//      AmazonCloudWatch cw = 
//			    AmazonCloudWatchClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();
		AwsSdkMetrics.enableDefaultMetrics();

//			MetricDatum datum = new MetricDatum()
//			    .withMetricName("VISITED")
//			    .withUnit(StandardUnit.None)
//			    .withValue(50.0);
//
//			PutMetricDataRequest request = new PutMetricDataRequest()
//					.withNamespace("PDFGenerator/TRAFFIC")
//					.withMetricData(datum);
//			cw.putMetricData(request);

		JasperReport jasperReport = JasperCompileManager.compileReport("template.jrxml");
		JSONObject jsonObj = new JSONObject(users);
		ByteArrayInputStream jsonDataStream = new ByteArrayInputStream(jsonObj.get("users").toString().getBytes());
		JsonDataSource ds = new JsonDataSource(jsonDataStream);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("ReportTitle", "List of Contacts");
		parameters.put("Author", "Prepared By Tetiana");
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, ds);
		JasperExportManager.exportReportToPdfFile(jasperPrint, "Jasper.pdf");
		String filename = "Jasper.pdf";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType("application/pdf"));

		headers.setContentDispositionFormData(filename, filename);
		headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
		byte[] contents = (Files.readAllBytes(file.toPath()));
		ResponseEntity<byte[]> response = new ResponseEntity<>(contents, headers, HttpStatus.OK);
		return response;
	}
}
