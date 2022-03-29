package com.rblbank.dms.controller;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.HttpRequest;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rblbank.dms.dto.FileUploadDTO;
import com.rblbank.dms.dto.LoggedInUserDTO;
import com.rblbank.dms.entity.DmsAppUser;
import com.rblbank.dms.entity.FileUpload;
import com.rblbank.dms.models.ERole;
import com.rblbank.dms.models.RefreshToken;
import com.rblbank.dms.models.User;
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
import com.rblbank.dms.module.LdapDtls;
import com.rblbank.dms.module.LoginRequest;
import com.rblbank.dms.module.LoginResponse;
import com.rblbank.dms.module.MessageKey;
import com.rblbank.dms.module.PasswordToken;
import com.rblbank.dms.module.RequestHeader;
import com.rblbank.dms.module.RequestMessageInfo;
import com.rblbank.dms.module.SearchDocDetails;
import com.rblbank.dms.module.SearchDocTag;
import com.rblbank.dms.module.SearchHeader;
import com.rblbank.dms.module.SearchReq;
import com.rblbank.dms.module.SearchReqBody;
import com.rblbank.dms.module.SearchReqDateofAccOpening;
import com.rblbank.dms.module.SearchReqLastKycCheck;
import com.rblbank.dms.module.SearchRes;
import com.rblbank.dms.module.SearchResBody;
import com.rblbank.dms.module.SearchResStatus;
import com.rblbank.dms.module.Security;
import com.rblbank.dms.module.Token;
import com.rblbank.dms.module.UpdateDocTags;
import com.rblbank.dms.module.UpdateReq;
import com.rblbank.dms.module.UpdateReqBody;
import com.rblbank.dms.module.UpdateRes;
import com.rblbank.dms.module.ViewDocDetails;
import com.rblbank.dms.module.ViewDocRes;
import com.rblbank.dms.module.ViewReq;
import com.rblbank.dms.module.ViewReqBody;
import com.rblbank.dms.module.ViewResBody;
import com.rblbank.dms.payload.response.JwtResponse;
import com.rblbank.dms.repository.UserRepository;
import com.rblbank.dms.security.MediaTypeUtil;
import com.rblbank.dms.security.jwt.JwtUtils;
import com.rblbank.dms.security.services.RefreshTokenService;
import com.rblbank.dms.security.services.UserServiceImpl;
import com.rblbank.dms.service.DmsUserService;
import com.rblbank.dms.service.FileUploadService;
import com.rblbank.dms.service.StorageService;

@CrossOrigin(origins = "*", maxAge = 3600)
@Controller
@RequestMapping("/dms-app")
public class DmsController {

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	JwtUtils jwtUtils;

	@Autowired
	RefreshTokenService refreshTokenService;

	@Autowired
	UserServiceImpl userInfo;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private FileUploadService fileuploadService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	private Environment env;

	@Autowired
	private ServletContext servletContext;

	@Autowired
	private RestTemplate restTemplate;

	private StorageService storageService;

	private static final Logger logger = LoggerFactory.getLogger(DmsController.class);

	private final String LOCALHOST_IPV4 = "127.0.0.1";
	private final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
	public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> uniqueMap = new ConcurrentHashMap<>();
		return t -> uniqueMap.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	public DmsController() {
	}

	public DmsController(StorageService storageService) {
		this.storageService = storageService;

	}

	public DmsController(FileUploadService fileUploadService) {
		this.fileuploadService = fileUploadService;

	}

	@GetMapping("/logoutSuccessful")
	public String logoutSuccessfulPage(Model model,HttpSession session,SessionStatus stat) {
		model.addAttribute("title", "Logout");
		session.invalidate();
		SecurityContextHolder.getContext().setAuthentication(null);
        stat.setComplete();
		return "logout";
	}
	
	@GetMapping("/utility-api/saveCAMSFile")
	public String addDocUtilty(HttpSession session, Model model) {

		StopWatch watch = new StopWatch();
		watch.start();
		String dirLoc = env.getProperty("camsDir");

		String instance = env.getProperty("dms.instance");

		String message = "";
		try {
			message = fileuploadService.saveCMSFile(Paths.get(URI.create(dirLoc)), instance, session, model);
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("message", e.getMessage());
		}
		model.addAttribute("message", message);
		watch.stop();
		long result = watch.getTime();
		System.out.println("Program took time---" + (result / 1000) % 60 + "seconds");
		return "utility";
	}

	@GetMapping("/index")
	//@PreAuthorize("hasRole('USER') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
	public String mainDMS(Model model) {

		logger.info("Application Started");
		
		SearchReq dmsReq = new SearchReq();
		SearchRes dmsRes = new SearchRes();
		SearchResBody body = new SearchResBody();
		SearchResStatus status = new SearchResStatus();
		SearchDocDetails details = new SearchDocDetails();
		
		List<SearchDocDetails> doclist = new ArrayList<>();
		doclist.add(details);
		body.setDocumentDetails(doclist);
		dmsRes.setBody(body);
		dmsRes.setStatus(status);

		AddReq addReq = new AddReq();
		AddReqBody bodd = new AddReqBody();
		AddDocTags doctag = new AddDocTags();
		AddDocumentDetails detailsnew = new AddDocumentDetails();
		bodd.setDocumentDetails(detailsnew);
		bodd.setDocumentTags(doctag);
		addReq.setBody(bodd);

		SearchResStatus result = new SearchResStatus();
		
		model.addAttribute("searchReq", dmsReq);
		model.addAttribute("searchRes", dmsRes);
		model.addAttribute("addReq", doctag);
		model.addAttribute("addRes", result);
		model.addAttribute("updateDocTags", new UpdateDocTags());

		return "dms";
	}

	@GetMapping("/app-utility")
	public String loginPage(Model model) {
		model.addAttribute("message","");
		return "utility";
	}

	@GetMapping("/logout")
	public String logoutPage(Model model) {
		return "logout";
	}

