package main.java.hello;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.metrics.AwsSdkMetrics;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JsonDataSource;

@RestController
@EnableScheduling
public class GreetingController {

	private final Counter generatedPdfs;
	private final Timer pdf_generate_time;
	Logger logger = LoggerFactory.getLogger(GreetingController.class);

	GreetingController(MeterRegistry meterRegistry) {
		generatedPdfs = meterRegistry.counter("pdfs.generated");
		pdf_generate_time = meterRegistry.timer("pdf.generation.time", "unit", "Microseconds");
	}

	@PostMapping("/")
	@Timed
	public ResponseEntity<byte[]> generatePdf(@RequestBody String users) {
		long startTime = System.currentTimeMillis();
		File file = new File("Jasper.pdf");
		HttpHeaders headers = new HttpHeaders();
		byte[] contents = null;
		try {
			JasperReport jasperReport = JasperCompileManager.compileReport("template.jrxml");
			JSONObject jsonObj = new JSONObject(users);
			ByteArrayInputStream jsonDataStream = new ByteArrayInputStream(jsonObj.get("users").toString().getBytes());
			JsonDataSource dataSource = new JsonDataSource(jsonDataStream);
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("ReportTitle", "List of Contacts");
			parameters.put("Author", "Prepared By Tetiana");
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
			JasperExportManager.exportReportToPdfFile(jasperPrint, "Jasper.pdf");
			String filename = "Jasper.pdf";

			headers.setContentType(MediaType.parseMediaType("application/pdf"));
			headers.setContentDispositionFormData(filename, filename);
			contents = (Files.readAllBytes(file.toPath()));
			pdf_generate_time.record(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS);
			generatedPdfs.increment();
		} catch (Exception e) {
			logger.error("pdf generation status=failed, error=" + e.getMessage());
		}

		ResponseEntity<byte[]> response = new ResponseEntity<>(contents, headers, HttpStatus.OK);
		return response;
	}
}
