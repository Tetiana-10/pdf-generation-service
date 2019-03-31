package main.java.hello;

import org.springframework.web.bind.annotation.RestController;

import com.ibm.icu.util.BytesTrie.Iterator;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Response;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RestController
public class GreetingController {
	Logger logger = LoggerFactory.getLogger(GreetingController.class);

	@PostMapping("/")
	public ResponseEntity<byte[]> generatePdf(@RequestBody String users) throws JRException, IOException {
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
		File file = new File("Jasper.pdf");
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.parseMediaType("application/pdf"));
	    
		headers.setContentDispositionFormData(filename, filename);
	    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
	    byte[] contents = (Files.readAllBytes(file.toPath()));
	    ResponseEntity<byte[]> response = new ResponseEntity<>(contents, headers, HttpStatus.OK);
		return response;
	}

	@PostMapping("/tetst")
	public ResponseEntity<String> newPeport(@RequestBody String request) throws JRException {
		JasperReport jasperReport = JasperCompileManager.compileReport("template.jrxml");
		Map<String, Object> parameters = new HashMap<String, Object>();
		JSONObject body = new JSONObject(request);
		for (String key : body.keySet()) {
			logger.debug(body.getString(key));
		}
		java.util.Iterator<?> keys = body.keys();

		while (keys.hasNext()) {
			logger.info("has keys");
			String key = (String) keys.next();
			String value = body.getString(key);
			parameters.put(key, value);
		}
		JRDataSource dataSource = new JREmptyDataSource();

		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
		JasperExportManager.exportReportToPdfFile(jasperPrint, "Jasper.pdf");
		return ResponseEntity.ok().body("ok");
	}

	public String sendRequest(String targetURL) {
		HttpURLConnection connection = null;
		StringBuilder response = new StringBuilder();
		try {
			URL url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response.toString();
	}
}