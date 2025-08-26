package com.example.api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class FileUploadController {
	@Value("${upload.base-dir:uploads}")
	private String uploadBaseDir;

	@PostMapping("/upload")
	public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
		Map<String, Object> body = new HashMap<>();
		if (file == null || file.isEmpty()) {
			body.put("message", "文件为空");
			return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
		}

		try {
			String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
			String extension = "";
			int dotIndex = originalFilename.lastIndexOf('.');
			if (dotIndex != -1 && dotIndex < originalFilename.length() - 1) {
				extension = originalFilename.substring(dotIndex);
			}
			String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;

			Path basePath = Paths.get(uploadBaseDir);
			Files.createDirectories(basePath);
			Path targetPath = basePath.resolve(newFilename).normalize().toAbsolutePath();
			Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

			String fileUrl = "/files/" + newFilename; // 返回相对路径，避免跨域
			body.put("url", fileUrl);
			body.put("filename", newFilename);
			return new ResponseEntity<>(body, HttpStatus.OK);
		} catch (IOException ex) {
			body.put("message", "文件保存失败");
			return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}