	@PostMapping("/admin/upload-doc")
	@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
	public ResponseEntity<?> uploadDoc(@RequestParam("file") MultipartFile file, @ModelAttribute AddDocTags doc,
			Model model, HttpSession session, HttpServletRequest requ) {

		logger.info("Application upload-doc Started...");
		LoggedInUserDTO loggedInUserDTO=getLoggedInUser(requ);
		logger.info("username:"+loggedInUserDTO.getUsername()+"--"+"userRole:"+loggedInUserDTO.getRole()+"--"+"userIpAddress:"+loggedInUserDTO.getClientIp()+"--"+"loggedon:"+new Date());
		
		String uri = "";
		if (env.getProperty("dms.instance").equalsIgnoreCase("uat")) {
			uri = env.getProperty("dms.uat.addDoc");
		} else if (env.getProperty("dms.instance").equalsIgnoreCase("dev")) {
			uri = env.getProperty("dms.dev.addDoc");
		} else if (env.getProperty("dms.instance").equalsIgnoreCase("prod")) {
			uri = env.getProperty("dms.prod.addDoc");
		} else {
			uri = env.getProperty("dms.local.addDoc");

		}

		logger.info("URI..." + uri);

		RequestHeader header = setReqHeader("addDoc");

		AddDocumentDetails addDocumentDetails = new AddDocumentDetails();

		String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
		String fnameNew = file.getOriginalFilename().substring(0, file.getOriginalFilename().indexOf("."));

		boolean isExists = false;
		try {
			isExists = fileuploadService.isFileExist(file.getName(), doc.getAccNo(), env.getProperty("dms.instance"));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		SearchResStatus result = new SearchResStatus();
		if (isExists) {
			result.setStatusCode("1");
			result.setDisplayText("File Already Exists in Dms.");
		} else {
			addDocumentDetails.setFileName(fnameNew.trim());
			String fileType = file.getContentType().substring(file.getContentType().lastIndexOf("/") + 1);

			if (extension.equalsIgnoreCase("tiff")) {
				fileType = "tif";
			}
			addDocumentDetails.setFileType(fileType);

			AddDocTags docTag = new AddDocTags();
			if (doc.getAccNo() != null && doc.getAccNo().trim().length() > 0) {
				docTag.setAccNo(doc.getAccNo());
			} else {
				docTag.setAccNo("");
			}

			docTag.setCifId(doc.getCifId());
			docTag.setDateOfAccOpening(doc.getDateOfAccOpening());
			docTag.setDocFolder(doc.getDocFolder());
			if (doc.getDocumentType().equalsIgnoreCase("select")) {
				docTag.setDocumentType("");
			} else {
				docTag.setDocumentType(doc.getDocumentType());
			}

			docTag.setKycId(doc.getKycId());
			docTag.setName(doc.getName());
			docTag.setUcic(doc.getUcic());
			docTag.setValidity(doc.getDateOfAccOpening());
			docTag.setLastKycCheck(doc.getDateOfAccOpening());

			if (docTag.getDocumentType().equalsIgnoreCase("POI") || docTag.getDocumentType().equalsIgnoreCase("POA")) {
				docTag.setKyc(true);
			} else {
				docTag.setKyc(false);
			}

			docTag.setReferenceNumber(doc.getReferenceNumber().trim());
			docTag.setSourceSystem(doc.getSourceSystem());
			docTag.setDocFolder(doc.getDocFolder());
			docTag.setIdentificationNumber(doc.getIdentificationNumber());
			AddReqBody bod = new AddReqBody();

			FileUploadDTO dto = new FileUploadDTO();
			dto.setAccNo(doc.getAccNo());
			dto.setCifId(doc.getCifId());
			dto.setDateOfOpening(doc.getDateOfAccOpening());
			dto.setRefNo(doc.getReferenceNumber());
			dto.setSource(doc.getSourceSystem());
			dto.setUcic(doc.getUcic());
			dto.setDocfolder(doc.getDocFolder());
			dto.setDoctype(doc.getDocumentType());

			bod.setDocumentDetails(addDocumentDetails);
			bod.setDocumentTags(docTag);
			try {
				dto.setFileName(file.getName());
				dto.setFileSize(String.valueOf(file.getSize()));
				dto.setFileType(file.getContentType());
				String b64 = java.util.Base64.getEncoder().encodeToString(file.getBytes());
				bod.setDocument(b64);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			AddReq dmsReq = new AddReq();
			dmsReq.setBody(bod);
			dmsReq.setHeader(header);

			String token = (String) session.getAttribute("apitoken");

			System.out.println("Session Token--" + token);

			if (token == null || token.trim().isEmpty()) {
				GenerateTokenResponseBody tokenBody = new GenerateTokenResponseBody();
				try {

					tokenBody = getToken(model);
					token = tokenBody.getAccess_token();

				} catch (MalformedURLException e1) { // TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

			try {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
				headers.setBearerAuth(token);
				headers.set("api-key", env.getProperty("dms-api-key"));
				HttpEntity<AddReq> request = new HttpEntity<>(dmsReq, headers);

				printObject(request);

				String jsonString = "";

				if (env.getProperty("dms.instance").equalsIgnoreCase("local")) {
					jsonString = restTemplate.getForObject(uri, String.class);
				} else {
					jsonString = restTemplate.postForObject(uri, request, String.class);

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

				// printObject("DTO---" + dto);
				result = res.getStatus();
				if (result.getStatusCode().equalsIgnoreCase("0")) {
					FileUpload postRequest = modelMapper.map(dto, FileUpload.class);
					FileUpload post;
					try {
						post = fileuploadService.createPost(postRequest);
						// printObject("fileupload--------" + post);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			} catch (RestClientException e) {
				// TODO Auto-generated catch block
				
				result.setStatusCode("1");
				result.setDisplayText("Upload API Resource not found. Please contact support team.");
				
				e.printStackTrace();
			}

			
			SearchReq req = new SearchReq();
			SearchRes dmsRes = new SearchRes();

			SearchResBody body = new SearchResBody();
			SearchDocDetails details = new SearchDocDetails();
			List<SearchDocDetails> doclist = new ArrayList<>();
			doclist.add(details);
			body.setDocumentDetails(doclist);
			dmsRes.setBody(body);
			model.addAttribute("searchReq", req);
			model.addAttribute("searchRes", dmsRes);
			model.addAttribute("addReq", dmsReq);
			model.addAttribute("addRes", result);
			model.addAttribute("updateDocTags", new UpdateDocTags());
			model.addAttribute("updateModal", "0");

		}

		return ResponseEntity.ok(result);
	}

	@PostMapping("/admin/update-doc")
	public ResponseEntity<?> updateDoc(@ModelAttribute UpdateDocTags doc, Model model, HttpServletRequest httpReq) throws Exception {

		logger.info("Application upload-doc Started...");

		String uri = "";
		if (env.getProperty("dms.instance").equalsIgnoreCase("uat")) {
			uri = env.getProperty("dms.uat.updateDoc");
		} else if (env.getProperty("dms.instance").equalsIgnoreCase("dev")) {
			uri = env.getProperty("dms.dev.updateDoc");
		} else if (env.getProperty("dms.instance").equalsIgnoreCase("prod")) {
			uri = env.getProperty("dms.prod.updateDoc");
		} else {
			uri = env.getProperty("dms.local.updateDoc");

		}
		logger.info("URI..." + uri);

		RequestHeader header = setReqHeader("addDoc");

		AddDocumentDetails addDocumentDetails = new AddDocumentDetails();

		UpdateReqBody viewResponse = getDoc(doc.getDocId(),httpReq);

		String fnameNew = viewResponse.getDocumentDetails().getFileName();

		addDocumentDetails.setFileName(fnameNew);

		String fileType = viewResponse.getDocumentDetails().getFileType();

		System.out.println("FIletype--" + fileType);
		addDocumentDetails.setFileType(fileType);

		UpdateDocTags docTag = new UpdateDocTags();

		String docFolder = "";

		docTag.setAccountNumber(doc.getAccountNumber());
		docTag.setCifId(doc.getCifId());
		docTag.setDateOfAccOpening(doc.getDateOfAccOpening());
		docTag.setDocFolder(docFolder);
		docTag.setDocumentType(doc.getDocumentType());
		docTag.setKycId(fnameNew);
		docTag.setName(doc.getName());
		docTag.setUcic(doc.getUcic());
		docTag.setValidity(doc.getDateOfAccOpening());
		docTag.setLastKycCheck(doc.getDateOfAccOpening());
		docTag.setReferenceNumber(doc.getReferenceNumber());
		docTag.setSourceSystem(doc.getSourceSystem());
		docTag.setDocFolder(doc.getDocFolder());

		UpdateReqBody bod = new UpdateReqBody();

		System.out.println("Acc--" + doc.getAccountNumber());
		System.out.println("CIF--" + doc.getCifId());
		System.out.println("DOB--" + doc.getDateOfAccOpening());
		System.out.println("KYC--" + doc.getKycId());
		System.out.println("UCIC--" + doc.getUcic());
		System.out.println("Ref--" + doc.getReferenceNumber());
		System.out.println("Source--" + doc.getSourceSystem());
		System.out.println("DocType--" + doc.getDocumentType());

		FileUploadDTO dto = new FileUploadDTO();
		dto.setAccNo(doc.getAccountNumber());
		dto.setCifId(doc.getCifId());
		dto.setDateOfOpening(doc.getDateOfAccOpening());
		dto.setRefNo(doc.getReferenceNumber());
		dto.setSource(doc.getSourceSystem());
		dto.setUcic(doc.getUcic());
		dto.setDocfolder(doc.getDocFolder());
		dto.setDoctype(doc.getDocumentType());
		dto.setFileName(fnameNew);
		dto.setFileSize("");
		dto.setFileType("");

		bod.setDocumentDetails(addDocumentDetails);
		bod.setDocumentTags(docTag);

		UpdateReq dmsReq = new UpdateReq();
		dmsReq.setBody(bod);
		dmsReq.setHeader(header);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		HttpEntity<UpdateReq> request = new HttpEntity<>(dmsReq, headers);

		printObject(request);

		String jsonString = "";

		if (env.getProperty("dms.instance").equalsIgnoreCase("local")) {
			jsonString = restTemplate.getForObject(uri, String.class);
		} else {
			jsonString = restTemplate.postForObject(uri, request, String.class);

		}
		System.out.println("Data::" + jsonString);

		UpdateRes res = new UpdateRes();
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		try {
			res = mapper.readValue(jsonString, UpdateRes.class);
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

		printObject("DTO---" + dto);
		SearchResStatus result = res.getStatus();
		if (result.getStatusCode().equalsIgnoreCase("0")) {
			FileUpload postRequest = modelMapper.map(dto, FileUpload.class);
			FileUpload post;
			try {
				post = fileuploadService.createPost(postRequest);
				printObject("fileupload--------" + post);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		System.out.println("Display text----" + result.getDisplayText());

		System.out.println("Statuscode----" + result.getStatusCode());

		SearchReq req = new SearchReq();
		SearchRes dmsRes = new SearchRes();

		SearchResBody body = new SearchResBody();
		SearchDocDetails details = new SearchDocDetails();
		List<SearchDocDetails> doclist = new ArrayList<>();
		doclist.add(details);
		body.setDocumentDetails(doclist);
		dmsRes.setBody(body);
		model.addAttribute("searchReq", req);
		model.addAttribute("searchRes", dmsRes);
		model.addAttribute("addReq", dmsReq);
		model.addAttribute("UpdateRes", result);
		model.addAttribute("updateDocTags", new UpdateDocTags());

		return ResponseEntity.ok(result);
	}

	@PostMapping("/admin/add-doc")
	public String addDMSDoc(@RequestParam("file") MultipartFile file, @ModelAttribute AddDocTags doc, Model model) {
		logger.info("Application aa-doc Started...");

		String uri = "";
		if (env.getProperty("dms.instance").equalsIgnoreCase("uat")) {
			uri = env.getProperty("dms.uat.addDoc");
		} else {
			uri = env.getProperty("dms.prod.addDoc");

		}

		logger.info("URI..." + uri);

		AddReq dmsReq = new AddReq();

		SearchHeader reqheader = new SearchHeader();

		reqheader.setChannelId(env.getProperty("dms.channelId"));
		reqheader.setRequestUuid(UUID.randomUUID().toString());
		reqheader.setServiceRequestId(env.getProperty("dms.serviceReqId"));

		AddReqBody bod = new AddReqBody();

		AddDocTags docTag = new AddDocTags();

		String cifId = "";
		String dateOfAccOpening = "";
		String docFolder = "";
		String documentType = "";
		String kycId = "";
		String name = "";
		String ucic = "";
		String validity = "";
		String isKyc = "";
		String referenceNumber = "";

		// docDetail.setFileName(addReq.getFileName());
		// docDetail.setFileType(addReq.getBody().getDocumentDetails().getFileType());

		docTag.setAccNo(doc.getAccNo());

		System.out.println("Acc--" + doc.getAccNo());

		docTag.setCifId(doc.getCifId());
		docTag.setDateOfAccOpening(doc.getDateOfAccOpening());
		docTag.setDocFolder(docFolder);
		docTag.setDocumentType(documentType);
		docTag.setKycId(doc.getKycId());
		docTag.setName(name);
		docTag.setUcic(doc.getUcic());
		docTag.setValidity(validity);
		// docTag.setIsKyc(doc.getIsKyc());
		docTag.setReferenceNumber(referenceNumber);

		FileUploadDTO dto = new FileUploadDTO();
		dto.setAccNo(doc.getAccNo());
		dto.setCifId(doc.getCifId());
		dto.setDateOfOpening(doc.getDateOfAccOpening());
		// dto.setIsKYC(doc.getIsKyc());
		dto.setRefNo(doc.getReferenceNumber());
		dto.setSource(doc.getSourceSystem());
		dto.setUcic(doc.getUcic());
		dto.setDocfolder(doc.getDocFolder());
		dto.setDoctype(doc.getDocumentType());

		try {
			System.out.println("Image Size in Bytes - " + file.getBytes().length);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String filename = StringUtils.cleanPath(file.getOriginalFilename());

		System.out.println("filename----" + filename);
		try {

			File fi = fileuploadService.convertMultipartToFile(file);

			byte[] normalFileContent = Files.readAllBytes(fi.toPath());

			String b64 = java.util.Base64.getEncoder().encodeToString(normalFileContent);

			System.out.println(b64);

			byte[] fileContent = file.getBytes();
			String encodedString = Base64.encodeBase64String(fileContent);

			// System.out.println(encodedString);

			bod.setDocument(encodedString);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// dmsReq.setHeader(reqheader);
		dmsReq.setBody(bod);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		HttpEntity<AddReq> request = new HttpEntity<>(dmsReq, headers);

		// String jsonString = restTemplate.postForObject(uri,request,String.class);

		String jsonString = restTemplate.getForObject(uri, String.class);

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

		SearchResStatus result = res.getStatus();
		System.out.println("Display text----" + result.getDisplayText());

		System.out.println("Statuscode----" + result.getStatusCode());

		SearchReq req = new SearchReq();
		SearchRes dmsRes = new SearchRes();

		SearchResBody body = new SearchResBody();
		SearchDocDetails details = new SearchDocDetails();
		List<SearchDocDetails> doclist = new ArrayList<>();
		doclist.add(details);
		body.setDocumentDetails(doclist);
		dmsRes.setBody(body);
		model.addAttribute("searchReq", req);
		model.addAttribute("searchRes", dmsRes);
		model.addAttribute("addReq", dmsReq);
		model.addAttribute("addRes", result);
		model.addAttribute("updateDocTags", new UpdateDocTags());
		
		return "dms";
	}
	
	public JwtResponse getToken(String username) {
		
	    try {
			UserDetails uObj=userInfo.loadUserByUsername(username);
			
			String jwt = jwtUtils.generateJwtToken(username);
			
			List<String> roles = uObj.getAuthorities().stream()
			    .map(item -> item.getAuthority())
			    .collect(Collectors.toList());
			
			RefreshToken refreshToken = refreshTokenService.createRefreshToken(username);

			return new JwtResponse(jwt, refreshToken.getToken(),uObj.getUsername(), roles);
		} catch (UsernameNotFoundException e) {
			// TODO Auto-generated catch block
			e.getMessage();
			//e.printStackTrace();
			return null;
		}
		
	}

	public LoggedInUserDTO getLoggedInUser( HttpServletRequest reqHttp) {
		
		LoggedInUserDTO loggedInUserDTO=new LoggedInUserDTO();
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username ="";
		if (principal instanceof UserDetails) {
		   username = ((UserDetails)principal).getUsername();
		} else {
		   username = principal.toString();
		}
		JwtResponse jwt=getToken(username);
		loggedInUserDTO.setUsername(jwt.getUsername());
		loggedInUserDTO.setClientIp(getClientIp(reqHttp));
		loggedInUserDTO.setRole(jwt.getRoles().stream().findFirst().get());
		
		return loggedInUserDTO;
		
		
	}
	
	@PostMapping("/login-app_old")
	public String ldapAuthenticate(@RequestParam("username") String username, @RequestParam("password") String password,
			RedirectAttributes redirectAttributes, HttpSession session, Model model,HttpServletRequest reqHttp) {

		logger.info("ldapAuthenticate Method Started");
		
		JwtResponse jwtRes= getToken(username);
		String ipAdd=getClientIp(reqHttp);
		if(jwtRes==null) {
			String message = username + " user not found!";
			model.addAttribute("message", message);
			
			logger.info("username:"+username+"--"+"userIpAddress:"+ipAdd+"--"+"loggedon:"+new Date());
			logger.info(message);
			return "login";
		}
		
		  if(username.equalsIgnoreCase("dmsuser") ||
		  username.equalsIgnoreCase("dmsadmin")) {
		  
		  if(!password.equalsIgnoreCase("dms@2022")) { 
			  logger.info("username:"+username+"--"+"userIpAddress:"+ipAdd+"--"+"loggedon:"+new Date());
			  model.addAttribute("message","Invalid Credentails."); 
			  return "login"; }
		  
		  User ppus=new User(); ppus.setUserid(username);
		  
		  String userRole=""; 
		  if(username.equalsIgnoreCase("dmsuser")) 
		  {
		  userRole="ROLE_USER"; 
		  } 
		  else if(username.equalsIgnoreCase("dmsadmin")) {
		  userRole="ROLE_ADMIN"; 
		  } 
		   
			logger.info("username:"+username+"--"+"userRole:"+userRole+"--"+"userIpAddress:"+ipAdd+"--"+"loggedon:"+new Date());
			
		  session.setAttribute("user", ppus.getUserid());
			session.setAttribute("userrole", userRole);
			session.setAttribute("token", jwtRes.getAccessToken());
		 // System.out.println(username); 
			return "redirect:/dms-app/index"; }
		 
		
		
		
		//System.out.println("Login app called---");
		
		//JwtResponse jwtRes= getToken(username, password);
		
		String uri = "";
		if (env.getProperty("dms.instance").equalsIgnoreCase("uat")) {
			uri = env.getProperty("ldap-uatUrl");
		} else if (env.getProperty("dms.instance").equalsIgnoreCase("dev")) {
			uri = env.getProperty("ldap-uatUrl");
		} else if (env.getProperty("dms.instance").equalsIgnoreCase("prod")) {
			uri = env.getProperty("ldap-prodUrl");
		} else {
			uri = env.getProperty("ldap-localUrl");

		}
		//System.out.println("URI:::" + uri);
		logger.info("URI:::" + uri);
		LdapDtls ldapsReq = new LdapDtls();
		LoginRequest req = new LoginRequest();

		req.setChlId("dms");
		req.setUserId(username);
		String encpass = java.util.Base64.getEncoder().encodeToString(password.getBytes());
		req.setLoginPwd(encpass);

		ldapsReq.setRequest(req);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_XML);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));

		LoginResponse res = new LoginResponse();

		String xmlString = "<LdapDtls> <Request> <UserId>" + username + "</UserId> <ChlId>dms</ChlId>\r\n"
				+ "<DeviceFamily></DeviceFamily> <DeviceFormat></DeviceFormat>\r\n"
				+ "<OperationId></OperationId> <LoginPwd>" + encpass + "</LoginPwd>\r\n"
				+ "<ClientAPIVer>1.0</ClientAPIVer> <SessionId></SessionId>\r\n"
				+ "<TransSeq>01</TransSeq> </Request> </LdapDtls>";

		//System.out.println("XML Request" + xmlString);

		
		HttpEntity<LdapDtls> request = new HttpEntity<>(ldapsReq, headers);

		
		String jsonString = "";
		if (env.getProperty("dms.instance").equalsIgnoreCase("local")) {
			jsonString = restTemplate.getForObject(uri, String.class);
		} else {
			jsonString = restTemplate.postForObject(uri, request, String.class);

		}

	//	System.out.println("jsonString----" + jsonString);

		XmlMapper xmlMapper = new XmlMapper();

		LdapDtls respon = new LdapDtls();

		try {
			respon = xmlMapper.readValue(jsonString, LdapDtls.class);
			//System.out.println(respon.getResponse().getStatus());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		res = respon.getResponse();
		if (res.getStatus().equalsIgnoreCase("Success")) {
			System.out.println("user--" + res.getDetails().getUserId());
			
			
			try {
				User appUser = userRepository.findByUsername(username)
						.orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));


				
			//	String ipAdd=getClientIp(reqHttp);
				
			//	System.out.println("ipAdd---"+ipAdd);
				
				 
				 
				
				/*
				 * String instance=env.getProperty("dms.instance"); try { //
				 * userService.insertUser(dmsUser, instance); } catch (Exception e) { // TODO
				 * Auto-generated catch block e.printStackTrace(); }
				 */
				//GenerateTokenResponseBody resp = getToken(model);
			//Optional<Role> userRole = appUser.getRoles().stream().findFirst().get();
				//System.out.println("api token"+resp.getAccess_token());
				ERole firstSkillNames = null;
				if(!appUser.getRoles().isEmpty()) {
					firstSkillNames = appUser.getRoles().iterator().next().getName();
				}
				

				session.setAttribute("user", appUser.getUserid());
				session.setAttribute("userrole", firstSkillNames.name());
				session.setAttribute("token", jwtRes.getAccessToken());
				//session.setAttribute("apitoken", resp.getAccess_token());

				///System.out.println("User id---" + appUser.getUser_id());
				//System.out.println("Role  User---" + appUser.getUser_role());
				logger.info("username:"+jwtRes.getUsername()+"--"+"userRole:"+jwtRes.getRoles()+"--"+"userIpAddress:"+ipAdd+"--"+"loggedon:"+new Date());
				
				return "redirect:/dms-app/index";
			} catch (UsernameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				String message = res.getUsername() + " user not found!";
				model.addAttribute("message", message);
				logger.error(message);
				logger.info("username:"+jwtRes.getUsername()+"--"+"userRole:"+jwtRes.getRoles()+"--"+"userIpAddress:"+ipAdd+"--"+"loggedon:"+new Date());
			
				return "login";
			}

			
		} else {
			String message = "Invalid User Credential!";
			model.addAttribute("message", message);
			logger.info(message);
			logger.info("username:"+jwtRes.getUsername()+"--"+"userRole:"+jwtRes.getRoles()+"--"+"userIpAddress:"+ipAdd+"--"+"loggedon:"+new Date());
			return "login";
		}
	}
	
	
	@PostMapping("/login-app")
	public String ldapAuthenticate_new(@RequestParam("username") String username, @RequestParam("password") String password,
			RedirectAttributes redirectAttributes, HttpSession session, Model model,HttpServletRequest reqHttp) {

		logger.info("ldapAuthenticate Method Started");
		
		JwtResponse jwtRes= getToken(username);
		String ipAdd=getClientIp(reqHttp);
		if(jwtRes==null) {
			String message = username + " user not found!";
			model.addAttribute("message", message);
			
			logger.info("username:"+username+"--"+"userIpAddress:"+ipAdd+"--"+"loggedon:"+new Date());
			logger.info(message);
			return "login";
		}
		
		  if(username.equalsIgnoreCase("dmsuser") ||
		  username.equalsIgnoreCase("dmsadmin")) {
		  
		  if(!password.equalsIgnoreCase("dms@2022")) { 
			  logger.info("username:"+username+"--"+"userIpAddress:"+ipAdd+"--"+"loggedon:"+new Date());
			  model.addAttribute("message","Invalid Credentails."); 
			  return "login"; }
		  
		  User ppus=new User(); ppus.setUserid(username);
		  
		  String userRole=""; 
		  if(username.equalsIgnoreCase("dmsuser")) 
		  {
		  userRole="ROLE_USER"; 
		  } 
		  else if(username.equalsIgnoreCase("dmsadmin")) {
		  userRole="ROLE_ADMIN"; 
		  } 
		   
			logger.info("username:"+username+"--"+"userRole:"+userRole+"--"+"userIpAddress:"+ipAdd+"--"+"loggedon:"+new Date());
			
		  session.setAttribute("user", ppus.getUserid());
			session.setAttribute("userrole", userRole);
			session.setAttribute("token", jwtRes.getAccessToken());
		 // System.out.println(username); 
			return "redirect:/dms-app/index"; }
			
			try {
				User appUser = userRepository.findByUsername(username)
						.orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

				ERole firstSkillNames = null;
				if(!appUser.getRoles().isEmpty()) {
					firstSkillNames = appUser.getRoles().iterator().next().getName();
				}
				

				session.setAttribute("user", appUser.getUserid());
				session.setAttribute("userrole", firstSkillNames.name());
				session.setAttribute("token", jwtRes.getAccessToken());
				
				logger.info("username:"+jwtRes.getUsername()+"--"+"userRole:"+jwtRes.getRoles()+"--"+"userIpAddress:"+ipAdd+"--"+"loggedon:"+new Date());
				
				return "redirect:/dms-app/index";
			} catch (UsernameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				String message = username + " user not found!";
				model.addAttribute("message", message);
				logger.error(message);
				logger.info("username:"+jwtRes.getUsername()+"--"+"userRole:"+jwtRes.getRoles()+"--"+"userIpAddress:"+ipAdd+"--"+"loggedon:"+new Date());
			
				return "login";
			}

			
		
	}
	
	public String getClientIp(HttpServletRequest request) {
		String ipAddress = request.getHeader("X-Forwarded-For");
		if(StringUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("Proxy-Client-IP");
		}
		
		if(StringUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("WL-Proxy-Client-IP");
		}
		
		if(StringUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getRemoteAddr();
			if(LOCALHOST_IPV4.equals(ipAddress) || LOCALHOST_IPV6.equals(ipAddress)) {
				try {
					InetAddress inetAddress = InetAddress.getLocalHost();
					ipAddress = inetAddress.getHostAddress();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
		}
		
		if(!StringUtils.isEmpty(ipAddress) 
				&& ipAddress.length() > 15
				&& ipAddress.indexOf(",") > 0) {
			ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
		}
		
		return ipAddress;
	}

	@SuppressWarnings("unchecked")
	@PostMapping("/file-upload")
	public ResponseEntity<?> fileUpload(@RequestParam("file") MultipartFile file, @ModelAttribute AddDocTags doc,
			Model model) {

		logger.info("Application aa-doc Started...");

		if (file.isEmpty()) {
			return new ResponseEntity("please select a file!", HttpStatus.OK);
		}

		String uri = "";
		if (env.getProperty("dms.instance").equalsIgnoreCase("uat")) {
			uri = env.getProperty("dms.addDoc");
		} else {
			uri = env.getProperty("dms.prodUrl");

		}
		logger.info("URI..." + uri);

		AddReq dmsReq = new AddReq();

		SearchHeader reqheader = new SearchHeader();

		reqheader.setChannelId(env.getProperty("dms.channelId"));
		reqheader.setRequestUuid(UUID.randomUUID().toString());
		reqheader.setServiceRequestId(env.getProperty("dms.serviceReqId"));

		AddReqBody bod = new AddReqBody();

		AddDocTags docTag = new AddDocTags();

		String cifId = "";
		String dateOfAccOpening = "";
		String docFolder = "";
		String documentType = "";
		String kycId = "";
		String name = "";
		String ucic = "";
		String validity = "";
		String isKyc = "";
		String referenceNumber = "";

		docTag.setAccNo(doc.getAccNo());

		/*
		 * System.out.println("Acc--" + doc.getAccNo()); System.out.println("CIF--" +
		 * doc.getCifId()); System.out.println("DOB--" + doc.getDateOfAccOpening()); //
		 * System.out.println("KYC--" + doc.getIsKyc()); System.out.println("Acc--" +
		 * doc.getUcic());
		 */

		// System.out.println("Acc--" + dmsReq.getBody().getDocumentTags().getAccNo());

		docTag.setCifId(cifId);
		docTag.setDateOfAccOpening(dateOfAccOpening);
		docTag.setDocFolder(docFolder);
		docTag.setDocumentType(documentType);
		docTag.setKycId(kycId);
		docTag.setName(name);
		docTag.setUcic(ucic);
		docTag.setValidity(validity);
		// docTag.setIsKyc(isKyc);
		docTag.setReferenceNumber(referenceNumber);
		docTag.setLastKycCheck(dateOfAccOpening);

		/*
		 * try { // System.out.println("Image Size in Bytes - " +
		 * file.getBytes().length);
		 * 
		 * } catch (IOException e1) { // TODO Auto-generated catch block
		 * e1.printStackTrace(); }
		 */

		String fileNewName = storageService.store(file);

		// System.out.println("--fileNewName--"+fileNewName);

		String filename = StringUtils.cleanPath(file.getOriginalFilename());

		//System.out.println("filename----" + filename);
		try {
			byte[] fileContent = file.getBytes();
			String encodedString = Base64.encodeBase64String(fileContent);

			//System.out.println(encodedString);

			bod.setDocument(encodedString);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// dmsReq.setHeader(reqheader);
		dmsReq.setBody(bod);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		HttpEntity<AddReq> request = new HttpEntity<>(dmsReq, headers);

		// String jsonString = restTemplate.postForObject(uri,request,String.class);

		String jsonString = restTemplate.getForObject(uri, String.class);

		//System.out.println("Data::" + jsonString);

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

		SearchResStatus result = res.getStatus();
	//	System.out.println("Display text----" + result.getDisplayText());

	//	System.out.println("Statuscode----" + result.getStatusCode());

		SearchReq req = new SearchReq();
		SearchRes dmsRes = new SearchRes();

		SearchResBody body = new SearchResBody();
		SearchDocDetails details = new SearchDocDetails();
		List<SearchDocDetails> doclist = new ArrayList<>();
		doclist.add(details);
		body.setDocumentDetails(doclist);
		dmsRes.setBody(body);
		model.addAttribute("searchReq", req);
		model.addAttribute("searchRes", dmsRes);
		model.addAttribute("addReq", dmsReq);
		model.addAttribute("addRes", result);
		model.addAttribute("updateDocTags", new UpdateDocTags());
		return ResponseEntity.ok(result);
	}

	@GetMapping("/get-files")
	public String listAllFiles(Model model) {

		List<String> files = storageService.loadAll().map(path -> ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/view-doc/").path(path.getFileName().toString()).toUriString()).collect(Collectors.toList());

		files.forEach(System.out::println);

		model.addAttribute("files", files);

		List<File> filelist = storageService.loadAll().map(Path::toFile).collect(Collectors.toList());

		filelist.forEach(System.out::println);

		model.addAttribute("fileList", filelist);

		List<String> collect = storageService.loadAllFiles().map(String::valueOf).sorted().collect(Collectors.toList());

		collect.forEach(System.out::println);

		model.addAttribute("collect", collect);

		for (File file : filelist) {

			System.out.printf("File: %s - " + new Date(file.lastModified()) + "\n", file.getName());
		}

		List<File> filesNmae = storageService.loadAllFiles().filter(Files::isRegularFile).map(Path::toFile)
				.collect(Collectors.toList());

		filesNmae.forEach(System.out::println);

		model.addAttribute("filesNmae", filesNmae);

		String dirName = "D:/Saravana/DMS/Sample";
		System.out.println("Directory Name Below---");
		try (Stream<Path> paths = Files.walk(Paths.get(dirName))) {
			paths.filter(Files::isDirectory).forEach(System.out::println);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Directory Name Below end---");

		try (Stream<Path> walk = Files.walk(Paths.get(dirName))) {

			// Filtering the paths by a folder and adding into a list.
			List<String> folderNamesList = walk.filter(Files::isDirectory).map(x -> x.toString())
					.collect(Collectors.toList());
			System.out.println("New SUbdirectory Name Start---");
			// printing the folder names
			folderNamesList.forEach(System.out::println);

		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("New SUbdirectory Name End---");

		// storageService.printFileNames(directory);

		return "index";
	}

	public RequestHeader setReqHeader(String searchType) {

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

		} else {
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
	

	@PostMapping("/user/search-doc")
	@PreAuthorize("hasRole('USER') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
	public String searchDoc(@ModelAttribute SearchReq searchReq, Model model, HttpSession session, HttpServletRequest httpReq) {
		logger.info("Application search-doc Started...");
		
		LoggedInUserDTO dto=getLoggedInUser(httpReq);
		logger.info("username:"+dto.getUsername()+"--"+"userRole:"+dto.getRole()+"--"+"userIpAddress:"+dto.getClientIp()+"--"+"loggedon:"+new Date());
		//System.out.println("username:"+dto.getUsername()+"--"+"userRole:"+dto.getRole()+"--"+"userIpAddress:"+dto.getClientIp()+"--"+"loggedon:"+new Date());
		
		String uri = "";
		if (env.getProperty("dms.instance").equalsIgnoreCase("uat")) {
			uri = env.getProperty("dms.uat.searchDoc");
		} else if (env.getProperty("dms.instance").equalsIgnoreCase("dev")) {
			uri = env.getProperty("dms.dev.searchDoc");
		} else if (env.getProperty("dms.instance").equalsIgnoreCase("prod")) {
			uri = env.getProperty("dms.prod.searchDoc");
		} else {
			uri = env.getProperty("dms.local.searchDoc");

		}
		//System.out.println("URI:::" + uri);
		SearchReq dmsReq = new SearchReq();

		RequestHeader reqheader = setReqHeader("search");

		SearchReqBody bod = new SearchReqBody();

		bod.setPageNumber("1");
		bod.setPageSize(50);

		SearchDocTag docTag = new SearchDocTag();
		SearchReqDateofAccOpening dateofacc = new SearchReqDateofAccOpening();

		SearchReqLastKycCheck dateOFKyc = new SearchReqLastKycCheck();

		dateOFKyc.setFrom(null);
		dateOFKyc.setTo(null);

		//System.out.println("acc--" + searchReq.getBody().getDocumentTags().getAccountNumber().length());

		if (searchReq.getBody().getDocumentTags().getAccountNumber() != null
				&& searchReq.getBody().getDocumentTags().getAccountNumber().length() > 0) {
			//System.out.println("entered");
			// String leftPadded = org.apache.commons.lang3.StringUtils.leftPad("" +
			// searchReq.getBody().getDocumentTags().getAccountNumber(), 16, "0");
			docTag.setAccountNumber(searchReq.getBody().getDocumentTags().getAccountNumber().trim());
		} else {
			docTag.setAccountNumber("");
		}

		if (searchReq.getBody().getDocumentTags().getCifId() != null) {
			docTag.setCifId(searchReq.getBody().getDocumentTags().getCifId().trim());
		} else {
			docTag.setCifId("");
		}

		if (searchReq.getBody().getDocumentTags().getUcic() != null) {
			docTag.setUcic(searchReq.getBody().getDocumentTags().getUcic().trim());
		} else {
			docTag.setUcic("");
		}

		if (searchReq.getBody().getDocumentTags().getDocFolder() != null) {
			docTag.setDocFolder(searchReq.getBody().getDocumentTags().getDocFolder());
		} else {
			docTag.setDocFolder("");
		}

		if (searchReq.getBody().getDocumentTags().getReferenceNumber() != null) {
			docTag.setReferenceNumber(searchReq.getBody().getDocumentTags().getReferenceNumber().trim());
		} else {
			docTag.setReferenceNumber("");
		}

		if (!searchReq.getBody().getDocumentTags().getDateOfAccOpening().getFrom().trim().equalsIgnoreCase("")) {
			dateofacc.setFrom(searchReq.getBody().getDocumentTags().getDateOfAccOpening().getFrom());
		} else {
			dateofacc.setFrom(null);
		}

		if (!searchReq.getBody().getDocumentTags().getDateOfAccOpening().getTo().trim().equalsIgnoreCase("")) {
			dateofacc.setTo(searchReq.getBody().getDocumentTags().getDateOfAccOpening().getTo());

		}

		else {
			dateofacc.setTo(null);
		}

		//System.out.println("source---" + searchReq.getBody().getDocumentTags().getSourceSystem());
		if (searchReq.getBody().getDocumentTags().getSourceSystem() != null) {
			docTag.setSourceSystem(searchReq.getBody().getDocumentTags().getSourceSystem());
		} else {
			docTag.setSourceSystem("");
		}

		docTag.setDateOfAccOpening(dateofacc);

		docTag.setLastKycCheck(dateOFKyc);
		bod.setDocumentTags(docTag);
		dmsReq.setHeader(reqheader);
		dmsReq.setBody(bod);

		// System.out.println("URI::" + uri);
		logger.info("Application search-doc Started..." + uri);

		String token = (String) session.getAttribute("apitoken");
		
		//String token = null;

		//System.out.println("Session Token--" + token);

		if (token == null || token.trim().isEmpty()) {
			GenerateTokenResponseBody tokenBody = new GenerateTokenResponseBody();
			try {

				// long todaySec=EncrytedPasswordUtils.getTodayPassedSeconds();
				// System.out.println("todaySec---"+todaySec);
				tokenBody = getToken(model);

				token = tokenBody.getAccess_token();

			} catch (MalformedURLException e1) { // TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
		SearchRes result = new SearchRes();
		SearchResStatus addRes = new SearchResStatus();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			headers.setBearerAuth(token);
			headers.set("api-key", env.getProperty("dms-api-key"));
			HttpEntity<SearchReq> request = new HttpEntity<>(dmsReq, headers);

			String jsonString = "";

			if (env.getProperty("dms.instance").equalsIgnoreCase("local")) {
				// RestTemplate restTemplate = new RestTemplate(requestFactory);
				jsonString = restTemplate.getForObject(uri, String.class);
			} else {
				jsonString = restTemplate.postForObject(uri, request, String.class);

			}
			printObject(request);
			//System.out.println("Data::" + jsonString);

			
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			try {
				result = mapper.readValue(jsonString, SearchRes.class);
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
			printObject("Response--" + result);
			
			

			if (result.getStatus().getStatusCode().equalsIgnoreCase("0")) {

				List<SearchDocDetails> searchDocList = result.getBody().getDocumentDetails();

				List<SearchDocDetails> ckycList = new ArrayList<SearchDocDetails>();

				List<SearchDocDetails> nonckycList = new ArrayList<SearchDocDetails>();

				for (SearchDocDetails line : searchDocList) {

					if (("POA").equalsIgnoreCase(line.getDocumentType())
							|| ("POI").equalsIgnoreCase(line.getDocumentType())) { // we dont like mkyong
						ckycList.add(line);
					} else {
						nonckycList.add(line);
					}

				}

				if (null != nonckycList) {
					//System.out.println("Non-kyc");
					nonckycList.forEach(System.out::println);
				}

				if (null != ckycList) {
					//System.out.println("kyc");
					ckycList.forEach(System.out::println);

				}

				List<SearchDocDetails> distinctElements = searchDocList.stream()
						.filter(distinctByKey(cust -> cust.getCifId())).collect(Collectors.toList());
				distinctElements.forEach(System.out::println);
				
				List<String> docID=new ArrayList<String>();
				for(SearchDocDetails sc:searchDocList) {
					docID.add(sc.getDocumentId());
				}
				
				 String json = ""; try { json = mapper.writeValueAsString(docID); }
				  catch (Exception e) { e.printStackTrace(); }
				 
				model.addAttribute("searchList", docID);
				model.addAttribute("respList", distinctElements);

				model.addAttribute("ckycList", ckycList);
				model.addAttribute("nonckycList", nonckycList);
			}
			
			

			

			AddDocTags doctag = new AddDocTags();
			model.addAttribute("addReq", doctag);
			model.addAttribute("searchReq", dmsReq);
			model.addAttribute("searchRes", result);
			model.addAttribute("updateDocTags", new UpdateDocTags());
			model.addAttribute("updateModal", "0");
			// printObject(result);

			model.addAttribute("addRes", addRes);
		} catch (RestClientException e) {
			// TODO Auto-generated catch block
			
			SearchResStatus status1=new SearchResStatus();
			status1.setStatusCode("1");
			status1.setDisplayText("Search API resource not found. Please contact support team.");
			result.setStatus(status1);
			AddDocTags doctag = new AddDocTags();
			model.addAttribute("addReq", doctag);
			model.addAttribute("searchReq", dmsReq);
			model.addAttribute("searchRes", result);
			model.addAttribute("updateDocTags", new UpdateDocTags());
			model.addAttribute("updateModal", "0");
			model.addAttribute("addRes", addRes);
			e.printStackTrace();
		}

		return "dms";
	}

	

	@PostMapping("/downloadfile/preview/")
	public ResponseEntity<?> getProfileImage(@RequestBody byte[] fileName) {
		try {
			String fileBasePath = env.getProperty("downloadpath");

			//System.out.println("Byte Array--" + fileName);

			String test = "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";

			// String fileBasePath = "C:\\Users\\Saravana Kumar\\Documents\\";
			Path path = Paths.get(fileBasePath + fileName);

			if (path != null) {
				logger.debug("Getting image from " + path.toString());

				ByteArrayResource resource = new ByteArrayResource(test.getBytes());

				return ResponseEntity.ok().contentLength(test.length()).contentType(MediaType.IMAGE_JPEG)
						.body(resource);
			} else {
				logger.debug("Image not found  " + fileName);
				return ResponseEntity.status(HttpStatus.OK).build();
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/download2/{fileName}")
	public ResponseEntity<ByteArrayResource> downloadFile2(@PathVariable String fileName) throws IOException {

		MediaType mediaType = MediaTypeUtil.getMediaTypeForFileName(this.servletContext, fileName);
		//System.out.println("fileName: " + fileName);
		//System.out.println("mediaType: " + mediaType);

		String fileBasePath = "C:\\Users\\Saravana Kumar\\Documents\\";
		Path path = Paths.get(fileBasePath + fileName);
		byte[] data = Files.readAllBytes(path);

		String test = "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";

		ByteArrayResource resource = new ByteArrayResource(test.getBytes());

		return ResponseEntity.ok()
				// Content-Disposition
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + path.getFileName().toString())
				// Content-Type
				.contentType(mediaType) //
				// Content-Lengh
				.contentLength(data.length) //
				.body(resource);
	}

	@GetMapping("admin/downloadfile/{fileName}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) { //

		// Load file as Resource
		Resource resource;

		String fileBasePath = "C:\\Users\\Saravana Kumar\\Documents\\";
		Path path = Paths.get(fileBasePath + fileName);
		try {
			resource = new UrlResource(path.toUri());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}

		// Try to determine file's content type
		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException ex) {
			System.out.println("Could not determine file type.");
		}

		// Fallback to the default content type if type could not be determined
		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}

	@ResponseBody
	@GetMapping("/user/get-doc/{documentId:.+}")
	public UpdateReqBody getDoc(@PathVariable String documentId,HttpServletRequest httpReq) throws MalformedURLException {
		logger.info("Application Get-doc Started...");
		LoggedInUserDTO dto=getLoggedInUser(httpReq);
		logger.info("username:"+dto.getUsername()+"--"+"userRole:"+dto.getRole()+"--"+"userIpAddress:"+dto.getClientIp()+"--"+"loggedon:"+new Date());
		
		try {
			String uri = "";
			if (env.getProperty("dms.instance").equalsIgnoreCase("uat")) {
				uri = env.getProperty("dms.uat.viewDoc");
			} else if (env.getProperty("dms.instance").equalsIgnoreCase("dev")) {
				uri = env.getProperty("dms.dev.viewDoc");
			} else if (env.getProperty("dms.instance").equalsIgnoreCase("prod")) {
				uri = env.getProperty("dms.prod.viewDoc");
			} else {
				uri = env.getProperty("dms.local.viewDoc");

			}

			ViewReq dmsReq = new ViewReq();

			RequestHeader reqheader = setReqHeader("view");

			ViewReqBody bod = new ViewReqBody();

			bod.setDocumentId(documentId);
			dmsReq.setHeader(reqheader);
			dmsReq.setBody(bod);

			System.out.println("URI::" + uri);
			logger.info("Application view-doc Started..." + uri);
			logger.info("Application Document ID..." + documentId);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			HttpEntity<ViewReq> serReq = new HttpEntity<>(dmsReq, headers);

			// printObject(serReq);
			String jsonString = "";

			if (env.getProperty("dms.instance").equalsIgnoreCase("local")) {
				jsonString = restTemplate.getForObject(uri, String.class);
			} else {
				jsonString = restTemplate.postForObject(uri, serReq, String.class);
			}

			//System.out.println("Data::" + jsonString);

			ViewDocRes result = new ViewDocRes();
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			try {
				result = mapper.readValue(jsonString, ViewDocRes.class);
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

			UpdateReqBody updareqbody = new UpdateReqBody();

			if (result.getStatus().getStatusCode().equalsIgnoreCase("0")) {

				UpdateDocTags tag = new UpdateDocTags();

				AddDocumentDetails addDetails = new AddDocumentDetails();
				ViewResBody reqbod = result.getBody();
				ViewDocDetails det = reqbod.getDocumentDetails();

				tag.setAccountNumber(det.getAccNo());
				tag.setCifId(det.getCifid());
				tag.setDateOfAccOpening(det.getDateOfAccOpening());
				tag.setDocId(det.getDocumentId());
				tag.setReferenceNumber(det.getReferenceNumber());
				tag.setDocumentType(det.getDocumentType());
				tag.setSourceSystem(det.getSourceSystem());
				tag.setValidity(det.getValidity());
				tag.setUcic(det.getUcic());
				tag.setLastKycCheck(det.getLastKycCheck());
				tag.setName(det.getName());
				addDetails.setFileName(det.getFileName());
				addDetails.setFileType(det.getFileType());
				updareqbody.setDocumentTags(tag);
				updareqbody.setDocumentDetails(addDetails);

				return updareqbody;
			}

			else {
				return updareqbody;
			}
		} catch (RestClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}

	}

	@GetMapping("/admin/up-doc/{documentId:.+}")
	public String getDocDetails(@PathVariable String documentId, Model model, HttpSession session)
			throws MalformedURLException {
		logger.info("Application Get-doc Started...");
		try {
			String uri = "";
			if (env.getProperty("dms.instance").equalsIgnoreCase("uat")) {
				uri = env.getProperty("dms.uat.viewDoc");
			} else if (env.getProperty("dms.instance").equalsIgnoreCase("dev")) {
				uri = env.getProperty("dms.dev.viewDoc");
			} else if (env.getProperty("dms.instance").equalsIgnoreCase("prod")) {
				uri = env.getProperty("dms.prod.viewDoc");
			} else {
				uri = env.getProperty("dms.local.viewDoc");

			}

			ViewReq dmsReq = new ViewReq();

			RequestHeader reqheader = setReqHeader("view");

			ViewReqBody bod = new ViewReqBody();

			bod.setDocumentId(documentId);
			dmsReq.setHeader(reqheader);
			dmsReq.setBody(bod);

			System.out.println("URI::" + uri);
			logger.info("Application view-doc Started..." + uri);
			logger.info("Application Document ID..." + documentId);

			String token = (String) session.getAttribute("token");

			//System.out.println("Session Token--" + token);

			if (token == null || token.trim().isEmpty()) {
				GenerateTokenResponseBody tokenBody = new GenerateTokenResponseBody();
				try {

					// long todaySec=EncrytedPasswordUtils.getTodayPassedSeconds();
					// System.out.println("todaySec---"+todaySec);
					tokenBody = getToken(model);

					token = tokenBody.getAccess_token();

				} catch (MalformedURLException e1) { // TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			headers.setBearerAuth(token);
			headers.set("api-key", env.getProperty("dms-api-key"));
			HttpEntity<ViewReq> serReq = new HttpEntity<>(dmsReq, headers);

			// printObject(serReq);
			String jsonString = "";

			if (env.getProperty("dms.instance").equalsIgnoreCase("local")) {
				jsonString = restTemplate.getForObject(uri, String.class);
			} else {
				jsonString = restTemplate.postForObject(uri, serReq, String.class);
			}

		//	System.out.println("Data::" + jsonString);

			ViewDocRes result = new ViewDocRes();
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			try {
				result = mapper.readValue(jsonString, ViewDocRes.class);

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

			UpdateReqBody updareqbody = new UpdateReqBody();

			UpdateDocTags tag = new UpdateDocTags();

			AddDocumentDetails addDetails = new AddDocumentDetails();
			ViewResBody reqbod = result.getBody();
			ViewDocDetails det = reqbod.getDocumentDetails();

			printObject(det);

			tag.setAccountNumber(det.getAccNo());
			tag.setCifId(det.getCifid());
			tag.setDateOfAccOpening(det.getDateOfAccOpening());
			tag.setDocId(det.getDocumentId());
			tag.setReferenceNumber(det.getReferenceNumber());
			tag.setDocumentType(det.getDocumentType());
			tag.setSourceSystem(det.getSourceSystem());
			tag.setValidity(det.getValidity());
			tag.setUcic(det.getUcic());
			tag.setLastKycCheck(det.getLastKycCheck());
			tag.setName(det.getName());
			tag.setDocFolder(det.getDocFolder());
			addDetails.setFileName(det.getFileName());
			addDetails.setFileType(det.getFileType());
			updareqbody.setDocumentTags(tag);
			updareqbody.setDocumentDetails(addDetails);

			SearchResStatus addRes = new SearchResStatus();

			AddDocTags doctag = new AddDocTags();
			model.addAttribute("addReq", doctag);
			model.addAttribute("searchReq", new SearchReq());
			SearchRes st = new SearchRes();
			st.setStatus(addRes);
			st.setBody(new SearchResBody());
			model.addAttribute("searchRes", st);
			model.addAttribute("updateDocTags", tag);
			model.addAttribute("addRes", addRes);
			// model.addAttribute("updateModal", "1");
			model.addAttribute("addRes", addRes);
			return "dms :: fileModalFragment";
			// return "dms";

		} catch (RestClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}

	}

	@GetMapping("/user/get-token")
	public GenerateTokenResponseBody getToken(Model model) throws MalformedURLException {
		logger.info("Application Get-token Started...");
		GetTokenRootRes result = new GetTokenRootRes();
		try {
			String uri = "";
			if (env.getProperty("dms.instance").equalsIgnoreCase("uat")) {
				uri = env.getProperty("dms.uat.getToken");
			} else if (env.getProperty("dms.instance").equalsIgnoreCase("dev")) {
				uri = env.getProperty("dms.dev.getToken");
			} else if (env.getProperty("dms.instance").equalsIgnoreCase("prod")) {
				uri = env.getProperty("dms.prod.getToken");
			} else {
				uri = env.getProperty("dms.local.getToken");

			}

			GetToken tokenReq = new GetToken();

			GetTokenReq dmsReq = new GetTokenReq();

			RequestHeader reqheader = setReqHeader("token");

			GetTokenRequestBody bod = new GetTokenRequestBody();

			GetTokenRequestBodyRoot rootreq = new GetTokenRequestBodyRoot();

			bod.setClientId(env.getProperty("clientId"));
			bod.setClientSecret(env.getProperty("clientSecret"));
			rootreq.setGetTokenRequestBody(bod);
			dmsReq.setRequestHeader(reqheader);
			dmsReq.setRequestBody(rootreq);
			tokenReq.setGetToken(dmsReq);
			System.out.println("URI::" + uri);
			logger.info("Application Get-token Started..." + uri);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			// headers.add("Authorization", "Bearer"+token);
			// headers.setBearerAuth(token);
			headers.set("api-key", env.getProperty("dms-api-key"));
			HttpEntity<GetToken> serReq = new HttpEntity<>(tokenReq, headers);

			printObject(serReq);
			String jsonString = "";

			if (env.getProperty("dms.instance").equalsIgnoreCase("local")) {
				jsonString = restTemplate.getForObject(uri, String.class);
			} else {
				jsonString = restTemplate.postForObject(uri, serReq, String.class);
			}

			//System.out.println("Data::" + jsonString);

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

				String accessToken = result.getGenerateTokenResponse().getResponseBody().getGenerateTokenResponseBody()
						.getAccess_token();

				String expiry = result.getGenerateTokenResponse().getResponseBody().getGenerateTokenResponseBody()
						.getExpires_in();

				String token_type = result.getGenerateTokenResponse().getResponseBody().getGenerateTokenResponseBody()
						.getToken_type();

				//System.out.println("accessToken" + accessToken);
				//System.out.println("expiry" + expiry);
				//System.out.println("token_type" + token_type);
				// return result;
				

			}
			else {
				
				return result.getGenerateTokenResponse().getResponseBody().getGenerateTokenResponseBody();
			}

		} catch (RestClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// return result;
		}

		return result.getGenerateTokenResponse().getResponseBody().getGenerateTokenResponseBody();

	}

	@GetMapping("/user/view-doc/{documentId:.+}")
	@PreAuthorize("hasRole('USER') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
	public ResponseEntity<?> viewDoc(@PathVariable String documentId, Model model, HttpServletRequest request,
			HttpSession session) throws MalformedURLException {
		logger.info("Application view-doc Started..." + documentId);
		
		LoggedInUserDTO dto=getLoggedInUser(request);
		logger.info("username:"+dto.getUsername()+"--"+"userRole:"+dto.getRole()+"--"+"userIpAddress:"+dto.getClientIp()+"--"+"loggedon:"+new Date());
		
		try {
			String uri = "";
			if (env.getProperty("dms.instance").equalsIgnoreCase("uat")) {
				uri = env.getProperty("dms.uat.viewDoc");
			} else if (env.getProperty("dms.instance").equalsIgnoreCase("dev")) {
				uri = env.getProperty("dms.dev.viewDoc");
			} else if (env.getProperty("dms.instance").equalsIgnoreCase("prod")) {
				uri = env.getProperty("dms.prod.viewDoc");
			} else {
				uri = env.getProperty("dms.local.viewDoc");

			}

			ViewReq dmsReq = new ViewReq();

			RequestHeader reqheader = setReqHeader("view");

			ViewReqBody bod = new ViewReqBody();

			bod.setDocumentId(documentId);
			dmsReq.setHeader(reqheader);
			dmsReq.setBody(bod);

			System.out.println("URI::" + uri);
			logger.info("Application view-doc Started..." + uri);
			logger.info("Application Document ID..." + documentId);

			String token = (String) session.getAttribute("apitoken");

			//System.out.println("Session Token--" + token);

			if (token == null || token.trim().isEmpty()) {
				GenerateTokenResponseBody tokenBody = new GenerateTokenResponseBody();
				try {

					// long todaySec=EncrytedPasswordUtils.getTodayPassedSeconds();
					// System.out.println("todaySec---"+todaySec);
					tokenBody = getToken(model);

					token = tokenBody.getAccess_token();

				} catch (MalformedURLException e1) { // TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			headers.setBearerAuth(token);
			headers.set("api-key", env.getProperty("dms-api-key"));
			HttpEntity<ViewReq> serReq = new HttpEntity<>(dmsReq, headers);

			// printObject(serReq);
			String jsonString = "";

			if (env.getProperty("dms.instance").equalsIgnoreCase("local")) {
				jsonString = restTemplate.getForObject(uri, String.class);
			} else {
				jsonString = restTemplate.postForObject(uri, serReq, String.class);
			}

			//System.out.println("Data::" + jsonString);

			ViewDocRes result = new ViewDocRes();
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			try {
				result = mapper.readValue(jsonString, ViewDocRes.class);
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

			if (result.getStatus().getStatusCode().equalsIgnoreCase("0")) {

				String fileType = result.getBody().getDocumentDetails().getFileType();

				//System.out.println("File type" + fileType);
				String contentType = "";
				if (fileType.equalsIgnoreCase("jpg") || fileType.equalsIgnoreCase("jpeg")
						|| fileType.equalsIgnoreCase("png")) {
					contentType = "application/image";
				} else if (fileType.equalsIgnoreCase("pdf")) {
					contentType = "application/pdf";
				} else if (fileType.equalsIgnoreCase("tif") || fileType.equalsIgnoreCase("tiff")) {
					contentType = "application/tiff";
				} else {
					contentType = "application/octet-stream";
				}

				String base64 = result.getBody().getDocument();// get base-64 encoded string
				//byte[] bytes = Base64.decodeBase64(base64);

				// System.out.println("decodeBytes--"+base64);

				final ByteArrayResource resource = new ByteArrayResource(base64.getBytes());

				return ResponseEntity.ok().contentLength(base64.length()).header("Content-type", contentType)
						.header("Content-disposition",
								"attachment; filename=\"" + result.getBody().getDocumentDetails().getName() + "\"")
						.body(resource);
			}

			else {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
		} catch (RestClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}

	}


	@GetMapping("/admin/download-doc/{documentId:.+}")
	@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
	public ResponseEntity<ByteArrayResource> downloadDoc(@PathVariable String documentId, Model model,
			HttpServletRequest request, HttpSession session) throws MalformedURLException {
		logger.info("Application Download-doc Started...");
		try {
			String uri = "";
			if (env.getProperty("dms.instance").equalsIgnoreCase("uat")) {
				uri = env.getProperty("dms.uat.viewDoc");
			} else if (env.getProperty("dms.instance").equalsIgnoreCase("dev")) {
				uri = env.getProperty("dms.dev.viewDoc");
			} else if (env.getProperty("dms.instance").equalsIgnoreCase("prod")) {
				uri = env.getProperty("dms.prod.viewDoc");
			} else {
				uri = env.getProperty("dms.local.viewDoc");

			}
			ViewReq dmsReq = new ViewReq();

			RequestHeader reqheader = setReqHeader("view");

			ViewReqBody bod = new ViewReqBody();

			bod.setDocumentId(documentId);
			dmsReq.setHeader(reqheader);
			dmsReq.setBody(bod);

			System.out.println("URI::" + uri);
			logger.info("Application Download-doc Started..." + uri);
			logger.info("Application Document ID..." + documentId);

			String token = null;

		//	System.out.println("Session Token--" + token);

			if (token == null || token.trim().isEmpty()) {
				GenerateTokenResponseBody tokenBody = new GenerateTokenResponseBody();
				try {

					// long todaySec=EncrytedPasswordUtils.getTodayPassedSeconds();
					// System.out.println("todaySec---"+todaySec);
					tokenBody = getToken(model);

					token = tokenBody.getAccess_token();

				} catch (MalformedURLException e1) { // TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			headers.setBearerAuth(token);
			headers.set("api-key", env.getProperty("dms-api-key"));
			HttpEntity<ViewReq> serReq = new HttpEntity<>(dmsReq, headers);

			printObject(serReq);
			String jsonString = "";

			if (env.getProperty("dms.instance").equalsIgnoreCase("local")) {
				// RestTemplate restTemplate = new RestTemplate(requestFactory);
				jsonString = restTemplate.getForObject(uri, String.class);
			} else {
				jsonString = restTemplate.postForObject(uri, serReq, String.class);

			}

			//System.out.println("Data::" + jsonString);

			ViewDocRes result = new ViewDocRes();
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			try {
				result = mapper.readValue(jsonString, ViewDocRes.class);
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

			if (result.getStatus().getStatusCode().equalsIgnoreCase("0")) {

				String fileType = result.getBody().getDocumentDetails().getFileType();

				String base64 = result.getBody().getDocument();// get base-64 encoded string
				byte[] bytes = Base64.decodeBase64(base64);

				//System.out.println("decodeBytes--" + base64);

				final ByteArrayResource resource = new ByteArrayResource(base64.getBytes());
				String contentType = "";
				if (fileType.equalsIgnoreCase("jpg") || fileType.equalsIgnoreCase("jpeg")
						|| fileType.equalsIgnoreCase("png")) {
					contentType = "application/image";
				} else if (fileType.equalsIgnoreCase("pdf")) {
					contentType = "application/pdf";
				} else if (fileType.equalsIgnoreCase("tiff")) {
					contentType = "application/tiff";
				} else {
					contentType = "application/octet-stream";
				}
				return ResponseEntity.ok().contentLength(base64.length()).header("Content-type", contentType)
						.header("Content-disposition",
								"attachment; filename=\"" + result.getBody().getDocumentDetails().getName() + "\"")
						.body(resource);
			}

			else {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
		} catch (RestClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}

	}

	public String convertToDateFormat(String date) throws ParseException {
		String format = "";
		SimpleDateFormat oldformatter = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		format = formatter.format(oldformatter.parse(date));
		//System.out.println(format);
		return format;
	}
	
	public List<SearchDocDetails> getFilterOutput(List<SearchDocDetails> lines, String filter1, String filter2,
			String match) {
		List<SearchDocDetails> result = new ArrayList<>();
		for (SearchDocDetails line : lines) {
			if (filter1.equalsIgnoreCase(line.getDocumentType()) || filter2.equalsIgnoreCase(line.getDocumentType())) { // we
				result.add(line);
			} else {
				result.add(line);
			}
		}
		return result;
	}

	public void printObject(Object object) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
	//	System.out.println("Printing object---" + gson.toJson(object));
	}

}
