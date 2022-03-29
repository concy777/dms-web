package com.rblbank.dms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
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
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rblbank.dms.dto.FileUploadDTO;
import com.rblbank.dms.entity.Account_Details;
import com.rblbank.dms.exception.StorageException;
import com.rblbank.dms.module.AddDocTags;
import com.rblbank.dms.module.AddDocumentDetails;
import com.rblbank.dms.module.AddReq;
import com.rblbank.dms.module.AddReqBody;
import com.rblbank.dms.module.AddRes;
import com.rblbank.dms.module.AdditionalInfo;
import com.rblbank.dms.module.DeviceInfo;
import com.rblbank.dms.module.MessageKey;
import com.rblbank.dms.module.PasswordToken;
import com.rblbank.dms.module.RequestHeader;
import com.rblbank.dms.module.RequestMessageInfo;
import com.rblbank.dms.module.SearchResStatus;
import com.rblbank.dms.module.Security;
import com.rblbank.dms.module.Token;

 
public class DemoClass {
	
	@Autowired
	private static Environment env;
	
	@Autowired
	private static RestTemplate restTemplate;
	
		
	public DemoClass() {
		// TODO Auto-generated constructor stub
	}
		
    public static void main(String... args) throws Exception {
    	
    	saveCAMS();
    }
    
    
	public static void saveCAMS() {

		String dirPat = "D:/Saravana/DMS/Sample";

		try (Stream<Path> walkpayj = Files.walk(Paths.get(dirPat))) {

			List<File> fileResult = Files.list(Paths.get(dirPat)).map(Path::toFile).collect(Collectors.toList());

			System.out.println("--fileResult Started---");

			fileResult.forEach(System.out::println);

			System.out.println("--fileResult Ended---");

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

									// String SELECT_BY_ID_QUERY = "SELECT top 1 * from Account where account_number
									// = '"+accNo+"'";

									// Account_Details accDetails= jdbcTemplate.queryForObject(SELECT_BY_ID_QUERY,
									// getMap(), accNo);

									// Account_Details accDetails= jdbcTemplate.queryForObject(SELECT_BY_ID_QUERY,
									// BeanPropertyRowMapper.newInstance(Account_Details.class));

									Account_Details accDe = getAccount_Details("account", accNo);

									if (accDe == null) {
										accDe = new Account_Details();
										accDe.setAccount_number(accNo);
										accDe.setError("No Record Found");
										accDe.setIserror("Y");
										accDe.setStatus("Failed");

									} else {
										accDe.setIserror("N");
										accDe.setStatus("Success");
									}

									try {
										List<File> files = Files.list(Paths.get(dPath)).map(Path::toFile)
												.collect(Collectors.toList());

										// files.forEach(System.out::println);

										for (File fl : files) {

											System.out.println("File getting Stored--" + fl.getName());

											storeMultipartFile(fl, accDe);
											// save(fl,accNo);
										}
									} catch (IOException e) {
										// TODO Auto-generated catch block
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

					if (camsDir.equalsIgnoreCase("Instruction")) {

						if (file.isDirectory()) {

							try (Stream<Path> walk = Files.walk(Paths.get(dNamePath))) {

								List<File> result = Files.list(Paths.get(dNamePath)).map(Path::toFile)
										.collect(Collectors.toList());
								System.out.println("--Instruction Folder Started---");

								result.forEach(System.out::println);

								System.out.println("--Instruction Folder Ended---");

								for (File fl : result) {

									System.out.println("File getting Stored--" + fl.getName());

									// String SELECT_BY_ID_QUERY = "SELECT top 1 * from Account where case_number =
									// '"+fl.getName()+"'";

									// Account_Details accDetails= jdbcTemplate.queryForObject(SELECT_BY_ID_QUERY,
									// getMap(), accNo);

									// Account_Details accDetai=

									Account_Details ins = getAccount_Details("case", getFileNameWithoutExtension(fl));

									if (ins == null) {
										ins = new Account_Details();
										ins.setCase_number(getFileNameWithoutExtension(fl));
										ins.setError("No Record Found");
										ins.setIserror("Y");
										ins.setStatus("Failed");

									} else {
										ins.setIserror("N");
										ins.setStatus("Success");
									}

									storeMultipartFile(fl, ins);
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

									Account_Details cifNoDe = getAccount_Details("cif", cifNo);

									if (cifNoDe == null) {
										cifNoDe = new Account_Details();
										cifNoDe.setCif_id(cifNo);
										cifNoDe.setError("No Record Found");
										cifNoDe.setIserror("Y");
										cifNoDe.setStatus("Failed");

									} else {
										cifNoDe.setIserror("N");
										cifNoDe.setStatus("Success");
									}

									try {
										List<File> instfiles = Files.list(Paths.get(dcmsPath)).map(Path::toFile)
												.collect(Collectors.toList());

										// files.forEach(System.out::println);

										for (File insf : instfiles) {

											System.out.println("File getting Stored--" + insf.getName());

											storeMultipartFile(insf, cifNoDe);
											// save(fl,accNo);
										}
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
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

					if (camsDir.equalsIgnoreCase("Instruction process in CAMS")) {

						try (Stream<Path> instwalk = Files.walk(Paths.get(dNamePath))) {

							List<File> isresult = Files.list(Paths.get(dNamePath)).map(Path::toFile)
									.collect(Collectors.toList());

							System.out.println("--Instruction Process Folder Started---");

							isresult.forEach(System.out::println);

							System.out.println("--Instruction Process Folder Ended---");

							for (File iuinFile : isresult) {

								if (iuinFile.isDirectory()) {

									System.out.println("Cams Directory: " + iuinFile.getAbsolutePath());

									String dPath = iuinFile.getAbsolutePath();

									String cifNo = dPath.substring(dPath.lastIndexOf("\\") + 1);

									System.out.println("cifNo: " + cifNo);
									// String SELECT_BY_ID_QUERY = "SELECT top 1 * from Account where cif_id =
									// '"+cifNo+"'";

									// Account_Details accDetails= jdbcTemplate.queryForObject(SELECT_BY_ID_QUERY,
									// getMap(), accNo);

									Account_Details cifNoDe = getAccount_Details("cif", cifNo);

									if (cifNoDe == null) {
										cifNoDe = new Account_Details();
										cifNoDe.setCif_id(cifNo);
										cifNoDe.setError("No Record Found");
										cifNoDe.setIserror("Y");
										cifNoDe.setStatus("Failed");

									} else {
										cifNoDe.setIserror("N");
										cifNoDe.setStatus("Success");
									}

									try {
										List<File> instfiles = Files.list(Paths.get(dPath)).map(Path::toFile)
												.collect(Collectors.toList());

										// files.forEach(System.out::println);

										for (File insf : instfiles) {

											System.out.println("File getting Stored--" + insf.getName());

											storeMultipartFile(insf, cifNoDe);
											// save(fl,accNo);
										}
									} catch (IOException e) {
										// TODO Auto-generated catch block
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
											getFileNameWithoutExtension(mafl));

									if (instruc == null) {
										instruc = new Account_Details();
										instruc.setAccount_number(getFileNameWithoutExtension(mafl));
										instruc.setError("No Record Found");
										instruc.setIserror("Y");
										instruc.setStatus("Failed");

									} else {
										instruc.setIserror("N");
										instruc.setStatus("Success");
									}

									storeMultipartFile(mafl, instruc);
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

	}
    
    
	private static String getFileNameWithoutExtension(File file) {
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
    
	public static Account_Details getAccount_Details(String type, String data) {

		SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
		dataSource.setDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
		dataSource.setUrl("jdbc:sqlserver://localhost:1433;databaseName=dms");
		dataSource.setUsername("sa");
		dataSource.setPassword("Sara@2020");

		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		String SELECT_BY_ID_QUERY = "";

		try {
			if (type.equalsIgnoreCase("account")) {

				SELECT_BY_ID_QUERY = "SELECT top 1 * from Account where account_number = '" + data + "'";

			}

			else if (type.equalsIgnoreCase("cif")) {

				SELECT_BY_ID_QUERY = "SELECT top 1 * from Account where cif_id = '" + data + "'";

			} else if (type.equalsIgnoreCase("case")) {

				SELECT_BY_ID_QUERY = "SELECT top 1 * from Account where case_number = '" + data + "'";

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

    
    private static RowMapper<Account_Details> getMap(){
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
    
    public static void showFile(File file) {
        if (file.isDirectory()) {
            
            String dNamePath=file.getAbsolutePath();
            
            System.out.println("Directory: " + file.getAbsolutePath());
            
            String camsDir=dNamePath.substring(dNamePath.lastIndexOf("\\") + 1);
            
            System.out.println("camsDir Directory: " + camsDir);
            
            Path camsPath=Paths.get(camsDir);
            
            System.out.println("camsPath: " + camsPath);
            
            if(camsDir.equalsIgnoreCase("Account opened in CAMS")) {
            	
            	try (Stream<Path> walk = Files.walk(Paths.get(dNamePath))) {

            		List<File> result = Files.list(Paths.get(dNamePath))
					        .map(Path::toFile)
					        .collect(Collectors.toList());

                    result.forEach(System.out::println);
                    
                    for(File camFile:result) {
                		
                    	
                    	if (camFile.isDirectory()) {
                    		
                    		System.out.println("Directory: " + camFile.getAbsolutePath());
                            
                            String dPath=camFile.getAbsolutePath();
                            
                            String accNo=dPath.substring(dPath.lastIndexOf("\\") + 1);
                            
                            System.out.println("accNo: " + accNo);
                            
                            try {
        						List<File> files = Files.list(Paths.get(dPath))
        						        .map(Path::toFile)
        						        .collect(Collectors.toList());
                    
        							//files.forEach(System.out::println);
        							
        							for(File fl:files) {
        								
        								System.out.println("File getting Stored--"+fl.getName());
        								
        								storeMultipartFile(fl,getAccount_Details("account",fl.getName()));
        								//save(fl,accNo);
        							}
        					} catch (IOException e) {
        						// TODO Auto-generated catch block
        						e.printStackTrace();
        					}
                    		
                    	}
                    	else {
                    		 System.out.println("File: " + file.getAbsolutePath());
                    	}
                	}
                    

                } catch (IOException e) {
                    e.printStackTrace();
                }
            	
            }
            
            if(camsDir.equalsIgnoreCase("Instruction")) {
            	
            	if (file.isDirectory()) {
            		
            		try (Stream<Path> walk = Files.walk(Paths.get(dNamePath))) {

                		List<File> result = Files.list(Paths.get(dNamePath))
    					        .map(Path::toFile)
    					        .collect(Collectors.toList());

                        result.forEach(System.out::println);
                        
            							for(File fl:result) {
            								
            								System.out.println("File getting Stored--"+fl.getName());
            								
            								storeMultipartFile(fl,getAccount_Details("account",fl.getName()));
            								//save(fl,accNo);
            							}
            					} catch (IOException e) {
            						// TODO Auto-generated catch block
            						e.printStackTrace();
            					}
                        		
                        	}
                        	else {
                        		 System.out.println("Instruction Invalid File Location: " + file.getAbsolutePath());
                        	}
                    	
                        

                    } 
                } 
        	else {
           System.out.println("Invalid File Location in Main CAMS Directory: " + file.getAbsolutePath());
     
        	}
    }

    
    public static RequestHeader setReqHeader(String searchType ) {
		
		RequestHeader reqheader = new RequestHeader();

		
		MessageKey msgKey=new MessageKey();
		msgKey.setRequestUUID(UUID.randomUUID().toString());
		msgKey.setChannelId(env.getProperty("dms.channelId"));
		
		if(searchType.equalsIgnoreCase("search")) {
			msgKey.setServiceRequestId(env.getProperty("dms.search.serviceReqId"));
			msgKey.setServiceRequestVersion(env.getProperty("dms.search.serviceReqVersion"));
			
		}
		else if(searchType.equalsIgnoreCase("add")) {
			msgKey.setServiceRequestId(env.getProperty("dms.add.serviceReqId"));
			msgKey.setServiceRequestVersion(env.getProperty("dms.add.serviceReqVersion"));
			
		}
		else if(searchType.equalsIgnoreCase("view")) {
			msgKey.setServiceRequestId(env.getProperty("dms.view.serviceReqId"));
			msgKey.setServiceRequestVersion(env.getProperty("dms.view.serviceReqVersion"));
			
		}
		else if(searchType.equalsIgnoreCase("update")) {
			msgKey.setServiceRequestId(env.getProperty("dms.search.serviceReqId"));
			msgKey.setServiceRequestVersion(env.getProperty("dms.search.serviceReqVersion"));
			
		}
		else {
			msgKey.setServiceRequestId(env.getProperty("dms.search.serviceReqId"));
			msgKey.setServiceRequestVersion(env.getProperty("dms.search.serviceReqVersion"));
			
		}
		
		
		RequestMessageInfo requestMessageInfo=new RequestMessageInfo();
		requestMessageInfo.setBankId("");
		requestMessageInfo.setMessageDateTime(String.valueOf(Instant.now().toEpochMilli()));
		requestMessageInfo.setTimeZone("");
		
		Token token=new Token();
		token.setCertificate("");
		token.setMessageHashKey("");
		token.setMessageIndex("");
		
		PasswordToken passToken=new PasswordToken();
		passToken.setPassword(env.getProperty("dms.password"));
		passToken.setUserId(env.getProperty("dms.userid"));
		
		DeviceInfo deviceInfo=new DeviceInfo();
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
		
		AdditionalInfo additionalInfo=new AdditionalInfo();
		additionalInfo.setSessionId(RequestContextHolder.currentRequestAttributes().getSessionId());
		additionalInfo.setLanguageId("");
		additionalInfo.setJourneyId("");
		additionalInfo.setSVersion("");
		
		token.setPasswordToken(passToken);
		
		Security security=new Security();
		security.setToken(token);
		
		reqheader.setMessageKey(msgKey);
		reqheader.setAdditionalInfo(additionalInfo);
		reqheader.setDeviceInfo(deviceInfo);
		reqheader.setSecurity(security);
		reqheader.setRequestMessageInfo(requestMessageInfo);
		
		return  reqheader;
	}
	public static String save(MultipartFile file,Account_Details accDetails) {
			
		System.out.println("Application upload-doc Started...");

		String uri = "";
		if (env.getProperty("dms.instance").equalsIgnoreCase("uat")) {
			uri = env.getProperty("dms.uat.addDoc");
		} else {
			uri = env.getProperty("dms.prod.addDoc");

		}
		
		
		RequestHeader header=setReqHeader("addDoc");
		AddDocumentDetails addDocumentDetails=new AddDocumentDetails();
		String fnameNew=file.getOriginalFilename().substring(0, file.getOriginalFilename().indexOf("."));
		addDocumentDetails.setFileName(fnameNew.replaceAll("[^a-zA-Z0-9]", " "));
		
		String fileType=file.getContentType().substring(file.getContentType().lastIndexOf("/")+1);
		System.out.println("FIletype--"+fileType);
		
		if(fileType.equalsIgnoreCase("tiff")) {
			fileType="tif";
		}
		addDocumentDetails.setFileType(fileType);
		
		AddDocTags docTag = new AddDocTags();

		String cifId = "";
		String dateOfAccOpening = "";
		String docFolder = "";
		String documentType = "";
		String kycId = "";
		//if(doc.getName()==nul)
		String name = "";
		String ucic = "";
		String validity = "";
		String isKyc = "";
		String referenceNumber = "";

		docTag.setAccNo(accDetails.getAccount_number());
		docTag.setCifId(accDetails.getCif_id());
		docTag.setDateOfAccOpening(accDetails.getDocDate());
		docTag.setDocFolder(accDetails.getDoc_Folder());
		docTag.setDocumentType(accDetails.getDocType());
		docTag.setKycId(accDetails.getKyc_Id());
		docTag.setName(accDetails.getFileName());
		docTag.setUcic(accDetails.getUcic());
		docTag.setValidity(accDetails.getValidity()); 
		docTag.setLastKycCheck(accDetails.getDocDate());
		
		if(docTag.getDocumentType().equalsIgnoreCase("POI") || docTag.getDocumentType().equalsIgnoreCase("POA") ) {
			docTag.setKyc(true);
		}
		else {
			docTag.setKyc(false);
		}
		
		docTag.setReferenceNumber(accDetails.getReference_Number());
		docTag.setSourceSystem(accDetails.getSource());
		AddReqBody bod = new AddReqBody();
		
		System.out.println("Acc--" + accDetails.getAccount_number());
		System.out.println("CIF--" + accDetails.getCif_id());
		System.out.println("DOB--" + accDetails.getDocDate());
		System.out.println("KYC--" + accDetails.getKyc_Id());
		System.out.println("UCIC--" + accDetails.getUcic());
		System.out.println("Ref--" + accDetails.getReference_Number());
		System.out.println("Source--" + accDetails.getSource());
		System.out.println("DocType--" + accDetails.getDocType());
		
		FileUploadDTO dto=new FileUploadDTO();
		dto.setAccNo(accDetails.getAccount_number());
		dto.setCifId(accDetails.getCif_id());
		dto.setDateOfOpening(accDetails.getDocDate());
		dto.setRefNo(accDetails.getReference_Number());
		dto.setSource(accDetails.getSource());
		dto.setUcic(accDetails.getUcic());
		dto.setDocfolder(accDetails.getDoc_Folder());
		dto.setDoctype(accDetails.getDocType());
		
		bod.setDocumentDetails(addDocumentDetails);
		bod.setDocumentTags(docTag);
		try {
			File fi=convertMultipartToFile(file);
			dto.setFileName(fi.getName());
			dto.setFileSize(String.valueOf(fi.length() / (1024 * 1024)) +"mb");
			dto.setFileType(file.getContentType());
			System.out.println("Byte---");
			byte[] normalFileContent=Files.readAllBytes(fi.toPath());
			String b64 = java.util.Base64.getEncoder().encodeToString(normalFileContent);
			bod.setDocument(b64);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		AddReq dmsReq = new AddReq();
		dmsReq.setBody(bod);
		dmsReq.setHeader(header);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		HttpEntity<AddReq> request = new HttpEntity<>(dmsReq, headers);

		printObject(request);
		
		String jsonString = "";
		
				if (env.getProperty("dms.instance").equalsIgnoreCase("uat")) {
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

		printObject("DTO---"+dto);
		SearchResStatus result = res.getStatus();
		
		System.out.println("Display text----" + result.getDisplayText());

		System.out.println("Statuscode----" + result.getStatusCode());
		
		if(result.getStatusCode().equalsIgnoreCase("0")) {
			insert(accDetails,file);
		}
		else {
			return "error";
		}
		
		return fnameNew;
		
	}
	
	public static  void printObject(Object object) {
	    Gson gson = new GsonBuilder().setPrettyPrinting().create();
	    System.out.println("Printing object---"+gson.toJson(object));
	}
	
	public static File convertMultipartToFile(MultipartFile mp) throws Exception{
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
	
	private static String storeMultipartFile(File file,Account_Details accDetails) {
		
		
		if(accDetails!=null) {
			accDetails.setFileName(file.getName());
			accDetails.setFile_path(file.getAbsolutePath());
		}
		
		
		String fileName="";
		try {
			FileInputStream input = new FileInputStream(file);
			MultipartFile multipartFile = new MockMultipartFile("file",file.getName(), "text/plain", IOUtils.toByteArray(input));
			fileName= store(multipartFile,accDetails);
			 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileName;
	}


	public static String store(MultipartFile file,Account_Details accDetails) {
		String filename = StringUtils.cleanPath(file.getOriginalFilename());
		
		String rootLoca="D:/Saravana/DMS/UploadedFiles";
		
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
				insert(accDetails,file);
			}
		} catch (IOException e) {
			throw new StorageException("Failed to store file " + filename, e);
		}

		return filename;
	}
	
	public static void insert(Account_Details acc,MultipartFile file) {
		
		
		if(acc==null) {
			
			
		}
		
		SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
        dataSource.setUrl("jdbc:sqlserver://localhost:1433;databaseName=dms");
        dataSource.setUsername("sa");
        dataSource.setPassword("Sara@2020");
		
		String sql = "INSERT INTO file_upload " +
	            "(acc_no, cif_id, date_of_opening,file_name,file_path,file_size,file_type,iskyc,ref_no,source,ucic,iserror,error,status,created_by,created_date) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,163983,getdate())";
	                 
		JdbcTemplate   jdbcTemplate = new JdbcTemplate(dataSource);
	                
	      int row=  jdbcTemplate.update(sql, new Object[] { acc.getAccount_number(),acc.getCif_id(),acc.getDocDate(),StringUtils.cleanPath(file.getOriginalFilename()),acc.getFile_path() ,file.getSize() / (1024 * 1024),file.getContentType(),
	        		
	            acc.getDocType(),acc.getCase_number(),acc.getSource(),acc.getUcic(),acc.getIserror(),acc.getError(),acc.getStatus()
	        });
	      
	      System.out.println("Rows added--"+row);
	}

	public static void storeFile(String filename ) {
		
		File file= new File(filename);
		
		if (file.isDirectory()) {
    		
    		System.out.println("Directory: " + file.getAbsolutePath());
            
            String dPath=file.getAbsolutePath();
            
            String accNo=dPath.substring(dPath.lastIndexOf("\\") + 1);
            
            System.out.println("accNo: " + accNo);
            
            try {
				List<File> files = Files.list(Paths.get(dPath))
				        .map(Path::toFile)
				        .collect(Collectors.toList());
    
					files.forEach(System.out::println);
					
					for(File fl:files) {
						
						//save(fl,accNo);
					}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	}
    	else {
    		 System.out.println("File: " + file.getAbsolutePath());
    	}
	}
}