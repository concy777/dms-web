package com.rblbank.dms.service;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import com.rblbank.dms.entity.Account_Details;
import com.rblbank.dms.entity.FileUpload;


public interface FileUploadService {
	
	
		List<FileUpload> getAllPosts() throws Exception;

		FileUpload createPost(FileUpload post) throws Exception;

		FileUpload updatePost(Integer id, FileUpload post) throws Exception;

		void deletePost(long id) throws Exception;

		FileUpload getPostById(long id) throws Exception;

		Account_Details findAccountDetails(String accNo) throws Exception;
	
		File convertMultipartToFile(MultipartFile mp) throws Exception;
		
		String saveCMSFile(Path dirLoc,String instance,HttpSession session, Model model) throws Exception;
		
		boolean isFileExist(String filename,Account_Details acc,String instance) throws Exception;
		boolean isFileExist(String filename,String acc,String instance) throws Exception;
		

}
