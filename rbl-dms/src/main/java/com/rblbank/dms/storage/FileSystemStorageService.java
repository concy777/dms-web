package com.rblbank.dms.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.rblbank.dms.exception.FileNotFoundException;
import com.rblbank.dms.exception.StorageException;
import com.rblbank.dms.service.StorageService;
import com.rblbank.dms.utils.ImageToBaseUtils;



@Service
public class FileSystemStorageService implements StorageService {
	
	private static final Logger logger = LoggerFactory.getLogger(FileSystemStorageService.class);

	private final Path rootLocation;
	private final Path dirLocation;
	private String rootpath;

	@Autowired
	public FileSystemStorageService(StorageProperties properties) {
		//if (properties.getInstance().equalsIgnoreCase("uat")) {

			this.rootLocation = Paths.get("D:/Project/rbl/DMS/UploadedFiles");
			this.rootpath = "D:/Project/rbl/DMS/UploadedFiles";
			this.dirLocation=Paths.get("D:/Project/rbl/DMS/Sample");

	//	} else {

	/*
	 * this.rootLocation = Paths.get("D:/Project/rbl/DMS/UploadedFiles");
	 * this.rootpath = "D:/Project/rbl/DMS/UploadedFiles";
	 * this.dirLocation=Paths.get("D:/Project/rbl/DMS/Sample");
	 */

		//}

	}

	@Override
	@PostConstruct
	public void init() {
		try {
			Files.createDirectories(rootLocation);
		} catch (IOException e) {
			throw new StorageException("Could not initialize storage location", e);
		}
	}

	@Override
	public String store(File file) {
		String filename = file.getName();
		String base64Image=ImageToBaseUtils.encoder(filename);
		return base64Image;
	}
	
	@Override
	public String store(MultipartFile file) {
		String filename = StringUtils.cleanPath(file.getOriginalFilename());
		try {
			if (file.isEmpty()) {
				logger.info("File is empty...");
				throw new StorageException("Failed to store empty file " + filename);
				
			}
			if (filename.contains("..")) {
				// This is a security check
				throw new StorageException(
						"Cannot store file with relative path outside current directory " + filename);
			}
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, this.rootLocation.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			logger.error("Failed to store file---",e);
			throw new StorageException("Failed to store file " + filename, e);
		}

		return filename;
	}

	@Override
	public Stream<Path> loadAll() {

		try {
			return Files.walk(this.dirLocation, 1).filter(path -> !path.equals(this.dirLocation))
					.map(this.dirLocation::relativize);
		} catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}

	}
	
	@Override
	public List<String> printFileNames(String directory) {
		 List<String> fileNamesList =new ArrayList<String>();
        // Reading the folder and getting Stream.
        try (Stream<Path> walk = Files.walk(Paths.get(directory))) {

            // Filtering the paths by a regualr file and adding into a list.
            fileNamesList = walk.filter(Files::isRegularFile).map(x -> x.toString())
                    .collect(Collectors.toList());

            // printing the file nams
            fileNamesList.forEach(System.out::println);
            
            

        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileNamesList;
    }
	@Override
	public String getAccID(String wholeString){
		   final int indexOf = wholeString.indexOf("_"); 
		   if(indexOf != -1){
		       return wholeString.substring(0, indexOf);
		   }
		   return wholeString;
		}
	@Override
	public List<String> printFolderNames(String directory) {
		List<String> folderNamesList =new ArrayList<String>();
        // Reading the folder and getting Stream.
        try (Stream<Path> walk = Files.walk(Paths.get(directory))) {

            // Filtering the paths by a folder and adding into a list.
            folderNamesList = walk.filter(Files::isDirectory).map(x -> x.toString())
                    .collect(Collectors.toList());

            // printing the folder names
            folderNamesList.forEach(System.out::println);

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return folderNamesList;
    }
	
	@Override
	public Stream<Path> loadAllFiles() {

		try {
				return Files.walk(this.dirLocation, Integer.MAX_VALUE);
		    
		}
		catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}

	}

	@Override
	public Path load(String filename) {
		return rootLocation.resolve(filename);
	}

	@Override
	public Resource loadAsResource(String filename) {
		try {

			Path file = load(filename);

			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new FileNotFoundException("Could not read file: " + filename);
			}
		} catch (MalformedURLException e) {
			throw new FileNotFoundException("Could not read file: " + filename, e);
		}
	}

	@Override
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(rootLocation.toFile());
	}

	public File[] getALLFiles() {
		File folder = new File(rootpath);

		File[] files = folder.listFiles();

		return files;

	}

}
