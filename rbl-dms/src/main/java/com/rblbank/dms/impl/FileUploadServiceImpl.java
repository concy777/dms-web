package com.rblbank.dms.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rblbank.dms.controller.DmsController;
import com.rblbank.dms.dao.FileUploadRepository;
import com.rblbank.dms.dto.FileUploadDTO;
import com.rblbank.dms.entity.Account_Details;
import com.rblbank.dms.entity.FileUpload;
import com.rblbank.dms.exception.ResourceNotFoundException;
import com.rblbank.dms.exception.StorageException;
import com.rblbank.dms.module.AddDocTags;
import com.rblbank.dms.module.AddDocumentDetails;
import com.rblbank.dms.module.AddReq;
import com.rblbank.dms.module.AddReqBody;
import com.rblbank.dms.module.AddRes;
import com.rblbank.dms.module.AdditionalInfo;
import com.rblbank.dms.module.DeviceInfo;
import com.rblbank.dms.module.GenerateTokenResponseBody;
import com.rblbank.dms.module.GetToken;
import com.rblbank.dms.module.GetTokenReq;
import com.rblbank.dms.module.GetTokenRequestBody;
import com.rblbank.dms.module.GetTokenRequestBodyRoot;
import com.rblbank.dms.module.GetTokenRootRes;
import com.rblbank.dms.module.MessageKey;
import com.rblbank.dms.module.PasswordToken;
import com.rblbank.dms.module.RequestHeader;
import com.rblbank.dms.module.RequestMessageInfo;
import com.rblbank.dms.module.SearchResStatus;
import com.rblbank.dms.module.Security;
import com.rblbank.dms.module.Token;
import com.rblbank.dms.service.FileUploadService;


@Service
public class FileUploadServiceImpl implements FileUploadService {
	
	@Autowired
	private FileUploadRepository fileRespository;
	
	@Autowired
    private JdbcTemplate jdbcTemplate;

	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private Environment env;
	
	public FileUploadServiceImpl() {
		// TODO Auto-generated constructor stub
	}

	
	private RowMapper<Account_Details> getMap(){
		  // Lambda block
		  RowMapper<Account_Details> accMap = (rs, rowNum) -> {
			  Account_Details acc = new Account_Details();
		      acc.setId(rs.getInt("acc_id"));
		      acc.setAccount_number(rs.getString("account_number"));
		      acc.setCase_number(rs.getString("case_number"));
		      acc.setCase_type(rs.getString("case_type"));
		      acc.setChannel_id(rs.getString("channel_id"));
		      acc.setCif_id(rs.getString("cif_id"));
		      acc.setDocDate(rs.getString("doc_date"));
		      acc.setDocType(rs.getString("doc_type"));
		      acc.setDocument(rs.getString("document"));
		      acc.setFileName(rs.getString("file_name"));
		      acc.setRep_name(rs.getString("rep_name"));
		      acc.setAccount_type(rs.getString("account_type"));
		      acc.setUcic(rs.getString("ucic"));
		      acc.setSource(rs.getString("source"));
		      return acc;
		  };
		  return accMap;
	}
	
	@Override
    public Account_Details findAccountDetails(String accNo) throws Exception {
		
		System.out.println("Passing accNo--"+accNo);
    	
    	String SELECT_BY_ID_QUERY = "SELECT * from Account where account_number = ?";
    	 
    	return this.jdbcTemplate.queryForObject(SELECT_BY_ID_QUERY, getMap(), accNo);
		
    }
	
	

	public List<Account_Details> getAccountDetails() throws Exception{
		List<Account_Details> accountDetails=new ArrayList<Account_Details>();
	 String SELECT_BY_ID_QUERY = "SELECT * from account";
    	
		accountDetails = jdbcTemplate.query(SELECT_BY_ID_QUERY,new BeanPropertyRowMapper<Account_Details>(Account_Details.class));
		
		return accountDetails;
		
	}
	@Override
	public List<FileUpload> getAllPosts() {
		return fileRespository.findAll();
	}

	@Override
	public FileUpload createPost(FileUpload post) throws Exception {
		// TODO Auto-generated method stub
		
		
		return fileRespository.save(post);
	}

	@Override
	public FileUpload updatePost(Integer id, FileUpload post) {
		
		
		
		Optional<FileUpload> result = fileRespository.findById( id);
		if(result.isPresent()) {
			return fileRespository.save(result.get());
		}else {
			throw new ResourceNotFoundException("id not found."+id);
		}
	
	
	
	}

	@Override
	public void deletePost(long id)  {
		
		
		
		Optional<FileUpload> result = fileRespository.findById((int) id);
		if(result.isPresent()) {
			fileRespository.delete(result.get());
		}else {
			throw new ResourceNotFoundException("id not found."+id);
		}
		// TODO Auto-generated method stub
		
	}

	@Override
	public FileUpload getPostById(long id) {
		// TODO Auto-generated method stub
		Optional<FileUpload> result = fileRespository.findById((int) id);
		if(result.isPresent()) {
			return result.get();
		}else {
			throw new ResourceNotFoundException("id not found."+id);
		}
		
	}

