package com.rblbank.dms.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    String store(MultipartFile file);

    Stream<Path> loadAll();
    
    
    
    
    Path load(String filename);
    

    Resource loadAsResource(String filename);

    void deleteAll();
    
    File[] getALLFiles();

	Stream<Path> loadAllFiles();

	List<String> printFolderNames(String directory);

	String getAccID(String wholeString);

	List<String> printFileNames(String directory);

	String store(File file);
    
    

}