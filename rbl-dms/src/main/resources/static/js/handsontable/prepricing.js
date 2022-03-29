function dopricing(a){
	function d()
	{	
		Handsontable.Dom.addEvent(savehandson,"click",function()
			{					
							alert(1);							
	                 		                                      			            			
             });
	}

	var a=document.getElementById(a),
	a=new Handsontable(a,{
		data:sdata,		
		rowHeaders:true, 		
		colHeaders:true, 
		colWidths: [110, 80, 80, 70, 70, 110, 80,70, 70, 70, 80,80,80, 80, 80, 80,80],
		minRows:0, 
		columnSorting: true,
		autoWrapRow:!0,
		fixedRowsHeader:true,
		manualRowResize:!0,
		manualColumnResize:!0,
		colHeaders:[		
		               "Reference Number",	             
		               "Lot Code",
			           "Commodity",
			           "Shape",			          
			           "Color",
			           "Internal Quality",
			           "Sieve Size",	             
		               "MM Size",
			           "LHS Qty1",
			           "LHS Qty2",			          
			           "MCP Rate",
			           "MCP Amount",
			           "Description",	             
		               "RHS Qty1",
			           "RHS Qty2",
			           "SP Rate",
			           "SP Amount"
		              ],
		columns:[
		         //{data: 'pre_pricing_det_id',type:"numeric",className:"htRight",format:"0"},
		         //{data: 'pre_pricing_comp_id',type:"numeric",className:"htRight",format:"0"},
		         {data: 'ref_no',type:"text"},	
		         {data: 'lot_code',type: 'autocomplete',
		        	 source: function (query, process) {
		             $.ajax({		             
		               url: "<%=request.getContextPath()%>/marketing/prepricing.do?action=doGetLotCode",	
		               data:{
		                 query: query
		               },
		               contentType:'application/json',
		               success: function (data) {		                
		                //alert(data);		                
		                 process(data);
		               }
		             });
		           },	        	  
		           strict: false
		         },
		         {data: 'com_name',type:"text"},	
		         {data: 'shape_code',type:"text"},	
		         {data: 'color_code',type:"text"},
		         {data: 'internal_quality',type:"text"},		
		         {data: 'sieve_size',type:"text"},	
		         {data: 'mm_size',type:"text"},        
		         {data: 'lhs_qty1',type:"numeric",className:"htRight",format:"00.00"},		        
		         {data: 'lhs_qty2',type:"numeric",className:"htRight",format:"00.00"},
		         {data: 'mcp_rate',type:"numeric",className:"htRight",format:"00.00"},		        
		         {data: 'mcp_amt',type:"numeric",className:"htRight",format:"0,0.00"},
		         {data: 'description',type:"text"},
		         {data: 'rhs_qty1',type:"numeric",className:"htRight",format:"00.00"},		        
		         {data: 'rhs_qty2',type:"numeric",className:"htRight",format:"00.00"},
		         {data: 'sp_rate',type:"numeric",className:"htRight",format:"00.00"},		        
		         {data: 'sp_amt',type:"numeric",className:"htRight",format:"00.000"}
		       ],

		contextMenu:true,		
		minSpareRows:1,
     dropdownMenu:true,
     filters:true
		}),
		b=0;	  				  
		d();	
	}