	@Override
	public File convertMultipartToFile(MultipartFile mp) throws Exception{
		System.out.println("Inside convertMultipartToFile");
		// TODO Auto-generated method stub
		final File file = new File(mp.getOriginalFilename());
        try (final FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(mp.getBytes());
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        return file;
		
	}

	
	public Account_Details getAccount_Details(String type, String data,String instance) throws Exception {


		String SELECT_BY_ID_QUERY = "";

		try {
			if (type.equalsIgnoreCase("account")) {

				SELECT_BY_ID_QUERY = "SELECT * from account where account_number = '" + data + "' LIMIT 1";

			}

			else if (type.equalsIgnoreCase("cif")) {

				SELECT_BY_ID_QUERY = "SELECT top 1 * from account where cif_id = '" + data + "' LIMIT 1";

			} else if (type.equalsIgnoreCase("case")) {

				SELECT_BY_ID_QUERY = "SELECT top 1 * from account where case_number = '" + data + "' LIMIT 1";

			} else {
				System.out.println("Something went wrong");
			}

			Account_Details instruc = jdbcTemplate.queryForObject(SELECT_BY_ID_QUERY,
					BeanPropertyRowMapper.newInstance(Account_Details.class));

			
			return instruc;
		} catch (EmptyResultDataAccessException e) {
			// TODO Auto-generated catch block
			return null;
		}

	}
	
	public static String getRandomNumberString() {
	    // It will generate 6 digit random Number.
	    // from 0 to 999999
	    Random rnd = new Random();
	    int number = rnd.nextInt(999999);

	    // this will convert any number sequence into 6 character.
	    return String.format("%06d", number);
	}
	
	public String store(MultipartFile file,Account_Details accDetails) {
		String filename = StringUtils.cleanPath(file.getOriginalFilename());
		
		String rootLoca="D:/Saravana/DMS/UploadedFiles";
		
		String instance=env.getProperty("dms.instance");
		
		Path rootLocation=Paths.get(rootLoca);
		try {
			if (file.isEmpty()) {
				throw new StorageException("Failed to store empty file " + filename);
				
			}
			if (filename.contains("..")) {
				// This is a security check
				throw new StorageException(
						"Cannot store file with relative path outside current directory " + filename);
			}
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, rootLocation.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
				insert(accDetails,file,instance);
			}
		} catch (IOException e) {
			throw new StorageException("Failed to store file " + filename, e);
		}

		return filename;
	}
	
	
	
public boolean isFileExist(String filename, Account_Details acc, String instance) {
		
		
        boolean exists = false;
        String sql = "";
        
        System.out.println("Account isexists --"+acc.getAccount_number());
        System.out.println("cif isexists--"+acc.getCif_id());
		
        if(acc.getAccount_number()!=null && acc.getAccount_number().length()>0) {
        	 sql = "Select count(1) from file_upload where status='Y' and file_name='"+StringUtils.cleanPath(filename)+"' and acc_no='"+acc.getAccount_number()+"'and dms_env='"+instance+"'";
     	    
        }
        else if(acc.getCif_id()!=null && acc.getCif_id().length()>0) {
        	 sql = "Select count(1) from file_upload where status='Y' and file_name='"+StringUtils.cleanPath(filename)+"' and cif_id='"+acc.getCif_id()+"'and dms_env='"+instance+"'";
      	    
        }
        else {
        	sql = "Select count(1) from file_upload where status='Y' and file_name='"+StringUtils.cleanPath(filename)+"'and dms_env='"+instance+"'";
        }
		         
	      int row=  jdbcTemplate.queryForObject(sql, Integer.class);
	      exists = row > 0;
	      
	      System.out.println("Is file exist--"+filename+" "+exists);
	      
	    return exists;
	}

public boolean isFileExist(String filename, String accNumber, String instance) {
	
	
    boolean exists = false;
    String sql = "";
    
    System.out.println("Account isexists --"+accNumber);
	
    if(accNumber!=null && accNumber.length()>0) {
    	 sql = "Select count(1) from file_upload where file_name='"+StringUtils.cleanPath(filename)+"' and acc_no='"+accNumber+"'and dms_env='"+instance+"'";
 	    
    }
    else {
    	sql = "Select count(1) from file_upload where file_name='"+StringUtils.cleanPath(filename)+"'and dms_env='"+instance+"'";
    }
	  
      int row=  jdbcTemplate.queryForObject(sql, Integer.class);
      exists = row > 0;
      
      System.out.println("Is file exist--"+filename+" "+exists);
      
    return exists;
}

	
	public void insert(Account_Details acc,MultipartFile file,String instance) {
		
		String sql = "INSERT INTO file_upload " +
	            "(acc_no, cif_id, date_of_opening,file_name,file_path,file_size,file_type,iskyc,ref_no,source,ucic,iserror,error,status,trans_id,dms_env,created_by,created_date) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,25766,getdate())";
	                 
	      int row=  jdbcTemplate.update(sql, new Object[] { acc.getAccount_number(),acc.getCif_id(),acc.getDocDate(),StringUtils.cleanPath(file.getOriginalFilename()),acc.getFile_path() ,file.getSize() / (1024 * 1024),file.getContentType(),
	        		
	            acc.getDocType(),acc.getCase_number(),acc.getSource(),acc.getUcic(),acc.getIserror(),acc.getError(),acc.getStatus(),acc.getTransId(),instance
	        });
	      
	      System.out.println("Rows added--"+row);
	}

	
public void insertFile(Account_Details acc,File file,String instance) {
		
		
		String sql = "INSERT INTO file_upload " +
	            "(acc_no, cif_id, date_of_opening,file_name,iserror,error,status,trans_id,dms_env,created_by,created_date) VALUES (?,?,?,?,?,?,?,?,?,25766,getdate())";
	                 
	                
	      int row=  jdbcTemplate.update(sql, new Object[] { acc.getAccount_number(),acc.getCif_id(),acc.getDocDate(),StringUtils.cleanPath(file.getName()),acc.getIserror(),acc.getError(),acc.getStatus(),acc.getTransId(),instance
	        });
	      
	      System.out.println("Rows added--"+file.getName()+" "+row);
	}


public void insertFileRecord(Account_Details acc, String instance) {
	
	
	String sql = "INSERT INTO file_upload " +
            "(acc_no, cif_id, date_of_opening,file_name,iserror,error,status,trans_id,dms_env,created_by,created_date) VALUES (?,?,?,?,?,?,?,?,?,25766,getdate())";
                 
      int row=  jdbcTemplate.update(sql, new Object[] { acc.getAccount_number(),acc.getCif_id(),acc.getDocDate(),acc.getIserror(),acc.getError(),acc.getStatus(),acc.getTransId(),instance
        });
      
     
}
	
