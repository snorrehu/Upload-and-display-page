package com.mycompany.app;

import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import javax.naming.directory.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import java.nio.file.*;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.stream.Collectors;
import java.util.Optional;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mycompany.app.storage.StorageFileNotFoundException;
import com.mycompany.app.storage.StorageService;

@Controller
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }
	
    @GetMapping("/")
    public String listUploadedFiles() throws IOException {
        return "uploadForm";
    }

    @GetMapping("/{filename:.+}")
    @ResponseBody
    public ResponseEntity<?> serveFile(@PathVariable String filename) {
        Resource file = storageService.loadAsResource(filename);
        Optional<MediaType> mediaType = MediaTypeFactory.getMediaType(file);
		
			InputStream sFile = null;
				try {sFile = new FileInputStream(file.getFile());
				} catch (Exception e){
				}

				BufferedReader br = null;
    			try {br = new BufferedReader(new InputStreamReader(sFile)); }
    				catch(Exception e) {
    				}
    				StringBuilder resultStringBuilder = new StringBuilder();
        			String line;
	        		try { while ((line = br.readLine()) != null) {
	            		resultStringBuilder.append(line).append("\n");
	        			}
	        		}
	        		catch(Exception e) {
	        		}
        		
  		String fileContent = resultStringBuilder.toString();
  		String header = "<h1>"+filename+"</h1>";
  		String extension = filename.substring(filename.lastIndexOf("."));

		if(extension.equals(".java") || extension.equals(".cs") || extension.equals(".py") ){
			return ResponseEntity.ok().body(header + "<code>"+fileContent+"</code>");
		} else if (extension.equals(".txt")){
			return ResponseEntity.ok().body(header + "<p>"+fileContent+"</p>");
		} else if (extension.equals(".pdf")) {
			String filePath = "/pdf/"+filename;
			return ResponseEntity.ok().body(header + 
					"<object data="+filePath+" type=\"application/pdf\" width=\"100%\" height=\"800px\"> " +
					"<iframe src="+filePath+" style=\"border: none;\" width=\"100%\" height=\"800px\"> " +
					"This browser does not support PDFs. Please download the PDF to view it: <a href="+filePath+">Download PDF</a> " +
					"</iframe> " + 
					"</object> "
		);} else if (extension.equals(".png") || extension.equals(".jpeg")) {
			String filePath = "/pic/"+filename;
			BasicFileAttributes attr = null;
			try {attr = getMetaData(filename);}
			catch(Exception e) {
			}

			return ResponseEntity.ok().body(header + 
				"<image src="+filePath+" width=\" \" height=\" \">" +
				"<p>creationTime: " + attr.creationTime() +"</p>" +
				"<p>lastAccessTime: " + attr.lastAccessTime() +"</p>" +
				"<p>lastModifiedTime: " + attr.lastModifiedTime() +"</p>" +
				"<p>isDirectory: " + attr.isDirectory() +"</p>" +
				"<p>isOther: " + attr.isOther() +"</p>" +
				"<p>isRegularFile: " + attr.isRegularFile() +"</p>" +
				"<p>isSymbolicLink: " + attr.isSymbolicLink() +"</p>" +
				"<p>size: " + attr.size() +"</p>"
			);} else {
				BasicFileAttributes attr = null;
				try {attr = getMetaData(filename);}
				catch(Exception e) {
				}
				return ResponseEntity.ok().body(header + 
				"<p>creationTime: " + attr.creationTime() +"</p>" +
				"<p>lastAccessTime: " + attr.lastAccessTime() +"</p>" +
				"<p>lastModifiedTime: " + attr.lastModifiedTime() +"</p>" +
				"<p>isDirectory: " + attr.isDirectory() +"</p>" +
				"<p>isOther: " + attr.isOther() +"</p>" +
				"<p>isRegularFile: " + attr.isRegularFile() +"</p>" +
				"<p>isSymbolicLink: " + attr.isSymbolicLink() +"</p>" +
				"<p>size: " + attr.size() +"</p>"
			);}
    }

    public BasicFileAttributes getMetaData(String filename) throws IOException{
        Path file = Paths.get(System.getProperty("user.dir")+"/upload-dir/"+filename);
        return Files.readAttributes(file, BasicFileAttributes.class);
    }

    @GetMapping("/pdf/{filename:.+}")
    @ResponseBody
    public ResponseEntity<?> servePdfFile(@PathVariable String filename) {

    	Resource file = storageService.loadAsResource(filename);

        Optional<MediaType> mediaType = MediaTypeFactory.getMediaType(file);
			
		return ResponseEntity.ok().contentType(mediaType.get()).body(file);

    }

    @GetMapping("/pic/{filename:.+}")
    @ResponseBody
    public ResponseEntity<?> servePicFile(@PathVariable String filename) {

    	Resource file = storageService.loadAsResource(filename);

        Optional<MediaType> mediaType = MediaTypeFactory.getMediaType(file);
			
		return ResponseEntity.ok().contentType(mediaType.get()).body(file);

    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {

        storageService.store(file);
        return "redirect:/" + file.getOriginalFilename();
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}