	public  String storeMultipartFile(File file,Account_Details accDetails,HttpSession session,Model model) {
		
		
		if(accDetails!=null) {
			accDetails.setFileName(file.getName());
			accDetails.setFile_path(file.getAbsolutePath());
		}
		
		
		String fileName="";
		try {
			FileInputStream input = new FileInputStream(file);
			MultipartFile multipartFile = new MockMultipartFile("file",file.getName(), "text/plain", IOUtils.toByteArray(input));
			fileName= save(multipartFile,accDetails,session,model);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileName;
	}

	
public  String storeMultipartFile(File file,Account_Details accDetails,String instance,HttpSession session,Model model) {
		
		
		if(accDetails!=null) {
			accDetails.setFileName(file.getName());
			accDetails.setFile_path(file.getAbsolutePath());
		}
		
		
		String fileName="";
		try {
			FileInputStream input = new FileInputStream(file);
			MultipartFile multipartFile = new MockMultipartFile("file",file.getName(), "text/plain", IOUtils.toByteArray(input));
			if(instance.equalsIgnoreCase("test") || instance.equalsIgnoreCase("local")) {
				fileName= save(multipartFile,accDetails,session,model);
			}
			else{
				fileName= save(multipartFile,accDetails,session,model);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileName;
	}
	
	
	public static Path findUsingNIOApi(String sdir) throws IOException {
    Path dir = Paths.get(sdir);
    if (Files.isDirectory(dir)) {
    	
    	Optional<Path> walksing=Files.walk(Paths.get(sdir))
    			.sorted((f1, f2) -> -(int)(f1.toFile().lastModified() - f2.toFile().lastModified()))
    			.skip(1)
    			.findFirst();
    		    
    		    if (walksing.isPresent()){
    		    	System.out.println("--walksing--"+walksing.get());

    		        return walksing.get();
    		    }
    }
    
    return null;
	}

	@Override
	public String saveCMSFile(Path dirPat,String instance,HttpSession session,Model model) throws Exception {
	
		String transId=getRandomNumberString();
		
		System.out.println("Trans-id:::"+transId);
		//String dirPat = "D:/Saravana/DMS/Sample";

		try (Stream<Path> walkpayj = Files.walk(dirPat)) {

			List<File> fileResult = Files.list(dirPat).map(Path::toFile).collect(Collectors.toList());

			System.out.println("--fileResult Started---");

			fileResult.forEach(System.out::println);

			System.out.println("--fileResult Ended---");
			
			//Path filePath=findUsingNIOApi(dirPat);
			//System.out.println("--filePath--"+filePath);

			for (File file : fileResult) {

				if (file.isDirectory()) {

					String dNamePath = file.getAbsolutePath();

					System.out.println("Directory: " + file.getAbsolutePath());

					String camsDir = dNamePath.substring(dNamePath.lastIndexOf("\\") + 1);

					System.out.println("Main Directory: " + camsDir);

					Path camsPath = Paths.get(camsDir);

					System.out.println("MainPath: " + camsPath);

					if (camsDir.equalsIgnoreCase("Account opened in CAMS")) {

						try (Stream<Path> walk = Files.walk(Paths.get(dNamePath))) {

							List<File> result = Files.list(Paths.get(dNamePath)).map(Path::toFile)
									.collect(Collectors.toList());
							

							System.out.println("--Cams Folder Started---");

							result.forEach(System.out::println);

							System.out.println("--Cams Folder Ended---");
							
							

							for (File camFile : result) {

								if (camFile.isDirectory()) {

									System.out.println("Cams Directory: " + camFile.getAbsolutePath());

									String dPath = camFile.getAbsolutePath();

									String accNo = dPath.substring(dPath.lastIndexOf("\\") + 1);

									System.out.println("accNo: " + accNo);

									Account_Details accDe = getAccount_Details("account", accNo,instance);

									
									if (accDe == null) {
										accDe = new Account_Details();
										accDe.setAccount_number(accNo);
										accDe.setError("No Record Found");
										accDe.setIserror("Y");
										accDe.setStatus("Failed");
										insertFileRecord(accDe, instance);

									} else {
										accDe.setKyc(false);
										accDe.setIserror("N");
										accDe.setStatus("Success");
										accDe.setTransId(transId);
										try {
											List<File> files = Files.list(Paths.get(dPath)).map(Path::toFile)
													.collect(Collectors.toList());

											// files.forEach(System.out::println);

											for (File fl : files) {

												System.out.println("File getting Stored--" + fl.getName());
												
												if(isFileExist(fl.getName(),accDe, instance)) {
													accDe.setError("File Already Exists");
													accDe.setIserror("1");
													accDe.setStatus("N");
													insertFile(accDe, fl, instance);
													//continue;
													}
												else {
												String succ=storeMultipartFile(fl, accDe,instance,session,model);
												if(!succ.equalsIgnoreCase("success")) {
													System.out.println("Error Occured--"+succ);
													break;
												}
													
												}

												
												// save(fl,accNo);
											}
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
									

								} else {
									System.out.println("File: " + file.getAbsolutePath());
									System.out.println("Camsfile directory issue...");
								}
							}

						} catch (IOException e) {
							e.printStackTrace();
						}

					}

					if (camsDir.equalsIgnoreCase("Instruction")) {

						if (file.isDirectory()) {

							try (Stream<Path> walk = Files.walk(Paths.get(dNamePath))) {

								List<File> result = Files.list(Paths.get(dNamePath)).map(Path::toFile)
										.collect(Collectors.toList());
								System.out.println("--Instruction Folder Started---");

								result.forEach(System.out::println);

								System.out.println("--Instruction Folder Ended---");

								for (File instructionFile : result) {

									System.out.println("File getting Stored--" + instructionFile.getName());

									Account_Details ins = getAccount_Details("case", getFileNameWithoutExtension(instructionFile),instance);
									
									if (ins == null) {
										ins = new Account_Details();
										ins.setCase_number(getFileNameWithoutExtension(instructionFile));
										ins.setError("No Record Found");
										ins.setIserror("Y");
										ins.setStatus("Failed");
										insertFileRecord(ins, instance);

									} else {
										ins.setKyc(false);
										ins.setIserror("N");
										ins.setStatus("Success");
										ins.setTransId(transId);
										
										try {
											if(isFileExist(instructionFile.getName(),ins, instance)) {
												ins.setError("File Already Exists");
												ins.setIserror("1");
												ins.setStatus("N");
												insertFile(ins, instructionFile, instance);
												
												//continue;
												}
											else {
												storeMultipartFile(instructionFile, ins,instance,session,model);
												
											}
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
									
									
									
									// save(fl,accNo);
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						} else {
							System.out.println("Instruction Invalid File Location: " + file.getAbsolutePath());
						}

					}
 					if (camsDir.equalsIgnoreCase("CKYC_Images")) {
						


						try (Stream<Path> instwalk = Files.walk(Paths.get(dNamePath))) {

							List<File> isresult = Files.list(Paths.get(dNamePath)).map(Path::toFile)
									.collect(Collectors.toList());

							System.out.println("--CKYC_Images Folder Started---");

							isresult.forEach(System.out::println);

							System.out.println("--CKYC_Images Folder Ended---");

							for (File iuinFile : isresult) {

								if (iuinFile.isDirectory()) {

									System.out.println("CKYC_Images: " + iuinFile.getAbsolutePath());

									String dPath = iuinFile.getAbsolutePath();
									
									try (Stream<Path> camwk = Files.walk(Paths.get(dPath))) {

										List<File> cmsreu = Files.list(Paths.get(dPath)).map(Path::toFile)
												.collect(Collectors.toList());

										System.out.println("--CKYC_Images Folder Started---");

										cmsreu.forEach(System.out::println);

							System.out.println("--CKYC_Images Folder Ended---");

							for (File caFIle : cmsreu) {

								if (caFIle.isDirectory()) {

									System.out.println("Cams Directory: " + caFIle.getAbsolutePath());

									String dcmsPath = caFIle.getAbsolutePath();
									
									String cifNo = dcmsPath.substring(dcmsPath.lastIndexOf("\\") + 1);

									System.out.println("cifNo: " + cifNo);

									Account_Details cifNoDe = getAccount_Details("cif", cifNo,instance);
									
									if (cifNoDe == null) {
										cifNoDe = new Account_Details();
										cifNoDe.setCif_id(cifNo);
										cifNoDe.setError("No Record Found");
										cifNoDe.setIserror("Y");
										cifNoDe.setStatus("Failed");
										insertFileRecord(cifNoDe, instance);

									} else {
										cifNoDe.setKyc(true);
										cifNoDe.setDocType("POI");
										cifNoDe.setIserror("N");
										cifNoDe.setStatus("Success");
										cifNoDe.setTransId(transId);
										
										
										try {
											List<File> instfiles = Files.list(Paths.get(dcmsPath)).map(Path::toFile)
													.collect(Collectors.toList());

											// files.forEach(System.out::println);

											for (File insf : instfiles) {

												System.out.println("File getting Stored--" + insf.getName());

												if(isFileExist(insf.getName(),cifNoDe, instance)) {
													cifNoDe.setError("File Already Exists");
													cifNoDe.setIserror("1");
													cifNoDe.setStatus("N");
													insertFile(cifNoDe, insf, instance);
													//continue;
													}
												else {
													storeMultipartFile(insf, cifNoDe,instance,session,model);
													
												}
												// save(fl,accNo);
											}
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
									

								} else {
									System.out.println("File: " + file.getAbsolutePath());
								}
							}

						} catch (IOException e) {
							e.printStackTrace();
						}

									

								} else {
									System.out.println("File: " + file.getAbsolutePath());
								}
							}

						} catch (IOException e) {
							e.printStackTrace();
						}

					
					}
 					if (camsDir.equalsIgnoreCase("CKYC_Images_New")) {

						try (Stream<Path> instwalk = Files.walk(Paths.get(dNamePath))) {

							List<File> isresult = Files.list(Paths.get(dNamePath)).map(Path::toFile)
									.collect(Collectors.toList());

							System.out.println("--CKYC_Images_New---");

							isresult.forEach(System.out::println);

							System.out.println("--CKYC_Images_New ---");

							for (File proFile : isresult) {

								if (proFile.isDirectory()) {

									System.out.println("Cams Directory: " + proFile.getAbsolutePath());

									String dPath = proFile.getAbsolutePath();

									String cifNo = dPath.substring(dPath.lastIndexOf("\\") + 1);

									System.out.println("cifNo: " + cifNo);
									// String SELECT_BY_ID_QUERY = "SELECT top 1 * from Account where cif_id =
									// '"+cifNo+"'";

									// Account_Details accDetails= jdbcTemplate.queryForObject(SELECT_BY_ID_QUERY,
									// getMap(), accNo);

									Account_Details cifNoDe = getAccount_Details("cif", cifNo,instance);

									if (cifNoDe == null) {
										cifNoDe = new Account_Details();
										cifNoDe.setCif_id(cifNo);
										cifNoDe.setError("No Record Found");
										cifNoDe.setIserror("Y");
										cifNoDe.setStatus("Failed");
										insertFileRecord(cifNoDe, instance);

									} else {
										cifNoDe.setKyc(true);
										cifNoDe.setDocType("POI");
										cifNoDe.setIserror("N");
										cifNoDe.setStatus("Success");
										cifNoDe.setTransId(transId);
										try {
											List<File> instfiles = Files.list(Paths.get(dPath)).map(Path::toFile)
													.collect(Collectors.toList());

											// files.forEach(System.out::println);

											for (File insf : instfiles) {

												System.out.println("File getting Stored--" + insf.getName());

												if(isFileExist(insf.getName(),cifNoDe, instance)) {
													cifNoDe.setError("File Already Exists");
													cifNoDe.setIserror("1");
													cifNoDe.setStatus("N");
													insertFile(cifNoDe, insf, instance);
													//continue;
													}
												else {
													storeMultipartFile(insf, cifNoDe,instance,session,model);
													
												}
											
												//storeMultipartFile(insf, cifNoDe,instance);
												// save(fl,accNo);
											}
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
									

								} else {
									System.out.println("File: " + file.getAbsolutePath());
								}
							}

						} catch (IOException e) {
							e.printStackTrace();
						}

					}
					
					if (camsDir.equalsIgnoreCase("Instruction process in CAMS")) {

						try (Stream<Path> instwalk = Files.walk(Paths.get(dNamePath))) {

							List<File> isresult = Files.list(Paths.get(dNamePath)).map(Path::toFile)
									.collect(Collectors.toList());

							System.out.println("--Instruction Process Folder Started---");

							isresult.forEach(System.out::println);

							System.out.println("--Instruction Process Folder Ended---");

							for (File proFile : isresult) {

								if (proFile.isDirectory()) {

									System.out.println("Cams Directory: " + proFile.getAbsolutePath());

									String dPath = proFile.getAbsolutePath();

									String cifNo = dPath.substring(dPath.lastIndexOf("\\") + 1);

									System.out.println("cifNo: " + cifNo);
									// String SELECT_BY_ID_QUERY = "SELECT top 1 * from Account where cif_id =
									// '"+cifNo+"'";

									// Account_Details accDetails= jdbcTemplate.queryForObject(SELECT_BY_ID_QUERY,
									// getMap(), accNo);

									Account_Details cifNoDe = getAccount_Details("cif", cifNo,instance);

									if (cifNoDe == null) {
										cifNoDe = new Account_Details();
										cifNoDe.setCif_id(cifNo);
										cifNoDe.setError("No Record Found");
										cifNoDe.setIserror("Y");
										cifNoDe.setStatus("Failed");
										insertFileRecord(cifNoDe, instance);

									} else {
										cifNoDe.setKyc(true);
										cifNoDe.setIserror("N");
										cifNoDe.setStatus("Success");
										cifNoDe.setTransId(transId);
										try {
											List<File> instfiles = Files.list(Paths.get(dPath)).map(Path::toFile)
													.collect(Collectors.toList());

											// files.forEach(System.out::println);

											for (File insf : instfiles) {

												System.out.println("File getting Stored--" + insf.getName());

												if(isFileExist(insf.getName(),cifNoDe, instance)) {
													cifNoDe.setError("File Already Exists");
													cifNoDe.setIserror("1");
													cifNoDe.setStatus("N");
													insertFile(cifNoDe, insf, instance);
													//continue;
													}
												else {
													storeMultipartFile(insf, cifNoDe,instance,session,model);
													
												}
											
												//storeMultipartFile(insf, cifNoDe,instance);
												// save(fl,accNo);
											}
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
									

								} else {
									System.out.println("File: " + file.getAbsolutePath());
								}
							}

						} catch (IOException e) {
							e.printStackTrace();
						}

					}

					if (camsDir.equalsIgnoreCase("Manuale account opening")) {

						if (file.isDirectory()) {

							try (Stream<Path> manwalk = Files.walk(Paths.get(dNamePath))) {

								List<File> manresult = Files.list(Paths.get(dNamePath)).map(Path::toFile)
										.collect(Collectors.toList());
								System.out.println("--Manual Account Started---");

								manresult.forEach(System.out::println);

								System.out.println("--Manual Account  Ended---");

								for (File mafl : manresult) {

									System.out.println("File getting Stored--" + mafl.getName());

									Account_Details instruc = getAccount_Details("account",
											getFileNameWithoutExtension(mafl),instance);

									if (instruc == null) {
										instruc = new Account_Details();
										instruc.setAccount_number(getFileNameWithoutExtension(mafl));
										instruc.setError("No Record Found");
										instruc.setIserror("Y");
										instruc.setStatus("Failed");
										insertFileRecord(instruc, instance);
									} else {
										instruc.setIserror("N");
										instruc.setStatus("Success");
										instruc.setTransId(transId);
										
										if(isFileExist(instruc.getName(),instruc, instance)) {
											instruc.setError("File Already Exists");
											instruc.setIserror("1");
											instruc.setStatus("N");
											insertFile(instruc, mafl, instance);
											//continue;
											}
										else {
											storeMultipartFile(mafl, instruc,instance,session,model);
											
										}
									}
									
									// save(fl,accNo);
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						} else {
							System.out.println("Instruction Invalid File Location: " + file.getAbsolutePath());
						}

					}
				} else {
					System.out.println("Invalid File Location in Main CAMS Directory: " + file.getAbsolutePath());
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return "success -"+transId;
	}
	
	public String getFileNameWithoutExtension(File file) {
		String fileName = "";

		try {
			if (file != null && file.exists()) {
				String name = file.getName();
				fileName = name.replaceFirst("[.][^.]+$", "");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fileName = "";
		}

		return fileName;

	}

	public GenerateTokenResponseBody getToken(Model model)
			throws MalformedURLException {
		GetTokenRootRes result = new GetTokenRootRes();
		try {
			String uri = "";
			if (env.getProperty("dms.instance").equalsIgnoreCase("uat")) {
				uri = env.getProperty("dms.uat.getToken");
			} 
			else if (env.getProperty("dms.instance").equalsIgnoreCase("dev")) {
				uri = env.getProperty("dms.dev.getToken");
			}
			else if (env.getProperty("dms.instance").equalsIgnoreCase("prod")) {
				uri = env.getProperty("dms.prod.getToken");
			}
			else {
				uri = env.getProperty("dms.local.getToken");

			}

			GetToken tokenReq=new GetToken();
			
			GetTokenReq dmsReq = new GetTokenReq();

			RequestHeader reqheader = setReqHeader("token");

			GetTokenRequestBody bod = new GetTokenRequestBody();
			
			GetTokenRequestBodyRoot rootreq=new GetTokenRequestBodyRoot();
			

			bod.setClientId(env.getProperty("clientId"));
			bod.setClientSecret(env.getProperty("clientSecret"));
			rootreq.setGetTokenRequestBody(bod);
			dmsReq.setRequestHeader(reqheader);
			dmsReq.setRequestBody(rootreq);
			tokenReq.setGetToken(dmsReq);
			System.out.println("URI::" + uri);
			

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			//headers.add("Authorization", "Bearer"+token);
			//headers.setBearerAuth(token);
			HttpEntity<GetToken> serReq = new HttpEntity<>(tokenReq, headers);

			 printObject(serReq);
			String jsonString = "";

			if (env.getProperty("dms.instance").equalsIgnoreCase("local")) {
				jsonString = restTemplate.getForObject(uri, String.class);
			} else {
				jsonString = restTemplate.postForObject(uri, serReq, String.class);
			}

			System.out.println("Data::" + jsonString);

			
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			try {
				result = mapper.readValue(jsonString, GetTokenRootRes.class);
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (result.getGenerateTokenResponse().getStatus().getStatusCode().equalsIgnoreCase("0")) {

				String accessToken = result.getGenerateTokenResponse().getResponseBody().getGenerateTokenResponseBody().getAccess_token();
				
				String expiry = result.getGenerateTokenResponse().getResponseBody().getGenerateTokenResponseBody().getExpires_in();
				
				String token_type=result.getGenerateTokenResponse().getResponseBody().getGenerateTokenResponseBody().getToken_type();
				

				System.out.println("accessToken" + accessToken);
				System.out.println("expiry" + expiry);
				System.out.println("token_type" + token_type);
				//return result;
				
			}

			
		} catch (RestClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//return result;
		}
		
		return result.getGenerateTokenResponse().getResponseBody().getGenerateTokenResponseBody();

	}
	
	public String save(MultipartFile file,Account_Details accDetails, HttpSession session, Model model) {
		
		System.out.println("Application add-doc Started...");

		String fnameNew;
		try {
			String uri = "";
			if (env.getProperty("dms.instance").equalsIgnoreCase("uat")) {
				uri = env.getProperty("dms.uat.addDoc");
			} 
			else if (env.getProperty("dms.instance").equalsIgnoreCase("dev")) {
				uri = env.getProperty("dms.dev.addDoc");
			}
			else if (env.getProperty("dms.instance").equalsIgnoreCase("prod")) {
				uri = env.getProperty("dms.prod.addDoc");
			}
			else {
				uri = env.getProperty("dms.local.addDoc");

			}
			RequestHeader header=setReqHeader("addDoc");
			AddDocumentDetails addDocumentDetails=new AddDocumentDetails();
			fnameNew = file.getOriginalFilename().substring(0, file.getOriginalFilename().indexOf("."));
			addDocumentDetails.setFileName(fnameNew);

			System.out.println("Filename--"+addDocumentDetails.getFileName());
			accDetails.setFileName(fnameNew.trim());
			String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
			System.out.println(extension);
			String fileType=file.getContentType().substring(file.getContentType().lastIndexOf("/")+1);
			System.out.println("FIletype--"+fileType);
			
			if(extension.equalsIgnoreCase("tiff")) {
				extension="tif";
			}
			addDocumentDetails.setFileType(extension.toLowerCase());
			printObject("Acc Details::"+accDetails);
			AddDocTags docTag = new AddDocTags();

			System.out.println("Account details::"+accDetails.getAccount_number().trim());
			
			
			String leftPadded = org.apache.commons.lang3.StringUtils.leftPad("" + accDetails.getAccount_number().trim(), 16, "0");
			docTag.setAccNo(accDetails.getAccount_number().trim());
			
			
			docTag.setCifId(accDetails.getCif_id());
			
			if(!accDetails.getDocDate().equalsIgnoreCase("") || accDetails.getDocDate()!=null) {
				
				System.out.println("Doc_date:::"+accDetails.getDocDate());
				docTag.setDateOfAccOpening(convertToDateFormat(accDetails.getDocDate()));
			}
			else {
				docTag.setDateOfAccOpening("");
			}
			
			docTag.setDocFolder(accDetails.getDoc_Folder());
			docTag.setDocumentType(accDetails.getDocType());
			docTag.setKycId(accDetails.getDocument());
			docTag.setName(accDetails.getFileName().trim());
			docTag.setUcic(accDetails.getUcic());
			docTag.setValidity(docTag.getDateOfAccOpening()); 
			docTag.setLastKycCheck(convertToDateFormat(accDetails.getDocDate()));
			//docTag.setDocFolder(accDetails.getSource());
			docTag.setKyc(accDetails.isKyc());
			docTag.setIdentificationNumber(docTag.getCifId());
			
			FileUploadDTO dto=new FileUploadDTO();
			dto.setAccNo(accDetails.getAccount_number().trim());
			dto.setCifId(accDetails.getCif_id());
			dto.setDateOfOpening(accDetails.getDocDate());
			dto.setRefNo(accDetails.getReference_Number());
			dto.setSource(accDetails.getSource());
			dto.setUcic(accDetails.getUcic());
			dto.setDocfolder(accDetails.getSource());
			dto.setDoctype(accDetails.getDocType());
			dto.setTransId(accDetails.getTransId());
			
			System.out.println("Is kYc--"+accDetails.isKyc());
			System.out.println("document type--"+docTag.getDocumentType());
			
			if(accDetails.isKyc()) {
				dto.setIsKYC("1");
			}
			else {
				dto.setIsKYC("0");
			}
			
			
			if(!Strings.isNullOrEmpty(docTag.getDocumentType())) {
				if(docTag.getDocumentType().equalsIgnoreCase("POI") || docTag.getDocumentType().equalsIgnoreCase("POA") ) {
					docTag.setKyc(true);
				}
				else {
					docTag.setKyc(false);
				}
			}
			else {
				accDetails.setError("POA is Empty or NULL");
			}
			
			docTag.setReferenceNumber(accDetails.getCase_number());
			docTag.setSourceSystem(accDetails.getSource());
			AddReqBody bod = new AddReqBody();
			
			System.out.println("Acc--" + docTag.getAccNo());
			System.out.println("CIF--" + docTag.getCifId());
			System.out.println("DOB--" + docTag.getDateOfAccOpening());
			System.out.println("KYCID--" + docTag.getKycId());
			System.out.println("KYC--" + docTag.isKyc());
			System.out.println("UCIC--" + docTag.getUcic());
			System.out.println("Ref--" + docTag.getReferenceNumber());
			System.out.println("Source--" + docTag.getSourceSystem());
			System.out.println("DocType--" + docTag.getDocumentType());
			System.out.println("Docfolder--" + docTag.getDocFolder());
			
			
			
			bod.setDocumentDetails(addDocumentDetails);
			bod.setDocumentTags(docTag);
			try {
				//File fi=convertMultipartToFile(file);
				dto.setFileName(file.getName());
				//dto.setFileSize(String.valueOf(fi.length() / (1024 * 1024)) +"mb");
				dto.setFileSize(String.valueOf(file.getSize()));
				dto.setFileType(file.getContentType());
				System.out.println("Byte---");
				String b64=java.util.Base64.getEncoder().encodeToString(file.getBytes());
				//byte[] normalFileContent=Files.readAllBytes(fi.toPath());
				//String b64 = java.util.Base64.getEncoder().encodeToString(normalFileContent);
				bod.setDocument(b64);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				dto.setError(e.getMessage());
				e.printStackTrace();
			}
			AddReq dmsReq = new AddReq();
			dmsReq.setBody(bod);
			dmsReq.setHeader(header);
			
			String token=(String) session.getAttribute("token");
			
			System.out.println("Session Token--"+token);
			
			if(token==null || token.trim().isEmpty())
			{		
			  GenerateTokenResponseBody tokenBody=new GenerateTokenResponseBody(); try {
			  
			  //long todaySec=EncrytedPasswordUtils.getTodayPassedSeconds();
			  //System.out.println("todaySec---"+todaySec); 
				  
				  
			  tokenBody = getToken(model); 
			  
			  token=tokenBody.getAccess_token();
			  
			  session.setAttribute("token", token);
			  
			  
			  } catch (MalformedURLException e1) { // TODO Auto-generated catch block
			  e1.printStackTrace(); }
			 
			}
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			headers.setBearerAuth(token);
			
			HttpEntity<AddReq> request = new HttpEntity<>(dmsReq, headers);

			//printObject(request);
			
			String jsonString = "";
			
					if (env.getProperty("dms.instance").equalsIgnoreCase("local")) {
				 jsonString = restTemplate.getForObject(uri, String.class);
			} else {
				 jsonString = restTemplate.postForObject(uri,request,String.class);

			}
			System.out.println("Data::" + jsonString);

			AddRes res = new AddRes();
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			try {
				res = mapper.readValue(jsonString, AddRes.class);
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//printObject("DTO---"+dto);
			SearchResStatus result = res.getStatus();
			
			System.out.println("Display text----" + result.getDisplayText());

			System.out.println("Statuscode----" + result.getStatusCode());
			
			if(result.getStatusCode().equalsIgnoreCase("0")) {
				accDetails.setIserror("0");
				accDetails.setStatus("Y");
				
				insert(accDetails,file,env.getProperty("dms.instance"));
				return "success";
			}
			else {
				accDetails.setIserror("1");
				accDetails.setError(result.getErrorMessage());
				accDetails.setStatus("N");
				insert(accDetails,file,env.getProperty("dms.instance"));
				return "error";
			}
		} catch ( Exception e) {
			// TODO Auto-generated catch block
			accDetails.setIserror("1");
			accDetails.setError("ADD API Resource Not Found.");
			accDetails.setStatus("N");
			insert(accDetails,file,env.getProperty("dms.instance"));
			e.printStackTrace();
			return e.getMessage();
		}
		//return fnameNew;
		
	}
	
	public RequestHeader setReqHeader(String searchType ) {

		RequestHeader reqheader = new RequestHeader();

		MessageKey msgKey = new MessageKey();
		msgKey.setRequestUUID(UUID.randomUUID().toString());
		msgKey.setChannelId(env.getProperty("dms.channelId"));

		if (searchType.equalsIgnoreCase("search")) {
			msgKey.setServiceRequestId(env.getProperty("dms.search.serviceReqId"));
			msgKey.setServiceRequestVersion(env.getProperty("dms.search.serviceReqVersion"));

		} else if (searchType.equalsIgnoreCase("add")) {
			msgKey.setServiceRequestId(env.getProperty("dms.add.serviceReqId"));
			msgKey.setServiceRequestVersion(env.getProperty("dms.add.serviceReqVersion"));

		} else if (searchType.equalsIgnoreCase("view")) {
			msgKey.setServiceRequestId(env.getProperty("dms.view.serviceReqId"));
			msgKey.setServiceRequestVersion(env.getProperty("dms.view.serviceReqVersion"));

		} else if (searchType.equalsIgnoreCase("update")) {
			msgKey.setServiceRequestId(env.getProperty("dms.search.serviceReqId"));
			msgKey.setServiceRequestVersion(env.getProperty("dms.search.serviceReqVersion"));

		} else if (searchType.equalsIgnoreCase("token")) {
			msgKey.setChannelId("");
			msgKey.setServiceRequestId(env.getProperty("dms.getToken.serviceReqId"));
			msgKey.setServiceRequestVersion(env.getProperty("dms.getToken.serviceReqVersion"));

		}else {
			msgKey.setServiceRequestId(env.getProperty("dms.search.serviceReqId"));
			msgKey.setServiceRequestVersion(env.getProperty("dms.search.serviceReqVersion"));

		}

		RequestMessageInfo requestMessageInfo = new RequestMessageInfo();
		requestMessageInfo.setBankId("");
		requestMessageInfo.setMessageDateTime(String.valueOf(Instant.now().toEpochMilli()));
		requestMessageInfo.setTimeZone("");

		Token token = new Token();
		token.setCertificate("");
		token.setMessageHashKey("");
		token.setMessageIndex("");

		PasswordToken passToken = new PasswordToken();
		passToken.setPassword(env.getProperty("dms.password"));
		passToken.setUserId(env.getProperty("dms.userid"));

		DeviceInfo deviceInfo = new DeviceInfo();
		deviceInfo.setAppVersion("");
		deviceInfo.setDeviceFamily("");
		deviceInfo.setDeviceFormat("");
		deviceInfo.setDeviceID("");
		deviceInfo.setDeviceIMEI("");
		deviceInfo.setDeviceIp("");
		deviceInfo.setDeviceName("");
		deviceInfo.setDeviceOS("");
		deviceInfo.setDeviceType("");
		deviceInfo.setDeviceVersion("");

		AdditionalInfo additionalInfo = new AdditionalInfo();
		additionalInfo.setSessionId(RequestContextHolder.currentRequestAttributes().getSessionId());
		additionalInfo.setLanguageId("");
		additionalInfo.setJourneyId("");
		additionalInfo.setSVersion("");

		token.setPasswordToken(passToken);

		Security security = new Security();
		security.setToken(token);

		reqheader.setMessageKey(msgKey);
		reqheader.setAdditionalInfo(additionalInfo);
		reqheader.setDeviceInfo(deviceInfo);
		reqheader.setSecurity(security);
		reqheader.setRequestMessageInfo(requestMessageInfo);

		return reqheader;
	}
	
	public  void printObject(Object object) {
	    Gson gson = new GsonBuilder().setPrettyPrinting().create();
	    System.out.println("Printing object---"+gson.toJson(object));
	}
	
	public String convertToDateFormat(String date) throws Exception {
		System.out.println("Date:::"+date);
		SimpleDateFormat oldformatter = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		 String format = formatter.format(oldformatter.parse(date));
		System.out.println(format);
		return format;
	}
}
